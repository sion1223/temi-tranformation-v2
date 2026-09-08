#define R3_HOST_TEST
#include "../firmware/smart_lock_box/smart_lock_box.ino"
#include <cstdio>
int checks=0,failed=0;
#define CHECK(x) do{++checks;if(!(x)){++failed;std::printf("FAIL line %d: %s\n",__LINE__,#x);}}while(0)
void cmd(const std::string &s,uint8_t channel=0){char b[192];strncpy(b,s.c_str(),191);b[191]=0;command(b,Serial,channel);}
void closedPins(){
  fakePins[RET]=HIGH;fakePins[DOOR]=LOW;fakePins[STOW]=HIGH;fakePins[EXT]=LOW;fakePins[TRAY]=LOW;fakePins[PANEL]=LOW;
  sr.begin();sd.begin();ss.begin();se.begin();st.begin();sp.begin();
}
void ready(){
  closedPins();fakeNow=20000;ctl.boot(0);readPower();safety(fakeNow);ctl.heartbeat(true,19000);ctl.heartbeat(true,20000);
  uint8_t k[32];for(int i=0;i<32;i++)k[i]=i;auth=Auth();auth.setup(k,4);Serial.output.clear();
}
std::string signature(){uint8_t tag[32];char out[65];auth.requestTag(tag);encodeHex(tag,32,out);return out;}
int main(){
  ready();cmd("UNLOCK 1234");CHECK(!ctl.coil);CHECK(Serial.output.find("ERR COMMAND")!=std::string::npos);
  cmd("CHAL UNLOCK");std::string tag=signature();cmd("AUTH "+tag);CHECK(ctl.coil);CHECK(PORTD&_BV(PD7));CHECK(OCR1A==31249);
  uint32_t oldStart=ctl.started;cmd("AUTH "+tag);CHECK(ctl.started==oldStart);
  TIMER1_COMPA_vect();CHECK(!(PORTD&_BV(PD7)));CHECK(timerCut);safety(fakeNow);CHECK(!ctl.coil&&(ctl.faults&TIMER));
  ready();cmd("CHAL UNLOCK");tag=signature();cmd("AUTH "+tag,1);CHECK(!ctl.coil);
  ready();cmd("CHAL UNLOCK");tag=signature();fakeNow+=2000;cmd("AUTH "+tag);CHECK(!ctl.coil);
  ready();Serial.input=std::string(100,'x')+"CHAL UNLOCK\n";Line b;
  while(Serial.available())readCommands(Serial,b,0);CHECK(!auth.pending);CHECK(!b.discard&&!b.length);
  Serial.input="CHAL STATUS\n";while(Serial.available())readCommands(Serial,b,0);CHECK(auth.pending&&auth.verb==STATUS);
  ready();Serial.input="CHAL\0UNLOCK\n"; // explicitly provide embedded control byte below
  Serial.input=std::string("CHAL",4)+char(0)+" UNLOCK\n";while(Serial.available())readCommands(Serial,b,0);CHECK(!auth.pending);
  ready();cmd("CHAL UNLOCK");tag=signature();cmd("AUTH "+tag);fakePins[PANEL]=HIGH;fakeNow+=1;safety(fakeNow);
  CHECK(!ctl.coil&&(ctl.faults&TAMPER)); // immediate raw opening, before 30 ms debounce
  ready();cmd("CHAL UNLOCK");tag=signature();cmd("AUTH "+tag);fakeAdc=400;fakeNow+=30;safety(fakeNow);
  CHECK(!ctl.coil&&(ctl.faults&POWER));fakeAdc=599;
  ready();int32_t raw=123;fakePins[HX_DATA]=HIGH;CHECK(!readHx(raw)&&raw==123);
  fakePins[HX_DATA]=LOW;weighing.adcFault=false;CHECK(!readHx(raw)&&weighing.adcFault&&weighing.count==0);
  // Configuration is not accepted over BLE or while the protected panel/door is closed.
  ready();std::string key(64,'1');cmd("PROVISION "+key);uint8_t check[32];CHECK(!readKey(check));
  fakePins[DOOR]=HIGH;fakePins[PANEL]=HIGH;sd.begin();sp.begin();cmd("PROVISION "+key,1);CHECK(!readKey(check));
  cmd("PROVISION "+key);CHECK(readKey(check));CHECK(!auth.ready);
  uint32_t e=0;CHECK(advanceEpoch(EEPROM,e)&&e==2);
  // Persist a real two-point calibration and reload it without capturing a new zero.
  CHECK(saveCalibration(1200000,1400));weighing.configure(0,0);loadCalibration();CHECK(weighing.calibrated&&weighing.zero==1200000);
  for(int k=0;k<20;k++)weighing.sample(1207000,fakeNow);CHECK(weighing.milligrams()==5000);
  EEPROM.bytes[128]^=1;weighing.calibrated=false;loadCalibration();CHECK(!weighing.calibrated);
  std::printf("RESULT checks=%d failed=%d\n",checks,failed);return failed?1:0;
}
