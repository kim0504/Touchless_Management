#include <Wire.h>
#include <SparkFun_APDS9960.h>
#include <SoftwareSerial.h>

#define BT_RXD 8
#define BT_TXD 7
#define buttonPin 2
#define TRIG 13 //TRIG 핀 설정 (초음파 보내는 핀)
#define ECHO 12 //ECHO 핀 설정 (초음파 받는 핀)

// Pins
#define APDS9960_INT    2 // Needs to be an interrupt pin

// Constants

// Global Variables
SparkFun_APDS9960 apds = SparkFun_APDS9960();
int isr_flag = 0;

SoftwareSerial bluetooth(BT_RXD, BT_TXD);        // 블루투스 설정 BTSerial(Tx, Rx)
int ultra_flag = 0;

void setup() {

  pinMode(buttonPin, OUTPUT);
  pinMode(TRIG, OUTPUT);
  pinMode(ECHO, INPUT);
  bluetooth.begin(9600);// 블루투스 통신 시작

  // Set interrupt pin as input
  pinMode(APDS9960_INT, INPUT);

  // Initialize Serial port
  
  Serial.println();
  Serial.println(F("--------------------------------"));
  Serial.println(F("SparkFun APDS-9960 - GestureTest"));
  Serial.println(F("--------------------------------"));
  
  // Initialize interrupt service routine
  attachInterrupt(0, interruptRoutine, FALLING);

  // Initialize APDS-9960 (configure I2C and initial values)
  if ( apds.init() ) {
    Serial.println(F("APDS-9960 initialization complete"));
  } else {
    Serial.println(F("Something went wrong during APDS-9960 init!"));
  }
  
  // Start running the APDS-9960 gesture sensor engine
  if ( apds.enableGestureSensor(true) ) {
    Serial.println(F("Gesture sensor is now running"));
  } else {
    Serial.println(F("Something went wrong during gesture sensor init!"));
  }
  Serial.begin(9600);
}

void loop() {

  if (Serial.available()) {           // 시리얼모니터에서 보낸 내용은 블루투스로 전송
    bluetooth.write(Serial.read());
  }
  
  if( isr_flag == 1 ) {
    detachInterrupt(0);
    handleGesture();
    isr_flag = 0;
    attachInterrupt(0, interruptRoutine, FALLING);
  }
}

void interruptRoutine() {
  isr_flag = 1;
}

void handleGesture() {
    if ( apds.isGestureAvailable() ) {
    switch ( apds.readGesture() ) {
      case DIR_UP:
        Serial.println("UP");
        bluetooth.write("U");
        break;
      case DIR_DOWN:
        Serial.println("DOWN");
        bluetooth.write("D");
        break;
      case DIR_LEFT:
        Serial.println("LEFT");
        bluetooth.write("L");
        break;
      case DIR_RIGHT:
        Serial.println("RIGHT");
        bluetooth.write("R");
        break;
      case DIR_NEAR:
        Serial.println("NEAR");
        bluetooth.write("o");
        break;
      case DIR_FAR:
        Serial.println("FAR");
        bluetooth.write("o");
        break;
      default:
        Serial.println("NONE");
    }
  }
}
