Here the C code running on the PIC 16F690 of the power consumption probe.
Hi-Tech C compiler + mplab

```
#define _OMNI_CODE_
#define _XTAL_FREQ 8000000
#define __PICCPRO__
#define _PLIB

// nRF commands
#define W_TX_PAYLOAD    0b10100000
#define W_CONFIG        0b00100000
#define R_CONFIG        0b00000000
#define W_TX_ADDR       0b00110000
#define W_SETUP_RETR    0b00100100
#define W_RF_SETUP      0b00100110
#define W_SATUS         0b00100111
#define R_SATUS         0b00000111
#define FLUSH_TX        0b11100001
#define W_FEATURE       0b00111101
#define W_DYNPD         0b00111100

// nFR24L01 control pins
#define CE  RC3
#define CSN RC4

// other pins
#define LED_SENDING RA5
#define LED_COMPARATOR RA4
#define LED_COUNT RC5

#include <htc.h>           /* Global Header File */
#include <pic.h>

__CONFIG(0x0FD4);

//proto
void spiRW(char command, char * data, int dataLength);
void sendData ();

//global var
unsigned char payload[32];        //Payload for nRF24L01+

bit bTimeMS = 0;
bit bFlip = 0;
bit bDataSent = 0;
unsigned long timeMS = 0;     // 32bits
unsigned long timeMStoSend = 0;     // 32bits
unsigned long timeMSflipUP = 0;     // 32bits
unsigned long timeMSflipDOWN = 0;     // 32bits
unsigned long timeLed = 0;     // 32bits
unsigned long watt = 0;         //32bits
const unsigned long WHR10_MS = 0x2255100;  // 10WHr (one revolution) in ms
                                           // 10*60*60*1000

static void interrupt isr(void) {    // Here be interrupt function
    if (TMR1IF == 1){
        TMR1H = 0xFC;   // timer1 value ~1000 before overflow
        TMR1L = 0x24;   // 8Mhz/4 /2 /1000 = 1ms
        TMR1IF = 0;
        timeMS++;
        bTimeMS = 1;
    }
}

int main(void) {
    IRCF0 = 1;              // 8Mhz
    IRCF1 = 1;              // ''
    IRCF2 = 1;              // ''
    C1ON = 0;               // comparator 1 off
    C2ON = 1;               // comparator 2 on
    C2CH0 = 0; C2CH0 = 1;   // Comparator 2 input C12IN1-
    C2POL = 1;              // We looking for tension drop on C2IN1+

    ANSEL  = 0b00000011;          // analog input
    ANSELH = 0b00000000;

    TRISA = 0b00000000;      // PORTA output
    TRISB = 0b00010000;      // PORTB  B4 input (spi)
    TRISC = 0b00000011;      // PORTC output, C1:0 comparator 2

    CKP = 0;                 // SPI clk phase
    CKE = 1;                 // SPI clk default low
    SSPEN = 1;               // SPI enable
    SSPM0 = 0;               // SPI clock Fosc/64
    SSPM1 = 1;
    SSPM2 = 0;
    SSPM3 = 0;

    PEIE = 1;                // periferal  interrupt
    GIE = 1;                 // General interrupt

    T1CKPS0 = 1;             // Timer1 prescale :2 to have 1Mhz count
    TMR1IE = 1;              // Timer1 on
    TMR1ON = 1;

    CSN = 1;
    CE = 0;

    __delay_ms(50);
    __delay_ms(50);

    // Main loop
    while (1) {
        if (bTimeMS) {
            //Led comparator status
            if (C2OUT) LED_COMPARATOR = 1;
            else LED_COMPARATOR = 0;

            //Led sending data
            if (bDataSent) LED_SENDING = (timeMS/50)%2;
            else LED_SENDING = 0;

            //Led counting status
            if (timeMS >= timeLed) {
                timeLed = timeMS+(timeMS/256);
            }

            LED_COUNT = timeMS >= timeLed - 16;

            // Time hysteresis 
            if (C2OUT){
                timeMSflipUP++;
                timeMSflipDOWN = 0;
            } else {
                timeMSflipUP = 0;
                timeMSflipDOWN++;
            }

            // 5000ms limits max power to 7.2KW
            if (C2OUT && timeMSflipUP > 300 && timeMS > 5000 && !bDataSent) {
                timeMStoSend = timeMS;
                watt = WHR10_MS / timeMS;
                timeMS = 0;
                timeLed = 0;
                sendData();
                bDataSent = 1;
            }
            if (!C2OUT && timeMSflipDOWN > 300 && timeMS > 3000) {
                bDataSent = 0;
            }

            bTimeMS = 0;
        }
    }
}

void sendData(){
    // nFR24L01+ re-init
    payload[0] = 0b00001110;
    spiRW(W_CONFIG, payload, 1);  // Power up
    __delay_ms(5);

    payload[0] = 0b00000100;
    spiRW(W_FEATURE, payload, 1);  // write FEATURE enable dynamic payload

    payload[0] = 0b00111111;
    spiRW(W_DYNPD, payload, 1);  // enable dynamic payload for all pipes

    payload[0] = 0xC3;
    payload[1] = 0xC2;
    payload[2] = 0xC2;
    payload[3] = 0xC2;
    payload[4] = 0xC2;
    spiRW(W_TX_ADDR, payload, 5);  // TX address

    payload[0] = 0b00100110;
    spiRW(W_RF_SETUP, payload, 1);  // 250Kbps

    payload[0] = 0b00011111;
    spiRW(W_SETUP_RETR, payload, 1);  // retransmit up to 15 times every 500us

    payload[0] = 0b01111110;
    spiRW(W_SATUS, payload, 1);  // clear MAX_TR flag

    spiRW(FLUSH_TX, payload, 0);       // Flush TX fifo

    // Send over the air
    payload[0] = 'W';
    payload[1] = '2';
    payload[2] = watt;
    payload[3] = watt>>8;
    payload[4] = 'R';
    payload[5] = '4';
    payload[6] = timeMStoSend;
    payload[7] = timeMStoSend>>8;
    payload[8] = timeMStoSend>>16;
    payload[9] = timeMStoSend>>24;

    spiRW(W_TX_PAYLOAD, payload, 10);

    // sending signal
    CE = 1;
    __delay_us(50);
    CE = 0;
    __delay_ms(1);
    payload[0] = 0b00001100;
    spiRW(W_CONFIG, payload, 1);  // Power down
    __delay_ms(1);
    
}
        


void spiRW(char command, char * data, int dataLength){
    CSN = 0;
    SSPBUF = command;
    while (BF == 0){;}
    TXREG = SSPBUF;
    while (TRMT == 0){;}

    for(unsigned int i=0; i<dataLength; i++){
        SSPBUF = data[i];
        while (BF == 0){;}
        data[i] = SSPBUF;
        TXREG = data[i];
        while (TRMT == 0){;}
    }
    CSN = 1;
}
```