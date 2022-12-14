#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "contiki.h"
#include "coap-engine.h"
#include "coap-blocking-api.h"
#include "sys/etimer.h"
#include "dev/leds.h"
#include "os/dev/serial-line.h"

/* Log configuration */
#include "sys/log.h"
#define LOG_MODULE "App"
/*	Declare server IP	*/
#define SERVER_EP "coap://[fd00::1]:5683" 
#define REQ_INTERVAL 5

/*	Declare external resources to be activated	*/
extern coap_resource_t bin;
extern coap_resource_t res_unregister;

//UNREGISTER event generated by the res_unregister
extern process_event_t UNREGISTER;

//timer
static struct etimer e_timer;
//The URL for the registration
char *registration_url = "/registration";
//registration status	
static bool registration_status = false;
//device type
//static int device_type = 0;	


/* Declare and auto-start the process */
PROCESS(device_process, "Device");
AUTOSTART_PROCESSES(&device_process);

//Handler response from the server
void client_chunk_handler(coap_message_t *response) {
	if(response == NULL) { 
		printf("Request timed out\n"); 
		return;
	}
	const uint8_t *chunk;
	coap_get_payload(response, &chunk);
	printf("Received Response: %s from server\n", (char *)chunk);

	//If the response is "Accept" I registered the device
	if(strcmp( (char *)chunk, "Accept") == 0){
		registration_status = true;
		printf("Registration accepted.\n");
	} else {
		registration_status = false;
		printf("Registration not accepted.\n");
	}

}

//Process
PROCESS_THREAD(device_process, ev, data){

	static coap_endpoint_t server_ep;
  	static coap_message_t request[1]; 

	PROCESS_BEGIN();

	coap_activate_resource(&bin, "bin");
	printf("To register to the cloud application, type \"register\"\n"); 
	
	while(1){
		PROCESS_WAIT_EVENT_UNTIL(ev == serial_line_event_message);
		if(strcmp(data, "register") == 0){
				
			// Prepare the message
			coap_endpoint_parse(SERVER_EP, strlen(SERVER_EP), &server_ep);
			coap_init_message(request, COAP_TYPE_CON, COAP_POST, 0);
			coap_set_header_uri_path(request, registration_url);

			while(!registration_status){
				printf("Sending registration request to the server\n");
				//Send the registration request to the server
				COAP_BLOCKING_REQUEST(&server_ep, request, client_chunk_handler);
			}

			printf("Registration status: %s\n", registration_status ? "true" : "false");		
			etimer_set(&e_timer, CLOCK_SECOND * REQ_INTERVAL);
			break;	

		}	
	}

	while(1){

		//wait for an event
		PROCESS_WAIT_EVENT_UNTIL(ev == PROCESS_EVENT_TIMER || ev == UNREGISTER );//|| ev == serial_line_event_message);

		//need to check if it's registered.
		if(ev == PROCESS_EVENT_TIMER && registration_status){
			bin.trigger();
			etimer_reset(&e_timer);
		}

		//If UNREGISTER, I unregister the device and stop the timer -> No more observation needed
		else if(ev == UNREGISTER){

			registration_status = false;
			etimer_stop(&e_timer);
			printf("Registration status set to: %s\n", registration_status ? "true" : "false");
			printf("\nType \"register\" for registering to the cloud application\n");
		}
	}
  	PROCESS_END();
}
