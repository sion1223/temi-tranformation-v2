"""Check evaluated mesh movement, motor containment, roller contact and exports.
Surface intersections are logged separately from deliberate assembly contacts.
"""
import bpy,json,math
from pathlib import Path
from mathutils import Vector
from mathutils.bvhtree import BVHTree
ROOT=Path(__file__).resolve().parent
bpy.ops.wm.open_mainfile(filepath=str(ROOT/'smart_lock_box_r3_lift.blend'))
sc=bpy.context.scene
def bounds(o):
    pts=[o.matrix_world@Vector(p) for p in o.bound_box]
    return [[min(p[i] for p in pts) for i in range(3)],[max(p[i] for p in pts) for i in range(3)]]
def tree(o):
    e=o.evaluated_get(bpy.context.evaluated_depsgraph_get());m=e.to_mesh()
    verts=[e.matrix_world@v.co for v in m.vertices];faces=[tuple(p.vertices) for p in m.polygons]
    t=BVHTree.FromPolygons(verts,faces,all_triangles=False,epsilon=.0001);e.to_mesh_clear();return t
def overlap(a,b):return all(min(a[1][i],b[1][i])-max(a[0][i],b[0][i])>.02 for i in range(3))
errors=[];pairs={};samples=[]
motor_names=['L_PQ12_BODY','L_PQ12_rear_eye','L_PQ12_rod','L_PQ12_rod_eye','L_PQ12_link_clevis','L_output_link']
for f in range(1,241):
    sc.frame_set(f);bpy.context.view_layer.update()
    for n in motor_names:
        b=bounds(bpy.data.objects[n])
        if any(b[0][i]<[22,6,6][i]-.01 or b[1][i]>[243,318,41][i]+.01 for i in range(3)):errors.append([f,'motor outside protected bay',n,b])
    b=bounds(bpy.data.objects['R3_single_weighing_tray'])
    if max(abs(b[1][0]-237.5),abs(b[0][0]-24),abs(b[1][1]-316),abs(b[0][1]-8))>.01:errors.append([f,'tray XY moved'])
    s=bpy.data.objects['SLIDER_Y_18mm'].location.y;z=bpy.data.objects['LIFT_VERTICAL_27mm'].location.z
    if abs(z-1.5*s)>.005:errors.append([f,'lift synchronization',s,z])
    if z>.01 and not 72<=f<=168:errors.append([f,'lift before door stowed'])
    if f in [1,72,108,168,240]:samples.append({'frame':f,'tray_bounds':b,'slider_y':s,'lift_z':z})
    objects=[o for o in sc.objects if o.type=='MESH' and o.get('kind') not in ['reference','annotation']]
    bb={o.name:bounds(o) for o in objects};trees={}
    for i,a in enumerate(objects):
        for b in objects[i+1:]:
            # Inspect cross-system interference; fasteners and intentionally joined
            # co-moving parts are not clearance pairs.
            if a.parent==b.parent and a.parent is not None:continue
            if not (a.parent is not None or b.parent is not None):continue
            if not overlap(bb[a.name],bb[b.name]):continue
            if a.name not in trees:trees[a.name]=tree(a)
            if b.name not in trees:trees[b.name]=tree(b)
            if trees[a.name].overlap(trees[b.name]):pairs.setdefault('|'.join(sorted([a.name,b.name])),[]).append(f)
P=json.loads((ROOT/'parameters.json').read_text())
contact=[]
for s in [i/10 for i in range(181)]:
    z=29+1.5*s;surface=P['ramp_surface_at_roller_y_mm']+1.5*s
    gap=(z-surface)/math.sqrt(3.25)-5
    contact.append(abs(gap))
assert max(contact)<1e-9
# Envelope math plus nominal force reserve, not measured hardware performance.
mass={'paper_kg':.5,'tray_kg':.2135*.308*.003*2700,'load_cell_kg_allowance':.25,'cradle_and_fasteners_kg_allowance':.35,'skirt_and_harness_kg_allowance':.1}
weight=sum(mass.values())*9.81
ideal=weight*1.5;design=ideal*1.25
expected={
 'L_MGN7C_100|L_MGN7_200_rail_100':'Purchased bearing carriage envelopes overlap the rail they run on.',
 'L_MGN7C_178|L_MGN7_200_rail_178':'Purchased bearing carriage envelopes overlap the rail they run on.',
 'L_PQ12_rod|L_PQ12_rod_eye':'Rod and rod-end eye are joined subshapes of the same purchased actuator.',
 'L_PQ12_M3_pin_248|L_PQ12_rod':'Clevis cross-pin at the rod-end mounting joint.'}
unexpected={k:v for k,v in pairs.items() if k not in expected}
result={'revision':'R3-LIFT','frames_checked':240,'surface_intersection_poses':240,'errors':errors,'samples':samples,'motor_containment_pass':not errors,'roller_normal_gap_max_mm':max(contact),'cross_system_surface_intersections':pairs,'expected_contact_reasons':expected,'unexpected_intersections':unexpected,'dynamic_mesh_check_pass':not errors and not unexpected,'mass_budget':mass,'ideal_horizontal_force_N':ideal,'design_force_with_25pct_loss_N':design,'actuator_max_N':50,'force_margin':50/design,'backdrive_N':35,'static_hold_margin':35/ideal,'physical_tested':False,'scope':'Cross-system surface intersections for moving meshes; same-parent joints and static/static pairs excluded. AABB envelopes plus surface crossing cannot detect every fully contained solid. No FEA, tolerance stack, wear, harness flex or hardware verification.'}
(ROOT/'validation/geometry.json').write_text(json.dumps(result,ensure_ascii=False,indent=2),encoding='utf8')
print(json.dumps({'errors':errors,'intersection_pair_count':len(pairs),'force_N':design,'margin':50/design},ensure_ascii=False))
assert result['dynamic_mesh_check_pass'],unexpected
