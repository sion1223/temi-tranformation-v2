"""Verify GLB units/animation, CSV links and source preservation; package outputs."""
from pathlib import Path
import json,struct,csv,hashlib,zipfile,itertools
import numpy as np
ROOT=Path(__file__).resolve().parent
p=json.loads((ROOT/'parameters.json').read_text())
assert hashlib.sha256((ROOT.parent/'smart_lock_box_r3/smart_lock_box_r3.blend').read_bytes()).hexdigest()==p['source_sha256']
data=(ROOT/'smart_lock_box_r3_lift.glb').read_bytes();assert data[:4]==b'glTF'
n=struct.unpack_from('<I',data,12)[0];g=json.loads(data[20:20+n])
assert len(g['animations'])==1
channels=g['animations'][0]['channels'];targets=[(c['target']['node'],c['target']['path']) for c in channels]
assert len(targets)==len(set(targets)), 'Duplicate animation tracks'
names={x['name']:i for i,x in enumerate(g['nodes']) if 'name' in x}
for name in ['LIFT_VERTICAL_27mm','SLIDER_Y_18mm','RIG_도어_Y축회전_하강']:
    assert name in names and any(t[0]==names[name] for t in targets),name
def transform(node):
    if 'matrix'in node:return np.array(node['matrix']).reshape(4,4).T
    x,y,z,w=node.get('rotation',[0,0,0,1])
    r=np.array([[1-2*(y*y+z*z),2*(x*y-z*w),2*(x*z+y*w)],[2*(x*y+z*w),1-2*(x*x+z*z),2*(y*z-x*w)],[2*(x*z-y*w),2*(y*z+x*w),1-2*(x*x+y*y)]])
    m=np.eye(4);m[:3,:3]=r@np.diag(node.get('scale',[1,1,1]));m[:3,3]=node.get('translation',[0,0,0]);return m
pts=[]
def visit(i,parent):
    node=g['nodes'][i];m=parent@transform(node)
    if 'mesh' in node:
        for prim in g['meshes'][node['mesh']]['primitives']:
            acc=g['accessors'][prim['attributes']['POSITION']]
            for xyz in itertools.product(*zip(acc['min'],acc['max'])):pts.append((m@np.array([*xyz,1]))[:3])
    for c in node.get('children',[]):visit(c,m)
for i in g['scenes'][g.get('scene',0)]['nodes']:visit(i,np.eye(4))
pts=np.array(pts);size=pts.max(axis=0)-pts.min(axis=0)
assert np.max(np.abs(size-np.array([.255,.108,.324])))<.0005,size
with (ROOT/'BOM.csv').open(encoding='utf-8-sig') as f:rows=list(csv.DictReader(f))
assert len(rows)>=30 and all(r['purchase_url'].startswith('https://') and r['order_option'] for r in rows)
geo=json.loads((ROOT/'validation/geometry.json').read_text(encoding='utf8'))
assert geo['dynamic_mesh_check_pass'] and geo['frames_checked']==240
from PIL import Image
images=list((ROOT/'drawings').glob('*.png'))
assert len(images)==6
assert 'RENDERS_COMPLETE' in (ROOT/'validation/render.log').read_text(encoding='utf8',errors='replace'), 'Wait for the latest render process to finish'
for path in images:
    with Image.open(path) as im:assert im.size==(1440,1080)
report={'source_preserved':True,'glb_animation_count':1,'glb_unique_targets':len(targets),'closed_glb_size_metres_xyz':size.tolist(),'bom_rows':len(rows),'drawings':len(images),'motion_frames_checked':240,'dynamic_mesh_check_pass':True,'hardware_tested':False}
(ROOT/'validation/package.json').write_text(json.dumps(report,indent=2),encoding='utf8')
exclude={'inspect_source.py','source_inspection.json','review_scene.blend','validation_build.log'}
files=[x for x in ROOT.rglob('*') if x.is_file() and x.name not in exclude and x.suffix not in ['.blend1','.pdf','.log'] and '__pycache__' not in x.parts and not (x.parent.name=='sources' and x.suffix=='.png') and x.name!='package_manifest.json']
manifest={str(x.relative_to(ROOT)).replace('\\','/'):hashlib.sha256(x.read_bytes()).hexdigest() for x in files}
(ROOT/'package_manifest.json').write_text(json.dumps(manifest,ensure_ascii=False,indent=2),encoding='utf8')
with zipfile.ZipFile(ROOT.parent/'smart_lock_box_r3_lift_package.zip','w',zipfile.ZIP_DEFLATED) as z:
    for x in files+[ROOT/'package_manifest.json']:z.write(x,Path(ROOT.name)/x.relative_to(ROOT))
print(json.dumps(report,indent=2))
