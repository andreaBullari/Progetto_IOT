#include "contiki.h"
#include "coap-engine.h"
#include <string.h>
#include "os/dev/leds.h"

/* Log configuration */
#include "sys/log.h"
#define LOG_MODULE "Sprinkler"
#define LOG_LEVEL LOG_LEVEL_APP

//DEFINE EVENT UNREGISTER
process_event_t UNREGISTER;
//take the process where the event must be posted
extern struct process device_process;

static unsigned int post_accept = APPLICATION_JSON;

static void res_post_put_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);

/*----------------------------------------------------------------------------------------------*/

RESOURCE(res_unregister,
         "title=\"Unregister\";rt=\"unregister\";if=\"unregister\"",
	 NULL,
         res_post_put_handler,
         res_post_put_handler,
         NULL);
        
/*----------------------------------------------------------------------------------------------*/

/* Sprinkler Activator: it changes its status, and the leds accordingly RED: OFF, GREEN: ON */

static void res_post_put_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset){

	if(request == NULL){

		LOG_INFO("[UNREGISTER]: Empty request\n");
		return;
	}



	coap_get_header_accept(request, &post_accept);
	//Handler only JSON format request
	if(post_accept == APPLICATION_JSON){
		
		size_t pay_len = 0;
		char *var = NULL;
		bool good_req = false;

		const uint8_t **message;
		message = malloc(request->payload_len);

		if(message == NULL){
			LOG_INFO("[UNREGISTER]: Empty payload\n");
			return;
		}


		pay_len = coap_get_payload(request, message);
		LOG_INFO("Message received: %s\n", (char *)*message);

	
		if(pay_len > 0){
		
			//Splitting the payload
			char *split;
			

			//Take the variable
			split = strtok((char*)*message, ":");	//	{"unregister"
			const char* start = split + 2;
			const char* end = split + strlen(split)-1;
			size_t size = end - start;

			if(size == 0) {
				LOG_INFO("Size equal to 0.\n");
				return;
			} else {
				var = malloc(size);
				strncpy(var, start, size);
				var[size] = '\0';
			}

			//I don't care the value

			//UNREGISTER DEVICE
			if(strcmp(var, "unregister") == 0){

				//POST THE EVENT
				process_post(&device_process, UNREGISTER, NULL);
				good_req = true;
				

			}
			
		}
		//send response
		if(good_req)
			coap_set_status_code(response, CHANGED_2_04);

		if(!good_req)
			coap_set_status_code(response, BAD_REQUEST_4_00);		
		
	}else{
		coap_set_status_code(response, NOT_ACCEPTABLE_4_06);
	   	const char *msg = "Supported content-types:application/json";
	    	coap_set_payload(response, msg, strlen(msg));
	}


}
