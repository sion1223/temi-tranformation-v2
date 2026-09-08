"""Verify GLB scene bounds (world transforms, metres) and the combined animation."""
import json,struct,math
from pathlib import Path
import numpy as np
ROOT=Path(__file__).resolve().parent
p=ROOT/'smart_lock_box_r3.glb';data=p.read_bytes()
magic,version,length=struct.unpack_from('<4sII',data)
assert magic==b'glTF' and version==2 and length==len(data)
size,kind=struct.unpack_from('<II',data,12);g=json.loads(data[20:20+size])
def matrix(node):
    if 'matrix'in node:return np.array(node['matrix']).reshape(4,4).T
    x,y,z,w=node.get('rotation',[0,0,0,1]);m=np.eye(4)
    m[:3,:3]=[[1-2*(y*y+z*z),2*(x*y-z*w),2*(x*z+y*w)],
               [2*(x*y+z*w),1-2*(x*x+z*z),2*(y*z-x*w)],
               [2*(x*z-y*w),2*(y*z+x*w),1-2*(x*x+y*y)]]
    m[:3,:3]=m[:3,:3]@np.diag(node.get('scale',[1,1,1]));m[:3,3]=node.get('translation',[0,0,0]);return m
pts=[]
def visit(index,parent):
    n=g['nodes'][index];m=parent@matrix(n)
    if 'mesh'in n:
        for prim in g['meshes'][n['mesh']]['primitives']:
            a=g['accessors'][prim['attributes']['POSITION']]
            for x in (a['min'][0],a['max'][0]):
                for y in (a['min'][1],a['max'][1]):
                    for z in (a['min'][2],a['max'][2]):pts.append((m@np.array([x,y,z,1]))[:3])
    for ch in n.get('children',[]):visit(ch,m)
for n in g['scenes'][g.get('scene',0)]['nodes']:visit(n,np.eye(4))
v=np.array(pts);bounds=v.max(0)-v.min(0)
assert np.allclose(bounds,[.255,.078,.324],atol=1e-5),bounds
animations=g.get('animations',[]);assert len(animations)==1
targets={g['nodes'][c['target']['node']].get('name') for c in animations[0]['channels']}
assert 'RIG_도어_Y축회전_하강' in targets and 'R3_lock_bolt_4p3mm' in targets
for path in (ROOT/'STL_print').glob('*.stl'):
    b=path.read_bytes();count=struct.unpack_from('<I',b,80)[0];assert len(b)==84+50*count
out={'scale_ok':True,'closed_size_metres_X_Yup_Z':[float(x) for x in bounds],
     'animation_count':len(animations),'animation_targets':sorted(targets),
     'stl_units':'millimetres','print_stl_count':len(list((ROOT/'STL_print').glob('*.stl'))),
     'fabrication_reference_stl_count':len(list((ROOT/'fabrication').glob('*.stl')))}
(ROOT/'validation/exports.json').write_text(json.dumps(out,ensure_ascii=False,indent=2),encoding='utf8')
print(json.dumps(out,ensure_ascii=False,indent=2))
