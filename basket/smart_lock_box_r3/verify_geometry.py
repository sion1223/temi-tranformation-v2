"""Independent sampled geometry audit. Does not simulate force, friction or elasticity."""
import bpy,bmesh,math,json,sys,itertools
from pathlib import Path
from mathutils import Matrix,Vector
from mathutils.bvhtree import BVHTree
ROOT=Path(__file__).resolve().parent
bpy.ops.wm.open_mainfile(filepath=str(ROOT/'smart_lock_box_r3.blend'))
sc=bpy.context.scene;sc.frame_set(1);bpy.context.view_layer.update()
objects=[o for o in sc.objects if o.type=='MESH' and o.get('kind')!='annotation']
moving=[o for o in objects if o.get('moving_with_door')]
fixed=[o for o in objects if o not in moving]
bolt=bpy.data.objects['R3_lock_bolt_4p3mm'];keeper=bpy.data.objects['R3_door_keeper']
def geom(o,M=None):
    mat=o.matrix_world if M is None else M@o.matrix_world
    vs=[mat@v.co for v in o.data.vertices]
    bvh=BVHTree.FromPolygons(vs,[list(p.vertices) for p in o.data.polygons])
    return (bvh,vs,tuple(min(p[i] for p in vs) for i in range(3)),tuple(max(p[i] for p in vs) for i in range(3)))
def aabb(a,b,eps=1e-5):return all(min(a[3][i],b[3][i])-max(a[2][i],b[2][i])>eps for i in range(3))
def inside(p,g):
    # Odd/even ray crossings, offset ray to avoid vertices. Used only after no surface hit.
    ray=Vector((1,.371,.219)).normalized();pos=p.copy();n=0
    for _ in range(128):
        h,_,_,_=g[0].ray_cast(pos,ray,10000)
        if h is None:break
        n+=1;pos=h+ray*.0001
    return n%2==1
def intersects(a,b):
    if not aabb(a,b):return False
    if a[0].overlap(b[0]):return True
    return inside(a[1][0],b) or inside(b[1][0],a)
def pose(deg,z=74):
    return Matrix.Translation((9,0,z))@Matrix.Rotation(math.radians(-deg),4,'Y')@Matrix.Translation((-9,0,-74))
fg={o.name:geom(o,Matrix.Translation((-4.3,0,0)) if o==bolt else None) for o in fixed}
poses=[('rotate',i*.05,pose(i*.05)) for i in range(1,21)]
poses += [('rotate',i*.25,pose(i*.25)) for i in range(5,361)]
poses += [('slide',74-i*.25,pose(90,74-i*.25)) for i in range(1,256)]
poses += [('slide',10.2,pose(90,10.2))]
hits={}
for phase,value,M in poses:
    for o in moving:
        g=geom(o,M)
        for name,f in fg.items():
            if intersects(g,f):
                key=o.name+' / '+name
                hits.setdefault(key,{'first':[phase,round(value,4)],'count':0})['count']+=1
locked=[]
bg=geom(bolt)
for i in range(1,241):
    a=i*.05
    if intersects(geom(keeper,pose(a)),bg):locked.append(round(a,3))
# Inspect actual animation, including the separate slug track and closing sequence.
anim_hits={}
for f in range(1,145):
    sc.frame_set(f);bpy.context.view_layer.update()
    fk={o.name:geom(o) for o in fixed}
    for o in moving:
        g=geom(o)
        for name,other in fk.items():
            if intersects(g,other):anim_hits.setdefault(o.name+' / '+name,[]).append(f)
sc.frame_set(1);bpy.context.view_layer.update()
# Entire weighing assembly is isolated from the fixed deck at rest and at 0.6 mm deflection.
measuring=[o for o in objects if o.get('measuring_part')]
scale_hits={}
scale_fixed=[o for o in fixed if o not in measuring and o.name!='R3_L6D_3kg_envelope']
for dz in (0,-.3,-.6):
    for o in measuring:
        a=geom(o,Matrix.Translation((0,0,dz)))
        for other in scale_fixed:
            if intersects(a,geom(other)):scale_hits.setdefault(o.name+' / '+other.name,[]).append(dz)
# Tolerance study at the latch only: ±0.3 mm keeper placement in all three axes.
tol=[]
for dx,dy,dz in itertools.product((-.3,0,.3),repeat=3):
    T=Matrix.Translation((dx,dy,dz));contact=None
    for i in range(1,61):
        a=i*.05
        if intersects(geom(keeper,pose(a)@T),geom(bolt)):contact=round(a,3);break
    released=not intersects(geom(keeper,T),geom(bolt,Matrix.Translation((-4.3,0,0))))
    tol.append({'offset_mm':[dx,dy,dz],'first_contact_deg':contact,'released_at_rest':released})
nonmanifold=[]
for o in objects:
    if o.get('kind') not in ('print','sheet_fabrication','metal_fabrication','machined_part'):continue
    bm=bmesh.new();bm.from_mesh(o.data);n=sum(not e.is_manifold for e in bm.edges);bm.free()
    if n:nonmanifold.append([o.name,n])
stow=[pose(90,10.2)@p for o in moving for p in geom(o)[1]]
tray=geom(bpy.data.objects['R3_single_weighing_tray'])
out={
 'scope':'Sampled surface intersections plus containment. No contact-force, stiffness, thermal, printed-part tolerances or actual robot mounting simulation.',
 'sampled_release_poses':len(poses),'moving_meshes':len(moving),'fixed_meshes':len(fixed),
 'released_motion_intersections':hits,'actual_animation_frames':144,'animation_intersections':anim_hits,
 'locked_first_contact_deg':locked[0] if locked else None,'latch_tolerance_cases':tol,
 'weighing_assembly_intersections_0_to_0p6mm':scale_hits,
 'fabrication_nonmanifold':nonmanifold,
 'tray_size_mm':[tray[3][i]-tray[2][i] for i in range(3)],
 'a4_210x297_fits':tray[3][0]-tray[2][0]>=210 and tray[3][1]-tray[2][1]>=297,
 'closed_storage_depth_mm':70-tray[3][2],
 'stowed_top_mm':max(p.z for p in stow),'stowed_protrusion_above_78_mm':max(p.z for p in stow)-78,
 'excluded':'wires and compliant switch levers, threaded interference inside purchased load cell, actual load cell deflection shape',
 'hardware_tests_done':False,
}
out['geometry_pass']=not hits and not anim_hits and not scale_hits and not nonmanifold and bool(locked) and all(t['released_at_rest'] and t['first_contact_deg'] is not None for t in tol)
(ROOT/'validation/geometry.json').write_text(json.dumps(out,ensure_ascii=False,indent=2),encoding='utf8')
print(json.dumps({k:v for k,v in out.items() if k!='latch_tolerance_cases'},ensure_ascii=False,indent=2))
if not out['geometry_pass']:sys.exit(2)
