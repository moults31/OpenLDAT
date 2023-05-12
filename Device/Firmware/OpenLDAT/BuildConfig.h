#define OSCILLOSCOPE_DEBUG                    //when defined, pulses will be generated on pin 10 after each buffer (in buffered mode) or after each read (in unbuffered mode). Can be read with a scope to get timing metrics, does not slow down execution
//#define SERIAL_DEBUG                          //when defined, commands output as text instead of binary, allowing easier debugging but slowing down execution significantly
#define LIGHTSENSOR_SUPPORT                   //when defined, enables light sensor support
#define FIRMWARE_VERSION F("20210612")        //firmware version as ASCII string
#define PROTOTYPE                             //when defined, marks this device as a prototype
#define MIN_APP_VERSION 1                     //minimum versioncode of the PC driver/app required to use this device
