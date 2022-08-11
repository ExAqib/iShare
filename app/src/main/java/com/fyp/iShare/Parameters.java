package com.fyp.iShare;

import android.view.View;

import java.io.BufferedReader;
import java.io.PrintWriter;

public class Parameters {

    View view;
    String path;
    BufferedReader bufferedReader;
    PrintWriter printWriter;
    public Parameters(String path, BufferedReader bufferedReader, PrintWriter printWriter) {
        this.path = path;
        this.bufferedReader = bufferedReader;
        this.printWriter = printWriter;
    }

    public Parameters(View view, String path, BufferedReader bufferedReader, PrintWriter printWriter) {
        this.view = view;
        this.path = path;
        this.bufferedReader = bufferedReader;
        this.printWriter = printWriter;
    }

    public BufferedReader getBufferedReader() {
        return bufferedReader;
    }

    public PrintWriter getPrintWriter() {
        return printWriter;
    }

    public View getView() {
        return view;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

}
