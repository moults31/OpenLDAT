#ifdef LIGHTSENSOR_SUPPORT

#include "HID-Project.h"

#define SMALL_BUFFER_SIZE 21
#define LARGE_BUFFER_SIZE 32

#define COMMAND_LIGHTSENSOR   0x4C  //monitor/autofire/button mode (L)

#define FEATURE_AUTOFIRE  0b00000001 //automatically generate clicks at ~1hz
#define FEATURE_NOBUFFER  0b00000010 //use unbuffered mode, slower sampling but captures 100% of the signal
#define FEATURE_HIGHSENS1 0b00000100 //increases sensor gain (lsb)
#define FEATURE_MONITOR   0b00001000 //just monitor the light values, ignore clicks
#define FEATURE_NOCLICK   0b00010000 //don't send clicks to PC (will still be visible in the app and the LED)
#define FEATURE_FASTADC   0b00100000 //fast ADC=26us conversion, accurate ADC=104us conversion
#define FEATURE_HIGHSENS2 0b01000000 //increases sensor gain (msb)
//#define FEATURE_7       0b10000000

#define BUTTON_DEBOUNCE_MS 200
volatile byte buttonPressed = 0;
unsigned long lastButtonISR = 0;
void buttonISR() {
  unsigned long t = millis();
  unsigned long tdiff = t - lastButtonISR;
  if (tdiff >= BUTTON_DEBOUNCE_MS) {
    if(PINE&0x40){
      buttonPressed = 1;
      Mouse.press(MOUSE_LEFT);
    }else{
      Mouse.release(MOUSE_LEFT);  
    }
  }
  lastButtonISR = t;
}

void autofireISR() {
  if(PINE&0x40){
    buttonPressed = 1;
    Mouse.press(MOUSE_LEFT);
  }else{
    Mouse.release(MOUSE_LEFT);  
  }
}

void buttonISR_noclick() {
  unsigned long t = millis();
  unsigned long tdiff = t - lastButtonISR;
  if (tdiff >= BUTTON_DEBOUNCE_MS) {
    if(PINE&0x40) buttonPressed = 1;
  }
  lastButtonISR = t;
}

void autofireISR_noclick() {
  if(PINE&0x40) buttonPressed = 1;
}


void lightSensor_resetPins() {
  //disable button power and set pin to high impedance
  digitalWrite(4, LOW);
  pinMode(4, INPUT);
  //disable autofire and set pin to high impedance
  digitalWrite(6, LOW);
  pinMode(6, INPUT);
  //deatach ISR (if any)
  detachInterrupt(digitalPinToInterrupt(7));
  //set pin 7 as input for button/autofire
  pinMode(7, INPUT);
  //set pins 14,15 to high impedance (max sensitivity)
  pinMode(14, INPUT);
  pinMode(15, INPUT);
}

void lightSensor_unbuffered_click() {
  //code for both monitoring and clicking, slower
  OSCILLOSCOPE_DEBUG_INIT();
  byte* buffer = (byte*)malloc(sizeof(int)+sizeof(byte));
  int* v=(int*)buffer;
  byte* b=buffer+sizeof(int);
  #ifdef SERIAL_DEBUG
  Serial.println(F("A0"));
  #endif
  while (!Serial.available()) {
    OSCILLOSCOPE_DEBUG_PULSE();
    *v = analogRead(A0);
    *b = buttonPressed;
    buttonPressed = 0;
    #ifdef SERIAL_DEBUG
    Serial.print(*v);
    Serial.print(F(","));
    Serial.println(*b);
    #else
    Serial.write(buffer, sizeof(int)+sizeof(byte));
    #endif
    OSCILLOSCOPE_DEBUG_PULSE();
  }
  free(buffer);
  OSCILLOSCOPE_DEBUG_END();
}

void lightSensor_unbuffered_monitor() {
  //code for just monitoring, faster
  OSCILLOSCOPE_DEBUG_INIT();
  int v;
  #ifdef SERIAL_DEBUG
  Serial.println(F("A0"));
  #endif
  while (!Serial.available()) {
    OSCILLOSCOPE_DEBUG_PULSE();
    v = analogRead(A0);
    #ifdef SERIAL_DEBUG
    Serial.println(v);
    #else
    Serial.write((byte*)&v, sizeof(int));
    #endif
    OSCILLOSCOPE_DEBUG_PULSE();
  }
  OSCILLOSCOPE_DEBUG_END();
}

void lightSensor_buffered_monitor() {
  //code for just monitoring, faster
  OSCILLOSCOPE_DEBUG_INIT();
  uint8_t counter = 0;
  int* buffer = (int*)malloc(LARGE_BUFFER_SIZE * sizeof(int));
  #ifdef SERIAL_DEBUG
  Serial.println(F("A0"));
  #endif
  OSCILLOSCOPE_DEBUG_PULSE();
  while (!Serial.available()) {
    buffer[counter] = analogRead(A0);
    if (++counter == LARGE_BUFFER_SIZE) {
      OSCILLOSCOPE_DEBUG_PULSE();
      #ifdef SERIAL_DEBUG
      for(counter=0;counter<LARGE_BUFFER_SIZE;counter++){
        Serial.println(buffer[counter]);
      }
      #else
      Serial.write((byte*)buffer, LARGE_BUFFER_SIZE * sizeof(int));
      #endif
      counter = 0;
      OSCILLOSCOPE_DEBUG_PULSE();
    }
  }
  //release buffer
  free(buffer);
  OSCILLOSCOPE_DEBUG_END();
}

void lightSensor_buffered_click() {
  //code for both monitoring and clicking, slower
  OSCILLOSCOPE_DEBUG_INIT();
  uint8_t counter = 0;
  byte* buffer = (byte*)malloc(SMALL_BUFFER_SIZE * (sizeof(int) + sizeof(byte)));
  int* sbuffer = (int*)buffer;
  byte* bbuffer = buffer + SMALL_BUFFER_SIZE * sizeof(int);
  #ifdef SERIAL_DEBUG
  Serial.println(F("Light,Click"));
  #endif
  OSCILLOSCOPE_DEBUG_PULSE();
  while (!Serial.available()) {
    sbuffer[counter] = analogRead(A0);
    bbuffer[counter] = buttonPressed;
    buttonPressed = 0;
    if (++counter == SMALL_BUFFER_SIZE) {
      OSCILLOSCOPE_DEBUG_PULSE();
      #ifdef SERIAL_DEBUG
      for(counter=0;counter<SMALL_BUFFER_SIZE;counter++){
        Serial.print(sbuffer[counter]);
        Serial.print(F(","));
        Serial.println(bbuffer[counter]);
      }
      #else
      Serial.write(buffer, SMALL_BUFFER_SIZE * (sizeof(int) + sizeof(byte)));
      #endif
      counter = 0;
      OSCILLOSCOPE_DEBUG_PULSE();
    }
  }
  //release buffer
  free(buffer);
  OSCILLOSCOPE_DEBUG_END();
}

void lightSensor(byte flags) {
  //set up automatic or manual clicking if requested
  if (!(flags & FEATURE_MONITOR)) {
    if (flags & FEATURE_AUTOFIRE) {
      //configure pin 6 to generate ~1hz pulses for autofire
      pinMode(6, OUTPUT);
      noInterrupts();
      TCCR4C |= _BV(COM4D1);
      TCCR4C &= ~(_BV(COM4D0));
      OCR4D = 64;
      TCCR4B &= ~(_BV(CS43) | _BV(CS42) | _BV(CS41) | _BV(CS40));
      TCCR4B |= _BV(CS43) | _BV(CS42) | _BV(CS41) | _BV(CS40);
      TCCR4D &= ~(_BV(WGM41) | _BV(WGM40));
      TC4H = 3;
      OCR4C = 255;
      TCNT4H = 0;
      TCNT4 = 0;
      interrupts();
      attachInterrupt(digitalPinToInterrupt(7), (flags&FEATURE_NOCLICK)?autofireISR_noclick:autofireISR, CHANGE);
    } else {
      //enable button power
      pinMode(4, OUTPUT);
      digitalWrite(4, HIGH);
      attachInterrupt(digitalPinToInterrupt(7), (flags&FEATURE_NOCLICK)?buttonISR_noclick:buttonISR, CHANGE);
    }
  }
  //configure ADC
  if (flags & FEATURE_FASTADC) ADCSRA = (ADCSRA & 0xF80) | 0x05; else ADCSRA = (ADCSRA & 0xF80) | 0x07;
  ADCSRB |= (1<<ADHSM);
  //select sensor gain
  /* HIGHSENS2 | HIGHSENS1 | GAIN   | RESISTANCE between sensor- and gnd
   *    0           0        Low       14.3k (330k, 47k, 22k)
   *    0           1        Mid       20.6k (330k, 22k)
   *    1           0        High      41.1k (330k, 47k)
   *    1           1        Max       330k  (330k)
   */
  //47k resistor between sensor- and pin 15
  if(flags&FEATURE_HIGHSENS1){
    pinMode(15, INPUT);
  }else{
    pinMode(15,OUTPUT);
    digitalWrite(15,LOW);  
  }
  //22k resistor between sensor- and pin 14
  if(flags&FEATURE_HIGHSENS2){
    pinMode(14, INPUT);
  }else{
    pinMode(14,OUTPUT);
    digitalWrite(14,LOW);  
  }
  //discharge static (probably not necessary)
  for(int i=0;i<100;i++) analogRead(A0);
  //begin monitoring
  if (flags & FEATURE_NOBUFFER) {
    if (flags & FEATURE_MONITOR) {
      lightSensor_unbuffered_monitor();
    } else {
      lightSensor_unbuffered_click();
    }
  } else {
    if (flags & FEATURE_MONITOR) {
      lightSensor_buffered_monitor();
    } else {
      lightSensor_buffered_click();
    }
  }
  lightSensor_resetPins();
}

void LIGHTSENSOR_COMMANDS(byte cmd, byte flags){
  switch(cmd){
    case COMMAND_LIGHTSENSOR:{
      lightSensor(flags);
    } break;
    default: break;
  }
}

void LIGHTSENSOR_INITIALIZE(){
  Mouse.begin();
  lightSensor_resetPins();
}

#else
void LIGHTSENSOR_COMMANDS(byte cmd, byte flags){}
void LIGHTSENSOR_INITIALIZE(){}
#endif
