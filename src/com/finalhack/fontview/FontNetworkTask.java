package com.finalhack.fontview;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * This background tasks remote font retrieval. Pull a font from a network resource and save it
 * locally.
 */
public class FontNetworkTask extends AsyncTask<Integer, Integer, Integer> {

    private static final int BUFFER_SIZE = 10000;

    // Tag whether we've tried to download this before
    // Currently, the app will need to be restated to re-download the font (for updates)
    // TODO: add either a cache timeout or etag strategy
    public static Boolean DOWNLOADED = false;

    // Save off basic contextual information we'll get from a constructor
    private Context mApplicationContext;
    private FontReceiver mFontReceiver;
    private String mFontUrl;

    /**
     * Standard constructor. Save contextual information.
     */
    public FontNetworkTask(Context applicationContext, FontReceiver fontReceiver, String fontUrl) {
        mApplicationContext = applicationContext;
        mFontReceiver = fontReceiver;
        mFontUrl = fontUrl;
    }

    @Override
    protected Integer doInBackground(Integer... params) {

        // Run a early check to see if we've already downloaded the file.
        // If we have, don't re-download
        if (DOWNLOADED) return null;

        // Only let one thread try to pull the file at a time
        synchronized (DOWNLOADED) {

            // If other threads were already waiting to enter this synchronized block, and we have
            // the file by the time they enter it...
            // Don't re-download
            if (DOWNLOADED) return null;

            long startTime = System.currentTimeMillis();

            String filename = null;
            int totalBytes = 0;
            // Download font file here
            try {
                // Open the connection
                HttpURLConnection connection = (HttpURLConnection) new URL(mFontUrl).openConnection();
                // Get ready to save the file data
                filename = FontView.hashUrlToFilename(mFontUrl);
                FileOutputStream fos = new FileOutputStream(new File(mApplicationContext.getExternalFilesDir(null), filename));
                // Stream the data in
                InputStream inputStream = connection.getInputStream();
                byte[] buffer = new byte[BUFFER_SIZE];
                int read;
                while ((read = inputStream.read(buffer)) != -1) {
                    // Make sure we limit the buffer bytes we're writing, to those we just read
                    fos.write(buffer, 0, read);
                    totalBytes += read;
                }
                // Cleanup
                fos.close();
                inputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            long endTime = System.currentTimeMillis();

            if (FontView.mDebugEnabled)
                Log.d(this.getClass().getName(), "Time to download font file(" + mFontUrl + "): " + (endTime - startTime) + "ms," +
                        "" + totalBytes + " bytes");

            // Tell everyone that we've successfully pulled the font file
            DOWNLOADED = true;

            return null;
        }

    }

    /**
     * When we've pulled down the font file, tell our view who is patiently waiting
     */
    @Override
    protected void onPostExecute(Integer result) {
        // Pass 0 and null because we don't care about reporting back anything
        // We already know where the file is stored
        if (mFontReceiver != null) mFontReceiver.send(0, null);
    }

}