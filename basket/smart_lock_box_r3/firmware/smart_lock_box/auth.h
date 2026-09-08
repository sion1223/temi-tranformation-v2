#pragma once
#include <stdint.h>
#include <string.h>
#include "SHA256.h"
#include "Crypto.h"
#include "control.h"
namespace r3 {
inline int8_t hexNibble(char c){if(c>='0'&&c<='9')return c-'0';if(c>='a'&&c<='f')return c-'a'+10;return -1;}
inline bool decodeHex(const char *s,uint8_t *out,uint8_t n){
  if(!s||strlen(s)!=size_t(n)*2)return false;
  for(uint8_t i=0;i<n;i++){int8_t a=hexNibble(s[2*i]),b=hexNibble(s[2*i+1]);if(a<0||b<0)return false;out[i]=(a<<4)|b;}return true;
}
inline void encodeHex(const uint8_t *in,uint8_t n,char *out){
  const char *h="0123456789abcdef";
  for(uint8_t i=0;i<n;i++){out[i*2]=h[in[i]>>4];out[i*2+1]=h[in[i]&15];}out[n*2]=0;
}
inline uint16_t crc16(const uint8_t *p,uint16_t len){
  uint16_t c=0xffff;while(len--){c^=*p++;for(uint8_t i=0;i<8;i++)c=(c>>1)^((c&1)?0xa001:0);}return c;
}
enum Verb:uint8_t { NONE,STOPPED,MOVING,UNLOCK,CLOSE,RESET,STATUS };
inline const char *verbName(Verb v){
  switch(v){case STOPPED:return "STOPPED";case MOVING:return "MOVING";case UNLOCK:return "UNLOCK";
    case CLOSE:return "CLOSE";case RESET:return "RESET";case STATUS:return "STATUS";default:return "NONE";}
}
inline Verb parseVerb(const char *s){for(uint8_t i=1;i<=STATUS;i++)if(!strcmp(s,verbName(Verb(i))))return Verb(i);return NONE;}
struct Auth {
  uint8_t key[32]={0},nonce[16]={0},channel=0,failures=0;
  uint32_t epoch=0,counter=0,issuedAt=0,blockedAt=0;
  bool ready=false,pending=false,blocked=false;
  Verb verb=NONE;
  void setup(const uint8_t *k,uint32_t committedEpoch){
    memcpy(key,k,32);epoch=committedEpoch;counter=0;ready=epoch!=0;pending=false;
  }
  bool challenge(Verb v,uint8_t ch,uint32_t now){
    if(blocked&&elapsed(now,blockedAt)<30000)return false;blocked=false;
    if(!ready||v==NONE||counter==0xffffffffUL)return false;
    ++counter;uint8_t seed[9];
    for(uint8_t i=0;i<4;i++){seed[i]=epoch>>(i*8);seed[4+i]=counter>>(i*8);}seed[8]=ch;
    SHA256 h;h.resetHMAC(key,32);h.update("R3-NONCE|",9);h.update(seed,9);h.finalizeHMAC(key,32,nonce,16);
    verb=v;channel=ch;issuedAt=now;pending=true;return true;
  }
  void requestTag(uint8_t *tag)const{
    char nh[33];encodeHex(nonce,16,nh);SHA256 h;h.resetHMAC(key,32);
    h.update("R3-REQ|",7);h.update(verbName(verb),strlen(verbName(verb)));h.update("|",1);h.update(nh,32);
    h.finalizeHMAC(key,32,tag,32);
  }
  bool verify(const char *hex,uint8_t ch,uint32_t now){
    bool eligible=ready&&pending&&ch==channel&&elapsed(now,issuedAt)<2000&&(!blocked||elapsed(now,blockedAt)>=30000);
    pending=false;uint8_t received[32],expected[32];
    if(eligible&&decodeHex(hex,received,32)){
      requestTag(expected);if(secure_compare(received,expected,32)){failures=0;return true;}
    }
    if(++failures>=5){blocked=true;blockedAt=now;failures=0;}return false;
  }
  void responseTag(const char *payload,uint8_t *tag)const{
    char nh[33];encodeHex(nonce,16,nh);SHA256 h;h.resetHMAC(key,32);
    h.update("R3-RESP|",8);h.update(nh,32);h.update("|",1);h.update(payload,strlen(payload));h.finalizeHMAC(key,32,tag,32);
  }
};

// Two EEPROM slots. Invalidate destination, write data+CRC, then commit last.
// A challenge may be issued only after the new epoch is read back successfully.
template<class Storage> bool readEpoch(Storage &s,uint16_t addr,uint32_t &epoch){
  uint8_t b[11];for(uint8_t i=0;i<11;i++)b[i]=s.read(addr+i);
  if(b[10]!=0xa5||crc16(b,8)!=(uint16_t(b[8])|(uint16_t(b[9])<<8)))return false;
  uint32_t a=0,inv=0;for(uint8_t i=0;i<4;i++){a|=uint32_t(b[i])<<(8*i);inv|=uint32_t(b[4+i])<<(8*i);}
  if(!a||inv!=~a)return false;epoch=a;return true;
}
template<class Storage> bool writeEpoch(Storage &s,uint16_t addr,uint32_t epoch){
  uint8_t b[10];for(uint8_t i=0;i<4;i++){b[i]=epoch>>(i*8);b[4+i]=(~epoch)>>(i*8);}
  uint16_t c=crc16(b,8);b[8]=c;b[9]=c>>8;s.update(addr+10,0);
  for(uint8_t i=0;i<10;i++)s.update(addr+i,b[i]);s.update(addr+10,0xa5);
  uint32_t check=0;return readEpoch(s,addr,check)&&check==epoch;
}
template<class Storage> bool advanceEpoch(Storage &s,uint32_t &epoch){
  uint32_t a=0,b=0;bool va=readEpoch(s,64,a),vb=readEpoch(s,80,b);
  if(!va&&!vb)return false;
  uint32_t last=a>b?a:b;if(last==0xffffffffUL)return false;
  epoch=last+1;return writeEpoch(s,(!vb||(va&&a>=b))?80:64,epoch);
}
}
