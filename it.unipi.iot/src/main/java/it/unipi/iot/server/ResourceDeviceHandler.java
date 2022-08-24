package it.unipi.iot.server;

import it.unipi.iot.resource_devices.ResourceDevice;
import it.unipi.iot.resource_devices.Sensor;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;


public class ResourceDeviceHandler {
	
	//A single instance of this class is needed to maintain shared and consistent info of the devices
	private static ResourceDeviceHandler singleInstance = null;
	
	//ID counter to be assigned to devices
	private static int deviceID = 0;
	
	/* DATA STRUCTURES TO HOLD DEVICES */
	protected HashMap<Integer, Sensor> binSensors = new HashMap<Integer, Sensor>();	//ID - bin
	protected HashMap<Integer, ResourceDevice> idDeviceMap = new HashMap<Integer, ResourceDevice>();	//id - Device
	protected HashMap<String, ArrayList<Integer>> addressIDs = new HashMap<String, ArrayList<Integer>>();	//address - List of IDs
	
	
	
	/* CONSTATS DEFINITION */
	static final int MIN_VARIATION = 1;
	static final int MAX_VARIATION = 5;
	static final int DEFAULT_AREA_MAX_BIN = 100;
	static final int DEFAULT_AREA_MIN_BIN = 0;
	
	private ResourceDeviceHandler() {
        ArrayList<ResourceDevice> devices = new ArrayList<ResourceDevice>();

    }
	
	//Get the instance of ResourceDeviceHandler. Only one instance atomically shared between everyone
	public static ResourceDeviceHandler getInstance(){
		if (singleInstance == null) {
            singleInstance = new ResourceDeviceHandler();
		}
        return singleInstance;
    }
	
	public int getDeviceID() {
		return deviceID;
	}

	public void setDeviceID(int deviceID) {
		ResourceDeviceHandler.deviceID = deviceID;
	}


	public void addBinSens(Integer id, Sensor sensor) {
		binSensors.put(id, sensor);
	}
	

	public HashMap<Integer, Sensor> getBinSensors() {
		return binSensors;
	}
	

	public ResourceDevice getDeviceFromId(Integer id) {
		return idDeviceMap.get(id);
	}

	public HashMap<Integer,ResourceDevice> getIdDeviceMap(){
		return this.idDeviceMap;
	}	
	
	public void setIdDeviceMap(Integer id, ResourceDevice rd){
		this.idDeviceMap.put(id, rd);
	}
	
	public HashMap<String, ArrayList<Integer>> getAddressIDs() {
		return addressIDs;
	}

/*
 * 
 * 		RETURNS DEVICES LISTS
 * 
 */
	
	// PRINT THE LIST OF ALL THE SPRINKLER ACTUATORS

	
	// PRINT THE LIST OF ALL THE LIGHT ACTUATORS


	//	PRINT THE LIST OF ALL THE BIN SENSORS
	public void binSensorList() {
		for(Integer id: binSensors.keySet()) {
			Sensor s = binSensors.get(id);
			System.out.println("ID: " + s.getId() + ", addr: " + s.getHostAddress() + ", type: " + s.getDeviceType() + ", Resource: " + s.getResourceType());
		}
	}
	
	//	PRINT THE LIST OF ALL THE DEVICES
	public void devicesList() {
		this.binSensorList();
	}

	// RETURNS IF A DEVICE WITH THAT ADDRESS IS PRESENT
	public boolean getDevice(Integer id) {

		if(binSensors.containsKey(id))
			return true;

		return false;
	}

	//PRINT ADDRESSES
	public void getAddressesList() {
		
		System.out.print("[");
		for(String addr: addressIDs.keySet()) {
			System.out.print(" " + addr);
		}
		System.out.println(" ]\n");
	}
	
	//PRINT THE LIST OF IDs FOR A GIVEN ADDRESS
	public void getAddressIDs(String address){
		
		if(!addressIDs.containsKey(address))
			System.out.println("[" + address + "]: []");
		else {
			System.out.println("[" + address + "]:");
			for(Integer id: addressIDs.get(address)) {
				System.out.println("[ ID: " + id + ", Resource: " + idDeviceMap.get(id).getResourceType() + " ]");
			}
			System.out.println("");
		}
		
	}
	
	//GET THE BIN AVG OF ALL THE SENSORS
	public void getSensorsBinFullness() {
		for(Integer id: binSensors.keySet()) {
			Sensor s = binSensors.get(id);
			System.out.println("Sensor Area: " + s.getArea() + ", Address: " + s.getHostAddress() + 
					", Average Bin Fullness: " + s.getLastAvgObservation());
		}
	}


	
	//Unregister Device
	public boolean unRegisterDevice(String address) {
		
		CoapClient c = new CoapClient("coap://[" + address + "]:5683/unregister");
		
		JSONObject json = new JSONObject();
		
		
		json.put("unregister", "true");
			
		
		//send post request
		CoapResponse response = c.post(json.toString(), MediaTypeRegistry.APPLICATION_JSON);
		
		//Check the return code: Success 2.xx
		if(!response.getCode().toString().startsWith("2")) {
			System.out.println("Error code: " + response.getCode().toString());
			return false;
		}
		
		return true;
		
	}

	
	/*
	 * 
	 * REMOVE DEVICES
	 * 
	 */
	
	//REMOVE ALL DEVICES WITH GIVEN ADDRESS
	public boolean removeDevicesAddress(String address) {
		
		//I take all the IDs of the device with the given address
		ArrayList<Integer> ids = new ArrayList<>();
		for(Integer el: this.getAddressIDs().get(address))
			ids.add(el);

		//For each device, I remove it
		for(Integer id: ids) {
			if(!removeDevice(id))
				return false;
		}
		
		ids.clear();
		System.out.println("Removed devices with address: " + address);
		
		//Notify the device that has been unregistered from the application
		if(unRegisterDevice(address))
			System.out.println("Unregister devices with address: " + address + "\n");
		return true;
			
	}
	
	//REMOVE DEVICE WITH A GIVE ID
	public boolean removeDevice(Integer id) {
		
		ResourceDevice rd = idDeviceMap.get(id);
		String address = rd.getHostAddress();
		
		switch(rd.getResourceType()) {
		case "bin":
			binSensors.remove(id);		//remove from hbinSens map
			idDeviceMap.remove(id);
			break;
		default:
			System.out.println("Error in removing device " + id + "\n");
			return false;
		}
		
		//Remove the ID for the list 
		int index = this.getAddressIDs().get(address).indexOf(id);	//take the index position
		this.getAddressIDs().get(address).remove(index);

		
		System.out.println("Device " + id + " removed\n");
		
		//remove the address from the map
		if(this.getAddressIDs().get(address).isEmpty()) {
			System.out.println("No more devices with the address: " + address + ". Remove it\n");
			this.getAddressIDs().remove(address);
		}
		
		return true;
		
	}

	//REMOVE ALL DEVICES
	public boolean removeAllDevices() {
		
		System.out.println("Removing all the Devices...");
		
		//I take all the devices addresses in the system
		ArrayList<String> addresses = new ArrayList<>();
		for(String address: addressIDs.keySet())
			addresses.add(address);
		
		//For each address, I call the remove function
		for(String address: addresses)
			if(!removeDevicesAddress(address))
				return false;
		
		addresses.clear();
		System.out.println("All devices have been unregistered and removed\n");
		return true;
	}
	
}
