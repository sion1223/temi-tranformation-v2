"""Build the R4 motorised revision from the exact preserved R3 assembly.

blender -b --python basket/smart_lock_box_r4/build_r4.py
All model coordinates and STL coordinates are millimetres. GLB is metres.
Motor, bearing, spring, damper and screw threads are procurement envelopes.
"""
import bpy
import bmesh
import math
import json
import sys
import hashlib
from pathlib import Path
from mathutils import Matrix, Vector

ROOT = Path(__file__).resolve().parent
sys.path.insert(0, str(ROOT))
from kinematics import P, pose, engineering_summary
SOURCE = ROOT.parent / 'smart_lock_box_r3' / 'smart_lock_box_r3.blend'
bpy.ops.wm.open_mainfile(filepath=str(SOURCE))
sc = bpy.context.scene
sc.frame_set(1)
bpy.context.view_layer.update()
for o in list(sc.objects):
    o.animation_data_clear()
    if o.type in ('FONT', 'CAMERA', 'LIGHT') or o.get('kind') == 'annotation':
        bpy.data.objects.remove(o, do_unlink=True)
for a in list(bpy.data.actions):
    if a.users == 0:
        bpy.data.actions.remove(a)

COLORS = {'steel':(.40,.49,.56,1), 'tray':(.12,.55,.62,1),
          'motor':(.045,.075,.11,1), 'drive':(1,.47,.08,1),
          'frame':(.64,.72,.78,1), 'brass':(.66,.45,.11,1),
          'sensor':(.40,.21,.65,1), 'paper':(.94,.94,.88,1)}

def coll(name):
    c = bpy.data.collections.get(name)
    if c is None:
        c=bpy.data.collections.new(name); sc.collection.children.link(c)
    return c

def finish(o,name,kind='metal_fabrication',color='steel',group='10_R4_structure'):
    o.name=name
    for c in list(o.users_collection): c.objects.unlink(o)
    coll(group).objects.link(o)
    o['kind']=kind; o['revision']='R4'; o.color=COLORS[color]
    return o

def box(name,lo,hi,**kw):
    bpy.ops.mesh.primitive_cube_add(size=1,location=tuple((a+b)/2 for a,b in zip(lo,hi)))
    o=bpy.context.object; o.dimensions=tuple(b-a for a,b in zip(lo,hi))
    bpy.ops.object.transform_apply(location=False,rotation=False,scale=True)
    return finish(o,name,**kw)

def cyl(name,p,r,h,axis='Z',**kw):
    idx={'X':0,'Y':1,'Z':2}[axis]; q=list(p); q[idx]+=h/2
    bpy.ops.mesh.primitive_cylinder_add(vertices=40,radius=r,depth=h,location=q)
    o=bpy.context.object
    if axis=='Y':o.rotation_euler.x=math.pi/2
    if axis=='X':o.rotation_euler.y=math.pi/2
    bpy.ops.object.transform_apply(location=False,rotation=True,scale=True)
    return finish(o,name,**kw)

def boolean(a,b,op='DIFFERENCE'):
    bpy.context.view_layer.objects.active=a
    mod=a.modifiers.new('R4 machining','BOOLEAN');mod.operation=op;mod.solver='EXACT';mod.object=b
    bpy.ops.object.modifier_apply(modifier=mod.name)
    bpy.data.objects.remove(b,do_unlink=True)

def hole(a,p,r,h,axis='Z'):
    boolean(a,cyl('CUT',p,r,h,axis))

def cut(a,lo,hi):boolean(a,box('CUT',lo,hi))

def erase(name):
    o=bpy.data.objects.get(name)
    if o:bpy.data.objects.remove(o,do_unlink=True)

def parent(o,rig):
    bpy.context.view_layer.update();m=o.matrix_world.copy();o.parent=rig;o.matrix_world=m

def empty(name,loc):
    o=bpy.data.objects.new(name,None);coll('13_R4_motion').objects.link(o);o.location=loc
    return o

def ring(name,p,outer,inner,h,axis='Z',**kw):
    o=cyl(name,p,outer,h,axis,**kw)
    q=list(p);q[{'X':0,'Y':1,'Z':2}[axis]]-=1
    hole(o,q,inner,h+2,axis)
    return o

def line(name,points,r=.6,color='drive',group='14_R4_routes'):
    d=bpy.data.curves.new(name,'CURVE');d.dimensions='3D';d.bevel_depth=r;d.bevel_resolution=1
    s=d.splines.new('POLY');s.points.add(len(points)-1)
    for pt,co in zip(s.points,points):pt.co=(*co,1)
    o=bpy.data.objects.new(name,d);coll(group).objects.link(o)
    o['kind']='route_envelope';o.color=COLORS[color];return o

def changed(o):o['revision']='R4';return o

lidrig=bpy.data.objects['RIG_도어_Y축회전_하강']
lidrig.name='R4_LID_FIXED_HINGE'
lidrig.location=(9,0,74);lidrig.rotation_euler=(0,0,0)
lid=bpy.data.objects['도어_본체'];changed(lid)
lid['material']='8 mm machined polymer, 1.0 kg total lid mass budget incl. hardware'
for name in ('도어_숄더볼트_앞','도어_숄더볼트_뒤','도어_격납자석','R3_D4_stowed_reed'):
    erase(name)

# Fixed 8 mm hinge replaces the old manual rotating/sliding shoulder-bolt joint.
for name,y in [('도어_가이드판_앞',6),('도어_가이드판_뒤',312)]:
    erase(name)
    o=box('R4_fixed_hinge_plate_'+str(y),(0,y,6),(22,y+6,84))
    hole(o,(9,y-1,74),4.2,8,'Y')
    for z in (20,50):hole(o,(9,y-1,z),1.7,8,'Y')
hole(lid,(9,11,74),4.1,302,'Y')
axle=cyl('R4_lid_8mm_axle',(9,-40,74),4,402,'Y',color='drive')
parent(axle,lidrig);axle['moving_with_door']=True
for y in (-3,319):
    b=ring('R4_lid_bearing_'+str(y),(9,y,74),8,4.1,6,'Y',kind='purchased_envelope')
    b['part']='8 mm bore bearing; choose actual axial retention and housing fit'
for y in (19,293):
    hub=ring('R4_keyed_lid_hub_'+str(y),(9,y,74),9,4.05,8,'Y',color='drive')
    parent(hub,lidrig);hub['moving_with_door']=True
    # Pockets preserve material separation at the clamped hub interface.
    hole(lid,(9,y-.1,74),9.15,8.2,'Y')
    arm=box('R4_lid_hub_arm_'+str(y),(9,y,78.1),(43,y+8,81.1),color='drive')
    parent(arm,lidrig);arm['moving_with_door']=True
    for x in (28,38):
        hole(lid,(x,y+4,74),1.7,8)
        hole(arm,(x,y+4,77),1.7,6)

# M1: feedback servo + 20T/60T timing reduction, behind a removable front guard.
motor=box('R4_M1_XM430_W350_T',(-3,-52,-15.5),(21,-18,31),kind='purchased_envelope',color='motor',group='11_R4_motors')
motor['part']='ROBOTIS XM430-W350-T; 24 x 34 x 46.5 envelope; vendor horn/bracket drawing governs'
cyl('R4_M1_horn',(9,-18,20),5,13,'Y',kind='purchased_envelope',color='drive')
drive=ring('R4_M1_20T_pulley',(9,-14,20),6.366,3,10,'Y',kind='purchased_envelope',color='drive')
driven=ring('R4_lid_60T_pulley',(9,-14,74),19.099,4.05,10,'Y',kind='purchased_envelope',color='drive')
parent(driven,lidrig)
# Pitch-line routing only; procure the timing belt and toothed pulleys.
v=Vector((0,0,-54));v.normalize();n=Vector((-v.z,0,v.x))
pts=[]
for center,radius in [((9,-9,74),19.099),((9,-9,20),6.366)]:
    for sign in (-1,1):pts.append(tuple(Vector(center)+n*radius*sign))
line('R4_M1_belt_run_A',[pts[0],pts[2]],1.2)
line('R4_M1_belt_run_B',[pts[1],pts[3]],1.2)
mount=box('R4_M1_mount',(-18,-57,-20),(27,-53,45))
for lo,hi in [((-18,-57,-20),(27,-17,-16)),((-18,-57,-20),(-14,0,8)),((-18,-6,0),(22,0,8))]:
    boolean(mount,box('BRACKET_WEB',lo,hi),'UNION')
for x in (-3,21):
    for z in (-12,28):hole(mount,(x,-58,z),1.7,6,'Y')
for x in (4,17):hole(mount,(x,-7,4),1.7,8,'Y')
for y in (-38,333):
    s=ring('R4_lid_spring_envelope_'+str(y),(9,y,74),10,5,18,'Y',kind='purchased_envelope',color='brass')
    s['spec']='Two torsion springs together: T(0)=1.50 Nm, T(105deg)=0.20 Nm; qualify real spring and damper.'
damper=ring('R4_lid_rotary_damper',(9,351,74),13,4.2,10,'Y',kind='purchased_envelope',color='motor')
damper['spec']='Hinge rotary damper: size to limit spring-driven opening to <=10 deg/s after loss of power'
stop=box('R4_open_stop_adjuster',(-7,325,91),(2.85,333,101),color='brass')
hole(stop,(-2,324,95),2.2,10,'Y')
stoparm=box('R4_lid_stop_arm',(9,325,73),(29,331,75),color='drive')
parent(stoparm,lidrig);stoparm['moving_with_door']=True
rearhinge=box('R4_rear_hinge_support',(-12,332,6),(25,335,101))
hole(rearhinge,(9,331,74),10.2,5,'Y')
boolean(rearhinge,box('REAR_FOOT',(-12,318,0),(25,335,6)),'UNION')
sensor=box('R4_LID_OPEN_switch',(-1,337,90),(5,344,96),kind='purchased_envelope',color='sensor')
sensor['trigger']='Non-contact 105 degree target behind stop arm; use independent shaft encoder in addition to Hall limit switch'

# Rework the sensor interface: the tray detaches from its weighing cradle during lift.
oldtray=bpy.data.objects['R3_single_weighing_tray'];erase(oldtray.name)
for o in list(sc.objects):
    if o.name.startswith('R3_cell_loaded_M6'):erase(o.name)
spacer=changed(bpy.data.objects['R3_cell_loaded_spacer'])
cut(spacer,(110,140,45),(155,180,49))
cradle=box('R4_weighing_cradle',(50,22,45),(205,302,48),color='steel')
for lo,hi in [((62,34,44),(122,148,49)),((140,34,44),(193,148,49)),
              ((62,173,44),(122,290,49)),((140,173,44),(193,290,49))]:cut(cradle,lo,hi)
for x in (123.25,138.25):
    hole(cradle,(x,160,44),3.2,5)
    hole(cradle,(x,160,46.3),5.5,3)
    cyl('R4_cradle_M6_'+str(x),(x,160,30),3,17.8,kind='purchased_envelope')
cradle['measuring_part']=True
cradle['load_path']='paper tray -> floating cradle -> L6D loaded spacer -> sensor -> base'
tray=box('R4_paper_tray',(24,8,48),(237.5,316,51),color='tray')
tray['material']='3 mm flat aluminium; moving tray mass incl. hardware <=0.7 kg'
trayrig=empty('R4_TRAY_LIFT_TILT',(35,0,43.3));parent(tray,trayrig)
tray['moving_tray']=True
# A continuous low-side wall fused into the floor forms an integral L stop.
# Keep its inner face at X25.2: the original A4 clearance and high edge stay.
lowstop=box('R4_low_paper_lip',(22,12.5,48),(25.2,311.5,66),
            color='tray',kind='print')
boolean(tray,lowstop,'UNION')
tray['material']='PETG prototype: 3 mm floor, integral 3.2 mm low-side wall; physical stiffness not yet tested'
tray['paper_retention']='Integral L stop: 15 mm above paper surface, 299 mm long; A4 stack <=10 mm; no gap under wall'
# Front/rear edge positions and the high edge are unchanged.
for name,lo,hi in [
    ('R4_front_paper_lip',(25.2,8,51),(237.5,9.2,56)),
    ('R4_rear_paper_lip',(25.2,314.8,51),(237.5,316,56))]:
    o=box(name,lo,hi,color='tray');parent(o,trayrig);o['moving_tray']=True
# Floating vertical pickup slots leave the load cell free at the parked position.
for y in (24,296):
    ear=box('R4_low_pickup_ear_'+str(y),(28,y,34),(42,y+5,48),color='tray')
    # Slot centre endpoints z40..43, radius3.3. Shaft radius3 at z41.2 is free.
    cutter=cyl('CUT',(35,y-1,40),3.3,7,'Y')
    boolean(cutter,cyl('CUT2',(35,y-1,43),3.3,7,'Y'),'UNION')
    boolean(cutter,box('CUT3',(31.7,y-1,40),(38.3,y+6,43)),'UNION')
    boolean(ear,cutter)
    parent(ear,trayrig);ear['moving_tray']=True
    ear['interface']='3 mm vertical lost-motion slot, 0.3 mm radial clearance, 2.1 mm pickup travel'
    for x in (29.5,40.5):
        hole(ear,(x,y+2.5,44),1.1,5)
        hole(tray,(x,y+2.5,47),1.1,5)
paper=box('R4_A4_stack_reference',(26,13,51.05),(236,310,61.05),kind='reference_only',color='paper',group='16_R4_reference')
parent(paper,trayrig);paper['moving_tray']=True
paper['spec']='210 x 297 mm, 10 mm maximum stack envelope; not a fabricated part'

# Side lift shafts pass through new protected slots in the walls and deck.
for name in ('R3_fixed_electronics_deck','R3_front_wall','R3_rear_wall',
             'R3_front_top_seal','R3_rear_top_seal','R3_captive_front_panel'):
    o=changed(bpy.data.objects[name])
    for x in (35,218):
        left,right_clearance=(8.5,9.5) if x==35 else (7.4,7.4)
        cut(o,(x-left, -1, 32),(x+right_clearance,325,155))

# The old overhanging seals and reed switch obstructed vertical tray lift.
cut(bpy.data.objects['R3_front_top_seal'],(20.5,7,63),(239,12.1,79))
cut(bpy.data.objects['R3_rear_top_seal'],(20.5,311.9,63),(239,317,79))
for name in ('R3_front_wall','R3_rear_wall'):
    hole(bpy.data.objects[name],(9,-1,74),4.2,326,'Y')
inner=changed(bpy.data.objects['셸_격납슬롯내벽'])
for y in (19,293):hole(inner,(9,y-.2,74),9.2,8.4,'Y')
for o in sc.objects:
    if o.name.startswith('R3_overload_stop') and o.location.y<100:
        o.location.y+=22;changed(o)
reed=changed(bpy.data.objects['R3_D3_closed_reed']);reed.location.x+=3.5
mag=changed(bpy.data.objects['R3_door_magnet']);mag.location.x+=3.5
cut(lid,(237.5,278,69.9),(245,286,72.1))
right=changed(bpy.data.objects['R3_right_wall'])
cut(right,(238,272.5,63.5),(245,289.5,67.5))
erase('R3_D3_wire')
line('R4_D3_wire',[(204,122,23),(247,140,32),(247,279,32),(242,280,65)],.4,color='sensor')

axisrigs={};screws=[]
for tag,x in [('M2_LOW',35),('M3_HIGH',218)]:
    sx=x+14;gx=x+34
    axisrig=empty('R4_'+tag+'_CARRIAGE',(0,0,41.2));axisrigs[tag]=axisrig
    m=box('R4_'+tag+'_17HS13_motor',(sx-21,-43,-36),(sx+21,-1,-2),kind='purchased_envelope',color='motor',group='11_R4_motors')
    m['part']='STEPPERONLINE 17HS13-0404S1; 42x42x34 mm; 0.4 A/phase; 0.26 Nm holding, running torque must be measured'
    cyl('R4_'+tag+'_motor_shaft',(sx,-22,-2),2.5,24,kind='purchased_envelope',color='drive')
    plate=box('R4_'+tag+'_motor_mount',(sx-25,-47,-2),(sx+25,3,2))
    hole(plate,(sx,-22,-3),11.2,6)
    for xx in (-15.5,15.5):
        for yy in (-15.5,15.5):hole(plate,(sx+xx,-22+yy,-3),1.7,6)
    for y in (-22,346):
        screw=cyl('R4_'+tag+'_Tr8x2_'+str(y),(sx,y,22 if y<0 else 5),4,122 if y<0 else 139,kind='purchased_envelope',color='steel')
        screw['lead_mm']=2;screw['axis']=tag;screws.append(screw)
        cyl('R4_'+tag+'_guide_'+str(y),(gx,y,20),3,124,kind='purchased_envelope')
        for z in ((24 if y<0 else 16),144):
            b=box('R4_'+tag+'_bearing_support_'+str(y)+'_'+str(z),(sx-10,y-13,z),(gx+10,y+13,z+7))
            hole(b,(sx,y,z-1),4.15,9)
            hole(b,(gx,y,z-1),3.1,9)
            for xx in (sx-6,gx+6):hole(b,(xx,y,z-1),1.7,9)
        car=box('R4_'+tag+'_slider_'+str(y),(x-8,y-12,33.2),(gx+8,y+12,49.2),color='drive')
        hole(car,(sx,y,32),4.3,19)
        hole(car,(gx,y,32),4.6,19)
        # Horizontal shaft clamps avoid the screw's centreline, preventing a crossed-shaft collision.
        hole(car,(x,y-13,41.2),3.05,26,'Y')
        parent(car,axisrig);car['moving_axis']=tag
        nut=ring('R4_'+tag+'_Tr8_nut_'+str(y),(sx,y,34.2),7,4.1,14,kind='purchased_envelope',color='brass')
        cut(car,(sx-7.2,y-7.2,33),(sx+7.2,y+7.2,49.4))
        parent(nut,axisrig);nut['moving_axis']=tag
        bush=ring('R4_'+tag+'_guide_bush_'+str(y),(gx,y,34.2),4.5,3.05,14,kind='purchased_envelope')
        parent(bush,axisrig);bush['moving_axis']=tag
        pulley=ring('R4_'+tag+'_20T_sync_'+str(y),(sx,y,5),6.366,2.55 if y<0 else 4.05,6,kind='purchased_envelope',color='drive')
        for what,z in [('HOME',27),('UPPER',137)]:
            box('R4_'+tag+'_'+what+'_'+str(y),(gx+10,y-6,z),(gx+17,y+6,z+5),kind='purchased_envelope',color='sensor')
        # Independent front/rear feedback detects a broken or jumped synchronising belt.
        sensor=box('R4_'+tag+'_linear_feedback_'+str(y),(gx+13,y-5,30),(gx+18,y+5,135),kind='purchased_envelope',color='sensor')
        sensor['spec']='100 mm linear position sensor, independent of motor step count'
    shaft=cyl('R4_'+tag+'_6mm_lift_shaft',(x,-12,41.2),3,348,'Y',color='drive')
    parent(shaft,axisrig);shaft['moving_axis']=tag
    if tag=='M3_HIGH':
        for y in (24,296):
            roller=ring('R4_high_roller_'+str(y),(x,y,41.2),6,3.05,5,'Y',kind='purchased_envelope',color='brass')
            parent(roller,axisrig);roller['moving_axis']=tag
    # Two 20T pulleys, 368 mm centres => exactly 776 mm pitch length, GT2 388T.
    for side in (-1,1):line('R4_'+tag+'_sync_belt_'+str(side),[(sx+side*6.366,-22,8),(sx+side*6.366,346,8)],.9)
    coupler=ring('R4_'+tag+'_coupler',(sx,-22,12),6,2.55,11,kind='purchased_envelope',color='drive')
    hole(coupler,(sx,-22,21.8),4.05,2.2)
    # Belt ports remain below all R3 electronics; no motor current in the HX711 cable.
    for name in ('R3_front_wall','R3_rear_wall','R3_captive_front_panel'):
        o=changed(bpy.data.objects[name])
        for xx in (sx-6.366,sx+6.366):cut(o,(xx-1.4,-1,6.8),(xx+1.4,325,9.2))
    rail=box('R4_'+tag+'_rear_base',(sx-12,324,0),(gx+22,364,6))
    for xx in (sx-6,gx+14):hole(rail,(xx,335,-1),2.2,8)
    for y in (-40,362):
        guard=box('R4_'+tag+'_column_guard_'+str(y),(x-12,y,2),(gx+23,y+2,154),kind='sheet_fabrication',color='frame',group='15_R4_guards')
        guard['drawing_note']='Open-side assembly cover; add flexible bellows over vertical arm slots after fitting'

# Motor power board fits in the R3 unused battery reservation.
for name,lo,hi in [
    ('R4_motion_MCU',(96,220,12),(160,255,23)),
    ('R4_M2_current_driver',(164,220,12),(195,252,25)),
    ('R4_M3_current_driver',(199,220,12),(230,252,25)),
    ('R4_protected_power_branches',(98,263,12),(163,290,27))]:
    box(name,lo,hi,kind='purchased_envelope',color='motor',group='12_R4_electronics')
line('R4_motor_supply_route',[(170,18,10),(153,200,10),(176,235,12)],.9)
line('R4_M1_signal_route',[(125,236,23),(85,201,26),(25,15,28),(9,-30,20)],.45,color='sensor')
line('R4_M2_power_route',[(180,230,26),(90,209,30),(72,30,29),(50,-22,-12)],.65)
line('R4_M3_power_route',[(216,230,26),(232,180,29),(234,25,29),(229,-22,-12)],.65)

# Fixed front/rear carrying handles: welded steel, bolted through the base.
# Feet run below the shell and between the motor bodies, so the removable
# electronics panel and the weighing/moving parts carry no handle load.
base=changed(bpy.data.objects['R3_base_255x324'])
hp=P['carry_handles']
handle_group='17_R4_carry_handles'
def cone(name,p,r1,r2,h,**kw):
    bpy.ops.mesh.primitive_cone_add(vertices=40,radius1=r1,radius2=r2,depth=h,
                                  location=(p[0],p[1],p[2]+h/2))
    return finish(bpy.context.object,name,**kw)
for tag,side in [('front',-1),('rear',1)]:
    wall_y=0 if side<0 else 324
    grip_y=wall_y+side*hp['projection_mm']
    x1,x2=hp['leg_x_mm'];z=hp['grip_z_mm'];r=hp['grip_diameter_mm']/2
    handle=cyl('R4_carry_handle_'+tag,(x1,grip_y,z),r,x2-x1,'X',
               color='motor',group=handle_group)
    for x in (x1,x2):
        bpy.ops.mesh.primitive_uv_sphere_add(segments=32,ring_count=16,radius=r,
                                            location=(x,grip_y,z))
        boolean(handle,finish(bpy.context.object,'WELDED_END'),'UNION')
        boolean(handle,cyl('WELDED_LEG',(x,grip_y,-1),5.5,z+1),'UNION')
        y1,y2=sorted((wall_y+side*(hp['projection_mm']+8),wall_y-side*34))
        boolean(handle,box('WELDED_FOOT',(x-6,y1,-4),(x+6,y2,0)),'UNION')
        for offset in hp['base_bolt_insets_mm']:
            y=wall_y-side*offset
            hole(handle,(x,y,-5),2.25,6)
            hole(base,(x,y,-1),2.25,8)
            boolean(base,cone('COUNTERSINK',(x,y,3.9),2.2,4.4,2.2))
            name='R4_handle_'+tag+'_M4_'+str(x)+'_'+str(offset)
            bolt=cyl(name,(x,y,-14),2,17.9,kind='purchased_envelope',group=handle_group)
            boolean(bolt,cone('CSK_HEAD',(x,y,3.85),2,4.15,2.15),'UNION')
            bolt['part']='M4 x 20 countersunk through-bolt; smooth thread/head envelope'
            ring(name+'_washer',(x,y,-5),4.5,2.2,1,kind='purchased_envelope',group=handle_group)
            nut=ring(name+'_locknut',(x,y,-11),4.2,2.1,6,kind='purchased_envelope',group=handle_group)
            nut['part']='M4 locking nut clearance envelope: OD8.4 x 6 mm maximum; select actual retained fastener'
    handle['material']='Welded steel: diameter 14 grip, diameter 11 legs, 4 mm feet; deburr all edges'
    handle['load_path']='Grip -> welded legs and under-base feet -> four M4 through-bolts -> fixed 6 mm base'
    handle['use']='Use both handles together with lid closed and tray parked; not a robot lifting point'
    handle['hardware_tested']=False
sc['carry_handles']='Two fixed steel carrying handles; front/rear; independent of lid, lift and load cell'
# Exact booleans leave coincident seam vertices around countersinks and welds.
# Join only sub-micron duplicates; preserve the designed clearances and shape.
for o in [base]+[o for o in sc.objects if o.type=='MESH' and
                o.name.startswith(('R4_carry_handle_','R4_handle_'))]:
    bm=bmesh.new();bm.from_mesh(o.data)
    bmesh.ops.remove_doubles(bm,verts=list(bm.verts),dist=.0001)
    bmesh.ops.dissolve_degenerate(bm,edges=list(bm.edges),dist=.0001)
    bmesh.ops.recalc_face_normals(bm,faces=list(bm.faces))
    bm.to_mesh(o.data);bm.free();o.data.update()

# Animate the coupled mechanisms and lock bolt. Do not use elapsed time as a real limit sensor.
bolt=bpy.data.objects['R3_lock_bolt_4p3mm'];bolt_rest=bolt.location.copy()
sc.timeline_markers.clear()
for name,f in [('LOCKED',1),('RELEASED',13),('LID_105',265),('PICKUP',289),
               ('RISING_AND_TILTING',505),('TILT_12',745),('CLOSE_REQUEST',865),('LOWERING_AND_LEVELING',1105),
               ('SEATED',1321),('LIFTERS_PARKED',1345),('CLOSE_LID',1369),
               ('PRELATCH_6',1601),('RELEASE_TO_CLOSE',1613),('CLOSED',1632),('RELOCKED',1644)]:
    sc.timeline_markers.new(name,frame=f)
frames=set(range(1,P['frame_end']+1,4))
frames.update(m.frame for m in sc.timeline_markers)
frames.update((35,47,P['frame_end']))
for f in sorted(frames):
    q=pose(f)
    lidrig.location=(9,0,74);lidrig.rotation_euler=(0,math.radians(-q['lid_deg']),0)
    lidrig.keyframe_insert(data_path='rotation_euler',frame=f)
    trayrig.location=(35,0,43.3+q['lift_mm']);trayrig.rotation_euler=(0,math.radians(-q['tilt_deg']),0)
    trayrig.keyframe_insert(data_path='location',frame=f);trayrig.keyframe_insert(data_path='rotation_euler',frame=f)
    for tag,key in [('M2_LOW','low_z'),('M3_HIGH','high_z')]:
        axisrigs[tag].location.z=q[key];axisrigs[tag].keyframe_insert(data_path='location',frame=f)
    for o in screws:
        key='low_z' if o['axis']=='M2_LOW' else 'high_z'
        o.rotation_euler.z=(q[key]-41.2)*math.pi # 2 mm/rev
        o.keyframe_insert(data_path='rotation_euler',frame=f)
    bolt.location=bolt_rest+Vector((q['bolt_dx'],0,0));bolt.keyframe_insert(data_path='location',frame=f)
for action in bpy.data.actions:
    for layer in action.layers:
        for strip in layer.strips:
            for bag in strip.channelbags:
                for fc in bag.fcurves:
                    for k in fc.keyframe_points:k.interpolation='LINEAR'

materials={}
for o in sc.objects:
    if o.type not in ('MESH','CURVE'):continue
    key=tuple(round(v,3) for v in o.color)
    if key not in materials:
        mat=bpy.data.materials.new('R4_material_'+str(len(materials)));mat.diffuse_color=key;mat.use_nodes=True
        node=mat.node_tree.nodes.get('Principled BSDF');node.inputs['Base Color'].default_value=key
        node.inputs['Roughness'].default_value=.42
        materials[key]=mat
    o.data.materials.clear();o.data.materials.append(materials[key])

sc.frame_start=1;sc.frame_end=P['frame_end'];sc.render.fps=P['fps'];sc.frame_set(1)
sc.unit_settings.system='METRIC';sc.unit_settings.scale_length=.001
sc['revision']='R4-INCLINED-LIFT / simultaneous rise and tilt / reinforced low stop'
sc['restored_from']='fa6051f (last R4 with carry handles); main R3-LIFT is preserved'
sc['hardware_tested']=False
sc['source_blend_sha256']=hashlib.sha256(SOURCE.read_bytes()).hexdigest()
sc['instructions']='1 closed, 745 raised tilted. Space plays full automatic reference cycle. Frame 1 for fabrication exports.'
sc['orientation']='Front is Y=0. Lid at low X. Low paper edge at low X; high edge at high X.'
sc['not_drop_in']='Fixed hinge replaces manual pocket slide. Front/rear modules extend outside original shell.'
for filename in ('parameters.json','kinematics.py','build_r4.py'):
    tx=bpy.data.texts.get(filename) or bpy.data.texts.new(filename);tx.clear();tx.write((ROOT/filename).read_text())
for screen in bpy.data.screens:
    for area in screen.areas:
        if area.type=='VIEW_3D':
            area.spaces.active.clip_end=10000;area.spaces.active.region_3d.view_distance=550
            area.spaces.active.region_3d.view_location=(125,160,90)
sc.render.engine='BLENDER_WORKBENCH';sc.display.shading.color_type='MATERIAL'
parts=ROOT/'parts';parts.mkdir(exist_ok=True)
for prior in parts.glob('*.stl'):prior.unlink()
for o in sc.objects:
    if o.type=='MESH' and o.get('revision')=='R4' and o.get('kind') in ('metal_fabrication','sheet_fabrication','print','machined_part'):
        bpy.ops.object.select_all(action='DESELECT');o.select_set(True);bpy.context.view_layer.objects.active=o
        name=o.name if o.name.isascii() else ('R4_modified_lid' if o==lid else 'R4_modified_slot_inner_wall')
        bpy.ops.wm.stl_export(filepath=str(parts/(name+'.stl')),export_selected_objects=True,use_scene_unit=False)
sc.frame_set(745);bpy.context.view_layer.update()
bpy.ops.wm.save_as_mainfile(filepath=str(ROOT/'smart_lock_box_r4.blend'),compress=True)

# Temporary metric root for standard GLB viewers; Blender scene remains in mm.
root=empty('R4_EXPORT_METRES',(0,0,0));tops=[o for o in sc.objects if o.parent is None and o!=root]
for o in tops:o.parent=root
root.scale=(.001,.001,.001);sc.unit_settings.scale_length=1
bpy.ops.object.select_all(action='SELECT')
bpy.ops.export_scene.gltf(filepath=str(ROOT/'smart_lock_box_r4.glb'),export_format='GLB',use_selection=True,export_animations=True,export_yup=True,export_extras=True,export_animation_mode='SCENE')
from merge_cycle import merge
merge(ROOT/'smart_lock_box_r4.glb')
root.scale=(1,1,1)
for o in tops:o.parent=None
erase(root.name);sc.unit_settings.scale_length=.001
sc.frame_set(1);bpy.context.view_layer.update()
inventory=[]
for o in sc.objects:
    if o.type!='MESH':continue
    ps=[o.matrix_world@Vector(p) for p in o.bound_box]
    inventory.append(dict(name=o.name,kind=o.get('kind','reference'),revision=o.get('revision','R3'),
        min_mm=[min(p[i] for p in ps) for i in range(3)],max_mm=[max(p[i] for p in ps) for i in range(3)]))
(ROOT/'object_inventory.json').write_text(json.dumps(inventory,ensure_ascii=False,indent=2))
(ROOT/'engineering_summary.json').write_text(json.dumps(engineering_summary(),indent=2))
print('R4_BUILD_COMPLETE',len(inventory),'meshes')
