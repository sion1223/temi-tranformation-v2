#pragma once
#include <stdint.h>
#define _BV(n) (1u<<(n))
#define PD7 7
#define OCIE1A 1
#define OCF1A 1
#define WGM12 3
#define CS12 2
#define CS10 0
#define ISR(n) void n()
inline uint8_t PORTD=0,TIMSK1=0,TCCR1A=0,TCCR1B=0,TIFR1=0,SREG=0,MCUSR=0;
inline uint16_t TCNT1=0,OCR1A=0;
inline void cli(){}
