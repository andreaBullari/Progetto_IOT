package it.unipi.iot.server;


import it.unipi.iot.resource_devices.Sensor;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.server.resources.CoapExchange;

import java.util.ArrayList;

public class Registration extends CoapResource{

	public Registration(String name) {
		super(name);
		System.out.println("Registration Resource created\n");
	}
	
	public void handlePOST(CoapExchange exchange) {
		
		System.out.println("[SERVER]: Handling Registration Request");
		
		
		//	Get node address	 
		String source_address = exchange.getSourceAddress().getHostAddress();
		System.out.println("SRC ADDR: " + source_address);
		
		// Create CoapClient Object	and get node information
		
		CoapClient client = new CoapClient("coap://[" + source_address + "]:5683/.well-known/core");
		CoapResponse response = client.get(MediaTypeRegistry.APPLICATION_JSON);

		String responseText = response.getResponseText();
		
		//Check the return code: Success 2.xx
		if(!response.getCode().toString().startsWith("2")) {
			System.out.println("Error code: " + response.getCode().toString());
			return;
		}
		
		
		/*
		 * Client response: </.well-known/core>;ct=40,
		 * </bin>;title="Bin fullness Sensor";rt="bin";if="sensor";obs,
		 * </unregister>;"title=\"Unregister\";rt=\"unregister\";if=\"unregister\""
		 */

		boolean registered = true;
		
		
		//Take each resource
		String []fragment = responseText.split(",");
		for(int i = 1; i < fragment.length; i++) {
			
			//for each, register in the application
			if(!deviceRegistration(source_address, fragment[i])) {
				System.out.println("Error in registering a resource\n");
				registered = false;
			}
			System.out.println("");

			
		}
		
		//Prepare and set response to the node
		Response accept = new Response(ResponseCode.CREATED);
		String payload = null;
		if(registered) {
			System.out.println("[SERVER]: Device Registered\n");
			
	        payload = "Accept";
		}else {
			
			// Registration of one or mode resources failed. 
			//REMOVE ALL THE ALREADY REGISTERED RESOURCE DEVICES FOR THE NODE
			ResourceDeviceHandler handler = ResourceDeviceHandler.getInstance();
			handler.removeDevicesAddress(source_address);
			payload = "Reject";
		}
	    accept.setPayload(payload);
	    exchange.respond(accept);
			
		
		
	}
	
	//For each resource, register the device
	private boolean deviceRegistration(String sourceAddress, String resource) {
		
		boolean registered = false;
		
		String []splitSemicolon = resource.split(";");
		
		//Take the resource name
		String resType = splitSemicolon[2].substring(splitSemicolon[2].indexOf("\"") + 1 , splitSemicolon[2].length()-1);
		
		//Take the device type (sensor/actuator)
		String deviceType = splitSemicolon[3].substring(splitSemicolon[3].indexOf("\"") + 1 , splitSemicolon[3].length()-1);
		
		//Check if it is observable
		String obs = splitSemicolon[splitSemicolon.length-1];
		boolean observable = (obs.compareTo("obs") == 0);
		
		ResourceDeviceHandler handler = ResourceDeviceHandler.getInstance();

		//	Switch sensor/actuator/unregister. I instantiate a Sensor/Actuator. 
		//	Add it to the data structure base on the res
		
		//This is the unregister resource. Not necessary to be stored in the system
		if(deviceType.compareTo("unregister") == 0) {
			System.out.println("Resource: " + resType + ", Resource observable: " + observable);
			return true;
		}
		//Sensor device
		else if(deviceType.compareTo("sensor") == 0) {
			
			
			final Sensor sensor = new Sensor(sourceAddress, deviceType, resType, observable);
			
			//I take the last ID, I set mine, I increment the shared resource
			int myId = handler.getDeviceID()+1;
			sensor.setId(myId);
			handler.setDeviceID(myId);
			

			
			//	add the sensor in the ResourceDeviceHandler map
			if(resType.compareTo("bin") == 0)
				handler.addBinSens(myId, sensor);

			//Add in the map: id - sensor
			handler.setIdDeviceMap(myId, sensor);
			//Put device in default area

			//If the resource is observable, start observing.
			if(observable) {
				new Thread() {
					public void run() {
						sensor.observeResource();
					}
				}.start();
				
			}
			System.out.println("Resource: " + resType + ", Resource observable: " + observable);
			registered = true;
			
			//Add the deviceID in the list related to its address  
			if(handler.getAddressIDs().containsKey(sourceAddress))
				handler.getAddressIDs().get(sourceAddress).add(myId);
			else {
				ArrayList<Integer> resIDs = new ArrayList<>();
				resIDs.add(myId);
				handler.getAddressIDs().put(sourceAddress, resIDs);
			}
			
		}

		System.out.print("Address: " + sourceAddress + " IDs:");
		for(Integer id: handler.getAddressIDs().get(sourceAddress))
			System.out.print(" " + id);
		
		
		
		return registered;
		
		
		
		
		
	}
	
	
	

}
