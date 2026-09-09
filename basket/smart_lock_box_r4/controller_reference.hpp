#pragma once
// Portable sequencing core, NOT a flashable replacement for R3 firmware.
// Hardware adapter must supply independent measured axes, debounced contacts,
// authenticated requests, a watchdog, and current/velocity-limited motion.
#include <cmath>
#include <cstdint>

namespace r4 {
enum class State { Boot, Closed, UnlockOpen, Opening, Pickup, Lift, Tilt, Open,
                   Level, Lower, Park, Closing, UnlockClose, Seat, Relock, Fault };
struct Input {
  uint32_t now=0, feedbackAge=0, robotAge=0;
  bool fresh=false, stopped=false, power=true, estop=false, obstruction=false;
  bool openRequest=false, closeRequest=false; // authenticated rising-edge events
  bool lidClosed=false, lidOpen=false, retracted=false, extended=false;
  bool seated=false, homes=false, scaleStable=false, sensorsPlausible=true;
  float lid=0, lowFront=41.2f, lowRear=41.2f, highFront=41.2f, highRear=41.2f;
};
struct Output {
  State state=State::Boot;
  float lid=0, low=41.2f, high=41.2f;
  bool move=false, coil=false, hold=true, weightValid=false, readyToTravel=false;
};
class Controller {
  State s=State::Boot;
  uint32_t entered=0, coilSince=0;
  bool coilWas=false;
  Output out;
  void go(State next,uint32_t now) { s=next; entered=now; }
  static bool near(float a,float b) { return std::fabs(a-b)<0.35f; }
  static bool at(const Input& i,float l,float h) {
    return near(i.lowFront,l)&&near(i.lowRear,l)&&near(i.highFront,h)&&near(i.highRear,h);
  }
public:
  Output tick(const Input& i) {
    const bool valid=i.fresh && i.feedbackAge<=200 && i.robotAge<=600;
    const bool finite=std::isfinite(i.lid)&&std::isfinite(i.lowFront)&&std::isfinite(i.lowRear)
       &&std::isfinite(i.highFront)&&std::isfinite(i.highRear);
    const bool coherent=finite && std::fabs(i.lowFront-i.lowRear)<=0.8f
       &&std::fabs(i.highFront-i.highRear)<=0.8f && !(i.lidClosed&&i.lidOpen)
       && !(i.extended&&i.retracted) && i.sensorsPlausible
       && i.lid>=-0.5f&&i.lid<=107&&i.lowFront>=40.5f&&i.lowFront<=80
       && i.highFront>=40.5f&&i.highFront<=118;
    const bool safe=valid && coherent && i.power && !i.estop && !i.obstruction;
    const bool movingState=s!=State::Boot&&s!=State::Closed&&s!=State::Open&&s!=State::Fault;
    if ((!safe && s!=State::Boot) || (movingState && !i.stopped))go(State::Fault,i.now);
    if ((s==State::Lift||s==State::Lower) && std::fabs(i.highFront-i.lowFront+1.3f)>0.8f)
      go(State::Fault,i.now);
    if ((s==State::Tilt||s==State::Level||s==State::Open) && std::fabs(i.lowFront-78.3f)>0.8f)
      go(State::Fault,i.now);
    if (s!=State::Fault && s!=State::Boot && s!=State::Closed && s!=State::Open
        && i.now-entered>30000) go(State::Fault,i.now);
    // Never rotate the lid into a raised tray, even after an unexpected sensor change.
    if ((s==State::Closing||s==State::UnlockClose||s==State::Seat||s==State::Relock)
        && (!i.seated||!i.homes||!at(i,41.2f,41.2f)))go(State::Fault,i.now);
    // A fully open lid must remain verified throughout all tray motion.
    if ((s==State::Pickup||s==State::Lift||s==State::Tilt||s==State::Open||s==State::Level||s==State::Lower||s==State::Park)
        && (!i.lidOpen||i.lid<103))go(State::Fault,i.now);
    if ((s==State::UnlockOpen||s==State::UnlockClose)&&i.now-entered>500&&!i.retracted)
        go(State::Fault,i.now);
    if (s==State::Relock&&i.now-entered>1000&&!i.extended)go(State::Fault,i.now);
    switch(s) {
      case State::Boot:
        if(safe&&i.lidClosed&&i.lid<0.5f&&i.extended&&!i.retracted&&i.seated&&i.homes&&at(i,41.2f,41.2f))go(State::Closed,i.now);
        break;
      case State::Closed:
        if(!i.lidClosed||!i.extended||!i.seated||!i.homes)go(State::Fault,i.now);
        else if(i.openRequest&&i.stopped)go(State::UnlockOpen,i.now);
        break;
      case State::UnlockOpen: if(i.retracted&&!i.extended)go(State::Opening,i.now);break;
      case State::Opening: if(i.lidOpen&&std::fabs(i.lid-105)<1)go(State::Pickup,i.now);break;
      case State::Pickup: if(at(i,43.3f,42.0f))go(State::Lift,i.now);break;
      case State::Lift: if(at(i,78.3f,77.0f))go(State::Tilt,i.now);break;
      case State::Tilt: if(at(i,78.3f,115.86881f))go(State::Open,i.now);break;
      case State::Open: if(i.closeRequest&&i.stopped)go(State::Level,i.now);break;
      case State::Level: if(at(i,78.3f,77.0f))go(State::Lower,i.now);break;
      case State::Lower: if(at(i,43.3f,42.0f)&&i.seated)go(State::Park,i.now);break;
      case State::Park: if(at(i,41.2f,41.2f)&&i.homes&&i.seated)go(State::Closing,i.now);break;
      case State::Closing: if(std::fabs(i.lid-6)<0.5f)go(State::UnlockClose,i.now);break;
      case State::UnlockClose: if(i.retracted&&!i.extended)go(State::Seat,i.now);break;
      case State::Seat: if(i.lidClosed&&i.lid<0.5f)go(State::Relock,i.now);break;
      case State::Relock: if(i.lidClosed&&i.extended&&!i.retracted)go(State::Closed,i.now);break;
      case State::Fault: break; // explicit service reset/rehome required; no automatic retry
    }
    out.state=s;out.coil=false;out.move=false;out.hold=true;
    out.weightValid=false;out.readyToTravel=false;
    switch(s) {
      case State::Closed:
        out.lid=0;out.low=out.high=41.2f;
        out.weightValid=valid&&i.stopped&&i.scaleStable&&i.seated&&i.homes;
        out.readyToTravel=out.weightValid&&i.lidClosed&&i.extended&&!i.retracted;
        break;
      case State::UnlockOpen:out.coil=true;break;
      case State::Opening:out.lid=105;out.move=true;out.coil=i.lid<9;break;
      case State::Pickup:out.low=43.3f;out.high=42;out.move=true;break;
      case State::Lift:out.low=78.3f;out.high=77;out.move=true;break;
      case State::Tilt:out.low=78.3f;out.high=115.86881f;out.move=true;break;
      case State::Open:break;
      case State::Level:out.low=78.3f;out.high=77;out.move=true;break;
      case State::Lower:out.low=43.3f;out.high=42;out.move=true;break;
      case State::Park:out.low=out.high=41.2f;out.move=true;break;
      case State::Closing:out.lid=6;out.move=true;break;
      case State::UnlockClose:out.coil=true;break;
      case State::Seat:out.lid=0;out.move=true;out.coil=true;break;
      case State::Relock:break;
      default:break;
    }
    if(out.coil&&!coilWas)coilSince=i.now;
    if(out.coil&&i.now-coilSince>=2000) {
      go(State::Fault,i.now);out.state=s;out.move=false;out.coil=false;
    }
    if(s==State::Fault||s==State::Boot) {
      // HOLD means freeze at measured position, not continue toward the last target.
      if(finite){out.lid=i.lid;out.low=i.lowFront;out.high=i.highFront;}
      out.move=false;out.coil=false;
    }
    coilWas=out.coil;
    return out;
  }
};
} // namespace r4
