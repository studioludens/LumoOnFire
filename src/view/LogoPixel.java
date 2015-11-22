package view;

import processing.core.*;

/**
 *  Class stands for a single pixel of the TU/e logo
 *  It has a width, height, location (x,y) and a color
 */

public class LogoPixel {
  
	private PApplet parent;
	
	private int x;
	private int y;
	  
	private int p_width;
	private int p_height;
	  
	private int p_color; 
	  
	private PImage pixel_img;
  
	public LogoPixel(PApplet p, float x, float y, float p_width, float p_height, int p_color) {
    
		this.parent = p;
		  
		this.x = (int)x;
	    this.y = (int)y;
	    
	    this.p_width  = (int)p_width;
	    this.p_height = (int)p_height;
	    
	    this.p_color  = p_color;
	    
	    createNewPixelImg();
	}
  
	public void draw() {
		drawPixel();
	}
  
	public void setColor(int p_color) {
		this.p_color = p_color;
		createNewPixelImg();
	}
  
	/**
	 *  create image that represents the "pixel
	 *  images created based on height, width and color;
	 */  
	private void createNewPixelImg() {  
		if(p_width > 0 && p_height > 0)
			this.pixel_img = parent.createImage(p_width, p_height, parent.RGB);
			      
			for(int i = 0; i < p_width; i++) {
				for(int j = 0; j < p_height; j++) {
					pixel_img.pixels[i+j*p_width] = p_color;
			}
		}        
	}
	 
	private void drawPixel() {
		parent.image(pixel_img, x, y);
	}    
}
