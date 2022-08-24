package it.unipi.iot.resource_devices;

import org.eclipse.californium.core.CoapClient;

public class ResourceDevice {
	
	protected String hostAddress;
	protected String deviceType;
	protected String resourceType;
	protected String area;
	protected int id;
	protected CoapClient client;
	protected boolean observable;
	
	public ResourceDevice(String hostAddress, String deviceType, String resourceType, boolean observable) {
		
		this.hostAddress = hostAddress;
		this.deviceType = deviceType;
		this.resourceType = resourceType;
		this.observable = observable;
		this.area = null;
		this.client = new CoapClient("coap://[" + this.hostAddress + "]:5683/"+ this.resourceType);
	}
	
	public String getHostAddress() {
		return hostAddress;
	}

	public String getDeviceType() {
		return deviceType;
	}

	public String getResourceType() {
		return resourceType;
	}

	public CoapClient getClient() {
		return client;
	}

	public boolean isObservable() {
		return observable;
	}

	public String getArea() {
		return area;
	}

	public void setArea(String area) {
		this.area = area;
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	
	

}
