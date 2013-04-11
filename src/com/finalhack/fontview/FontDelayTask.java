package com.finalhack.fontview;

import android.os.AsyncTask;

/**
 * This background tasks only backgrounds so that calling code can resume execution. This is needed
 * for finalizing the creation of parent layouts. Often, parent layouts need to complete
 * construction before we can get dimension information.
 */
public class FontDelayTask extends AsyncTask<Integer, Integer, Integer> {

	// Save off basic contextual information we'll get from a constructor
	private FontReceiver mFontReceiver;

	/**
	 * Standard constructor. Save contextual information.
	 */
	public FontDelayTask(FontReceiver fontReceiver) {
		mFontReceiver = fontReceiver;
	}

	/**
	 * Do nothing. Absolutely nothing. This is just a hand off mechanism that includes a small delay.
	 */
	@Override
	protected Integer doInBackground(Integer... params) {

		try {
			Thread.sleep(1);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * When we've pulled down the font file, tell our view who is patiently waiting
	 */
	@Override
	protected void onPostExecute(Integer result) {
		// Pass 0 and null because we don't care about reporting back anything
		// We already know where the file is stored
		mFontReceiver.send(0, null);
	}

}