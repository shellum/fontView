##fontView
An Android custom view to create styled images from characters in font files.
The view takes care of pulling fonts from a network location or locally and  caches them.

This project includes an example Android app which uses FontView.
Both the example .apk and fontview.jar are available in the bin/ directory.

####Example usage:

```java
  	// Setup our colors
	int red = getResources().getColor(R.color.red);
	int yellow = getResources().getColor(R.color.yellow);
	int white = getResources().getColor(R.color.white);

	// Initialize the FontView
	// A font can be pulled and cached from:
	// -Network locations (http url)
	// -Java/Android File objects
	// -Android Assets
	fontView.setFont("fonts/font.ttf");
	// fontView.setFont("http://some.network.location.com/badges.ttf", false);

	// Some configuration explination
	// To style the character you can choose:
	//
	// --A foreground color - the color of the character
	// --A background color - the background behind the character
	// --An outer color - the optional enclosing color when dealing with circles (outside the circle)
	// --A half color - the optional color for the bottom half of a shape's background
	// --A type - circle, square, half circle
	// --A gradient background flag - if both background and half colors are passed
	//
	// FontView.NOT_USED may be passed for a color that you want disabled

	// You can also fine tune placement of glyphs
	fontView.setYOffset(10);
	// Font size can be fine tuned as well
	fontView.setFontSizeMultiplier(0.8);

	// Load and show the character and configuration
	fontView.load(character, red, yellow, white, FontView.NOT_USED, false, FontView.ImageType.CIRCLE);
```
