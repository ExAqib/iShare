package com.HuimangTech.iShare.ui.fileTransfer;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.HuimangTech.iShare.LinkedDevices;
import com.HuimangTech.iShare.ui.login.LoginDetails;
import com.HuimangTech.iShare.Parameters;
import com.HuimangTech.iShare.R;
import com.HuimangTech.iShare.SingletonSocket;
import com.HuimangTech.iShare.ui.login.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.HashMap;

public class displayData extends Fragment {

    private static final String TAG = "tag";
    PrintWriter printWriter;
    InputStream inputStream;
    BufferedReader bufferedReader;
    LinearLayout linearLayout;
    Socket socket;

    public displayData() {
        // Required empty public constructor
    }

    public static displayData newInstance(String param1) {
        return new displayData();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate called for fragment");
    }

    @Override
    public void onStart() {
        Log.d(TAG, "onStart called for fragment");
        super.onStart();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_display_data, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.d(TAG, "onViewCreated called for fragment");

        linearLayout = getView().findViewById(R.id.fragmentLinear);
        Log.d(TAG, "calling start ");
        start();
    }

    void start() {
        try {
            socket = SingletonSocket.getSocket();
            printWriter = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
            inputStream = socket.getInputStream();
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            Connect connect = new Connect();
            connect.execute();

        } catch (IOException e) {
            Log.d(TAG, "start: Exception" + e);
            e.printStackTrace();
        }
    }

    class Connect extends AsyncTask<Void, Bundle, Bundle> {

        @Override
        protected Bundle doInBackground(Void... voids) {
            getPcInfo();
            setDriveNames();
            return null;
        }

        @Override
        protected void onProgressUpdate(Bundle... values) {
            super.onProgressUpdate(values);

            View driveLayout = getLayoutInflater().inflate(R.layout.drive_info_item, null, false);

            CardView.LayoutParams layoutParams = new CardView.LayoutParams(CardView.LayoutParams.MATCH_PARENT, CardView.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(5, 10, 5, 10);
            driveLayout.setLayoutParams(layoutParams);

            if (values[0].getString("name").trim().equals("Exception")) {

                View exceptionLayout = getLayoutInflater().inflate(R.layout.exception_file, null, false);
                driveLayout.setLayoutParams(layoutParams);
                TextView message = exceptionLayout.findViewById(R.id.exception_message);
                message.setText(values[0].getString("Exception").trim());

                linearLayout.addView(exceptionLayout);
                return;
            }

            TextView name = driveLayout.findViewById(R.id.tv_drive_name);
            TextView label = driveLayout.findViewById(R.id.tv_drive_label);
            TextView format = driveLayout.findViewById(R.id.tv_drive_format);
            ProgressBar bar = driveLayout.findViewById(R.id.capacityBar);
            TextView totalStore = driveLayout.findViewById(R.id.tv_totalStorage);
            TextView availableStore = driveLayout.findViewById(R.id.tv_availableStorage);


            name.setText(values[0].getString("name"));

            if (values[0].getString("label").trim().equals("Null")) {
                label.setText(null);
            } else {
                label.setText(values[0].getString("label"));
            }

            format.setText(values[0].getString("format"));

            try {
                long totalData = Long.parseLong(values[0].getString("totalSize"));
                long availableData = Long.parseLong(values[0].getString("totalSize")) - Long.parseLong(values[0].getString("usedSize"));

                totalStore.setText(humanReadableByte(totalData));
                availableStore.setText(humanReadableByte(availableData));

                //totalStore.setText(String.valueOf(availableData));
                //String AvailableData = totalData + "GB ";

                long percent = (availableData * 100 / totalData);
                Log.d(TAG, "onProgressUpdate: percent " + percent + " " + availableData + " " + totalData);
                bar.setProgress((int) percent);

                // TODO: 8/18/2022 this layout will bot be added in an exception
                linearLayout.addView(driveLayout);
            } catch (Exception e) {
                Log.d(TAG, "onProgressUpdate Error: " + e);
            }

            driveLayout.setOnClickListener(v -> {

                SingletonSocket.setNavigationPath();
                Log.d(TAG, "going to next fragment");
                Parameters parameters = new Parameters(v, values[0].getString("name"), bufferedReader, printWriter);

                SingletonSocket.getFragmentManger().beginTransaction()
                        .replace(((ViewGroup) getView().getParent()).getId(), directories.newInstance(null, null, parameters), "findThisFragment")
                        .addToBackStack(null)
                        .commit();


            });
        }

        private void getPcInfo() {
            try {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {

                    // User is signed in
                    //todo: save id of Mobile not PC

                    sendRequest("_PC_INFO_");
                    String ID = bufferedReader.readLine();
                    String PcName = bufferedReader.readLine();

                    HashMap<String, String> map = new HashMap<>();
                    map.put("ID", ID);
                    map.put("Name", PcName);
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Clients");
                    //databaseReference.child(LoginDetails.userKey).child("devices").child(ID).setValue(map);
                    databaseReference.child(user.getUid()).child("devices").child(ID).setValue(map);
                    LinkedDevices.AddDevice(PcName, ID);


                } else {
                    // No user is signed in
                }

            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, "Exception in getDriveNames()>> " + e);
            }
        }

        private void setDriveNames() {
            try {
                sendRequest("driveNames");
                Log.d(TAG, "Receiving Data from client");

                String data;
                Bundle bundle = new Bundle();
                int loop = 0;

                while (!(data = bufferedReader.readLine()).equals("EndOfStream")) {

                    Log.d(TAG, "Data received is " + data);
                    if (data.equals("Exception")) {
                        data = bufferedReader.readLine();
                        Log.d(TAG, "setDriveNames: Exception>> " + data);
                        bundle.putString("name", "Exception");
                        bundle.putString("Exception", data);
                        publishProgress(bundle);
                        bundle = new Bundle();
                        loop = 0;
                        continue;
                    }

                    switch (loop) {
                        case 0:
                            bundle.putString("name", data);
                            break;
                        case 1:
                            bundle.putString("label", data);
                            break;
                        case 2:
                            bundle.putString("type", data);
                            break;
                        case 3:
                            bundle.putString("format", data);
                            break;
                        case 4:
                            bundle.putString("totalSize", data);
                            break;
                        case 5:
                            bundle.putString("usedSize", data);
                            publishProgress(bundle);
                            bundle = new Bundle();
                            loop = -1;
                            break;
                    }
                    loop++;
                }
                Log.d(TAG, "Socket Data Ended");

                //socket.close();
                //Log.d(TAG, "Socket Closed");
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, "Exception in getDriveNames()>> " + e);
            }

        }

        void sendRequest(String request) {
            try {
                Log.d(TAG, "Sending Request to Server i.e >>  " + request);
                printWriter.println(request);
                printWriter.flush();

            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, "Exception in sendRequest() " + e);
            }
        }

        private String humanReadableByte(long fileSize) {
            long absB = fileSize == Long.MIN_VALUE ? Long.MAX_VALUE : Math.abs(fileSize);
            if (absB < 1024) {
                return fileSize + " B";
            }
            long value = absB;
            CharacterIterator ci = new StringCharacterIterator("KMGTPE");
            for (int i = 40; i >= 0 && absB > 0xfffccccccccccccL >> i; i -= 10) {
                value >>= 10;
                ci.next();
            }
            value *= Long.signum(fileSize);
            return String.format("%.1f %cB", value / 1024.0, ci.current());
        }

    }

}

