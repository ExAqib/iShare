package com.fyp.iShare.ui.downloads;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DownloadedFiles {
    static List<String> fileNames =  new ArrayList<>() ;
    public static List<String> fileSize =  new ArrayList<>() ;
    public static List<String> fileType =  new ArrayList<>() ;


    public static void AddFile(String FileName,String FileSize){
        try{
            fileNames. add(FileName);
            fileSize. add(FileSize);

        }catch(Exception e){
            Log.d("tag", "AddFile: "+e);
        }
    }

    public static List<String> GetFiles(){
            return fileNames;

    }
}
