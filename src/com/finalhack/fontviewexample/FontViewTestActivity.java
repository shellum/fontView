package com.finalhack.fontviewexample;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.finalhack.fontview.FontView;

/*
 * This demo shows an example of FontView.
 * It uses a simple activity with a single ListView.
 * Each row of the list view is populated with a graphically styled version of font characters.
 */
public class FontViewTestActivity extends Activity {

    // The main ListView that will show font characters graphically
    private ListView list;

    // Load up a simple layout with a ListView, and populate it
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.font_view_test);
        list = (ListView) findViewById(R.id.list);
    } 

    @Override
    protected void onResume() {
        super.onResume();
        List<String> strs = new ArrayList<String>();

        // Repeat some font characters to get a feel for performance
        for (int i = 0; i < 5; i++) {
            // Add some random characters from a .ttf (True Type) font file
            // Characters can be standard 'A', unicode '\u1234', or HTML entities '&#1234;'
            /*
             * strs.add("&#xe000;"); strs.add("&#xe001;"); strs.add("&#xe002;");
             * strs.add("&#xe003;"); strs.add("&#xe004;"); strs.add("&#xe005;");
             * strs.add("&#xe006;"); strs.add("&#xe007;"); strs.add("&#xe008;");
             * strs.add("&#xe009;"); strs.add("&#xe00a;"); strs.add("&#xe00b;");
             * strs.add("&#xe00c;"); strs.add("&#xe00d;"); strs.add("&#xe00e;");
             * strs.add("&#xe00f;");
             */
            strs.add("A");
            strs.add("\u00c5");
            strs.add("\u00c7");
            strs.add("E");
            strs.add("J");
            strs.add("M");
            strs.add("\u0103");
            strs.add("\u0060");
            strs.add("0");
            strs.add("Z");
            strs.add("d");
            strs.add("\u00fb");
            strs.add("&");
        }

        // Use a custom adapter to fill the ListView rows with FontViews
        list.setAdapter(new Adapter(this, strs));
    }

    /**
     * A custom adapter for populating ListView rows with a FontView
     */
    private class Adapter extends ArrayAdapter<String> {

        // Standard constructor needed by the super class
        public Adapter(Context context, List<String> objects) {
            super(context, R.layout.row, 0, objects);
        }

        // Setup each custom row in the ListView
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View view = convertView;

            // Recycle the rows if they're available and already inflated
            if (view == null)
                view = getLayoutInflater().inflate(R.layout.row, parent, false);

            FontView fontView;
            TextView textView;

            IdHolder idHolder = (IdHolder) view.getTag();
            if (idHolder == null) {
                // Grab the FontView for loading characters
                fontView = (FontView) view.findViewById(R.id.character);
                // Grab the TextView for showing the FontView's corresponding text character
                textView = (TextView) view.findViewById(R.id.text);
                idHolder = new IdHolder(fontView, textView);
                view.setTag(idHolder);
            } else {
                textView = idHolder.textView;
                fontView = idHolder.fontView;
            }

            // Grab the character we want to render and dress up
            String character = getItem(position);

            // Setup our colors
            int red = getResources().getColor(R.color.red);
            int yellow = getResources().getColor(R.color.yellow);
            int lightBlue = getResources().getColor(R.color.light_blue);
            int darkBlue = getResources().getColor(R.color.dark_blue);
            int green = getResources().getColor(R.color.green);
            int white = getResources().getColor(R.color.white);
            int black = getResources().getColor(R.color.black);

            // Initialize the FontView
            // A font can be pulled and cached from:
            // A network location (http url)
            // A Java/Android File object
            // Android Assets
            fontView.setupFont("fonts/font.ttf", character, FontView.ImageType.CIRCLE);
            fontView.resetDecorators();
            // fontView.setFont("http://172.29.132.92:8080/utftest/badges.ttf", false);

            // Tell this row to start loading the font
            // If we don't already have the font, and need it, get it from the Network or File
            // System
            // If we've already accessed it via the Network or File System (via a Java File or
            // Android Asset), it will be cached for us
            // It may take a second the first time if we need to grab the font file from the network
            // To style the character you can choose:
            //
            // --A foreground color - the color of the character
            // --A background color - the background behind the character
            // --An outer color - the optional enclosing color when dealing with circles (outside
            // the
            // circle)
            // --A half color - the optional color for the bottom half of a shape's background
            // --A type - circle, square, half circle
            // --A gradient background flag - if both background and half colors are passed
            //
            // FontView.NOT_USED may be passed for a color that you want disabled

            // You can also fine tune placement of glyphs
            fontView.setYOffset(10);
            // Font size can be fine tuned as well
            fontView.setFontSizeMultiplier(0.8);

            // Draw seemingly random sequences of shape/color combinations
            if (position % 5 == 0) {
                fontView.addForegroundColor(red);
                fontView.addBackgroundColor(white);
                fontView.addOuterColor(white);
                fontView.setupFont("fonts/font.ttf", character, FontView.ImageType.CIRCLE);
            } else if (position % 4 == 0) {
                fontView.addForegroundColor(white);
                fontView.addBackgroundColor(lightBlue);
                fontView.addOuterColor(white);
                fontView.addBottomHalfColor(darkBlue);
                fontView.setupFont("fonts/font.ttf", character, FontView.ImageType.CIRCLE);
            } else if (position % 3 == 0) {
                fontView.addForegroundColor(white);
                fontView.addBackgroundColor(green);
                fontView.addOuterColor(white);
                fontView.addBottomHalfColor(black);
                fontView.setupFont("fonts/font.ttf", character, FontView.ImageType.CIRCLE);
            } else if (position % 2 == 0) {
                fontView.addForegroundColor(yellow);
                fontView.addBackgroundColor(red);
                fontView.addOuterColor(white);
                fontView.setupFont("fonts/font.ttf", character, FontView.ImageType.SQUARE);
            } else {
                fontView.addForegroundColor(white);
                fontView.addBackgroundColor(darkBlue);
                fontView.addOuterColor(white);
                fontView.addBottomHalfColor(lightBlue);
                fontView.setBackgroundGradient(true);
                fontView.setupFont("fonts/font.ttf", character, FontView.ImageType.SQUARE);
            }

            // Show the corresponding text character
            textView.setText(character);

            return view;
        }
    }

    /**
     * Cache view lookups for ListView rows
     */
    private class IdHolder {
        public FontView fontView;
        public TextView textView;

        /**
         * A convenience constructor for quickly creating view caches
         * 
         * @param fontView
         * @param textView
         */
        public IdHolder(FontView fontView, TextView textView) {
            this.fontView = fontView;
            this.textView = textView;
        }
    }
}