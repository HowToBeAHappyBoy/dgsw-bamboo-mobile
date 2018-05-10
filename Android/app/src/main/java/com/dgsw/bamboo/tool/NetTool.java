package com.dgsw.bamboo.tool;

import android.os.AsyncTask;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;

public class NetTool {
    public static boolean isInternetAvailable() {
        try {
            return new Task().execute().get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static class Task extends AsyncTask<Void, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                InetAddress ipAddr = InetAddress.getByName("google.com");
                return !ipAddr.toString().equals("");
            } catch (UnknownHostException e) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
        }
    }
}
