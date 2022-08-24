package it.unipi.iot.resource_devices;

import it.unipi.iot.server.ResourceDeviceHandler;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.json.JSONObject;


public class Sensor extends ResourceDevice{

	protected static final int MAX_OBSERVATIONS = 5;
	protected int []observedValues;
	protected int index = 0;
	
	//Count the number of obs above/under thresholds
	protected int above = 0;
	protected int below = 0;
	
	protected boolean full = false; 
	
	
	
	public Sensor(String hostAddress, String deviceType, String resourceType, boolean observable) {
		super(hostAddress, deviceType, resourceType, observable);
		
		//set vector dimension
		observedValues = new int[MAX_OBSERVATIONS];
		
	}
	
	
	public int getIndex() {
		return index;
	}
	
	public boolean getFull() {
		return full;
	}
	
	public int[] getLastObservations() {
		return observedValues;
	}
	
	//The average is computed only when the observation vector is full
	public float getLastAvgObservation() {
		if(!full) {
			System.out.println("Wait some time, not enough observations made yet");
			return 0;
		}
		int avg = 0;
		for(int i = 0; i < MAX_OBSERVATIONS; i++)
			avg += observedValues[i];
		return avg/MAX_OBSERVATIONS;
	}
	
	public void observeResource() {
		
		if(observable) {
			
			client.observe(
					new CoapHandler() {
						public void onLoad(CoapResponse response) {
							JSONObject responseJSON = new JSONObject(response.getResponseText());
							//read and store the value in the array
							observedValues[index] = responseJSON.getInt(resourceType);
							//update the index
							index = (index+1)%MAX_OBSERVATIONS;
							//check if the array become full
							if(!full && index == 0)
								full = true;
							//Take the ResourceDeviceHandler instance
							final ResourceDeviceHandler handler = ResourceDeviceHandler.getInstance();
							
						}
							public void onError() {
								System.err.println("--- Observation Failed ---"); 
							}
					}, MediaTypeRegistry.APPLICATION_JSON);
			
		}else {
			System.out.println("The resource " + resourceType + " is not observable");
			return;
		}
	}

}
