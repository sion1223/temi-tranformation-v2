"""R3-derived internal-drive paper lift. Blender 4.5+, millimetres.
Purchased components are dimensioned envelopes, not manufacturing copies.
Run from any directory with blender -b --python <this file>.
"""
import bpy, math, json, csv, hashlib, sys
from pathlib import Path
from mathutils import Vector
ROOT=Path(__file__).resolve().parent
SOURCE=ROOT.parent/'smart_lock_box_r3/smart_lock_box_r3.blend'
for d in ['drawings','parts','validation']: (ROOT/d).mkdir(exist_ok=True)
bpy.ops.wm.open_mainfile(filepath=str(SOURCE))
sc=bpy.context.scene; sc.frame_set(1); bpy.context.view_layer.update()
BLUE=(.045,.31,.42,1); SHELL=(.69,.76,.79,1); METAL=(.57,.66,.71,1)
ORANGE=(.96,.36,.045,1); GREEN=(.045,.32,.19,1); BLACK=(.025,.045,.06,1)
WHITE=(.94,.94,.9,1); PURPLE=(.45,.2,.65,1)
def bounds(o):
    p=[o.matrix_world@Vector(c) for c in o.bound_box]
    return [[min(v[i] for v in p) for i in range(3)],[max(v[i] for v in p) for i in range(3)]]
def erase(o):
    if isinstance(o,str):o=bpy.data.objects.get(o)
    if o:bpy.data.objects.remove(o,do_unlink=True)
def group(n):
    c=bpy.data.collections.get(n)
    if not c:c=bpy.data.collections.new(n);sc.collection.children.link(c)
    return c
COL={n:group(n) for n in ['L01_SHELL','L02_DOOR_R3','L03_LIFT_CRADLE','L04_SLIDER','L05_PURCHASED','L06_ELECTRONICS','L07_GUARDS','L08_LABELS']}
def finish(o,n,c,col,kind='custom'):
    o.name=n
    for x in list(o.users_collection):x.objects.unlink(o)
    COL[c].objects.link(o);o.color=col;o['kind']=kind
    return o
def box(n,lo,hi,c='L05_PURCHASED',col=METAL,kind='purchased_envelope'):
    bpy.ops.mesh.primitive_cube_add(size=1,location=tuple((a+b)/2 for a,b in zip(lo,hi)))
    o=bpy.context.object;o.dimensions=tuple(b-a for a,b in zip(lo,hi))
    bpy.ops.object.transform_apply(location=False,rotation=False,scale=True)
    return finish(o,n,c,col,kind)
def cyl(n,p,r,h,axis='Z',c='L05_PURCHASED',col=METAL,kind='purchased_envelope'):
    q=list(p);q['XYZ'.index(axis)]+=h/2
    bpy.ops.mesh.primitive_cylinder_add(vertices=32,radius=r,depth=h,location=q)
    o=bpy.context.object
    if axis=='X':o.rotation_euler.y=math.pi/2
    if axis=='Y':o.rotation_euler.x=math.pi/2
    bpy.ops.object.transform_apply(location=False,rotation=True,scale=True)
    return finish(o,n,c,col,kind)
def subtract(a,b):
    bpy.context.view_layer.objects.active=a;m=a.modifiers.new('Machining','BOOLEAN');m.operation='DIFFERENCE';m.solver='EXACT';m.object=b
    bpy.ops.object.modifier_apply(modifier=m.name);erase(b)
def cutbox(a,lo,hi):subtract(a,box('cut',lo,hi))
def hole(a,p,r,h,axis='Z'):subtract(a,cyl('cut',p,r,h,axis))
def parent(o,p):
    bpy.context.view_layer.update();w=o.matrix_world.copy();o.parent=p;o.matrix_world=w
def empty(n):
    o=bpy.data.objects.new(n,None);sc.collection.objects.link(o);return o
def cable(n,pts,col=PURPLE,r=.5):
    d=bpy.data.curves.new(n,'CURVE');d.dimensions='3D';d.bevel_depth=r;d.bevel_resolution=1
    s=d.splines.new('POLY');s.points.add(len(pts)-1)
    for p,q in zip(s.points,pts):p.co=(*q,1)
    o=bpy.data.objects.new(n,d);COL['L06_ELECTRONICS'].objects.link(o);o.color=col;o['kind']='wire_route';return o
def text(n,s,loc,size=6):
    d=bpy.data.curves.new(n,'FONT');d.body=s;d.size=size
    o=bpy.data.objects.new(n,d);COL['L08_LABELS'].objects.link(o);o.location=loc;o.color=BLACK;o['kind']='annotation';return o

rig=bpy.data.objects['RIG_도어_Y축회전_하강'];rig.animation_data_clear()
keep={'셸_격납슬롯내벽','도어_가이드판_앞','도어_가이드판_뒤','도어_본체','도어_숄더볼트_앞','도어_숄더볼트_뒤',
      'R3_base_255x324','R3_right_wall','R3_single_weighing_tray','R3_L6D_3kg_envelope','R3_cell_loaded_spacer',
      'R3_cell_loaded_M6','R3_cell_loaded_M6.001','R3_door_keeper','R3_lock_base','R3_lock_bolt_4p3mm',
      '잠금_5065_프레임','잠금_5065_코일','잠금_5065_플랜지','R3_keeper_blind_M2p5','R3_keeper_blind_M2p5.001',
      'R3_keeper_inside_head','R3_keeper_inside_head.001'}
original={o.name:bounds(o) for o in sc.objects if o.type=='MESH' and o.name in keep}
for o in list(sc.objects):
    if o!=rig and o.name not in keep:erase(o)
for o in list(sc.objects):
    if o==rig:continue
    o.animation_data_clear()
    if o.parent is None and o.name!='R3_base_255x324':o.location.z+=30
    o['source']='R3 719c642; original mesh reused'
rig.location.z+=30
bpy.context.view_layer.update()
lid=bpy.data.objects['도어_본체'];lid['moving_with_door']=True
base=bpy.data.objects['R3_base_255x324']
# Reuse the R3 guide and lock sweep; new walls only extend downward.
front=box('L_front_wall',(0,0,6),(255,6,108),'L01_SHELL',SHELL,'custom')
cutbox(front,(25,-1,8),(241,7,66))
panel=box('L_captive_service_panel',(25.5,1,8.5),(240.5,5,65.5),'L01_SHELL',SHELL,'custom')
box('L_rear_wall',(0,318,6),(255,324,108),'L01_SHELL',SHELL,'custom')
box('L_right_lower_extension',(243,6,6),(255,318,36),'L01_SHELL',SHELL,'custom')
box('L_right_top_seal',(251.5,6,100),(255,318,108),'L01_SHELL',SHELL,'custom')
box('L_left_outer_wall',(0,12,6),(4,312,108),'L01_SHELL',SHELL,'custom')
box('L_slot_inner_extension',(13.5,12,6),(19.5,312,36),'L01_SHELL',SHELL,'custom')
for y in [6,312]:box('L_guide_lower_support_'+str(y),(0,y,6),(22,y+6,36),'L01_SHELL',SHELL,'custom')
for x in [29,229]:
    ear=box('L_panel_internal_ear_'+str(x),(x-3,4,60),(x+3,20,66),'L01_SHELL',SHELL,'custom')
    hole(ear,(x,16,59),1.4,8)
    cyl('L_panel_M3_'+str(x),(x,16,60),1.5,13)
# Covers sit inside a non-contact moving skirt, so the tray can pass the rim.
deck=box('L_fixed_guard_deck',(30,16,71),(232,308,74),'L07_GUARDS',BLUE,'custom')
cutbox(deck,(110,40,70),(151,176,75))
for x in [84,194]:
    for y in [140,270]:hole(deck,(x,y,70),8,6)
for x in [37,224]:
    for y in [185,300]:
        p=box('L_deck_post_%d_%d'%(x,y),(x-3,y-3,6),(x+3,y+3,71),'L01_SHELL',BLUE,'custom')
        hole(p,(x,y,62),1.4,10);hole(deck,(x,y,70),1.7,5);cyl('L_deck_M3_%d_%d'%(x,y),(x,y,64),1.5,10)
for x in [210,233]:
    for y in [48,72]:box('L_lock_pedestal_%d_%d'%(x,y),(x-2,y-2,6),(x+2,y+2,42),'L01_SHELL',METAL,'custom')

lift=empty('LIFT_VERTICAL_27mm');slide=empty('SLIDER_Y_18mm')
# An aluminium frame lifts the FIXED end of the load cell, not the sensing tray directly.
frameparts=[]
for x in [76,186]:frameparts.append(box('L_cradle_side_'+str(x),(x,40,34),(x+16,290,37),'L03_LIFT_CRADLE',BLUE,'custom'))
frameparts.append(box('L_cradle_front',(92,40,34),(186,68,37),'L03_LIFT_CRADLE',BLUE,'custom'))
frameparts.append(box('L_cradle_rear',(92,280,34),(186,290,37),'L03_LIFT_CRADLE',BLUE,'custom'))
ped=box('L_cell_fixed_pedestal',(112.75,42,37),(148.75,67,44),'L03_LIFT_CRADLE',METAL,'custom');frameparts.append(ped)
for x in [123.25,138.25]:
    hole(ped,(x,54,36),3.2,10);hole(frameparts[2],(x,54,33),3.2,5)
    parent(cyl('L_cell_fixed_M6_'+str(x),(x,54,34),3,22),lift)
for o in frameparts:parent(o,lift)
for n in ['R3_single_weighing_tray','R3_L6D_3kg_envelope','R3_cell_loaded_spacer','R3_cell_loaded_M6','R3_cell_loaded_M6.001']:
    if bpy.data.objects.get(n):parent(bpy.data.objects[n],lift)
tray=bpy.data.objects['R3_single_weighing_tray']
tray['load_path']='Tray > free end L6D > fixed end pedestal > guided cradle > rollers > synchronized slider > base'
for name,lo,hi in [('left',(25,10,44),(25.8,314,78)),('right',(235.7,10,44),(236.5,314,78)),('front',(25.8,10,74),(235.7,10.8,78)),('rear',(25.8,313.2,44),(235.7,314,78))]:
    o=box('L_moving_skirt_'+name,lo,hi,'L07_GUARDS',BLUE,'custom');parent(o,lift);o['weighed_part']=True
    if name=='right':cutbox(o,(235,24,43),(238,108,70))
for x in [80,198]:
    for y in [44,284]:
        stop=cyl('L_overload_stop_%d_%d'%(x,y),(x,y,37),2,40.2,c='L03_LIFT_CRADLE',col=BLACK,kind='custom')
        parent(stop,lift);stop['nominal_gap_mm']=.8;hole(deck,(x,y,70),3.2,6)
# Travel guides: 68 mm cut shafts, 19 mm long LM6UU. Shaft tops remain below down tray.
for x in [84,194]:
    for y in [140,270]:
        shaft=cyl('L_6mm_shaft_%d_%d'%(x,y),(x,y,7),3,68)
        shaft['part']='6 mm hardened smooth rod, cut to 68 mm'
        bearing=cyl('L_LM6UU_%d_%d'%(x,y),(x,y,27),6,19)
        hole(bearing,(x,y,26),3,21);parent(bearing,lift);bearing['part']='RobotDigg LM6UU 6x12x19'
        for f in frameparts:
            if f.name.startswith('L_cradle_side'):hole(f,(x,y,33),6.05,5)
        collar=cyl('L_bearing_holder_%d_%d'%(x,y),(x,y,30),8,13,c='L03_LIFT_CRADLE',col=BLUE,kind='custom')
        hole(collar,(x,y,29),6.05,15);parent(collar,lift)
        # Separate flange plate grips the bushing housing to the frame.
        for dx in [-10,10]:
            tab=box('L_bushing_tab_%d_%d_%d'%(x,y,dx),(x+dx-2,y-3,34),(x+dx+2,y+3,37),'L03_LIFT_CRADLE',BLUE,'custom');parent(tab,lift)
        socket=box('L_shaft_socket_%d_%d'%(x,y),(x-7,y-7,6),(x+7,y+7,17),'L01_SHELL',METAL,'custom')
        hole(socket,(x,y,6.8),3.02,12)
        for dy in [-5,5]:hole(socket,(x,y+dy,5),1.7,13);hole(base,(x,y+dy,-1),1.7,8)
        hole(socket,(x-8,y,12),1.4,16,'X')

SLOPE=1.5;ROLLER_RADIUS=5;ROLLER_Z=29
# The slope uses the normal distance to the roller centre, not a vertical-radius shortcut.
Z0=ROLLER_Z-ROLLER_RADIUS*math.sqrt(1+SLOPE*SLOPE)
def wedge(n,x,y):
    y0=y-24;y1=y+1;z0=Z0+36;z1=Z0-1.5
    verts=[(a,b,z) for a in [x-5,x+5] for b,z in [(y0,18),(y1,18),(y1,z1),(y0,z0)]]
    faces=[(0,3,2,1),(4,5,6,7),(0,1,5,4),(1,2,6,5),(2,3,7,6),(3,0,4,7)]
    mesh=bpy.data.meshes.new(n);mesh.from_pydata(verts,[],faces);mesh.update()
    o=bpy.data.objects.new(n,mesh);COL['L04_SLIDER'].objects.link(o);o.color=ORANGE;o['kind']='custom';o['material']='POM-C CNC ramp, 56.31 degrees';parent(o,slide)
    for yy in [y-19,y-6]:hole(o,(x,yy,17),1.4,5)
    return o
for x in [100,178]:
    rail=box('L_MGN7_200_rail_'+str(x),(x-3.5,78,6),(x+3.5,278,11.2))
    rail['part']='RobotDigg SS_MGN7-200L with MGN7C'
    # Rail mounting pitch 15; end pattern to be matched to delivered rail.
    for y in range(88,269,15):hole(rail,(x,y,5),1.2,8);hole(base,(x,y,-1),.8,8)
    carriage=box('L_MGN7C_'+str(x),(x-8.5,168.75,10),(x+8.5,191.25,14))
    parent(carriage,slide)
    spacer=box('L_slide_spacer_'+str(x),(x-8,169,14),(x+8,191,16),'L04_SLIDER',BLUE,'custom');parent(spacer,slide)
    strip=box('L_slider_bar_'+str(x),(x-8.5,82,16),(x+8.5,280,18),'L04_SLIDER',METAL,'custom');parent(strip,slide)
    for yy in [176,184]:
        for xx in [x-6,x+6]:hole(spacer,(xx,yy,13),1.2,5);hole(strip,(xx,yy,15),1.2,4)
    for y in [110,250]:
        wedge('L_POM_ramp_%d_%d'%(x,y),x,y)
        for yy in [y-19,y-6]:hole(strip,(x,yy,15),1.7,5)
        roller=cyl('L_623ZZ_%d_%d'%(x,y),(x-2,y,ROLLER_Z),5,4,'X');hole(roller,(x-3,y,ROLLER_Z),1.5,6,'X');parent(roller,lift)
        roller['part']='623ZZ 3x10x4 mm'
        if x==100:lo,hi=88,104
        else:lo,hi=174,190
        axle=cyl('L_roller_shoulder_M3_%d_%d'%(x,y),(lo,y,29),1.5,hi-lo,'X');parent(axle,lift)
        bracket=box('L_roller_bracket_%d_%d'%(x,y),(lo if x==100 else 184,y-4,26),(94 if x==100 else hi,y+4,37),'L03_LIFT_CRADLE',BLUE,'custom')
        hole(bracket,(lo-1,y,29),1.55,14,'X');parent(bracket,lift)
cross=box('L_slider_crossbar',(94,268,16),(184,280,18),'L04_SLIDER',METAL,'custom');parent(cross,slide)
# PQ12 Rev D: housing 36.5 x 21.5 x 15, rear/front eye spacing 42 mm + extension.
body=box('L_PQ12_BODY',(114,208,12.5),(135.5,244.5,27.5),'L05_PURCHASED',BLACK)
body['part']='Actuonix PQ12-100-12-S';body['motor']=True
rear=cyl('L_PQ12_rear_eye',(130,205,17.5),3,5);hole(rear,(130,205,16),1.5,8)
rod=box('L_PQ12_rod',(127.1,244.5,17.1),(132.9,248,22.9))
eye=cyl('L_PQ12_rod_eye',(130,248,17.5),2.9,5);hole(eye,(130,248,16),1.5,8);parent(eye,slide)
# Articulated link keeps side loads off the actuator rod.
link=box('L_output_link',(127,249,18),(133,273,20),'L04_SLIDER',BLUE,'custom');parent(link,slide)
hole(link,(130,251,17),1.55,5)
for y in [205,248]:
    pin=cyl('L_PQ12_M3_pin_'+str(y),(130,y,16),1.5,9)
    if y==248:parent(pin,slide)
mount=box('L_PQ12_fixed_mount',(124,200,6),(136,210,16),'L01_SHELL',BLUE,'custom')
hole(mount,(130,205,5),1.55,13)
for x in [126,134]:hole(mount,(x,203,5),1.7,12);hole(base,(x,203,-1),1.7,8)
br=box('L_PQ12_link_clevis',(124,245,14.5),(136,252,24.5),'L04_SLIDER',BLUE,'custom')
cutbox(br,(123,244,17),(137,250,23));hole(br,(130,248,13),1.55,13);parent(br,slide)
for y in [276,]:
    for x in [100,178]:hole(cross,(x,y,15),1.7,5)
# Mechanical upper/lower switches, actuated by the moving frame (not the weighed tray).
def switch(n,x,y,z):
    o=box(n,(x,y,z),(x+12.8,y+5.8,z+6.5),'L06_ELECTRONICS',BLACK)
    o['part']='D2F-01L';box(n+'_lever',(x,y+1,z+6.5),(x+13,y+4.8,z+7),'L06_ELECTRONICS',METAL)
    return o
for x in [58,210]:
    switch('L_DOWN_'+str(x),x,197,26.5);switch('L_UP_'+str(x),x,197,64.5)
    bpy.data.objects['L_UP_'+str(x)+'_lever'].location.z-=7
    tab=box('L_limit_flag_'+str(x),(x+9,193,34),(x+15,201,37),'L03_LIFT_CRADLE',BLUE,'custom');parent(tab,lift)
    # Short bridge from frame to flag.
    bridge=box('L_flag_bridge_'+str(x),(70 if x==58 else 202,193,34),(76 if x==58 else 225,196,37),'L03_LIFT_CRADLE',BLUE,'custom');parent(bridge,lift)
retracted=switch('L_latch_retracted',237,85,46.5)
for o in [retracted,bpy.data.objects['L_latch_retracted_lever']]:
    o.rotation_euler.z=math.pi/2;o.location.x=239.9;o.location.y=93.1
switch('L_latch_extended',226,30,46.5)
switch('L_panel_seated',65,7,54);switch('L_deck_seated',205,294,64)
closed=switch('L_lid_closed',243,285,91.5)
# Rotate the 12.8 mm direction into Y, placing the switch fully inside the right wall.
for o in [closed,bpy.data.objects['L_lid_closed_lever']]:
    o.rotation_euler.z=math.pi/2;o.location.x=246;o.location.y=291.4
cutbox(bpy.data.objects['R3_right_wall'],(242.9,284,90),(250,299,100))
switch('L_lid_stowed',20,146,36)
# Fixed brackets support each switch. Common backplates serve paired travel limits.
for x in [58,210]:
    box('L_limit_switch_backplate_'+str(x),(x,203,6),(x+13,206,72),'L01_SHELL',BLUE,'custom')
    foot=box('L_limit_switch_foot_'+str(x),(x-3,203,6),(x+16,209,9),'L01_SHELL',BLUE,'custom')
    for xx in [x,x+13]:hole(foot,(xx,206,5),1.7,6);hole(base,(xx,206,-1),1.7,8)
for n,lo,hi in [
    ('latch_retracted',(242.8,86.7,44),(245,99.5,54)),
    ('latch_extended',(226,35.8,6),(239,39,54)),
    ('panel',(65,12.8,6),(78,15.8,62)),
    ('deck',(205,299.8,6),(218,302.8,71)),
    ('closed',(243.1,297.8,89),(249,300.8,100)),
    ('stowed',(20,151.8,6),(33,154.8,44))]:
    box('L_switch_mount_'+n,lo,hi,'L01_SHELL',BLUE,'custom')
# Adjustable levers and brackets are explicit fabrication parts, final switch trip is measured.
cable('L_closed_switch_lever',[(246,294,98),(246,296,100)],METAL,.35)
cable('L_stowed_switch_lever',[(25,148,43),(18,148,43),(12,148,43)],METAL,.35)

def pcb(n,lo,wh,height,part):
    x,y,z=lo;w,h=wh
    b=box(n,(x,y,z),(x+w,y+h,z+1.6),'L06_ELECTRONICS',GREEN)
    b['part']=part;b['allocated_height_mm']=height
    box(n+'_components',(x+2,y+2,z+1.6),(x+w-2,y+h-2,z+height),'L06_ELECTRONICS',BLACK)
    for xx,yy in [(x+2,y+2),(x+w-2,y+h-2)]:cyl(n+'_standoff',(xx,yy,6),1.5,z-6,c='L06_ELECTRONICS',col=BLUE,kind='custom')
    return b
pcb('L_Nano',(39,20,12),(18,45),12,'Arduino A000005 18x45 mm')
pcb('L_HX711',(40,113,12),(25.5,23),12.1,'Adafruit 5974')
pcb('L_BLE',(39,260,12),(21,32),5,'Adafruit 2479 UART Friend')
pcb('L_D24V10F5',(160,20,12),(17.78,12.7),3.56,'Pololu 2831')
pcb('L_DRV8876',(207,220,12),(15.2,17.8),5,'Pololu 4036')
pcb('L_MCP23017',(34,210,12),(43,18),5,'Adafruit 5346')
pcb('L_power_and_latch_board',(203,112,12),(30,55),15,'Custom perfboard IRLZ44N / diode / fuses / capacitors')
box('L_IRLZ44N',(208,120,14),(218,124.5,31),'L06_ELECTRONICS',BLACK)
box('L_FUSE_MAIN',(160,9,12),(185,17,20),'L06_ELECTRONICS',BLACK)
box('L_FUSE_MOTOR',(207,180,12),(232,188,20),'L06_ELECTRONICS',BLACK)
hole(panel,(178,0,23),4.2,7,'Y');cyl('L_12V_jack',(178,2,23),4,11,'Y',c='L06_ELECTRONICS',col=BLACK)
cable('L_motor_cable',[(115,238,14),(114,265,12),(145,296,12),(218,296,12),(219,235,18)],ORANGE,.6)
cable('L_power_bus',[(178,13,23),(170,17,23),(199,20,22),(228,110,23),(228,185,22),(220,225,20)],ORANGE,.6)
cable('L_latch_wire',[(226,128,24),(235,102,28),(230,88,46),(229,70,54)],ORANGE,.6)
cable('L_HX_signal',[(52,62,18),(47,82,19),(53,114,18)],PURPLE,.4)
cable('L_sensor_service_loop',[(116,61,52),(108,61,54),(107,82,58),(70,86,35),(68,112,24),(63,124,22)],PURPLE,.55)
cable('L_BLE_UART',[(46,62,20),(32,76,20),(32,248,20),(46,266,18)],PURPLE,.4)
text('L_motor_label','PQ12 / 12V / INSIDE',(114,211,28),3)
text('L_tray_label','R3 INTERNAL LIFT',(58,265,81.05),7)
parent(bpy.data.objects['L_tray_label'],lift)
paper=box('L_A4_reference',(25.75,13.5,81.15),(235.75,310.5,81.25),'L08_LABELS',WHITE,'reference')
parent(paper,lift);paper['dimensions']='A4 210x297; illustration only'

# One complete manual-door / powered-floor cycle. Door movement is not falsely motorized.
sc.timeline_markers.clear();sc.frame_start=1;sc.frame_end=240;sc.render.fps=24
for f,a,z in [(1,0,104),(24,0,104),(48,-90,104),(72,-90,40.2),(168,-90,40.2),(192,-90,104),(216,0,104),(240,0,104)]:
    rig.location=(9,0,z);rig.rotation_euler=(0,math.radians(a),0)
    rig.keyframe_insert(data_path='location',frame=f);rig.keyframe_insert(data_path='rotation_euler',frame=f)
bolt=bpy.data.objects['R3_lock_bolt_4p3mm'];rest=bolt.location.copy()
for f,d in [(1,0),(16,0),(24,-4.3),(48,-4.3),(60,0),(180,0),(204,-4.3),(216,-4.3),(232,0),(240,0)]:
    bolt.location=rest+Vector((d,0,0));bolt.keyframe_insert(data_path='location',frame=f)
for f,s in [(1,0),(72,0),(108,18),(132,18),(168,0),(240,0)]:
    slide.location.y=s;slide.keyframe_insert(data_path='location',frame=f)
    lift.location.z=s*SLOPE;lift.keyframe_insert(data_path='location',frame=f)
    rod.dimensions.y=3.5+s;rod.location.y=244.5+(3.5+s)/2
    rod.keyframe_insert(data_path='scale',frame=f);rod.keyframe_insert(data_path='location',frame=f)
for a in bpy.data.actions:
    if hasattr(a,'fcurves'):
        for fc in a.fcurves:
            for k in fc.keyframe_points:k.interpolation='LINEAR'
    for layer in getattr(a,'layers',[]):
        for st in layer.strips:
            for bag in getattr(st,'channelbags',[]):
                for fc in bag.fcurves:
                    for k in fc.keyframe_points:k.interpolation='LINEAR'
for f,s in [(1,'CLOSED / FLOOR DOWN'),(48,'MANUAL DOOR UPRIGHT'),(72,'DOOR STOWED'),(108,'FLOOR UP 27mm'),(168,'FLOOR DOWN'),(216,'MANUAL DOOR CLOSED')]:sc.timeline_markers.new(s,frame=f)
materials={}
for o in sc.objects:
    if o.type not in ['MESH','CURVE','FONT']:continue
    key=tuple(round(v,3) for v in o.color)
    if key not in materials:
        m=bpy.data.materials.new('Lift_'+str(len(materials)));m.diffuse_color=key;m.use_nodes=True
        bs=next((n for n in m.node_tree.nodes if n.type=='BSDF_PRINCIPLED'),None)
        if bs is None:
            bs=m.node_tree.nodes.new('ShaderNodeBsdfPrincipled')
            out=m.node_tree.nodes.new('ShaderNodeOutputMaterial');m.node_tree.links.new(bs.outputs['BSDF'],out.inputs['Surface'])
        bs.inputs['Base Color'].default_value=key;bs.inputs['Roughness'].default_value=.42
        if key==METAL:bs.inputs['Metallic'].default_value=.4
        materials[key]=m
    o.data.materials.clear();o.data.materials.append(materials[key])
sc.unit_settings.system='METRIC';sc.unit_settings.scale_length=.001
sc['revision']='R3-LIFT / R4 retired';sc['outside_closed_mm']='255 x 324 x 108';sc['payload_design_target_kg']=.5
sc['actuator']='PQ12-100-12-S; stationary housing Z12.5..27.5; all extension inside shell'
sc['firmware_status']='Mechanical design only; R3 firmware cannot operate new lift without integration'
sc['source_sha256']=hashlib.sha256(SOURCE.read_bytes()).hexdigest()
sc['lift_mm']=27;sc['slider_mm']=18;sc['ramp_slope']=SLOPE
sc['render_notes']='A4 sheet is a reference, purchased components are envelopes, no physical qualification'
sc.render.engine='BLENDER_WORKBENCH';sc.display.shading.color_type='MATERIAL'
sc.frame_set(1);bpy.context.view_layer.update()
rows=[]
for o in sc.objects:
    if o.type!='MESH':continue
    b=bounds(o);rows.append(dict(name=o.name,kind=o.get('kind','R3_reused'),part=o.get('part',''),bounds_mm=b,parent=o.parent.name if o.parent else None))
    if o.get('kind') in ['custom','print','sheet_fabrication','metal_fabrication','machined_part']:
        bpy.ops.object.select_all(action='DESELECT');o.select_set(True);bpy.context.view_layer.objects.active=o
        bpy.ops.wm.stl_export(filepath=str(ROOT/'parts'/(o.name+'.stl')),export_selected_objects=True,use_scene_unit=False)
(ROOT/'object_inventory.json').write_text(json.dumps(rows,ensure_ascii=False,indent=2),encoding='utf8')
with (ROOT/'placement.csv').open('w',encoding='utf-8-sig',newline='') as f:
    w=csv.writer(f);w.writerow(['name','kind','part','xmin','ymin','zmin','xmax','ymax','zmax','parent'])
    for r in rows:w.writerow([r['name'],r['kind'],r['part'],*r['bounds_mm'][0],*r['bounds_mm'][1],r['parent']])
(ROOT/'parameters.json').write_text(json.dumps({'revision':'R3-LIFT','source_sha256':sc['source_sha256'],'outside_mm':[255,324,108],'tray_surface_down_mm':81,'tray_surface_up_mm':108,'lift_mm':27,'slider_mm':18,'ramp_slope':1.5,'roller_radius_mm':5,'roller_centre_down_z_mm':29,'ramp_surface_at_roller_y_mm':Z0,'payload_design_target_kg':.5,'motor_body_min_mm':[114,208,12.5],'motor_body_max_mm':[135.5,244.5,27.5]},indent=2),encoding='utf8')
# Source mesh bounding boxes must survive opening the newer Blender file.
reference=json.loads((ROOT.parent/'smart_lock_box_r3/object_inventory.json').read_text(encoding='utf8'))
lookup={r['name']:r for r in reference};diff=[]
for n,b in original.items():
    if n in lookup:
        old=[lookup[n]['min_mm'],lookup[n]['max_mm']]
        if max(abs(b[j][i]-old[j][i]) for j in range(2) for i in range(3))>.01:diff.append(n)
(ROOT/'validation/source_import.json').write_text(json.dumps({'blender_version':bpy.app.version_string,'source_saved_version':'5.2','compared_mesh_envelopes':len(original),'mismatches':diff,'note':'Envelope comparison, not full topology/version certification.'},indent=2),encoding='utf8')
assert not diff,diff
sc.frame_set(108)
for screen in bpy.data.screens:
    for area in screen.areas:
        if area.type=='VIEW_3D':
            area.spaces.active.clip_end=10000;area.spaces.active.region_3d.view_distance=490;area.spaces.active.region_3d.view_location=(127,162,110)
bpy.ops.wm.save_as_mainfile(filepath=str(ROOT/'smart_lock_box_r3_lift.blend'),compress=True)
# Export only physical objects at true metre scale; remove the temporary root afterwards.
sc.frame_set(1);tops=[o for o in sc.objects if not o.parent];root=empty('METRES_ROOT')
for o in tops:o.parent=root
root.scale=(.001,.001,.001);sc.unit_settings.scale_length=1
bpy.ops.object.select_all(action='DESELECT')
for o in sc.objects:
    if o.type=='EMPTY' or (o.type in ['MESH','CURVE'] and o.get('kind') not in ['reference','annotation']):o.select_set(True)
bpy.ops.export_scene.gltf(filepath=str(ROOT/'smart_lock_box_r3_lift.glb'),export_format='GLB',use_selection=True,export_animations=True,export_extras=True)
sys.path.insert(0,str(ROOT.parent/'smart_lock_box_r3'))
from merge_glb_animations import merge
merge(ROOT/'smart_lock_box_r3_lift.glb')
print('R3_LIFT_BUILD_COMPLETE',len(rows))
