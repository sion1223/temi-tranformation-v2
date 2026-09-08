"""Merge this model's simultaneous door and latch tracks into one viewer clip."""
import json,struct
from pathlib import Path
def merge(path):
    p=Path(path);data=p.read_bytes();chunks=[];i=12
    while i<len(data):
        size,kind=struct.unpack_from('<II',data,i);chunks.append([kind,data[i+8:i+8+size]]);i+=8+size
    g=json.loads(chunks[0][1]);animations=g.get('animations',[])
    if len(animations)>1:
        clip={'name':'R3_A4_Door_and_Lock_Cycle','samplers':[],'channels':[]}
        for a in animations:
            offset=len(clip['samplers']);clip['samplers'].extend(a['samplers'])
            for ch in a['channels']:
                ch=dict(ch);ch['sampler']+=offset;clip['channels'].append(ch)
        g['animations']=[clip]
    if g.get('animations'):
        g['animations'][0]['name']='R3_A4_Door_and_Lock_Cycle'
    payload=json.dumps(g,ensure_ascii=False,separators=(',',':')).encode('utf-8')
    payload+=b' '*((-len(payload))%4);chunks[0][1]=payload
    body=b''.join(struct.pack('<II',len(content),kind)+content for kind,content in chunks)
    p.write_bytes(struct.pack('<4sII',b'glTF',2,12+len(body))+body)
    return len(g.get('animations',[]))
if __name__=='__main__':
    import sys
    print('Animation clips:',merge(sys.argv[1]))
