//Also uses the Tone library. This


#include <IRremote.h>
#include <IRremoteInt.h>

//Define the hardware pins
#define L_LED 13                    //Left LED connected to pin 13
#define R_LED 6                     //Right LED conntected to pin 6
#define BUZZ_PIN 9                  //Buzzer is connected to pin 9
#define RECV_PIN 11                 //IR receiver is connected to pin 11

//Setup-debugging toggle, needed to fetch IR codes from remote and send over serial.
#define SERIAL_DEBUG false

//Lists Received IR codes for button presses & corresponding states. Numbers are entered by providing input with the remote used.
enum ButtonState {
  LEFT = 3772819033,                //Left button pressed state (Also contains the IR code)
  LEFT2 = 1972149634,
  LEFT_RELAX,                       //Provides a relaxing state in which we assume the left button pressed
  RIGHT = 3772794553,               //Right button pressed state (Also contains the IR code)
  RIGHT2 = 1400905448,
  RIGHT_RELAX,                      //Provides a relaxing state in which we assume the right button pressed
  NONE                              //No button pressed state
};

//Define global objects
IRrecv irrecv(RECV_PIN);            //Init the receiver object
decode_results irBuffer;            //Contains received IR data
ButtonState buttonState = NONE;     //State variable for button pressed


//Set and initialize hardware
void setup() {
  //Set the correct pin modes
  pinMode(R_LED, OUTPUT);
  pinMode(BUZZ_PIN, OUTPUT);
  pinMode(L_LED, OUTPUT);

  //Start serial connection to processing
  Serial.begin(9600);
  Serial.setTimeout(100);

  //Start the IR receiver
  irrecv.enableIRIn();
}


//Main program loop logic.
void loop() {
  processIR();
  //processSerial();
  delay(150);
}


void processIR() {
  if (irrecv.decode(&irBuffer)) {
    //A new code was received. Update or maintain button state
    switch (irBuffer.value) {
      case LEFT:
      case LEFT2:
        //Left press received.
        if (buttonState == LEFT_RELAX) {
          buttonState = LEFT;
        }
        if (buttonState != LEFT) {
          //Only send buttonState if it wasn't Left. Ensures only state updates are transmitted.
          buttonState = LEFT;            //Update ButtonState
          digitalWrite(R_LED, LOW);      //Set right LED on
          digitalWrite(L_LED, HIGH);     //Set left LED off
          Serial.print("L");             //Transmit buttonState update
          if (!SERIAL_DEBUG) {
            Serial.println();
          }
        }
        break;
      case RIGHT:
      case RIGHT2:
        //Right press received.
        if (buttonState == RIGHT_RELAX) {
          buttonState = RIGHT;
        }
        if (buttonState != RIGHT) {
          //Only send buttonState if it wasn't Right. Ensures only state updates are transmitted.
          buttonState = RIGHT;            //Update buttonState
          digitalWrite(R_LED, HIGH);      //Set right LED on
          digitalWrite(L_LED, LOW);       //Set left LED off

          Serial.print("R");              //Transmit buttonState update
          if (!SERIAL_DEBUG) {
            Serial.println();
          }
        }
        break;
      default:
        //Unknown code received.
        //Usually this happens when a unknown button is pressed.
        //There seems to be a different behaviour when a button is pressed continuously, then this behaves as a relaxing press.
        if (SERIAL_DEBUG) {
          Serial.print("U");
        }
        break;
    }

    //Serial debug output switch
    if (SERIAL_DEBUG) {
      //Send type and code received
      Serial.print(": ");
      Serial.println(irBuffer.value);
    }
    irrecv.resume(); // Receive the next value
  } else {
    //No new code was received. No button was pressed. Set LEDs and buttonState accordingly
    switch (buttonState) {
      case LEFT:
        buttonState = LEFT_RELAX;
        break;
      case RIGHT:
        buttonState = RIGHT_RELAX;
        break;
      case LEFT_RELAX:
      case RIGHT_RELAX:
        digitalWrite(R_LED, LOW);         //Turn right LED off
        digitalWrite(L_LED, LOW);         //Turn left LED off
        buttonState = NONE;
        Serial.println("NONE");
        break;

    }
  }
}


void processSerial() {
  String string = Serial.readStringUntil('\n');
  //if (string == "BUZZ\r\n") {
  //Create 5ms buzz sound at 1.2 kHz
  tone(BUZZ_PIN, 1200);
  delay(150);
  noTone(BUZZ_PIN);
  //}
}

