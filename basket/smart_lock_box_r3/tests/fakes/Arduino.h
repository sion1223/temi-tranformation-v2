#pragma once
#include <stdint.h>
#include <cstdio>
#include <string>
#include <cstring>
#define F(s) s
#define LOW 0
#define HIGH 1
#define INPUT_PULLUP 2
#define OUTPUT 1
#define LED_BUILTIN 13
#define A3 17
#define A6 20
inline uint32_t fakeNow=0;
inline int fakePins[32]={0};
inline int fakeAdc=599;
inline uint32_t millis(){return fakeNow;}
inline void pinMode(uint8_t,int){}
inline void digitalWrite(uint8_t pin,int value){fakePins[pin]=value;}
inline int digitalRead(uint8_t pin){return fakePins[pin];}
inline int analogRead(uint8_t){return fakeAdc;}
inline void delayMicroseconds(unsigned int){}
class Stream {
public:
  std::string input,output;
  int available(){return int(input.size());}
  int read(){if(input.empty())return -1;char c=input.front();input.erase(0,1);return c;}
  void begin(unsigned long){}
  void print(const char *s){output+=s;}
  void print(char *s){output+=s;}
  void print(char c){output+=c;}
  template<class T>void print(T v){output+=std::to_string(v);}
  void println(){output+='\n';}
  template<class T>void println(T v){print(v);println();}
};
inline Stream Serial;
