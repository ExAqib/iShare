package com.HuimangTech.iShare.ui.fileTransfer;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.HuimangTech.iShare.DownloadFile;
import com.HuimangTech.iShare.Parameters;
import com.HuimangTech.iShare.R;
import com.HuimangTech.iShare.SingletonSocket;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.ArrayList;


public class directories extends Fragment {

    private static final String TAG = "tag";
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    static Parameters parameters;
    static PrintWriter printWriter;
    static BufferedReader bufferedReader;
    LinearLayout linearlayout;
    ArrayList<Bundle> PcData = new ArrayList<>();

    public directories() {
        // Required empty public constructor
    }

    public static directories newInstance(String param1, String param2, Parameters p) {
        directories fragment = new directories();
        parameters = p;
        printWriter = p.getPrintWriter();
        bufferedReader = p.getBufferedReader();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate called for directory fragment");

        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView called for directory fragment");

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_directories, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated called for directory fragment");

    }

    @Override
    public void onStart() {
        super.onStart();

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Handle the back button event
                if (PcData.isEmpty()) {
                    try {
                        SingletonSocket.getNavigationPath().clear();
                    } catch (Exception e) {
                        Log.d(TAG, "Exception " + e);
                    }
                    SingletonSocket.getFragmentManger().popBackStack();

                } else {
                    try {
                        SingletonSocket.getNavigationPath().remove(SingletonSocket.getNavigationPath().size() - 1);
                    } catch (Exception e) {
                        Log.d(TAG, "Exception " + e);
                    }
                    Log.d(TAG, "Restoring data ");
                    restoreData();
                }
            }
        };

        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
        Log.d(TAG, "onViewStart called for directory fragment");

        linearlayout = requireView().findViewById(R.id.show_directory_linear_layout);

        DirectoryProcessor directoryProcessor = new DirectoryProcessor();
        directoryProcessor.execute(parameters.getPath());

    }

    void sendRequest(String request) {
        try {
            Log.d(TAG, "Sending Request to Server i.e >>  " + request);
//           PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
            printWriter.println(request);
            printWriter.flush();

        } catch (Exception e) {
            e.printStackTrace();
            sendToast("Request Timeout");
            Log.d(TAG, "Exception in sendRequest() " + e);
        }
    }

    private void sendToast(String message) {
        requireActivity().runOnUiThread(() -> {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        });
    }

    private void restoreData() {

        Bundle b = PcData.get(PcData.size() - 1);

        PcData.remove(PcData.size() - 1);

        requireActivity().runOnUiThread(() -> linearlayout.removeAllViews());


        for (int i = 1; i <= b.getInt("SIZE"); i++) {

            final String data = b.getString(String.valueOf(i));

            View myLayout = inflateData(data);

            myLayout.setOnClickListener((View v) -> {

                PcData.add(b);

                requireActivity().runOnUiThread(() -> {

                    int totalChild = linearlayout.getChildCount();
                    Log.d(TAG, "Total children in linearlayout are " + totalChild);

                    if (totalChild > 0) {
                        linearlayout.removeAllViews();
                    }
                });

                DirectoryProcessor directoryProcessor = new DirectoryProcessor();
                directoryProcessor.execute(data);
            });

            requireActivity().runOnUiThread(() -> {
                linearlayout.addView(myLayout);
            });

        }

    }

    private View inflateData(String data) {
        View myLayout = getLayoutInflater().inflate(R.layout.directory, null, false);

        CardView.LayoutParams layoutParams = new CardView.LayoutParams(CardView.LayoutParams.MATCH_PARENT, CardView.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(5, 10, 5, 10);
        myLayout.setLayoutParams(layoutParams);

        TextView directoryName = myLayout.findViewById(R.id.directory_name);
        ImageView fileType = myLayout.findViewById(R.id.img_FileType);

        fileType.setImageResource(extractFileType(data));
        directoryName.setText(data);
        return myLayout;
    }

    class DirectoryProcessor extends AsyncTask<String, String, Bundle> {

        String path;
        Bundle bundle = new Bundle();
        int size = 0;

        @Override
        protected Bundle doInBackground(String... strings) {
            path = strings[0];
            getDirectory(path);
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);

            switch (values[0]) {
                case "File":
                    try {
                        requireActivity().runOnUiThread(() -> {

                            Log.d(TAG, "It is file!!");

                            AlertDialog.Builder confirmationMessage = new AlertDialog.Builder(requireContext());
                            confirmationMessage.setTitle("Download...");
                            confirmationMessage.setMessage("Are you Sure you want to download this file?");
                            confirmationMessage.setPositiveButton("Yes", (dialog, which) -> {

                                DownloadFile receiveFile = new DownloadFile(getContext());
                                receiveFile.execute(path);

                                restoreData();
                            });
                            confirmationMessage.setNegativeButton("No", (dialog, which) -> {

                                requireActivity().onBackPressed();

                            });

                            confirmationMessage.setCancelable(false);
                            confirmationMessage.show();
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.d(TAG, "Exception >> " + e);
                    }

                    break;
                case "CLIENT_LEFT":
                    sendToast("CLIENT HAS LEFT");
                    Log.d(TAG, "CLIENT HAS LEFT");
                    sendRequest("CLIENT_LEFT_ACKNOWLEDGEMENT_MOBILE");
                    requireActivity().runOnUiThread(() -> {
                        AlertDialog.Builder confirmationMessage = new AlertDialog.Builder(requireContext());
                        confirmationMessage.setTitle("CLIENT LEFT");
                        confirmationMessage.setMessage("YOUR PC HAS LEFT. MAKE SURE THE SOFTWARE IS RUNNING ON PC.");
                        confirmationMessage.setCancelable(false);
                        confirmationMessage.show();
                    });
                    break;

                default:
                    View myLayout = getLayoutInflater().inflate(R.layout.directory, null, false);

                    CardView.LayoutParams layoutParams = new CardView.LayoutParams(CardView.LayoutParams.MATCH_PARENT, CardView.LayoutParams.WRAP_CONTENT);
                    layoutParams.setMargins(5, 10, 5, 10);
                    myLayout.setLayoutParams(layoutParams);

                    TextView directoryName = myLayout.findViewById(R.id.directory_name);
                    ImageView fileType = myLayout.findViewById(R.id.img_FileType);

                    fileType.setImageResource(extractFileType(values[0]));
                    directoryName.setText(values[0]);

                    size++;
                    bundle.putInt("SIZE", size);
                    bundle.putString(String.valueOf(size), values[0]);
                    myLayout.setOnClickListener((View v) -> {

                        PcData.add(bundle);

                        requireActivity().runOnUiThread(() -> {

                            int totalChild = linearlayout.getChildCount();
                            Log.d(TAG, "Total Elements in linearlayout are " + totalChild);

                            if (totalChild > 0) {
                                linearlayout.removeAllViews();
                            }
                        });
                        DirectoryProcessor directoryProcessor = new DirectoryProcessor();
                        directoryProcessor.execute(values[0]);
                    });
                    requireActivity().runOnUiThread(() -> {
                        linearlayout.addView(myLayout);
                    });
                    break;
            }
        }

        private void getDirectory(String path) {
            sendRequest("subDirectories");
            //sendRequest(path);


            SingletonSocket.getNavigationPath().add(path);

            StringBuilder path2 = new StringBuilder();

            int i = 0;
            Log.d(TAG, "getDirectory: " + SingletonSocket.getNavigationPath());
            for (String s : SingletonSocket.getNavigationPath()) {
                if (i < 2) {
                    path2.append(s);
                } else {
                    path2.append("\\").append(s);
                }
                i++;
            }
            Log.d(TAG, "sending !:" + path2);
            sendRequest(path2.toString());
            //sendRequest(path2.toString());

            try {
                String data;
                boolean emptyDirectory = true;

                while (!(data = bufferedReader.readLine()).equals("EndOfStream")) {
                    emptyDirectory = false;
                    Log.d(TAG, "SubDirectory is " + data);
                    onProgressUpdate(data);
                }
                if (emptyDirectory) {
                    try {
                        SingletonSocket.getNavigationPath().remove(SingletonSocket.getNavigationPath().size() - 1);
                    } catch (Exception e) {
                        Log.d(TAG, "Exception " + e);
                    }
                    Log.d(TAG, "Empty Folder");

                    sendToast("Folder is Empty");
                    restoreData();
                    return;
                }
                Log.d(TAG, "Socket data ended " + data);

            } catch (Exception e) {
                e.printStackTrace();
            }

        }



    }

    private int extractFileType(String name) {

        String[] fileArray = name.split("\\.");

        if (fileArray[fileArray.length - 1].equals(name)) {
            return R.drawable.folder_48px;
        }
        switch (fileArray[fileArray.length - 1]) {
            case "txt": {
                return R.drawable.draft_48px;
            }
            case "mp3":
            case "m4a": {
                return R.drawable.music_note_48px;
            }
            case "png":
            case "jpg":
            case "jpeg": {
                return R.drawable.image_48px;
            }
            case "mp4": {
                return R.drawable.movie_48px;
            }
            case "rar": {
                return R.drawable.folder_zip_48px;
            }
            default:
                return R.drawable.draft_48px;
        }

    }

}