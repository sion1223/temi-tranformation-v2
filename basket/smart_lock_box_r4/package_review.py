"""Prepare small review images, validate GLB metadata, and record file hashes."""
from pathlib import Path
import hashlib
import json
import struct
from PIL import Image
ROOT=Path(__file__).resolve().parent
for p in (ROOT/'drawings').glob('*.png'):
    Image.open(p).convert('RGB').save(p.with_suffix('.webp'),quality=90,method=6)
data=(ROOT/'smart_lock_box_r4.glb').read_bytes()
magic,version,length=struct.unpack_from('<4sII',data)
assert magic==b'glTF' and version==2 and length==len(data)
n,kind=struct.unpack_from('<I4s',data,12);assert kind==b'JSON'
doc=json.loads(data[20:20+n])
assert len(doc.get('animations',[]))==1, 'Export the complete cycle as a single animation'
nodes={n.get('name'):n for n in doc['nodes']}
assert 'R4_EXPORT_METRES' in nodes
assert all(abs(s-.001)<1e-8 for s in nodes['R4_EXPORT_METRES']['scale'])
for name in ('R4_LID_FIXED_HINGE','R4_TRAY_LIFT_TILT','R4_M2_LOW_CARRIAGE','R4_M3_HIGH_CARRIAGE',
             'R4_carry_handle_front','R4_carry_handle_rear'):
    assert name in nodes, name
report={'format':'glTF 2.0 GLB','bytes':len(data),'animations':1,
        'metric_root_scale':nodes['R4_EXPORT_METRES']['scale'],
        'nodes':len(doc['nodes']),'meshes':len(doc['meshes']),
        'animation_channels':len(doc['animations'][0]['channels']),
        'pass':True}
(ROOT/'validation/exports.json').write_text(json.dumps(report,indent=2)+'\n')
manifest=[]
for p in sorted(ROOT.rglob('*')):
    if not p.is_file() or p.suffix in ('.png','.blend1','.pyc') or '__pycache__' in p.parts or p.name=='manifest.json':continue
    content=p.read_bytes()
    manifest.append({'path':p.relative_to(ROOT).as_posix(),'bytes':len(content),'sha256':hashlib.sha256(content).hexdigest()})
(ROOT/'manifest.json').write_text(json.dumps(manifest,ensure_ascii=False,indent=2)+'\n')
print(json.dumps(report));print('Manifest:',len(manifest),'files')
