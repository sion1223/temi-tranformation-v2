"""Reference R3 serial client. A robot adapter must supply real stationary/motion events.
No dependency is needed for cryptographic verification; serial transport uses pyserial.
Never use an unauthenticated text STATUS or a cached signed state to authorise travel.
"""
from __future__ import annotations
import argparse
import hashlib
import hmac
import json
import re
import secrets
import time
from dataclasses import dataclass
from pathlib import Path

VERBS={"STOPPED","MOVING","UNLOCK","CLOSE","RESET","STATUS"}
FIELDS=("closed","retracted","extended","stowed","tray","panel","faults","coil",
        "ready_to_travel","containment_ok","milligrams","weight_valid","occupied","power_mv","accepted")

def request(key:bytes,verb:str,challenge:str)->tuple[str,str]:
    if len(key)!=32 or verb not in VERBS:raise ValueError("Invalid key or verb")
    m=re.fullmatch(r"CHALLENGE ([A-Z]+) ([0-9a-f]{32})",challenge)
    if not m or m[1]!=verb:raise ValueError("Unexpected challenge")
    nonce=m[2]
    tag=hmac.new(key,f"R3-REQ|{verb}|{nonce}".encode("ascii"),hashlib.sha256).hexdigest()
    return nonce,"AUTH "+tag

def verify_state(key:bytes,nonce:str,line:str,round_trip_seconds:float)->dict[str,int]:
    if not 0<=round_trip_seconds<2.0:raise ValueError("Stale transaction")
    if not re.fullmatch(r"[0-9a-f]{32}",nonce):raise ValueError("Invalid nonce")
    try:payload,tag=line.rsplit(" ",1)
    except ValueError as exc:raise ValueError("Missing response authentication") from exc
    expected=hmac.new(key,f"R3-RESP|{nonce}|{payload}".encode("ascii"),hashlib.sha256).hexdigest()
    if not re.fullmatch(r"[0-9a-f]{64}",tag) or not hmac.compare_digest(tag,expected):raise ValueError("Response authentication failed")
    parts=payload.split(" ")
    if len(parts)!=16 or parts[0]!="S" or any(not re.fullmatch(r"-?[0-9]+",x) for x in parts[1:]):raise ValueError("Invalid state schema")
    state=dict(zip(FIELDS,map(int,parts[1:])))
    for k in FIELDS:
        if k not in ("faults","milligrams","power_mv") and state[k] not in (0,1):raise ValueError("Invalid boolean")
    return state

def may_start_travel(state:dict[str,int])->bool:
    return bool(state["accepted"] and state["ready_to_travel"] and state["containment_ok"] and
                state["weight_valid"] and not state["faults"] and not state["coil"] and
                state["closed"] and state["extended"] and not state["retracted"] and not state["stowed"] and
                state["tray"] and state["panel"] and 10800<=state["power_mv"]<=12200)

class Client:
    def __init__(self,port,key:bytes):self.port=port;self.key=key
    def _line(self,deadline:float)->str:
        while time.monotonic()<deadline:
            raw=self.port.readline()
            if raw:return raw.decode("ascii",errors="strict").strip()
        raise TimeoutError("Box did not reply")
    def transact(self,verb:str)->dict[str,int]:
        if verb not in VERBS:raise ValueError("Unknown verb")
        self.port.reset_input_buffer();started=time.monotonic();deadline=started+1.8
        self.port.write(("CHAL "+verb+"\n").encode("ascii"))
        nonce,auth=request(self.key,verb,self._line(deadline))
        self.port.write((auth+"\n").encode("ascii"))
        line=self._line(deadline)
        return verify_state(self.key,nonce,line,time.monotonic()-started)

def main():
    p=argparse.ArgumentParser(description=__doc__)
    p.add_argument("command",choices=["keygen","provision","status","unlock","close","reset","calibrate"])
    p.add_argument("--key-file",type=Path,required=True)
    p.add_argument("--port");p.add_argument("--baud",type=int,default=115200)
    p.add_argument("--bench-stationary",action="store_true",help="Manual bench only: operator confirms the robot is stopped")
    a=p.parse_args()
    if a.command=="keygen":
        with a.key_file.open("x",encoding="ascii") as f:json.dump({"key_hex":secrets.token_hex(32)},f)
        print("Key saved. Keep this file outside source control and out of the model package.");return
    key=bytes.fromhex(json.loads(a.key_file.read_text(encoding="ascii"))["key_hex"])
    if len(key)!=32:raise ValueError("Key must be 256 bits")
    if not a.port:p.error("--port is required")
    if a.command not in ("status","provision") and not a.bench_stationary:p.error("This bench client needs --bench-stationary; production must use real robot motion feedback")
    import serial
    with serial.Serial(a.port,a.baud,timeout=.15,write_timeout=1) as port:
        # Opening many Nano USB serial ports resets the board. Wait without auto-taring.
        time.sleep(2);port.reset_input_buffer();c=Client(port,key)
        if a.command=="provision":
            port.write(("PROVISION "+key.hex()+"\n").encode("ascii"));print(c._line(time.monotonic()+2));return
        if a.bench_stationary:
            # Allow boot cooldown and a fresh, stable sample window. No release retries.
            start=time.monotonic()
            while time.monotonic()-start<17:
                c.transact("STOPPED");time.sleep(.35)
        if a.command=="calibrate":
            # Keep ONE serial connection for the two-stage calibration. Reopening
            # a Nano USB connection may reset the unsaved CAL_ZERO staging value.
            for prompt,cmd in [("Empty the entire tray, then press Enter: ","CAL_ZERO"),
                               ("Place a verified 500 g mass centrally, then press Enter: ","CAL_500")]:
                input(prompt)
                start=time.monotonic()
                while time.monotonic()-start<4:
                    c.transact("STOPPED");time.sleep(.35)
                port.write((cmd+"\n").encode("ascii"));reply=c._line(time.monotonic()+2)
                if not reply.startswith("OK "):raise RuntimeError(reply)
                print(reply)
            return
        verb={"status":"STATUS","unlock":"UNLOCK","close":"CLOSE","reset":"RESET"}[a.command]
        state=c.transact(verb);print(json.dumps(state,indent=2))
        if verb in ("UNLOCK","CLOSE"):
            start=time.monotonic()
            while time.monotonic()-start<2.2:
                time.sleep(.25);c.transact("STOPPED")

if __name__=="__main__":main()
