package com.fyp.awacam;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContextWrapper;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.fyp.awacam.databinding.ActivityMainBinding;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;


public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    String[] Permissions = new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE};
    int RequestCode = 11;

    static final String TAG = "tag";
    ServerSocket serverSocket;
    Socket socket;
    static final int PORT_NUM = 4444;
    static final String IP_ADDRESS = "192.168.10.99";
    static boolean CLIENT_CONNECTED = false;

    public static DatagramSocket s_socket;
    public static DatagramSocket r_socket;
    FileOutputStream fileOutputStream;


    AudioRecord recorder;
    AudioTrack audioTrack;

    short[] buffer;
    int sampleRate = AudioTrack.getNativeOutputSampleRate(AudioManager.STREAM_MUSIC);//48000
    int channelConfig = AudioFormat.CHANNEL_IN_MONO;
    int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    int bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat);//3840

    DataOutputStream dataOutputStream;
    OutputStream outputStream;

    //final File file = new File(Environment.getExternalStorageDirectory() + "/myAudio.mp3");


    private boolean audioPlaying = true;
    private boolean r_status = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        if (!permissionsGranted()) {
            grantPermissions();
        }

        createServerSocket(PORT_NUM);
        //createClientSocket(IP_ADDRESS,PORT_NUM);

        binding.startRecording.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission") // if mic permssion was not granted
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Start Button Clicked");
                if (CLIENT_CONNECTED) {
                    try {
                        if (permissionsGranted()) {
                            new Thread(() -> {

                                // For recording audio
                                recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                                        sampleRate,
                                        channelConfig,
                                        AudioFormat.ENCODING_PCM_16BIT,
                                        bufferSize);

                                recorder.startRecording();
                                audioPlaying = true;

                                //For playing audio simultaneously
                                audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC
                                        , sampleRate
                                        , AudioFormat.CHANNEL_OUT_MONO
                                        , AudioFormat.ENCODING_PCM_16BIT
                                        , bufferSize
                                        , AudioTrack.MODE_STREAM);

                                audioTrack.setPlaybackRate(sampleRate);
                                //audioTrack.play();

                                buffer = new short[bufferSize / 4];

                                try {
                                    //For socket
                                    outputStream = null;
                                    outputStream = socket.getOutputStream();

                                    //For file
                                    //dataOutputStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(getRecordingFilePath())));

                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Log.d(TAG, "Exception >>" + e.toString());

                                }

                                while (audioPlaying) {
                                    //reading audio from buffer
                                    int bufferReadResult = recorder.read(buffer, 0, bufferSize / 4);

                                    //playing that audio simultaneously
                                    audioTrack.write(buffer, 0, bufferSize / 4);


                                    //Saving file
                                    try {
                                        //converting buffer(short) to bytes
                                        byte socketBuffer[]=new byte[buffer.length*2];
                                        ByteBuffer.wrap(socketBuffer).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(buffer);

                                         outputStream.write(socketBuffer, 0, socketBuffer.length);
//
//                                        for (int i = 0; i < bufferReadResult; i++) {
//                                            //For socket (NOt Working perfectly)
//                                            dataOutputStream.write(buffer[i]);
//                                            //For file
//                                            // dataOutputStream.writeShort(buffer[i]);
//                                        }

                                    } catch (Exception e) {
                                        Log.d(TAG, "Exception>>" + e.toString());
                                        e.printStackTrace();
                                    }
                                }
                                try {
                                    recorder.stop();
//                                    dataOutputStream.flush();
//                                    dataOutputStream.close();
                                    outputStream.flush();
                                    outputStream.close();
                                    socket.close();
                                    Log.d(TAG, "Socket closed");

                                    //fileOutputStream.flush();
                                    //fileOutputStream.close();
                                    //Log.d(TAG,"File flushed and closed");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Log.d(TAG, ">>Exception>>" + e.toString());

                                }
                            }).start();

                        } else {
                            grantPermissions();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.d(TAG, "Exception>>" + e.toString());
                    }
                }

            }
        });
        binding.stopRecording.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                audioPlaying = false;
                // playFileAudio();

            }

        });

    }

    private void playFileAudio() {
        byte[] byteData = null;
        File file = null;
        file = new File(getRecordingFilePath());
        byteData = new byte[(int) file.length()];
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(file);
            fileInputStream.read(byteData);
            fileInputStream.close();
            Log.d(TAG, "File Readed");

        } catch (Exception e) {
            Log.d(TAG, "Exception>>:" + e.toString());
            e.printStackTrace();
        }

        if (audioTrack != null) {
            audioTrack.play();
            Log.d(TAG, "File is being played");
            audioTrack.write(byteData, 0, byteData.length);
            audioTrack.stop();
            audioTrack.release();
        } else {
            Log.d(TAG, "audio track is not initialised ");

        }
    }

    private String getRecordingFilePath() {
        Log.d(TAG, "Creating File for Audio");

        ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());
        File musicDirectory = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        File file = new File(musicDirectory, "testAudioFile.pcm");
        if (file.exists()) {
            Log.d(TAG, "File Already exists:");
        } else {
            Log.d(TAG, "New file creted ");
        }
        String path = file.getPath();
        Log.d(TAG, "File path is:" + path);

        return path;
    }

    public void createServerSocket(int portNum) {
        Thread t1 = new Thread(() ->
        {
            try {
                serverSocket = new ServerSocket(PORT_NUM);
                Log.d(TAG, "Server is waiting at port num: " + PORT_NUM);
                socket = serverSocket.accept();

                Log.d(TAG, "Client Connected");
                CLIENT_CONNECTED = true;

//                InputStreamReader in= new InputStreamReader(socket.getInputStream());
//                BufferedReader br= new BufferedReader(in);
//
//                String message = br.readLine();
//                Log.d(TAG,"Message received from server is "+message);
//                Toast.makeText(this, "Message received form server ", Toast.LENGTH_SHORT).show();

            } catch (IOException e) {
                Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();

                e.printStackTrace();
            }

        });
        t1.start();
    }

    private void createClientSocket(String IP_ADDRESS, int portNum) {
        Thread t1 = new Thread(() ->
        {
            try {
                Log.d(TAG, "Client is waiting");
                Socket client = new Socket(IP_ADDRESS, portNum);
                Log.d(TAG, "Connected");

                InputStreamReader inputStreamReader = new InputStreamReader(client.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                String text = bufferedReader.readLine();
                Log.d(TAG, "Text is :" + text);


                PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(client.getOutputStream()));
                printWriter.println("ABC");
                printWriter.flush();

            } catch (IOException e) {
                Log.d(TAG, "Exception>>" + e.toString());
                e.printStackTrace();
            }
        });
        t1.start();
    }

    private void grantPermissions() {
        Log.d(TAG, "Granting Permission");
        ActivityCompat.requestPermissions(this, Permissions, RequestCode);
    }

    private boolean permissionsGranted() {
        for (String permission : Permissions) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Permissions not granted i.e " + permission);
                return false;
            }
        }
        return true;
    }

    private void sendTestMessage() {
        try {
            String message = "Muhammad Aqib";
            byte[] b = message.getBytes();

            OutputStream outputStream = null;
            outputStream = socket.getOutputStream();
            outputStream.write(b, 0, b.length);

            outputStream.flush();
            Log.d(TAG, "Outputstream flushed");

            socket.close();
            Log.d(TAG, "Socet Closed");

        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "Exception" + e.toString());
        }
    }

}
