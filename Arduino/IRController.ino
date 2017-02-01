/* IR remote controller receiver app
   Features:
   - Receives Left and Right buttons
   - Controls Left and Right buttons
   - Different Beeps determined by serial messages


   Note: Uses both the IR and the IRRemote library. They clash as they both use the same timer.
   This is fixable by changing the lines of code in IRRemoteInt.h from:
        // #define IR_USE_TIMER1      // tx = pin 9
        #define IR_USE_TIMER2         // tx = pin 3
   to:
        #define IR_USE_TIMER1         // tx = pin 9
        //#define IR_USE_TIMER2       // tx = pin 3
*/



#include <IRremote.h>
#include <IRremoteInt.h>

//Define the hardware pins
#define L_LED 13                    //Left LED connected to pin 13
#define R_LED 6                     //Right LED conntected to pin 6
#define BUZZ_PIN 9                  //Buzzer is connected to pin 9
#define RECV_PIN 11                 //IR receiver is connected to pin 11

//Setup-debugging toggle, needed to fetch IR codes from remote and send over serial.
#define SERIAL_DEBUG false

/*
   Codes for used remotes
   Michiel:
    Left    3216
    Right   1168

   Rick:
    Left    3772819033
    left2   1972149634
    right   3772794553
    right2  1400905448

*/

//Lists Received IR codes for button presses & corresponding states. Numbers are entered by providing input with the remote used.
enum ButtonState {
  LEFT = 3216,                      //Left button pressed state (Also contains the IR code)
  LEFT2,                            //Some smart tv remotes have multiple codes for keeping the button pressed.
  LEFT_RELAX,                       //Provides a relaxing state in which we assume the left button pressed
  RIGHT = 1168,                     //Right button pressed state (Also contains the IR code)
  RIGHT2,                           //Some smart tv remotes have multiple codes for keeping the button pressed.
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
  Serial.setTimeout(100);           //To not interrupt the programm for too long, a fast timeout for Serial.read is required.

  //Start the IR receiver
  irrecv.enableIRIn();
}


//Main program loop logic.
void loop() {
  //Handle the IR stuff and the LEDs
  processIR();

  //Handle the Buzzing. Makes sure that minimal timout is 50ms.
  long buzzmark = millis();
  processSerial();
  long buzztime = millis() - buzzmark;
  if (buzztime < 50) {
    delay(50 - buzztime);
  }
}


void processIR() {
  if (irrecv.decode(&irBuffer)) {
    //A new code was received. Update or maintain button state
    switch (irBuffer.value) {
      case LEFT:
      case LEFT2:
        //Left press received.
        if (buttonState == LEFT_RELAX) {
          //We were in a relaxing state. Button was confirmed to be pressed. Reset state to LEFT.
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
          //We were in a relaxing state. Button was confirmed to be pressed. Reset state to RIGHT.
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
    irrecv.resume();                       // Receive the next value
  } else {
    //No new code was received. No button was pressed. Set LEDs and buttonState accordingly
    switch (buttonState) {
      //Check if current state is LEFT or RIGHT. Then set a Relaxing state before setting and transmitting the NONE state.
      case LEFT:
        buttonState = LEFT_RELAX;
        break;
      case RIGHT:
        buttonState = RIGHT_RELAX;
        break;
      case LEFT_RELAX:
      case RIGHT_RELAX:
        digitalWrite(R_LED, LOW);          //Turn right LED off
        digitalWrite(L_LED, LOW);          //Turn left LED off
        buttonState = NONE;                //Set buttonState to None
        Serial.println("N");               //Transmit buttonState update
        break;

    }
  }
}

//Method for receiving the Serial communication for the beeps.
void processSerial() {
  //Check if there is anything in the Serial buffer
  if (Serial.available() > 0) {
    int soundMessage = (int) Serial.read();
    //Switch on the message received for the different sounds
    switch (soundMessage) {
      case 0:
        buzz(1000);
        break;
      case 1:
        buzz(2000);
        break;
      case 2:
        buzz(3000);
        break;
      default:
        break;
    }
  }
}

//Method for buzzing for 50 ms at a given frequency.
void buzz(int freq) {
  tone(BUZZ_PIN, freq);
  delay(50);
  noTone(BUZZ_PIN);
}


