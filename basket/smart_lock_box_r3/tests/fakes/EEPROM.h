#pragma once
#include <stdint.h>
#include <cstring>
class EEPROMClass {
public:
  uint8_t bytes[1024];EEPROMClass(){memset(bytes,255,sizeof(bytes));}
  uint8_t read(uint16_t a){return bytes[a];}
  void update(uint16_t a,uint8_t v){bytes[a]=v;}
};
inline EEPROMClass EEPROM;
