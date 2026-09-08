/* R3-A4 bench firmware. Nano ATmega328P / 5 V / 16 MHz.
 * Not flashed automatically. Read README and qualify on physical hardware.
 * Fixed 12 V input; battery cells must NOT be connected directly.
 * HX711 A/128 at 10 SPS. No automatic boot tare. HMAC protects commands/state.
 */
#include <Arduino.h>
#include <SoftwareSerial.h>
#include <EEPROM.h>
#include <avr/wdt.h>
#include <avr/interrupt.h>
#include "control.h"
#include "auth.h"

using namespace r3;
constexpr uint8_t RET=2,DOOR=3,STOW=4,EXT=5,LED=6,COIL=7,HX_DATA=8,HX_CLOCK=9,TRAY=12,PANEL=A3,POWER_ADC=A6;
constexpr uint16_t ADC_REFERENCE_MV=5000; // Measure the real 5 V rail and adjust before qualification.
SoftwareSerial ble(10,11);
Controller ctl;Scale weighing;Auth auth;
uint32_t bootAt=0,lastPower=0,lastOffSample=0;
uint16_t powerMv=0;
bool hwCoil=false,zeroPending=false;
int32_t calZero=0;
volatile bool timerCut=false;
struct Debounce {
  uint8_t pin;bool raw=false,active=false;uint32_t changed=0;
  explicit Debounce(uint8_t p):pin(p){}
  void begin(){pinMode(pin,INPUT_PULLUP);raw=active=digitalRead(pin)==LOW;changed=millis();}
  void update(uint32_t now){bool v=digitalRead(pin)==LOW;if(v!=raw){raw=v;changed=now;}if(elapsed(now,changed)>=30)active=raw;}
};
Debounce sr(RET),sd(DOOR),ss(STOW),se(EXT),st(TRAY),sp(PANEL);
struct Line {char text[96];uint8_t length=0;bool discard=false;};
Line usbLine,bleLine;

// Disable a previously running watchdog early. An appropriate Nano bootloader is
// still required; verify watchdog recovery on the actual board before service.
#ifndef R3_HOST_TEST
void earlyWdt() __attribute__((naked,section(".init3")));
void earlyWdt(){MCUSR=0;wdt_disable();}
#endif
ISR(TIMER1_COMPA_vect){PORTD&=~_BV(PD7);TIMSK1&=~_BV(OCIE1A);timerCut=true;}
void applyCoil(){
  if(ctl.coil==hwCoil)return;
  uint8_t saved=SREG;cli();
  if(ctl.coil){
    timerCut=false;TCNT1=0;OCR1A=31249;TIFR1=_BV(OCF1A);
    TCCR1A=0;TCCR1B=_BV(WGM12)|_BV(CS12)|_BV(CS10);TIMSK1|=_BV(OCIE1A);PORTD|=_BV(PD7);
  }else {PORTD&=~_BV(PD7);TIMSK1&=~_BV(OCIE1A);TCCR1B=0;}
  hwCoil=ctl.coil;SREG=saved;
}
bool environmentOK(uint32_t now){
  return ctl.stationary(now)&&!ctl.coil&&elapsed(now,ctl.lastOff)>=3000&&elapsed(now,bootAt)>=15000&&ctl.in.tray&&ctl.in.panel&&ctl.in.power;
}
void readPower(){
  analogRead(POWER_ADC);
  powerMv=uint32_t(analogRead(POWER_ADC))*ADC_REFERENCE_MV*133UL/(33UL*1023UL);
}
void safety(uint32_t now){
  sr.update(now);sd.update(now);ss.update(now);se.update(now);st.update(now);sp.update(now);
  if(elapsed(now,lastPower)>=25){lastPower=now;readPower();}
  Inputs in;in.ret=sr.active;in.closed=sd.active;in.stowed=ss.active;in.ext=se.active;
  in.tray=st.active;in.panel=sp.active;in.power=powerMv>=10800&&powerMv<=12200;
  // Raw open-loop detection stops an active coil before software debounce completes.
  if(ctl.coil&&(!st.raw||!sp.raw))ctl.fail(TAMPER,now);
  if(timerCut){timerCut=false;ctl.fail(TIMER,now);}
  ctl.tick(in,now);applyCoil();
}
bool readHx(int32_t &out){
  if(digitalRead(HX_DATA)!=LOW)return false; // Never wait for a missing ADC.
  uint32_t data=0;
  // Keep each high pulse short even during SoftwareSerial interrupts (HX711 <60 us).
  for(uint8_t i=0;i<25;i++){
    uint8_t saved=SREG;cli();digitalWrite(HX_CLOCK,HIGH);delayMicroseconds(1);
    if(i<24)data=(data<<1)|(digitalRead(HX_DATA)?1:0);
    digitalWrite(HX_CLOCK,LOW);SREG=saved;delayMicroseconds(1);
  }
  // DOUT must return high for the next conversion; a grounded data wire is not a zero-gram sample.
  if(digitalRead(HX_DATA)==LOW){weighing.adcFault=true;weighing.clearWindow();return false;}
  out=(data&0x800000UL)?int32_t(data|0xff000000UL):int32_t(data);return true;
}
bool readKey(uint8_t *key){
  if(EEPROM.read(34)!=0xa6)return false;
  for(uint8_t i=0;i<32;i++)key[i]=EEPROM.read(i);
  return crc16(key,32)==(uint16_t(EEPROM.read(32))|(uint16_t(EEPROM.read(33))<<8));
}
void loadCalibration(){
  uint8_t b[10];for(uint8_t i=0;i<10;i++)b[i]=EEPROM.read(128+i);
  if(EEPROM.read(138)!=0xc3||crc16(b,8)!=(uint16_t(b[8])|(uint16_t(b[9])<<8)))return;
  int32_t z;float g;memcpy(&z,b,4);memcpy(&g,b+4,4);weighing.configure(z,g);
}
bool saveCalibration(int32_t z,float gain){
  uint8_t b[10];memcpy(b,&z,4);memcpy(b+4,&gain,4);uint16_t c=crc16(b,8);b[8]=c;b[9]=c>>8;
  EEPROM.update(138,0);for(uint8_t i=0;i<10;i++)EEPROM.update(128+i,b[i]);EEPROM.update(138,0xc3);
  for(uint8_t i=0;i<10;i++)if(EEPROM.read(128+i)!=b[i])return false;
  weighing.configure(z,gain);return EEPROM.read(138)==0xc3;
}
void signedState(Stream &out,uint8_t result){
  uint32_t now=millis();safety(now);
  bool valid=weighing.valid(now,environmentOK(now));
  bool ready=ctl.contained()&&valid;
  // Canonical ASCII: never authenticate a raw STATUS string received elsewhere.
  char payload[128];
  snprintf(payload,sizeof(payload),"S %u %u %u %u %u %u %u %u %u %u %ld %u %u %u %u",
      ctl.in.closed,ctl.in.ret,ctl.in.ext,ctl.in.stowed,ctl.in.tray,ctl.in.panel,ctl.faults,ctl.coil,
      ready,ctl.contained(),long(weighing.milligrams()),valid,weighing.occupied,powerMv,result);
  uint8_t tag[32];char hex[65];auth.responseTag(payload,tag);encodeHex(tag,32,hex);
  out.print(payload);out.print(' ');out.println(hex);
}
bool serviceAvailable(uint8_t channel){return channel==0&&!sd.active&&!sp.active&&!ctl.coil;}
void command(char *line,Stream &out,uint8_t channel){
  uint32_t now=millis();safety(now);
  char *cmd=strtok(line," ");if(!cmd)return;
  char *arg=strtok(NULL," ");char *extra=strtok(NULL," ");
  if(extra){out.println(F("ERR FORMAT"));return;}
  if(!strcmp(cmd,"PROVISION")){
    uint8_t key[32],old[32];
    if(!serviceAvailable(channel)||!decodeHex(arg,key,32)){out.println(F("ERR LOCAL_SERVICE"));return;}
    uint8_t bits=0;for(uint8_t i=0;i<32;i++)bits|=key[i];
    if(!bits||(readKey(old)&&secure_compare(key,old,32))){out.println(F("ERR NEW_RANDOM_KEY_REQUIRED"));return;}
    auth.ready=false;EEPROM.update(34,0);EEPROM.update(74,0);EEPROM.update(90,0);
    for(uint8_t i=0;i<32;i++)EEPROM.update(i,key[i]);uint16_t c=crc16(key,32);EEPROM.update(32,c);EEPROM.update(33,c>>8);
    if(!writeEpoch(EEPROM,64,1)){out.println(F("ERR EEPROM"));return;}
    EEPROM.update(34,0xa6);out.println(F("OK PROVISIONED_REBOOT"));return;
  }
  if(!strcmp(cmd,"CAL_ZERO")||!strcmp(cmd,"CAL_500")){
    if(arg||!serviceAvailable(channel)||!ctl.stationary(now)||!weighing.fresh(now)||weighing.spread()>800){out.println(F("ERR CAL_CONDITIONS"));return;}
    if(!strcmp(cmd,"CAL_ZERO")){calZero=weighing.mean();zeroPending=true;out.println(F("OK ZERO_CAPTURED_ADD_500G"));return;}
    float gain=(int64_t(weighing.mean())-calZero)/500.0f;
    if(!zeroPending||fabsf(gain)<100||fabsf(gain)>10000){out.println(F("ERR CAL_SPAN"));return;}
    if(!saveCalibration(calZero,gain)){out.println(F("ERR EEPROM"));return;}
    zeroPending=false;out.println(F("OK CAL_SAVED"));return;
  }
  if(!strcmp(cmd,"CHAL")){
    Verb v=arg?parseVerb(arg):NONE;
    if(!auth.challenge(v,channel,now)){out.println(F("ERR CHALLENGE"));return;}
    char hex[33];encodeHex(auth.nonce,16,hex);out.print(F("CHALLENGE "));out.print(verbName(v));out.print(' ');out.println(hex);return;
  }
  if(!strcmp(cmd,"AUTH")){
    if(!auth.verify(arg,channel,now)){out.println(F("ERR AUTH"));return;}
    now=millis();safety(now);bool ok=true;
    switch(auth.verb){
      case STOPPED:ctl.heartbeat(true,now);break;
      case MOVING:ctl.heartbeat(false,now);weighing.clearWindow();break;
      case UNLOCK:ok=ctl.release(false,now);break;
      case CLOSE:ok=ctl.release(true,now);break;
      case RESET:ok=ctl.reset(now);break;
      case STATUS:break;
      default:ok=false;
    }
    applyCoil();signedState(out,ok?1:0);return;
  }
  if(!strcmp(cmd,"INFO")&&!arg){out.println(F("R3-A4; CHAL verb / AUTH hmac; physical USB service only"));return;}
  out.println(F("ERR COMMAND"));
}
void readCommands(Stream &port,Line &buf,uint8_t channel){
  uint8_t budget=16;
  while(port.available()&&budget--){
    char c=char(port.read());
    if(c=='\n'){
      if(!buf.discard){buf.text[buf.length]=0;command(buf.text,port,channel);}
      buf.length=0;buf.discard=false;
    }else if(c!='\r'&&!buf.discard){
      if(c<32||c>126||buf.length>=sizeof(buf.text)-1){buf.discard=true;buf.length=0;}
      else buf.text[buf.length++]=c;
    }
  }
}
void setup(){
  digitalWrite(COIL,LOW);pinMode(COIL,OUTPUT);pinMode(LED,OUTPUT);pinMode(LED_BUILTIN,OUTPUT);
  digitalWrite(HX_CLOCK,LOW);pinMode(HX_CLOCK,OUTPUT);pinMode(HX_DATA,INPUT_PULLUP);
  sr.begin();sd.begin();ss.begin();se.begin();st.begin();sp.begin();
  Serial.begin(115200);ble.begin(9600);bootAt=millis();ctl.boot(bootAt);readPower();
  uint8_t key[32];uint32_t epoch=0;
  if(readKey(key)&&advanceEpoch(EEPROM,epoch))auth.setup(key,epoch);
  clean(key,32);loadCalibration();wdt_enable(WDTO_1S);
  Serial.println(F("R3-A4 READY; key and calibration must be provisioned; no boot tare"));
}
void loop(){
  wdt_reset();uint32_t now=millis();safety(now);
  int32_t raw;
  if(readHx(raw)){
    // Service calibration can collect samples with panel open, but transport validity remains false.
    if(ctl.stationary(now)&&!ctl.coil&&elapsed(now,ctl.lastOff)>=3000)weighing.sample(raw,now);
    else weighing.clearWindow();
  }
  if(ctl.lastOff!=lastOffSample){lastOffSample=ctl.lastOff;weighing.clearWindow();}
  weighing.updateOccupancy(now,weighing.valid(now,environmentOK(now)));
  readCommands(Serial,usbLine,0);safety(millis());readCommands(ble,bleLine,1);safety(millis());
  digitalWrite(LED,ctl.contained()?HIGH:LOW);
  digitalWrite(LED_BUILTIN,ctl.faults?((millis()/200)%2):ctl.coil);
}
