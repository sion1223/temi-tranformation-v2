#include <cstdio>
#include <cstring>
#include <stdexcept>
#include "../firmware/smart_lock_box/control.h"
#include "../firmware/smart_lock_box/auth.h"
using namespace r3;
int checks=0,failed=0;
#define CHECK(x) do{++checks;if(!(x)){++failed;std::printf("FAIL line %d: %s\n",__LINE__,#x);}}while(0)
Inputs safe(){Inputs i;i.closed=i.ext=i.tray=i.panel=i.power=true;return i;}
Controller ready(uint32_t start=0){Controller c;c.boot(start);auto i=safe();c.tick(i,start);c.heartbeat(true,start+6000);c.heartbeat(true,start+7000);c.tick(i,start+7000);return c;}
void controlTests(){
  auto i=safe();Controller c;c.boot(0);c.tick(i,1);CHECK(!c.release(false,7000));
  c.heartbeat(true,7000);CHECK(!c.release(false,7999));c.heartbeat(true,8000);CHECK(c.release(false,8000));
  CHECK(!c.release(false,8050));CHECK(c.started==8000);
  i.ext=false;i.ret=true;c.tick(i,8100);CHECK(c.coil);
  i.closed=false;c.tick(i,8200);CHECK(!c.coil&&c.lastOff==8200);CHECK(!c.contained());
  c=ready();CHECK(c.contained());CHECK(c.release(false,7000));i=safe();c.tick(i,7500);CHECK(!c.coil&&(c.faults&LATCH));
  c=ready();CHECK(c.release(false,7000));i=safe();i.ext=false;i.ret=true;c.tick(i,7100);c.tick(i,9000);CHECK(!c.coil);CHECK(c.lastOff==9000);
  c=ready();CHECK(c.release(false,7000));i=safe();i.ext=false;i.ret=true;c.tick(i,7100);c.tick(i,8900);CHECK(c.coil);c.tick(i,9000);CHECK(!c.coil);
  c=ready();CHECK(c.release(false,7000));c.heartbeat(false,7050);CHECK(!c.coil&&!c.stationary(7050));
  c=ready();CHECK(c.release(false,7000));i=safe();i.panel=false;c.tick(i,7010);CHECK(!c.coil&&(c.faults&TAMPER));
  i.panel=true;c.tick(i,7050);CHECK(!c.contained());CHECK(c.reset(7100));CHECK(c.contained());
  for(uint8_t which=0;which<5;which++){
    c=ready();i=safe();
    if(which==0)i.tray=false;if(which==1)i.power=false;if(which==2)i.ret=true;
    if(which==3)i.stowed=true;if(which==4)i.panel=false;
    c.tick(i,7100);CHECK(!c.contained()&&c.faults);CHECK(!c.release(false,7100));
  }
  c=ready();i=safe();i.closed=false;c.tick(i,7100);CHECK(!c.release(false,7100));CHECK(c.release(true,7100));
  i.ext=false;i.ret=true;c.tick(i,7200);CHECK(c.coil);i.closed=true;c.tick(i,7300);CHECK(!c.coil);
  i.ret=false;i.ext=true;c.tick(i,7400);CHECK(c.contained());
  c=ready();CHECK(c.release(false,7000));i=safe();i.closed=false;i.ext=false;i.ret=true;c.tick(i,7100);
  i.ret=false;i.ext=true;i.stowed=true;c.tick(i,7200);CHECK(!c.release(true,14000));
  // Exact freshness boundaries and unsigned millis rollover.
  c=ready();CHECK(c.stationary(8999));CHECK(!c.stationary(9000));
  uint32_t start=0xfffff000UL;c=ready(start);CHECK(c.stationary(start+7500));CHECK(c.release(false,start+7500));
  i=safe();i.ext=false;i.ret=true;c.tick(i,start+7600);i.closed=false;c.tick(i,start+7700);CHECK(!c.coil);
  // Reset is not allowed to erase a contradictory or physically absent state.
  c=ready();i=safe();i.panel=false;c.tick(i,7100);CHECK(!c.reset(7100));
  c=ready();i=safe();i.ext=false;c.tick(i,7100);c.tick(i,9200);CHECK(c.faults&LATCH);
}
void scaleTests(){
  Scale s;s.configure(1000000,1400);uint32_t t=20000;
  for(int k=0;k<20;k++,t+=100)s.sample(1000000+7000+(k%3-1)*70,t);
  CHECK(s.valid(t,true));CHECK(s.milligrams()>4980&&s.milligrams()<5020);
  for(int k=0;k<11;k++,t+=100){s.sample(1007000,t);s.updateOccupancy(t,s.valid(t,true));}
  CHECK(s.occupied);CHECK(!s.valid(t,false));CHECK(!s.valid(t+351,true));
  // A reboot restores the saved tare, not the 5 g sheet on the tray.
  Scale boot;boot.configure(s.zero,s.countsPerGram);
  for(int k=0;k<20;k++,t+=100)boot.sample(1007000,t);
  CHECK(boot.milligrams()==5000&&boot.valid(t,true));
  for(int k=0;k<20;k++,t+=100)s.sample(1000000,t);
  for(int k=0;k<17;k++,t+=100){s.sample(1000000,t);s.updateOccupancy(t,s.valid(t,true));}
  CHECK(!s.occupied);
  s.sample(8388607,t);CHECK(!s.valid(t,true)&&s.adcFault);
  for(int k=0;k<20;k++,t+=100)s.sample(1000000+(k%2)*2000,t);
  CHECK(!s.valid(t,true));
  for(int k=0;k<20;k++,t+=100)s.sample(997200,t);CHECK(!s.valid(t,true)); // -2 g drift, no automatic zero
  for(int k=0;k<20;k++,t+=100)s.sample(1000000+1400*1001,t);CHECK(!s.valid(t,true));
  Scale uncal;for(int k=0;k<20;k++)uncal.sample(1007000,t);CHECK(!uncal.valid(t,true));
  s.configure(0,0);CHECK(!s.calibrated);s.configure(0,NAN);CHECK(!s.calibrated);
  s.configure(-1000,-1400);for(int k=0;k<20;k++)s.sample(-8000,t);CHECK(s.milligrams()==5000);
  // Software paper/noise stress: a synthetic signal test, not measured sensor performance.
  for(int position=0;position<9;position++)for(int rep=0;rep<10;rep++){
    Scale p;p.configure(1000000,1400);
    for(int k=0;k<40;k++,t+=100){
      int noise=((k*17+rep*13+position*7)%501)-250;
      p.sample(1000000+6985+noise,t);p.updateOccupancy(t,p.valid(t,true));
    }
    CHECK(p.valid(t,true)&&p.occupied);
  }
}
struct Memory {
  uint8_t b[256];int left=-1;
  Memory(){memset(b,255,sizeof(b));}
  uint8_t read(uint16_t a){return b[a];}
  void update(uint16_t a,uint8_t v){if(left==0)throw std::runtime_error("power cut");if(left>0)--left;b[a]=v;}
};
void authTests(){
  // RFC 4231 test case 1, verified against a published known answer.
  uint8_t k[20];memset(k,0x0b,20);uint8_t tag[32];char text[65];SHA256 h;
  h.resetHMAC(k,20);h.update("Hi There",8);h.finalizeHMAC(k,20,tag,32);encodeHex(tag,32,text);
  CHECK(!strcmp(text,"b0344c61d8db38535ca8afceaf0bf12b881dc200c9833da726e9376c2e32cff7"));
  uint8_t key[32];for(int i=0;i<32;i++)key[i]=i;
  Auth a;a.setup(key,2);CHECK(a.challenge(UNLOCK,1,100));a.requestTag(tag);encodeHex(tag,32,text);
  CHECK(a.verify(text,1,101));CHECK(!a.verify(text,1,102));
  CHECK(a.challenge(STOPPED,1,200));a.requestTag(tag);encodeHex(tag,32,text);CHECK(!a.verify(text,0,201));
  CHECK(a.challenge(UNLOCK,0,300));a.requestTag(tag);encodeHex(tag,32,text);CHECK(!a.verify(text,0,2300));
  CHECK(a.challenge(UNLOCK,0,2400));a.requestTag(tag);encodeHex(tag,32,text);a.verb=CLOSE;CHECK(!a.verify(text,0,2401));
  CHECK(a.challenge(STATUS,0,2500));a.requestTag(tag);encodeHex(tag,32,text);text[0]=text[0]=='0'?'1':'0';CHECK(!a.verify(text,0,2501));
  Auth expired;expired.setup(key,3);
  for(int i=0;i<5;i++){expired.challenge(STATUS,0,100+i);CHECK(!expired.verify("bad",0,100+i));}
  CHECK(!expired.challenge(STATUS,0,30103));CHECK(expired.challenge(STATUS,0,30104));
  Auth rollover;rollover.setup(key,4);CHECK(rollover.challenge(STATUS,0,0xffffff00UL));
  rollover.requestTag(tag);encodeHex(tag,32,text);CHECK(rollover.verify(text,0,0x100));
  Auth reboot;reboot.setup(key,3);reboot.challenge(UNLOCK,0,1);uint8_t n[16];memcpy(n,reboot.nonce,16);
  reboot.setup(key,4);reboot.challenge(UNLOCK,0,1);CHECK(memcmp(n,reboot.nonce,16)!=0);
  reboot.counter=0xffffffffUL;CHECK(!reboot.challenge(STATUS,0,2));
  Memory m;uint32_t epoch=0;CHECK(!advanceEpoch(m,epoch));CHECK(writeEpoch(m,64,1));CHECK(advanceEpoch(m,epoch)&&epoch==2);
  // Power loss at every byte write of the next epoch. On reboot, no issued epoch is reused.
  for(int cut=0;cut<=12;cut++){
    Memory copy=m;copy.left=cut;
    try{advanceEpoch(copy,epoch);}catch(const std::runtime_error&){}
    copy.left=-1;CHECK(advanceEpoch(copy,epoch));CHECK(epoch>=3&&epoch<=4);
  }
  CHECK(writeEpoch(m,64,0xffffffffUL));CHECK(!advanceEpoch(m,epoch));
  // Golden messages for Python cross-language HMAC validation.
  Auth cross;cross.setup(key,7);cross.challenge(STATUS,1,100);char nonce[33];encodeHex(cross.nonce,16,nonce);
  cross.requestTag(tag);encodeHex(tag,32,text);std::printf("VECTOR %s %s\n",nonce,text);
  cross.responseTag("S 1 0 1 0 1 1 0 0 1 1 4990 1 1 11800 1",tag);encodeHex(tag,32,text);std::printf("RESPONSE %s\n",text);
}
int main(){controlTests();scaleTests();authTests();std::printf("RESULT checks=%d failed=%d\n",checks,failed);return failed?1:0;}
