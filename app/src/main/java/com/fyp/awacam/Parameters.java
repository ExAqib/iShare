package com.fyp.awacam;

import android.view.View;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Parameters {

        View view;
        String path;

    public void setPath(String path) {
        this.path = path;
    }

    BufferedReader bufferedReader;
        PrintWriter printWriter;

    public BufferedReader getBufferedReader() {
        return bufferedReader;
    }

    public Parameters(String path, BufferedReader bufferedReader, PrintWriter printWriter) {
        this.path = path;
        this.bufferedReader = bufferedReader;
        this.printWriter = printWriter;
    }

    public PrintWriter getPrintWriter() {
        return printWriter;
    }

    public Parameters(View view, String path, BufferedReader bufferedReader, PrintWriter printWriter) {
        this.view = view;
        this.path = path;
        this.bufferedReader = bufferedReader;
        this.printWriter = printWriter;
    }

        public View getView() {
            return view;
        }

        public String getPath() {
            return path;
        }

}
