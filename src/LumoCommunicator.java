import processing.core.*;
import processing.net.*;


public class LumoCommunicator {// extends PApplet {
	
	/**
	 * variables
	 */
	
	// Client
	public Client lumoClient;
	
	public boolean isConnected;
	public boolean isLoggedIn = false;
	public boolean serverIsSet = false;
	public boolean serverIsReady = false;
	public int frameCount = 0;
	
	private PApplet parent;

    public String lumoResponse = "";
	
	public static void main(String[] args) {
		PApplet.main(new String[] { "LumoCommunicator" });
		
	}
	
	public void setup(PApplet p) {
		    //size(600,600);
		    //background(20);
		    //frameRate(10);
		    
			this.parent = p;
		    
		    // try to make a connection to the LumoServer
		    try {
		    	isConnected = true;
		    	lumoClient = new Client(parent, "localhost", 7070);
		    	parent.println("Server Connection acquired");
		    	
		    } catch (Exception e){
		    	isConnected = false;
		    	parent.println("Server Connection failed");
		    }
	}
	
	public void draw(int[] pixels){
		
		frameCount++;
		
		parent.println("draw!");
		
		String data = "";
		
		data += "task.animation.set(leds, 50, ";
		
        if(isLoggedIn ){//&& serverIsReady){
        	
        	for(int i = 0; i < pixels.length; i++) { 
        		String value = Integer.toHexString(pixels[i]);
        		value = value.substring(2, value.length());
        		data += "#" + value + ",";        		
        	}
        	data = data.substring(0, data.length() - 1);
        	data += ")\n";
        	
        	parent.println(data);
        	lumoClient.write(data);
        	
        	
        	serverIsReady = false;
        }
        
	}
	
	public void clientEvent(Client myClient) {
		
		String response = myClient.readString();	
		
		if(response != null)
			parent.println("response: " + response);
		
		if(response != null && isConnected) {
			
			if(parent.match(response, "100") != null && !isLoggedIn) {
				
				parent.println("trying to log in...");
	            myClient.write("login(test,test)\n");
			}
			
			if(parent.match(response, "200") != null && !isLoggedIn) {
				
				isLoggedIn = true;
				myClient.write("task.set(active,true)\n");
				parent.println("we are logged in!");
			}
			
			if(parent.match(response, "200") != null && isLoggedIn) {
				
				serverIsReady = true;
				parent.println("server is now ready for tasks!");
			}
		}		
	}
}
