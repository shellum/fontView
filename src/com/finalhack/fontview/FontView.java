package com.finalhack.fontview;

import java.io.File;
import java.security.MessageDigest;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.text.Html;
import android.util.AttributeSet;
import android.widget.ImageView;

public class FontView extends ImageView { 

	private static final int HALF_CIRCLE_SWEEP_DISTANCE = 180;
	private static final int HALF_CIRCLE_TOP_START = 180;
	private static final int HALF_CIRCLE_BOTTOM_START = 0;
	private static final int TOP = 0;
	private static final int LEFT = 0;

	public static final int NOT_USED = -1;

	// Metrics for calculating and placing glyphs
	private int mMidX;
	private int mMidY;
	private int mHeight;
	private int mWidth;
	private int textSize;
	private int mWidthDifferential;
	private int mHeightDifferential;
	private Integer mForegroundColor = null;
	private Integer mBackgroundColor = null;
	private Integer mOuterColor = null;
	private Integer mBottomHalfColor = null;
	private boolean mHasBackgroundGradient = false;
	private int mXOffset;
	private int mYOffset;
	private double mFontSizeMultiplier = 1.0;

	// The character we're writing
	// This is a String to allow HTML entities
	private String mCharacter;

	private String mFontLocation;
	private LocationType mFontLocationType;
	private File mFontFile;

	// Color and drawing resources
	private Paint mForegroundPaint;
	private Paint mBackgroundPaint;
	private Paint mBackgroundGradientPaint;
	private Paint mOuterPaint;
	private Paint mBottomHalfPaint;
	private Bitmap mBitmap;
	private Canvas mCanvas;
	private ImageType mType;

	// A safe context
	private Context mApplicationContext;

	// IPC callback hand off mechanism
	public FontReceiver mFontReceiver = new FontReceiver(getHandler(), this);

	// Cache our font
	private static Typeface mTypeFace;

	/**
	 * Required constructor from super class
	 * 
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public FontView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mApplicationContext = context.getApplicationContext();
	}

	/**
	 * Required constructor from super class
	 * 
	 * @param context
	 * @param attrs
	 */
	public FontView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mApplicationContext = context.getApplicationContext();
	}

	/**
	 * Required constructor from super class
	 * 
	 * @param context
	 */
	public FontView(Context context) {
		super(context);
		mApplicationContext = context.getApplicationContext();
	}

	/**
	 * When needed, pull the font from a File on the file system
	 * 
	 * @param fontFile
	 */
	public void setFont(File fontFile) {
		mFontFile = fontFile;
		mFontLocationType = LocationType.FILE;
	}

	/**
	 * WHen needed, pull the font from a network location
	 * 
	 * @param networkLocation
	 * @param mIsNetworkHttps
	 */
	public void setFont(String networkLocation, boolean mIsNetworkHttps) {
		mFontLocation = networkLocation;
		mFontLocationType = LocationType.NETWORK;
	}

	/**
	 * When needed, pull the font from the apk's assets folder
	 * 
	 * @param assetLocation
	 */
	public void setFont(String assetLocation) {
		// We need to null out set colors for cases like ListView row recycling
		mForegroundColor = null;
		mOuterColor = null;
		mBackgroundColor = null;
		mBottomHalfColor = null;
		mHasBackgroundGradient = false;
		
		mFontLocation = assetLocation;
		mFontLocationType = LocationType.ASSET;
	}

	/**
	 * Draws font data on the calling view Call this
	 * 
	 * @param character
	 * @param foregroundColor
	 * @param backgroundColor
	 * @param outerColor
	 * @param type
	 */
	public void load(String character, ImageType type) {
		if (mFontLocationType == null)
			throw new RuntimeException("Please call setFont() before calling load");

		mCharacter = character;
		mType = type;

		if (mFontLocationType == LocationType.NETWORK)
			new FontNetworkTask(mApplicationContext, mFontReceiver, mFontLocation).execute();
		else
			new FontDelayTask(mFontReceiver).execute();
	}

	/**
	 * Do not call. Called by caching mechanism to load the cached font and begin drawing. Cache the
	 * font by pulling it from one of the font location type locations
	 */
	public void internalUpdate() {
		if (mTypeFace == null) {
			switch (mFontLocationType) {
			case NETWORK:
				mTypeFace = Typeface.createFromFile(new File(mApplicationContext.getExternalFilesDir(null),
						hashUrlToFilename(mFontLocation)));
				break;
			case FILE:
				mTypeFace = Typeface.createFromFile(mFontFile);
				break;
			case ASSET:
				mTypeFace = Typeface.createFromAsset(mApplicationContext.getAssets(), mFontLocation);
				break;
			}
		}

		// Now we have all the data we need.
		// Draw everything.
		draw();
	}

	/**
	 * Setup our paint and calculate glyph measurement for correct placement
	 */
	private void setupImage() {
		// Get the dimensions of the current view
		mHeight = getMeasuredHeight();
		mWidth = getMeasuredWidth();

		// Grab mid points
		mMidX = mWidth / 2;
		mMidY = mHeight / 2;

		// Try to maximize the glyph size within the region, taking into account user modification
		textSize = (int) (mHeight * mFontSizeMultiplier);

		// Setup our glyph color
		mForegroundPaint = new Paint();
		mForegroundPaint.setTextSize(textSize);
		if (mForegroundColor != null)
			mForegroundPaint.setColor(mForegroundColor);
		else
			mForegroundPaint.setColor(Color.BLACK);
		mForegroundPaint.setAntiAlias(true);
		mForegroundPaint.setTypeface(mTypeFace);
		Paint.FontMetrics metrics = new Paint.FontMetrics();
		mForegroundPaint.getFontMetrics(metrics);

		// Setup our glyph background
		if (mBackgroundColor != null) {
			mBackgroundPaint = new Paint();
			mBackgroundPaint.setColor(mBackgroundColor);
			mBackgroundPaint.setAntiAlias(true);
		}

		// Sometimes there is a surrounding color which gives the appearance of clipping/shaping
		// This will be the background color for that region.
		if (mOuterColor != null) {
			mOuterPaint = new Paint();
			mOuterPaint.setColor(mOuterColor);
			mOuterPaint.setAntiAlias(true);
		}

		// Sometimes there is a bottom half paint color
		if (mBottomHalfColor != null) {
			mBottomHalfPaint = new Paint();
			mBottomHalfPaint.setColor(mBottomHalfColor);
			mBottomHalfPaint.setAntiAlias(true);
		}

		// Sometimes there is a gradient background
		if (mBackgroundColor != null && mBottomHalfColor != null) {
			mBackgroundGradientPaint = new Paint();
			LinearGradient linearGradient = new LinearGradient(mMidX, TOP, mMidX, mHeight, mBackgroundColor, mBottomHalfColor,
					Shader.TileMode.REPEAT);
			mBackgroundGradientPaint.setDither(false);
			mBackgroundGradientPaint.setShader(linearGradient);
		}

		// Setup our canvas
		mBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
		mCanvas = new Canvas(mBitmap);

		// Sometimes, glyphs have extra padding. We want to take into account
		// the maximum padding available
		int width = (int) mForegroundPaint.measureText(Html.fromHtml(mCharacter).toString());

		// The width differential will be the starting x for drawing the glyph
		// This will be the center of the view offset by half of the glyph width
		mWidthDifferential = mMidX - (width / 2);

		// Save some intermediate data for reuse
		double absGlyphTop = Math.abs(metrics.top);
		double absGlyphBottom = Math.abs(metrics.bottom);
		double glyphHeight = absGlyphTop + absGlyphBottom;

		// Calculate the total glyph height (character above baseline + character below baseline)
		// and get it's midpoint
		// Use this midpoint to offset the center of the view we're drawing in
		mHeightDifferential = (int) (mMidY + ((glyphHeight / 2)));

		// By default, center the part of the glyph that is above the baseline
		// Move the glyph down such that the glyph part that is above the baseline is centered
		mHeightDifferential *= (1.0 - (absGlyphBottom / absGlyphTop));

		// Allow for a user specified x or y offset to fine tune how they want the glyphs displayed
		mWidthDifferential += mXOffset;
		mHeightDifferential += mYOffset;
	}

	/**
	 * Draw the shapes and glyph
	 */
	private void draw() {
		// Calculate glyph metrics we need
		setupImage();

		// Draw image type specific parts
		if (mType == ImageType.SQUARE) {
			if (mBottomHalfColor == null) {
				if (mBackgroundColor != null)
					mCanvas.drawColor(mBackgroundColor);
			} else {
				if (mHasBackgroundGradient) {
					mCanvas.drawRect(new Rect(LEFT, TOP, mWidth, mHeight), mBackgroundGradientPaint);
				} else {
					mCanvas.drawRect(new Rect(LEFT, TOP, mWidth, mMidY), mBackgroundPaint);
					mCanvas.drawRect(new Rect(LEFT, mMidY, mWidth, mHeight), mBottomHalfPaint);
				}
			}
		}
		if (mType == ImageType.CIRCLE) {
			mCanvas.drawColor(mOuterColor);
			if (mBottomHalfColor == null) {
				if (mBackgroundColor != null)
					mCanvas.drawCircle(mMidX, mMidY, mMidX, mBackgroundPaint);
			} else {
				if (mHasBackgroundGradient) {
					mCanvas.drawCircle(mMidX, mMidY, mMidX, mBackgroundGradientPaint);
				} else {
					// Draw the circle's bottom half
					mCanvas.drawArc(new RectF(LEFT, TOP, mWidth, mHeight), HALF_CIRCLE_BOTTOM_START, HALF_CIRCLE_SWEEP_DISTANCE, true,
							mBackgroundPaint);
					// Draw the circle's top half
					mCanvas.drawArc(new RectF(LEFT, TOP, mWidth, mHeight), HALF_CIRCLE_TOP_START, HALF_CIRCLE_SWEEP_DISTANCE, true,
							mBottomHalfPaint);
				}
			}
		}

		// Draw the glyph
		mCanvas.drawText(Html.fromHtml(mCharacter).toString(), mWidthDifferential, mHeightDifferential, mForegroundPaint);

		// Push the bitmap to the image
		setImageBitmap(mBitmap);
	}

	// Convenience classes for tracking mutable attributes
	public enum ImageType {
		SQUARE, CIRCLE;
	}

	public enum LocationType {
		NETWORK, ASSET, FILE
	}

	/**
	 * A convenience method for creating local font file names that are pulled from a network
	 * connection while allowing the user to use multiple fonts at once
	 * 
	 * @param stringToHash
	 * @return
	 */
	public static String hashUrlToFilename(String stringToHash) {
		final String HASH_TYPE = "MD5";
		try {
			// Create byte hash
			MessageDigest messageDigest = MessageDigest.getInstance(HASH_TYPE);
			messageDigest.update(stringToHash.getBytes());
			byte digestBytes[] = messageDigest.digest();

			// Rewrite the byte has as a hex hash
			StringBuilder hexString = new StringBuilder();
			for (byte i : digestBytes)
				hexString.append(Integer.toHexString(0xFF & i));

			return hexString.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Fine tune glyph placement on the x-axis
	 * 
	 * @param xOffset
	 * @return fontView
	 */
	public FontView setXOffset(int xOffset) {
		mXOffset = xOffset;
		return this;
	}

	/**
	 * Fine tune glyph placement on the y-axis
	 * 
	 * @param yOffset
	 * @return fontView
	 */
	public FontView setYOffset(int yOffset) {
		mYOffset = yOffset;
		return this;
	}

	/**
	 * Fine tune the font size
	 * 
	 * @param fontSizeMultiplier
	 *            (1.1=10% bigger, 0.9=10% smaller)
	 * @return fontView
	 */
	public FontView setFontSizeMultiplier(double fontSizeMultiplier) {
		this.mFontSizeMultiplier = fontSizeMultiplier;
		return this;
	}

	/**
	 * Adds a background color behind the character.
	 * 
	 * @param backgroundColor
	 * @return fontView
	 */
	public FontView addBackgroundColor(int backgroundColor) {
		this.mBackgroundColor = backgroundColor;
		return this;
	}

	/**
	 * Adds a foreground (character) color
	 * 
	 * @param foregroundColor
	 * @return fontView
	 */
	public FontView addForegroundColor(int foregroundColor) {
		this.mForegroundColor = foregroundColor;
		return this;
	}

	/**
	 * Adds an outer color, used behind shapes like circles where the circle background color only
	 * resides in the circle. The outerColor is the rest of the square that circumscribes the
	 * circle.
	 * 
	 * @param outerColor
	 * @return fontView
	 */
	public FontView addOuterColor(int outerColor) {
		this.mOuterColor = outerColor;
		return this;
	}

	/**
	 * Adds a color to the bottom half of the shapes background. Is usually used in conjunction with
	 * setting a background color. Also used to set the second color when turning on a gradient
	 * background.
	 * 
	 * @param halfColor
	 * @return fontView
	 */
	public FontView addBottomHalfColor(int bottomHalfColor) {
		this.mBottomHalfColor = bottomHalfColor;
		return this;
	}

	/**
	 * Makes the background behind the character a gradient. The gradient is based on two colors:
	 * background and bottomHalfColor
	 * 
	 * @param mHasBackgroundGradient
	 * @return fontView
	 */
	public FontView setBackgroundGradient(boolean mHasBackgroundGradient) {
		this.mHasBackgroundGradient = mHasBackgroundGradient;
		return this;
	}

}