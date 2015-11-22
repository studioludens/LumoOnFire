package controller;
import processing.core.*;

public class RandomPattern {

	private PApplet parent;
	
	private float value_range_low = 0;
	private float value_range_high = 1;
  
	private int duration_low =  500;
	private int duration_high = 2000;
	private int current_duration;
  
	private float current_value;
	private float old_value;
	private float target_value;
  
	private float min_d_value;
  
	public RandomPattern(PApplet p) {
		this.parent = p;
	}
  
	public void setValueRange(float value_range_low, float value_range_high) {
		this.value_range_low = value_range_low;
		this.value_range_high = value_range_high;
 	}
  
 	public void setDurationRange(int duration_low, int duration_high) {
 		this.duration_low = duration_low;
 		this.duration_high = duration_high;
 	}
  
	public void randomizeCurrentDuration() {
		current_duration = (int) parent.random(duration_low, duration_high);
	}
  
	public void randomizeTargetAndCurrentValue() {
		current_value = parent.random(value_range_low, value_range_high);
		old_value = current_value;
		target_value = parent.random(value_range_low, value_range_high);
	}
  
	public void randomizeTargetValue() {
		target_value = parent.random(value_range_low, value_range_high);
	}
  
	public int getCurrentDuration() {
		return current_duration;
	}
  
	public void setMinDValue(float min_d_value) {
		this.min_d_value = min_d_value;
	}
  
public float nextValue() {

	//float val = (target_value - old_value);
	// calculate next step for current value
    
	current_value += (target_value - old_value) * ((1/parent.frameRate*1000) / current_duration);
    
	// create new target value if current target value is reached
	if( ( target_value > old_value && current_value > target_value ) ||
	    ( target_value < old_value && current_value < target_value ) ) {
			old_value = target_value;
      
		  	float d_value = 0;
		  	int counter = 0;
		  	
		  	while(d_value < (min_d_value * (value_range_high - value_range_low))) {
		  		randomizeTargetValue();
		  		d_value = parent.abs(target_value - old_value);
		  		counter++;
		  	}
      
		  	current_duration = (int) parent.random(duration_low, duration_high);
		  	
		}
    
		return current_value;
	}
}
