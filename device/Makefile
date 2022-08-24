CONTIKI_PROJECT = device
all: $(CONTIKI_PROJECT)
CONTIKI = ../../..

CFLAGS += -DPROJECT_CONF_H=\"project-conf.h\"
MODULES_REL += ./resources

# Include CoAP implementation
include $(CONTIKI)/Makefile.dir-variables
MODULES += $(CONTIKI_NG_APP_LAYER_DIR)/coap

# Include optional target-specific module
include $(CONTIKI)/Makefile.identify-target
MODULES_REL += $(TARGET)

include $(CONTIKI)/Makefile.include
