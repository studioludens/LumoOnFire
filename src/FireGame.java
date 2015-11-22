import java.awt.Color;

import controller.RandomPattern;
import view.LogoPixel;
import processing.core.*;
import processing.net.Client;

public class FireGame extends PApplet {
	
	/****
	 *
	 *  logo mask & pattern generators
	 *
	 ****/
	
	
	// B&W mask covering the pixels
	public PImage logo_mask;
	
	// two random pattern generators to play with hue of the two flames
	public RandomPattern random_pattern_hue1 = new RandomPattern(this);
	public RandomPattern random_pattern_hue2 = new RandomPattern(this);
	
	/****
	 *
	 *  Pixel Data
	 *
	 ****/
	
	// 2D array for pixels of logo [letter][pixel from top to bottom]
	public LogoPixel[][] logo_pixels;
	 
	// displacement of logo relative to window origin
	// (0,0 of actual pixels is at left side of "T" (x) and
	// top of SLASH (y)
	// values based on original image (900x450px)
	public int logo_dx = 45;
	public int logo_dy = 80;
	
	public int[] pixelArray = new int[19];
	
	// x & y values of the logo pixels, as located in the original (900x500px) image
	// the conversion factor is used to get the relative values for the
	// current screen size (now: 450x250px, so 0.5)
	public int[] pixels_x   = { 0, 235, 490, 645 };
	public int[][] pixels_y = { { 0, 80, 144, 205, 265 }, 
	                            { 0, 80, 144, 205, 265 },
	                            { 0, 80, 144, 205, 265, 325},
	                            {        118, 205, 265 }
	                          };
	                          
	// width and heights of the logo pixels
	// as located in the original 900x500px image
	public int[] pixels_width    = { 235, 255, 155, 160 };
	public int[][] pixels_height = { { 80, 64, 61, 60, 60 }, 
	                                 { 80, 64, 61, 60, 60 },
	                                 { 80, 64, 61, 60, 60, 45 },
	                                 {     87, 60, 60 }
	                               };
	    
	// delay (order) of the pixels as activated by increasing p1_level
	// inverted for player 2
	public int[][] pixels_delay = { { 8, 6, 4, 2, 0 },
	                                { 9, 7, 5, 3, 1 },
	                                { 10, 11, 13, 14, 16, 17 },
	                                { 12, 15, 18 }
	                              };
	
	// array with a random pattern generator for each pixel
	public RandomPattern[][] pixels_pattern = new RandomPattern[4][6];
	 
	// pixel slope = dy/dx = pixel value / player level 
	public float pixel_slope_hue = 1.25f;
	//public float pixel_slope_hue = 2;
	public float pixel_slope_brightness = 6;
	// pixel interval will be calculated in setup based on nubmer of pixels and slope
	public float pixel_interval;
	// minimum brightness value of any pixel
	public float pixel_min_brightness = 0.10f;
	
	// conversion factor is the factor between the current size of the logo and the original (900 px)
	public float conversionFactor;
	
	/****
	 *
	 *  user interface controls
	 *
	 ****/
	 
	// player's levels
	public float p1_level = 0; 
	public float p2_level = 0;
	// minimum level of a player (to have a minimum flame visible)
	public float player_min_level = 0.1f;
	
	// check whether the "button" of the user is released
	public boolean p1_release = true;
	public boolean p2_release = true;
	
	// player 1 = ctrl key
	public int p1_key_code = 17;
	// player 2 = right arrow
	public int p2_key_code = 39;
	
	// increase of level when player's button is pressed once
	public float input_increase = 0.06f;
	// decrease of players' levels per frame
	public float input_decrease = 0.006f;
	
	// hue range of player 1
	public float p1_hue_min = 0.00f;
	public float p1_hue_max = 0.15f;
	// hue range of player 2
	public float p2_hue_min = 0.00f;
	public float p2_hue_max = 0.15f;
	// hue randomness per pixel (both players)
	public float hue_randomness = 0.03f;
	
	public LumoCommunicator lumoCom = new LumoCommunicator(); 
	
	
	/****
	 *
	 *  setup & draw functions
	 *
	 ****/
	
	public void setup() {
		
		lumoCom.setup(this);

		// set some basic applet stuff
		frameRate(20);
		colorMode(HSB, 1);
		background(0,0,0);
		size(450, 250);
		  
		// calc conversion factor
		conversionFactor = (float)(width)/900;
		  
		logo_pixels = new LogoPixel[4][0];
		logo_pixels[0] = (LogoPixel[]) expand(logo_pixels[0], 5);
		logo_pixels[1] = (LogoPixel[]) expand(logo_pixels[1], 5);
		logo_pixels[2] = (LogoPixel[]) expand(logo_pixels[2], 6);
		logo_pixels[3] = (LogoPixel[]) expand(logo_pixels[3], 3);
		  
		/* setup random patterns */
		// this pattern is used for some randomness in the first player's hue
		float low_level = -0.02f;
		float high_level = 0.02f;
		
		//random_pattern_hue1.setValueRange(p1_hue_min, p1_hue_max);
		random_pattern_hue1.setValueRange(low_level, high_level);
		random_pattern_hue1.randomizeTargetAndCurrentValue();
		random_pattern_hue1.setDurationRange(1000, 2000);
		random_pattern_hue1.randomizeCurrentDuration();
		random_pattern_hue1.setMinDValue(0.05f);
		  
		// this pattern is used for some randomness in the second player's hue
		//random_pattern_hue2.setValueRange(p2_hue_min, p2_hue_max);
		random_pattern_hue2.setValueRange(low_level, high_level);
		random_pattern_hue2.randomizeTargetAndCurrentValue();
		random_pattern_hue2.setDurationRange(1000, 2000);
		random_pattern_hue2.randomizeCurrentDuration();
		random_pattern_hue2.setMinDValue(0.005f);
		 
		// calculate the interval between each pixel's delay
		pixel_interval = (1 - (1 / pixel_slope_brightness)) / 19;
		  
		// load mask from file
		loadMask();
		// initiate logo pixels
		createPixels();
	}
	
	public void draw() {	
		
		lowerPlayerLevels();
		
		// first draw new pixels...
		drawPixelsInGame();
		// ... then draw new pixels over it
		drawMask();
		
		setSpeedOfFire();
		
		//println("draw!");
		//println("p1: " + p1_level);
		//println("p2: " + p2_level);
		//println();
		
		lumoCom.draw(pixelArray);
	}
	
	
	public void keyPressed() {
	  
		// player 1 pressed
		if(keyCode == p1_key_code && p1_release) {
			p1_level += input_increase;
			p1_release = false;
		}
		  
		  
		// player 2 pressed
		if(keyCode == p2_key_code && p2_release) {
			p2_level += input_increase;
			p2_release = false;
		}
	}
	
	public void keyReleased() {
		if(keyCode == p1_key_code)
			p1_release = true;
		if(keyCode == p2_key_code)
			p2_release = true;
	}
	
	/**
	 *  load mask from file
	 */
	public void loadMask() {
		logo_mask = loadImage("../images/TUe_mask_450x250.png");
	}
	
	/**
	 *  draw mask on screen
	 */
	public void drawMask() { 
		if(logo_mask != null)
			image(logo_mask, 0, 0);
	}
	
	/**
	 *  create the logo pixels based on the values defined at the start
	 *  the conversionFactor is used to make the pixels fit the current screen
	 */
	public void createPixels() {
	  
		for(int column = 0; column < logo_pixels.length; column++) {
			for(int row = 0; row < logo_pixels[column].length; row++) {
				logo_pixels[column][row] = 
					new LogoPixel((PApplet)this,
								(pixels_x[column] + logo_dx) * conversionFactor,
								(pixels_y[column][row] + logo_dy) * conversionFactor,
								pixels_width[column] * conversionFactor,
								pixels_height[column][row] * conversionFactor,
								color(0f, 0f, 0f));
	      
				RandomPattern random_pattern = new RandomPattern(this);
				random_pattern.setValueRange(-hue_randomness, hue_randomness);
				random_pattern.randomizeTargetAndCurrentValue();
				random_pattern.setDurationRange(1000, 2000);
				random_pattern.randomizeCurrentDuration();
				random_pattern.setMinDValue(0.2f);
				pixels_pattern[column][row] = random_pattern;
			}
		}
	}
	
	/**
	 *  draw new logo pixels, using the random pattern generators
	 *  this functions creates a gradient from dark to light from top to bottom
	 *  with a hue value that flickers
	 */
	public void drawPixelsInGame() {
	  
		float hue1_presence;
		float hue2_presence;
		float raw_hue1;// = random_pattern_hue1.nextValue();
		float raw_hue2;// = random_pattern_hue2.nextValue();
		float hue1_ratio;
		float hue2_ratio;
		float new_hue;
		float raw_brightness1;
		float raw_brightness2;
		float new_brightness;
		float randomness;
		
		float hue1_random = 0;//random_pattern_hue1.nextValue();
		println("hue1_random" + hue1_random);
		float hue2_random = 0;//random_pattern_hue2.nextValue();
		
		int counter = 0;
	  
		for(int column = 0; column < logo_pixels.length; column++) {
			for(int row = 0; row < logo_pixels[column].length; row++) {
				
				float this_level = pixel_interval * pixels_delay[column][row];
				
				raw_hue1 = p1_hue_min + (p1_hue_max - p1_hue_min) * max(0, (p1_level - (pixel_interval * pixels_delay[column][row]))) * pixel_slope_hue + random_pattern_hue1.nextValue();
				raw_hue2 = p2_hue_min + (p2_hue_max - p2_hue_min) * max(0, (p2_level - (pixel_interval * (18-pixels_delay[column][row])))) * pixel_slope_hue + random_pattern_hue2.nextValue();
				
				randomness = pixels_pattern[column][row].nextValue();
				hue1_presence = max(0, (p1_level - (pixel_interval * pixels_delay[column][row])));
				hue2_presence = max(0, (p2_level - (pixel_interval * (18-pixels_delay[column][row]))));
	      
				hue1_ratio = hue1_presence / (hue1_presence + hue2_presence);
				hue2_ratio = hue2_presence / (hue1_presence + hue2_presence);

				new_hue = ((hue1_ratio * raw_hue1 + hue1_random) + (hue2_ratio * raw_hue2 + hue1_random));// + randomness;
	      
				raw_brightness1 = max(0, (p1_level - (pixel_interval * pixels_delay[column][row]))) 
	                                * pixel_slope_brightness;
				raw_brightness2 = max(0, (p2_level - (pixel_interval * (18-pixels_delay[column][row]))))
	                                * pixel_slope_brightness;
				//new_brightness = constrain(raw_brightness1 + raw_brightness2, pixel_min_brightness, 1);
				
				new_brightness = constrain(raw_brightness1 + raw_brightness2, pixel_min_brightness, 1);
				//if((hue1_presence + hue2_presence) > 0.8) {
					new_brightness = new_brightness - (pow((1 - abs(p1_level - this_level)), 8));
				//}
	      		
				/*
				hue1_presence = max(0, p1_level - (pixel_interval * pixels_delay[column][row])) * pixel_slope_hue;
				hue2_presence = max(0, p2_level - (pixel_interval * (18 - pixels_delay[column][row]))) * pixel_slope_hue;
				
				hue1_ratio = hue1_presence / (hue1_presence + hue2_presence);
				hue2_ratio = hue2_presence / (hue1_presence + hue2_presence);

				new_hue = (hue1_ratio * raw_hue1) + (hue2_ratio * raw_hue2);// + randomness;
				
				raw_brightness1 = max(0, (p1_level - (pixel_interval * pixels_delay[column][row]))) * pixel_slope_brightness;
				raw_brightness2 = max(0, (p2_level - (pixel_interval * (18-pixels_delay[column][row])))) * pixel_slope_brightness;
				new_brightness = constrain(raw_brightness1 + raw_brightness2, pixel_min_brightness, 1);
				*/
				logo_pixels[column][row].setColor(color(new_hue, 1, new_brightness));
				logo_pixels[column][row].draw();
				
				
			    
			    pixelArray[counter] =  Color.HSBtoRGB(constrain(new_hue, 0, 1), 1, constrain(new_brightness, 0, 1));
			    //println(pixelArray[counter]);
			    counter++;
			}
		}
	} 
	
	public void lowerPlayerLevels() {
		p1_level -= input_decrease;
		p2_level -= input_decrease;
		constrainPlayerLevels();
	}
	
	public void constrainPlayerLevels() {
		p1_level = constrain(p1_level, player_min_level, 1);
		p2_level = constrain(p2_level, player_min_level, 1);
	  
		if(p1_level + p2_level > 1) {
			float ratio = (p1_level + p2_level) / 1;
			p1_level = p1_level / ratio;
			p2_level = p2_level / ratio;
		}
	}
	
	public void setSpeedOfFire() {
		int min_duration = (int) 100;
		int p1_max_duration = (int) ((1-p1_level) * 1000) + 100;
		int p2_max_duration = (int) ((1-p2_level) * 1000) + 100;
		float min_d_value1  = 0.6f * (p1_level / 2);
		float min_d_value2  = 0.6f * (p2_level / 2);
	  
		random_pattern_hue1.setDurationRange(min_duration, p1_max_duration);
		random_pattern_hue1.randomizeCurrentDuration();
		random_pattern_hue1.setMinDValue(min_d_value1);
	  
		random_pattern_hue2.setDurationRange(min_duration, p2_max_duration);
		random_pattern_hue2.randomizeCurrentDuration();
		random_pattern_hue2.setMinDValue(min_d_value2);
	}
	
	public void clientEvent(Client myClient) {
		lumoCom.clientEvent(myClient);
	}
	
	/*
	public Object hsv2rgb(float hue, float sat, float val) {
		float red, grn, blu;
		float i, f, p, q, t;
		hue%=360;
		if(val==0) return {r:0, g:0, v:0};
		sat/=100;
		val/=100;
		hue/=60;
		i = Math.floor(hue);
		f = hue-i;
		p = val*(1-sat);
		q = val*(1-(sat*f));
		t = val*(1-(sat*(1-f)));
		if (i==0) {red=val; grn=t; blu=p;}
		else if (i==1) {red=q; grn=val; blu=p;}
		else if (i==2) {red=p; grn=val; blu=t;}
		else if (i==3) {red=p; grn=q; blu=val;}
		else if (i==4) {red=t; grn=p; blu=val;}
		else if (i==5) {red=val; grn=p; blu=q;}
		red = Math.floor(red*255);
		grn = Math.floor(grn*255);
		blu = Math.floor(blu*255);
		
		var rgb:Object = {r:red, g:grn, b:blu}
		return rgb;
	}
	*/
	
	public int HSLtoRGB(float H, float S, float L) {
		
		float R, G, B;
		float var_1, var_2;
		
		if ( S == 0 )                       //HSL from 0 to 1
		{
		   R = L * 255;                      //RGB results from 0 to 255
		   G = L * 255;
		   B = L * 255;
		}
		else
		{
		   if ( L < 0.5 ) var_2 = L * ( 1 + S );
		   else           var_2 = ( L + S ) - ( S * L );
	
		   var_1 = 2 * L - var_2;
	
		   R = 255 * Hue_2_RGB( var_1, var_2, H + ( 1 / 3 ) );
		   G = 255 * Hue_2_RGB( var_1, var_2, H );
		   B = 255 * Hue_2_RGB( var_1, var_2, H - ( 1 / 3 ) );
		}
		
		return color((int)R, (int)G, (int)B);
	}
	
	public float Hue_2_RGB(float v1, float v2, float vH ) {        //Function Hue_2_RGB
	   if ( vH < 0 ) vH += 1;
	   if ( vH > 1 ) vH -= 1;
	   if ( ( 6 * vH ) < 1 ) return ( v1 + ( v2 - v1 ) * 6 * vH );
	   if ( ( 2 * vH ) < 1 ) return ( v2 );
	   if ( ( 3 * vH ) < 2 ) return ( v1 + ( v2 - v1 ) * ( ( 2 / 3 ) - vH ) * 6 );
	   return ( v1 );
	}
	
	
}
