"""Sample actual Blender meshes and independent contact/sequence invariants.
Does not replace contact-force, tolerance, deflection or physical robot tests.
"""
import bpy
import bmesh
import json
import sys
import math
from pathlib import Path
from mathutils import Vector
from mathutils.bvhtree import BVHTree
ROOT=Path(__file__).resolve().parent
sys.path.insert(0,str(ROOT))
from kinematics import pose, P, transform_tray, shaft_heights
bpy.ops.wm.open_mainfile(filepath=str(ROOT/'smart_lock_box_r4.blend'))
sc=bpy.context.scene
def geom(o):
    v=[o.matrix_world@p.co for p in o.data.vertices]
    f=[list(p.vertices) for p in o.data.polygons]
    return (BVHTree.FromPolygons(v,f),v,
            [min(p[i] for p in v) for i in range(3)], [max(p[i] for p in v) for i in range(3)])
def inside(p,g):
    ray=Vector((1,.371,.219)).normalized();n=0;pos=p.copy()
    for _ in range(128):
        h,_,_,_=g[0].ray_cast(pos,ray,10000)
        if h is None:break
        n+=1;pos=h+ray*.0001
    return bool(n%2)
def hit(a,b):
    if any(a[3][i]<=b[2][i]+.02 or b[3][i]<=a[2][i]+.02 for i in range(3)):return False
    return bool(a[0].overlap(b[0])) or inside(a[1][0],b) or inside(b[1][0],a)

def overlap_volume(a,b):
    # Under-base feet intentionally share a face with the base at Z=0.
    # Exact intersection volume distinguishes that seating face from intrusion.
    probe=a.copy();probe.data=a.data.copy();sc.collection.objects.link(probe)
    probe.parent=None;probe.matrix_world=a.matrix_world.copy()
    bpy.context.view_layer.objects.active=probe
    mod=probe.modifiers.new('Static handle interference','BOOLEAN')
    mod.operation='INTERSECT';mod.solver='EXACT';mod.object=b
    bpy.ops.object.modifier_apply(modifier=mod.name)
    bm=bmesh.new();bm.from_mesh(probe.data);volume=abs(bm.calc_volume());bm.free()
    data=probe.data;bpy.data.objects.remove(probe,do_unlink=True);bpy.data.meshes.remove(data)
    return volume

objects=[o for o in sc.objects if o.type=='MESH']
tray=[o for o in objects if o.get('moving_tray') and o.get('kind')!='reference_only']
lid=[o for o in objects if o.parent and o.parent.name=='R4_LID_FIXED_HINGE']
# Fixed shell and pre-existing electronics, plus new motor bodies/posts/covers.
# Guide bushes, clamp shafts, bearings, nuts and tray contact rollers are joint
# interfaces, inspected analytically below rather than counted as collisions.
fixed=[o for o in objects if o not in tray+lid and o.get('kind')!='reference_only'
       and not o.get('moving_axis') and not o.name.startswith('R3_cell_loaded')
       and not o.name.startswith('R4_cradle_M6') and o.name!='R4_weighing_cradle']
fixed=[o for o in fixed if not any(s in o.name for s in
       ('lid_bearing','lid_spring','lid_rotary_damper','lid_60T','lid_hub','open_stop','fixed_hinge_plate'))]
frames=sorted(set(range(1,1645,6))|{1,13,35,47,265,289,505,745,865,1105,1321,1345,1369,1601,1613,1632,1644})
collisions={};invariants=[];axis_collisions={}
axes=[o for o in objects if o.get('moving_axis')]
for f in frames:
    sc.frame_set(f);bpy.context.view_layer.update()
    fg={o.name:geom(o) for o in fixed}
    moving=tray+lid
    for o in moving:
        g=geom(o)
        for other,og in fg.items():
            # Latch contact in fully closed state is expected only with its keeper.
            if o.name=='R3_door_keeper' and other=='R3_lock_bolt_4p3mm' and f in (1,1644):continue
            if hit(g,og):collisions.setdefault(o.name+' / '+other,[]).append(f)
    for a in tray:
        for b in lid:
            if hit(geom(a),geom(b)):collisions.setdefault(a.name+' / '+b.name,[]).append(f)
    for o in axes:
        g=geom(o)
        for other,og in fg.items():
            # A nut surrounds its screw and a bushing surrounds its guide.
            if o['moving_axis'] in other and ('Tr8x2' in other or '_guide_' in other):continue
            if hit(g,og):axis_collisions.setdefault(o.name+' / '+other,[]).append(f)
    q=pose(f)
    if (q['lift_mm']>1e-6 or q['tilt_deg']>1e-6) and q['lid_deg']<104.99:invariants.append([f,'tray moved before lid fully open'])
    if q['lid_deg']<104.99 and q['pickup']>1e-6:invariants.append([f,'lid rotates before lifters park'])
    if abs(q['tilt_deg']/12-q['lift_mm']/35)>1e-6:invariants.append([f,'rise/tilt progress mismatch'])
    # Actual world-space geometry must match the requested direction.
    verts=[bpy.data.objects['R4_paper_tray'].matrix_world@v.co for v in bpy.data.objects['R4_paper_tray'].data.vertices]
    if f==745:
        low=[v.z for v in verts if v.x<30];high=[v.z for v in verts if v.x>228]
        if not (max(low)<min(high) and min(low)>78):invariants.append([f,'wrong slope or low edge still below rim'])
nonmanifold=[]
for o in objects:
    if o.get('revision')!='R4' or o.get('kind') not in ('print','metal_fabrication','sheet_fabrication','machined_part'):continue
    bm=bmesh.new();bm.from_mesh(o.data);n=sum(not e.is_manifold for e in bm.edges);bm.free()
    if n:nonmanifold.append([o.name,n])
sc.frame_set(1);bpy.context.view_layer.update()
# A new carrying assembly must also clear fixed motor bodies, guards, the
# electronics panel and electronics. Mating faces may touch; holes are real.
handles=[o for o in objects if o.name.startswith(('R4_carry_handle_','R4_handle_'))]
handle_static_collisions=[]
parked={o.name:geom(o) for o in objects if o.get('kind')!='reference_only'}
seen=set()
for h in handles:
    if h.parent or h.animation_data:invariants.append([1,h.name+' must stay on the fixed base'])
    for other,g in parked.items():
        pair=tuple(sorted((h.name,other)))
        if other==h.name or pair in seen:continue
        seen.add(pair)
        if hit(parked[h.name],g):
            volume=overlap_volume(h,bpy.data.objects[other])
            if volume>.01:handle_static_collisions.append({'parts':list(pair),'overlap_mm3':volume})
# Count retained R3 mesh objects and compare extents of unmodified objects to inventory.
old=json.loads((ROOT.parent/'smart_lock_box_r3/object_inventory.json').read_text())
retained=[];differences=[]
for r in old:
    o=bpy.data.objects.get(r['name'])
    if o and o.type=='MESH' and o.get('revision')!='R4' and o not in lid:
        g=geom(o);err=max(abs(g[k][j]-r[key][j]) for k,key in [(2,'min_mm'),(3,'max_mm')] for j in range(3))
        retained.append(r['name'])
        if err>.03:differences.append([o.name,err])

analytic=[]
for j in range(121):
    a=j/10;low,high=shaft_heights(35*a/12,a);t=math.radians(a)
    # Radius-6 circle tangency to tray underside, measured normal distance.
    distance=((low-high)*math.cos(t)+183*math.sin(t)+4.7)
    if abs(distance-6)>1e-8:analytic.append(['roller tangency',a,distance])
    if high-41.2>100 or low-41.2>100:analytic.append(['travel limit',a])

# Verify every nominal frame for axis speed and the unchanged final high edge.
max_axis_speed=0
for f in range(290,746):
    q0,q1=pose(f-1),pose(f)
    max_axis_speed=max(max_axis_speed,*[abs(q1[k]-q0[k])*P['fps'] for k in ('low_z','high_z')])
if max_axis_speed>P['stepper_max_speed_mm_s']+1e-5:analytic.append(['axis speed',max_axis_speed])
final_high=transform_tray((237.5,162,51),35,12)
if abs(final_high[2]-127.93385391624656)>1e-6:analytic.append(['original high edge changed',final_high])

report=dict(source_blender_warning='R3 was written by Blender 5.2; opened in 4.5.3. Unmodified mesh extents checked against source inventory; old animations deliberately replaced.',
    sampled_frames=len(frames),collision_pairs=collisions,axis_collision_pairs=axis_collisions,sequence_invariant_failures=invariants,
    fabrication_nonmanifold=nonmanifold,retained_R3_meshes_checked=len(retained),retained_mesh_extent_errors=differences,
    analytical_contact_cases=121,analytical_errors=analytic,max_axis_speed_mm_s=max_axis_speed,
    low_stop={'wall_thickness_mm':3.2,'height_above_tray_mm':15,'floor_thickness_mm':3,'length_mm':299,'joint':'integral union with tray floor; no bottom gap'},
    carry_handle_assembly_meshes=len(handles),carry_handle_static_collisions=handle_static_collisions,
    scope='Nominal sampled mesh intersections for tray/lid vs rigid obstacles; explicit joint-interface exclusions. Not full FEA, tolerances, belt tooth meshing, sensor force, spring/damper proof or robot-mounted validation.',
    excluded_joint_interfaces=['hinge axle/bearings/hubs/springs','lead screw/nut/guide bush/shaft clamps','tray cradle and intentional roller/slot contact','wires/belts are routing envelopes'],
    hardware_tested=False)
report['pass']=not(collisions or axis_collisions or invariants or nonmanifold or differences or analytic or handle_static_collisions)
(ROOT/'validation').mkdir(exist_ok=True)
(ROOT/'validation/geometry.json').write_text(json.dumps(report,ensure_ascii=False,indent=2))
envelopes={}
for name,f in [('closed',1),('open_raised',745)]:
    sc.frame_set(f);bpy.context.view_layer.update()
    pts=[o.matrix_world@p.co for o in objects if o.get('kind') not in ('annotation','reference_only') for p in o.data.vertices]
    lo=[min(p[i] for p in pts) for i in range(3)];hi=[max(p[i] for p in pts) for i in range(3)]
    envelopes[name]={'min_mm':lo,'max_mm':hi,'size_mm':[hi[i]-lo[i] for i in range(3)]}
(ROOT/'validation/envelopes.json').write_text(json.dumps(envelopes,indent=2))
print(json.dumps(report,ensure_ascii=False,indent=2))
if not report['pass']:raise SystemExit(2)
