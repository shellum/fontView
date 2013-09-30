package com.finalhack.fontview;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

/**
 * A simple IPS hand off mechanism for updating views after a network check
 */
public class FontReceiver extends ResultReceiver {

    private FontView mCallback;

    public FontReceiver(Handler handler, FontView callback) {
        super(handler);
        mCallback = callback;
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        // If we've left the view, squelch any errors
        // The callback could
        try {
            mCallback.internalUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
