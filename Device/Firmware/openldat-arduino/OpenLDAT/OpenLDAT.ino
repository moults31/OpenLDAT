#include "BuildConfig.h"
#include "OscilloscopeDebug.h"
#include "LightSensor.h"
#include "EEPROM.h"

#define COMMAND_IDLE          0x49  //stop current activity and wait for next command (I)
#define COMMAND_ID            0x44  //identify device version and capabilities (D)

void setup() {
  Serial.begin(2000000); //pro micro uses USB CDC so speed is not relevant
  LIGHTSENSOR_INITIALIZE();
  //turn off LED on prototype
  #ifdef PROTOTYPE
  pinMode(1,OUTPUT);
  digitalWrite(1,LOW);
  #endif
}

void printSerialNumber(){
  int addr=0x0330;
  byte b=EEPROM.read(addr++);
  if(b==0x37){
    do{
      b=EEPROM.read(addr++);
      if(b==0x00||b==0xFF||b=='\r'||b=='\n') break;
      Serial.write(b);
    }while(addr<=0x0340);
    Serial.println(F(""));
  }else Serial.println(F("DIY"));
}

void identify(byte flags) {
  Serial.println(F("OpenLDAT Model 1"));
  Serial.print(F("FW: "));
  Serial.println(FIRMWARE_VERSION);
  Serial.print(F("LightSensor: "));
  #ifdef LIGHTSENSOR_SUPPORT
  Serial.println(F("1"));
  Serial.print(F("LBuffer: "));
  Serial.println(LARGE_BUFFER_SIZE);
  Serial.print(F("SBuffer: "));
  Serial.println(SMALL_BUFFER_SIZE);
  #else
  Serial.println(F("0"));
  #endif
  Serial.print(F("OscilloscopeDebug: "));
  #ifdef OSCILLOSCOPE_DEBUG
  Serial.println(F("1"));
  #else
  Serial.println(F("0"));
  #endif
  Serial.print(F("SerialDebug: "));
  #ifdef SERIAL_DEBUG
  Serial.println(F("1"));
  #else
  Serial.println(F("0"));
  #endif
  Serial.print(F("Prototype: "));
  #ifdef PROTOTYPE
  Serial.println(F("1"));
  #else
  Serial.println(F("0"));
  #endif
  Serial.print(F("MinAppVer: "));
  Serial.println(MIN_APP_VERSION);
  Serial.print(F("SerialNo: "));
  printSerialNumber();
  //end info list
  Serial.println(F(""));
}

#ifdef SERIAL_DEBUG
byte hexCharsToByte(byte c1, byte c2){
  byte ret=0;
  if(c1>='0'&&c1<='9') ret|=(c1-'0')<<4;
  if(c1>='a'&&c1<='f') ret|=(c1-'a'+10)<<4;
  if(c1>='A'&&c1<='F') ret|=(c1-'A'+10)<<4;
  if(c2>='0'&&c2<='9') ret|=(c2-'0');
  if(c2>='a'&&c2<='f') ret|=(c2-'a'+10);
  if(c2>='A'&&c2<='F') ret|=(c2-'A'+10);
  return ret;
}
#endif

void loop() {
  //wait for command byte
  while (!Serial.available()) delay(10);
  //read command and flags
  #ifdef SERIAL_DEBUG
    //all commands in serial debug mode start with lowercase x, if that's not what we're reading, act like we're not in serial debug mode
    byte c=Serial.read(), cmd, flags;
    if(c==0x78){
      byte b1=Serial.read(), b2=Serial.read(), b3=Serial.read(), b4=Serial.read();
      cmd=hexCharsToByte(b1,b2);
      flags=hexCharsToByte(b3,b4);
    }else{
      cmd=c;
      flags=Serial.read();
    }
    while (Serial.available()) Serial.read(); //discard other bytes (such as \n)
  #else
    byte cmd = Serial.read();
    byte flags = Serial.read();
  #endif
  //run command
  switch (cmd) {
    case COMMAND_IDLE: break;
    case COMMAND_ID: {
        identify(flags);
      } break;
    default: break;
  }
  LIGHTSENSOR_COMMANDS(cmd,flags);
}
