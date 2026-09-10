"""Combine simultaneous glTF tracks into one full-cycle clip without rebaking."""
import json
import struct
from pathlib import Path

def merge(path):
    p=Path(path);raw=p.read_bytes();chunks=[];offset=12
    assert raw[:4]==b'glTF'
    while offset<len(raw):
        size,kind=struct.unpack_from('<II',raw,offset)
        chunks.append([kind,raw[offset+8:offset+8+size]]);offset+=8+size
    doc=json.loads(chunks[0][1]);clip={'name':'R4_Full_Automatic_Cycle','channels':[],'samplers':[]}
    targets=set()
    for animation in doc.get('animations',[]):
        first=len(clip['samplers']);clip['samplers'].extend(animation['samplers'])
        for channel in animation['channels']:
            channel=dict(channel);target=(channel['target']['node'],channel['target']['path'])
            assert target not in targets, 'Conflicting simultaneous channels'
            targets.add(target);channel['sampler']+=first;clip['channels'].append(channel)
    assert clip['channels'], 'No exported animation'
    doc['animations']=[clip]
    encoded=json.dumps(doc,ensure_ascii=False,separators=(',',':')).encode()
    chunks[0][1]=encoded+b' '*(-len(encoded)%4)
    body=b''.join(struct.pack('<II',len(data),kind)+data for kind,data in chunks)
    p.write_bytes(struct.pack('<4sII',b'glTF',2,len(body)+12)+body)

if __name__=='__main__':
    import sys
    merge(sys.argv[1])
