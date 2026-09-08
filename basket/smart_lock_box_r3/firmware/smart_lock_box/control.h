#pragma once
#include <stdint.h>
#include <math.h>

namespace r3 {
inline uint32_t elapsed(uint32_t now,uint32_t then){return uint32_t(now-then);}
struct Inputs { bool ret=false,closed=false,stowed=false,ext=false,tray=false,panel=false,power=false; };
enum Fault:uint8_t { TAMPER=1,LATCH=2,POWER=4,TIMER=8 };
struct Controller {
  Inputs in;
  bool coil=false,closeCycle=false,haveMotion=false,stopped=false,wasClosed=false;
  uint8_t faults=0;
  uint32_t started=0,lastOff=0,heartbeatAt=0,stoppedAt=0,closedAt=0;
  void boot(uint32_t now){*this=Controller();lastOff=closedAt=now;}
  bool stationary(uint32_t now)const {
    return haveMotion&&stopped&&elapsed(now,heartbeatAt)<2000&&elapsed(now,stoppedAt)>=1000;
  }
  void off(uint32_t now){if(coil){coil=false;lastOff=now;closedAt=now;}}
  void fail(uint8_t reason,uint32_t now){faults|=reason;off(now);}
  void heartbeat(bool isStopped,uint32_t now){
    if(!haveMotion||!stopped||elapsed(now,heartbeatAt)>=2000)stoppedAt=now;
    haveMotion=true;stopped=isStopped;heartbeatAt=now;
    if(!isStopped)off(now);
  }
  bool contained()const {
    return in.closed&&in.ext&&!in.ret&&!in.stowed&&in.tray&&in.panel&&in.power&&!coil&&!faults;
  }
  void tick(const Inputs &value,uint32_t now){
    in=value;
    if(!in.tray||!in.panel)fail(TAMPER,now);
    if(!in.power)fail(POWER,now);
    if((in.ret&&in.ext)||(in.closed&&in.stowed))fail(LATCH,now);
    if(in.closed&&!wasClosed)closedAt=now;
    if(coil){
      if(!stationary(now))fail(LATCH,now);
      else if(elapsed(now,started)>=500&&!in.ret)fail(LATCH,now);
      else if(elapsed(now,started)>=2000||(closeCycle?in.closed:!in.closed))off(now);
    }
    if(in.closed&&!coil&&elapsed(now,closedAt)>2000&&(!in.ext||in.ret))fail(LATCH,now);
    wasClosed=in.closed;
  }
  // No repeated request extends a pulse. The initial boot always includes a 6 s rest.
  bool release(bool toClose,uint32_t now){
    if(faults||coil||!in.tray||!in.panel||!in.power||!stationary(now)||elapsed(now,lastOff)<6000)return false;
    if(!in.ext||in.ret||in.stowed)return false;
    if(toClose?in.closed:!in.closed)return false;
    coil=true;closeCycle=toClose;started=now;return true;
  }
  bool reset(uint32_t now){
    if(coil||!stationary(now)||!in.tray||!in.panel||!in.power||
       (in.ret&&in.ext)||(in.closed&&in.stowed)||!in.ext||in.ret)return false;
    faults=0;return true;
  }
};

// State is NEVER zeroed at boot or automatically re-tared while loaded.
struct Scale {
  static const uint8_t N=20;
  int32_t ring[N]={0},zero=0;
  float countsPerGram=0;
  uint8_t count=0,index=0;
  uint32_t sampleAt=0,pendingAt=0;
  bool calibrated=false,occupied=false,pending=false,adcFault=true;
  void clearWindow(){count=index=0;}
  void configure(int32_t tare,float gain){
    zero=tare;countsPerGram=gain;
    calibrated=isfinite(gain)&&fabsf(gain)>=100&&fabsf(gain)<=10000;
    clearWindow();
  }
  void sample(int32_t raw,uint32_t now){
    sampleAt=now;adcFault=raw<=-8388607L||raw>=8388606L;
    if(adcFault){clearWindow();return;}
    ring[index]=raw;index=(index+1)%N;if(count<N)++count;
  }
  bool fresh(uint32_t now)const{return count==N&&!adcFault&&elapsed(now,sampleAt)<=350;}
  int32_t mean()const{
    if(!count)return 0;
    int64_t sum=0;for(uint8_t i=0;i<count;i++)sum+=ring[i];return int32_t(sum/count);
  }
  int32_t spread()const{
    if(!count)return 0;int32_t lo=ring[0],hi=lo;
    for(uint8_t i=1;i<count;i++){if(ring[i]<lo)lo=ring[i];if(ring[i]>hi)hi=ring[i];}
    return hi-lo;
  }
  int32_t milligrams()const{
    if(!calibrated)return 0;
    return int32_t(((float)(int64_t(mean())-zero))*1000.0f/countsPerGram);
  }
  bool valid(uint32_t now,bool environmentStable)const{
    return calibrated&&fresh(now)&&environmentStable&&spread()/fabsf(countsPerGram)<=.8f&&
           milligrams()>=-1000&&milligrams()<=1000000;
  }
  void updateOccupancy(uint32_t now,bool ok){
    if(!ok){pending=occupied;pendingAt=now;return;}
    int32_t mg=milligrams();bool next=occupied?mg>1000:mg>=3000;
    if(next!=pending){pending=next;pendingAt=now;}
    if(next!=occupied&&elapsed(now,pendingAt)>=(next?800u:1500u))occupied=next;
  }
};
}
