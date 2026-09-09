#include "../controller_reference.hpp"
#include <cassert>
#include <iostream>
using namespace r4;
Input home(){Input i;i.fresh=true;i.stopped=true;i.lidClosed=true;i.extended=true;i.seated=true;i.homes=true;i.scaleStable=true;return i;}
int main(){
  Controller c;auto i=home();auto o=c.tick(i);assert(o.state==State::Closed&&o.readyToTravel);
  i.openRequest=true;i.now+=20;o=c.tick(i);assert(o.state==State::UnlockOpen&&o.coil&&!o.move);
  i.openRequest=false;i.extended=false;i.retracted=true;i.now+=200;o=c.tick(i);assert(o.state==State::Opening);
  i.lidClosed=false;i.lid=12;i.now+=1200;o=c.tick(i);assert(!o.coil);
  i.retracted=false;i.extended=true;i.lid=105;i.lidOpen=true;i.now+=10000;o=c.tick(i);assert(o.state==State::Pickup);
  i.lowFront=i.lowRear=43.3f;i.highFront=i.highRear=42;i.now+=500;o=c.tick(i);assert(o.state==State::Lift);
  i.seated=false;i.homes=false;i.lowFront=i.lowRear=78.3f;i.highFront=i.highRear=77;i.now+=9000;o=c.tick(i);assert(o.state==State::Tilt);
  i.highFront=i.highRear=115.86881f;i.now+=10000;o=c.tick(i);assert(o.state==State::Open&&!o.weightValid&&!o.readyToTravel);
  i.closeRequest=true;i.now+=100;o=c.tick(i);assert(o.state==State::Level&&o.lid==105);
  i.closeRequest=false;i.highFront=i.highRear=77;i.now+=10000;o=c.tick(i);assert(o.state==State::Lower&&o.lid==105);
  i.lowFront=i.lowRear=43.3f;i.highFront=i.highRear=42;i.seated=true;i.now+=9000;o=c.tick(i);assert(o.state==State::Park);
  i.lowFront=i.lowRear=i.highFront=i.highRear=41.2f;i.homes=true;i.now+=1000;o=c.tick(i);assert(o.state==State::Closing&&o.lid==6&&!o.coil);
  i.lidOpen=false;i.lid=6;i.now+=10000;o=c.tick(i);assert(o.state==State::UnlockClose&&o.coil&&!o.move);
  i.extended=false;i.retracted=true;i.now+=200;o=c.tick(i);assert(o.state==State::Seat&&o.lid==0);
  i.lid=0;i.lidClosed=true;i.now+=800;o=c.tick(i);assert(o.state==State::Relock&&!o.coil);
  i.retracted=false;i.extended=true;i.now+=300;o=c.tick(i);assert(o.state==State::Closed&&o.readyToTravel);
  // No blind homing when booted in an unknown position.
  Controller boot;i=home();i.lidClosed=false;i.lid=40;o=boot.tick(i);assert(o.state==State::Boot&&!o.move);
  // A jammed latch cannot cause the door motor to pull through the keeper.
  Controller jam;i=home();jam.tick(i);i.openRequest=true;i.now=20;jam.tick(i);i.now=521;o=jam.tick(i);assert(o.state==State::Fault&&!o.coil&&!o.move);
  // Cross-shaft skew, stale feedback, obstruction and inconsistent contacts stop and latch a fault.
  for(int fault=0;fault<6;++fault){Controller f;i=home();f.tick(i);i.openRequest=true;i.now=20;f.tick(i);i.retracted=true;i.extended=false;i.now=40;f.tick(i);
    if(fault==0)i.lowRear+=1;
    if(fault==1)i.feedbackAge=201;
    if(fault==2)i.obstruction=true;
    if(fault==3)i.stopped=false;
    if(fault==4)i.lid=std::nanf("");
    if(fault==5)i.extended=true;
    i.now=60;o=f.tick(i);assert(o.state==State::Fault&&!o.move&&!o.coil);
    i=home();i.now=80;o=f.tick(i);assert(o.state==State::Fault);
  }
  // Firmware coil timeout is independent of whether angle feedback gets stuck.
  Controller coil;i=home();coil.tick(i);i.openRequest=true;i.now=20;coil.tick(i);i.retracted=true;i.extended=false;i.now=40;coil.tick(i);i.now=2020;o=coil.tick(i);assert(o.state==State::Fault&&!o.coil);
  std::cout<<"PASS: complete sensor-driven cycle; boot, latch jam, skew, stale data, obstruction, movement, NaN, contradictory contacts, fault latch, coil timeout\n";
}
