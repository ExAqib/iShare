package com.fyp.iShare;

import android.os.strictmode.InstanceCountViolation;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public   class LinkedDevices {
    static HashMap<String ,String> deviceDetails= new HashMap<>();
    static List<String> devices = new ArrayList<>();
    static String TAG= "tag";

    static public void AddDevice(String name ,String ID){

        try {
            deviceDetails.put(name,ID);
            devices.add(name);
        }
        catch (Exception e){
            Log.d(TAG, "AddDevice: Exception"+e);
        }
    }

    static public String GetDeviceID(String name){
        try {
            if(deviceDetails.containsKey(name)){
                return deviceDetails.get(name);
            }
            else{
                return null;
            }
        }
        catch (Exception e){
            Log.d(TAG, "GetDeviceID: Exception"+e);
            return null;
        }
    }

    static public List<String> GetAllDeviceNames(){
        if(devices.size()==0){
            List<String> emptyDevices = new ArrayList<>();
            emptyDevices.add("No Saved Device Available");
            return emptyDevices;
        }
        return devices;
    }

}
