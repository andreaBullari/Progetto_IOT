package it.unipi.iot;

import it.unipi.iot.server.ResourceDeviceHandler;
import it.unipi.iot.server.Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class MyClient {
	
	protected static Server server;
	protected static BufferedReader reader;
	protected static ResourceDeviceHandler handler;
	
	public static void main(String[] args) {
		
		System.out.println("---- CLIENT STARTED ----");
		
		server = new Server();
		
		//new thread to let server running 
		new Thread() {
			public void run() {
				server.start();
			}
		}.start();
		
		// Take ResourceDeviceHandler instance
		handler = ResourceDeviceHandler.getInstance();
		
		/* User interface */
		
		reader = new BufferedReader(new InputStreamReader(System.in));
		

		System.out.println("Type \"!help\" to know the commands\n");
				
		while(true) {
			System.out.println("Type a command\n");
			
			try {
				
				String command = reader.readLine();
				
				switch(command) {
					
					case "!help":
						showCommands();
						break;
						
					case "!getSensors":
						getSensors();
						break;

					case "!getAddressResources":
						getAddressResources();
						break;

					case "!getAvgBinFullness":
						getAvgBinFullness();
						break;
						
					case "!removeDevicesAddress":
						removeDevicesAddress();
						break;
						
					case "!stop":
						stop();
						break;
						
					default:
						System.out.println("Command not defined\n");
						break;
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
		
		
		
	}


	private static void showCommands() {
		
		System.out.println("");
		System.out.println("	--	This is the list of accepted command	--	\n");
		System.out.println("!help 			-->	Get the list of the available commands\n");
		
		
		System.out.println("	--	GET COMMANDS	--	");
		
		System.out.println("!getSensors 		-->	Get the list of registered sensors");
		System.out.println("!getAddressResources	-->	Get the list of registered IDs with a given address");
		System.out.println("!getAvgBinFullness		-->	Get the Avg bin fullness of the last 10 measurements for all the sensors");

		System.out.println("");
		System.out.println("	--	POST COMMANDS	--	");
		System.out.println("!removeDevicesAddress	-->	Remove the devices with given address");
		
		System.out.println("");
		System.out.println("!stop			-->	Stop the application");

		System.out.println("");
		

	}
	
/*
 * 
 * 		GET METHODS
 * 
 */
	
	//Get the list of the sensors (area, id, address, resType)
	private static void getSensors() {
		
		if(handler.getBinSensors().isEmpty() ) {
			System.out.println("No Sensor Registered\n");
			return;
		}
	
		System.out.println(" 	--	Sensors List	--	");
		handler.binSensorList();
		System.out.println("");
	}
	

	//For all the bin sensors, get the avg of the last 10 measurements
	private static void getAvgBinFullness() {
		
		System.out.println("	--	Last Average Bin fullness Detected	--	");
		handler.getSensorsBinFullness();
		System.out.println("");
	}
	
	//IT SHOWS ALL THE RESOURCES WITH A GIVEN ADDRESS
	private static void getAddressResources() {
		
		System.out.println("Available Devices: ");
		handler.getAddressesList();;
		
		System.out.println("\nType the address of the devices");
	
		try {
			//GET THE ADDRESS
			String address = reader.readLine();
			
			if(!handler.getAddressIDs().containsKey(address)) {
				System.out.println("Error! This is not a device address.\n ");
				return;
			}
			
			handler.getAddressIDs(address);
			
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}

	//Remove devices with given address
	
	//REMOVE AND UNREGISTER DEVICES WITH GIVEN ADDRESS
	private static void removeDevicesAddress() {
		
		System.out.println("Available Devices: ");
		handler.devicesList();
		
		System.out.println("\nType the address of the devices");
	
		try {
			String address = reader.readLine();
			
			if(!handler.getAddressIDs().containsKey(address)) {
				System.out.println("Error! This is not a device address.\n ");
				return;
			}
			
			handler.removeDevicesAddress(address);
			
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	

	//STOP THE APPLICATION
	private static void stop() {
		
		handler.removeAllDevices();
		
		System.out.println("Stopping the application...\n");
		server.stop();
		server.destroy();
		System.exit(0);
		
	}


}
