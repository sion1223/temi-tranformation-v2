"""Blender: create R3-A4 from the preserved R2 source. Coordinates in mm.
Run: blender --background --python build_r3.py
Purchased parts are envelopes; no claim of structural/thermal certification.
"""
import bpy, bmesh, math, json, csv, sys
from pathlib import Path
from mathutils import Matrix, Vector

ROOT = Path(__file__).resolve().parent
sys.path.insert(0,str(ROOT))
P = json.loads((ROOT/'design_parameters.json').read_text(encoding='utf8'))
bpy.ops.wm.open_mainfile(filepath=str(ROOT/'source_r2.blend'))
sc = bpy.context.scene
sc.frame_set(1)
bpy.context.view_layer.update()
rig = bpy.data.objects['RIG_도어_Y축회전_하강']
lid = bpy.data.objects['도어_본체']
METAL=(.58,.65,.70,1); SHELL=(.64,.71,.75,1); BLUE=(.04,.39,.53,1)
GREEN=(.02,.35,.22,1); ORANGE=(.92,.40,.07,1); BLACK=(.025,.04,.055,1)
RED=(.8,.06,.045,1); PURPLE=(.42,.16,.60,1); WHITE=(.9,.94,.96,1)

def coll(name):
    c=bpy.data.collections.get(name)
    if c is None:
        c=bpy.data.collections.new(name);sc.collection.children.link(c)
    return c
shell=coll('01_셸'); storage=coll('02_수납'); door=coll('03_도어')
lock=coll('04_잠금'); elec=coll('05_전장'); scale=coll('06_로드셀_단일적재판')
wires=coll('07_배선_커넥터'); hw=coll('08_체결부품'); info=coll('09_라벨_참고')
cuts=coll('99_TEMP')
def erase(o):
    if isinstance(o,str): o=bpy.data.objects.get(o)
    if o: bpy.data.objects.remove(o,do_unlink=True)
def finish(o,name,c,color,kind):
    o.name=name
    for old in list(o.users_collection):old.objects.unlink(o)
    c.objects.link(o);o.color=color;o['kind']=kind
    return o
def box(name,lo,hi,c=elec,color=GREEN,kind='purchased_envelope'):
    bpy.ops.mesh.primitive_cube_add(size=1,location=tuple((a+b)/2 for a,b in zip(lo,hi)))
    o=bpy.context.object;o.dimensions=tuple(b-a for a,b in zip(lo,hi))
    bpy.ops.object.transform_apply(location=False,rotation=False,scale=True)
    return finish(o,name,c,color,kind)
def cyl(name,p,r,h,axis='Z',c=hw,color=METAL,kind='purchased_envelope'):
    idx={'X':0,'Y':1,'Z':2}[axis];pos=list(p);pos[idx]+=h/2
    bpy.ops.mesh.primitive_cylinder_add(vertices=32,radius=r,depth=h,location=pos)
    o=bpy.context.object
    if axis=='X':o.rotation_euler.y=math.pi/2
    if axis=='Y':o.rotation_euler.x=math.pi/2
    bpy.ops.object.transform_apply(location=False,rotation=True,scale=True)
    return finish(o,name,c,color,kind)
def boolean(a,b,operation='DIFFERENCE'):
    bpy.context.view_layer.objects.active=a
    m=a.modifiers.new('Machining','BOOLEAN');m.operation=operation;m.solver='EXACT';m.object=b
    bpy.ops.object.modifier_apply(modifier=m.name);erase(b)
def cutbox(a,lo,hi):boolean(a,box('CUT',lo,hi,cuts))
def cutcyl(a,p,r,h,axis='Z'):boolean(a,cyl('CUT',p,r,h,axis,cuts))
def label(name,text,p,size=5):
    d=bpy.data.curves.new(name,'FONT');d.body=text;d.size=size
    o=bpy.data.objects.new(name,d);info.objects.link(o);o.location=p;o.color=WHITE;o['kind']='annotation';return o
def wire(name,points,color=BLUE,r=.45):
    d=bpy.data.curves.new(name,'CURVE');d.dimensions='3D';d.bevel_depth=r;d.bevel_resolution=1
    s=d.splines.new('POLY');s.points.add(len(points)-1)
    for p,co in zip(s.points,points):p.co=(*co,1)
    o=bpy.data.objects.new(name,d);wires.objects.link(o);o.color=color;o['kind']='wire_route'
    return o
def moving(o):
    bpy.context.view_layer.update();mw=o.matrix_world.copy();o.parent=rig;o.matrix_world=mw
    o['moving_with_door']=True
def pcb(name,lo,size):
    x,y,z=lo;w,d=size
    b=box(name,lo,(x+w,y+d,z+1.6))
    box(name+'_IC',(x+w*.25,y+d*.3,z+1.6),(x+w*.65,y+d*.65,z+3.5),color=BLACK)
    for xx,yy in [(x+2,y+2),(x+w-2,y+d-2)]:
        cyl(name+'_spacer',(xx,yy,9),2, z-9,c=elec,color=BLUE,kind='print')
    return b

# Keep the proven left-side door guide geometry and the moving shoulder bolts.
keep={'도어_본체','도어_숄더볼트_앞','도어_숄더볼트_뒤','도어_가이드판_앞','도어_가이드판_뒤',
      '도어_격납자석','셸_좌측벽','셸_격납슬롯내벽','셸_격납입구내측립','셸_좌측상단마감'}
# Preserve selected purchased latch geometries before clearing their old placement.
latch_names=['잠금_5065_프레임','잠금_5065_코일','잠금_5065_플랜지',
             '잠금_가로잠금핀_4p3mm','잠금_S1_후퇴확인_NC','잠금_S2_전진확인_NO']
keep.update(latch_names)
for o in list(sc.objects):
    if o.type in ('MESH','CURVE','FONT') and o.name not in keep:erase(o)
for o in list(sc.objects):
    if o.type in ('CAMERA','LIGHT'):erase(o)
for o in sc.objects:
    if o.type=='MESH' and o.name.startswith('도어_가이드판'):o['kind']='metal_fabrication'

# Enlarge X by 12 mm to fit A4 flat without contact with the fixed shell.
base=box('R3_base_255x324',(0,0,0),(255,324,6),shell,SHELL,'sheet_fabrication')
front=box('R3_front_wall',(0,0,6),(255,6,78),shell,SHELL,'print')
cutbox(front,(24,-1,7.5),(241,7,39.5))
rear=box('R3_rear_wall',(0,318,6),(255,324,78),shell,SHELL,'print')
right=box('R3_right_wall',(243,6,6),(255,318,70),shell,SHELL,'print')
box('R3_front_top_seal',(22,6,64),(251.5,12,78),shell,SHELL,'print')
box('R3_rear_top_seal',(22,312,64),(251.5,318,78),shell,SHELL,'print')
box('R3_right_top_seal',(251.5,6,70),(255,318,78),shell,SHELL,'print')
cutbox(right,(242.9,5.9,63.9),(251.6,12.1,70.1))
cutbox(right,(242.9,311.9,63.9),(251.6,318.1,70.1))
inner=bpy.data.objects['셸_격납슬롯내벽']
for y0,y1 in [(0,6),(6,12),(312,318),(318,324)]:
    cutbox(inner,(13.4,y0-.001,5.9),(19.6,y1+.001,78.1))
cutbox(inner,(14,147,6.5),(19.6,165,11.5))
# Concealed underside keeper screw heads need a pocket at the closed lid.
cutbox(right,(242,50,65.5),(251.2,70,70.1))
# The long underfloor keeper initially swings outward. Its swept inner pocket
# leaves at least 7 mm of outer wall; verified again across the whole door cycle.
cutbox(right,(242,50.8,19.5),(248,69.2,70.1))
# Restore the old keeper pockets before machining the new underside seat.
boolean(lid,box('FILL_OLD',(20,12.5,70),(251,311.5,78),cuts,ORANGE),'UNION')
cutbox(lid,(238.5,51.5,69.9),(250.5,68.5,71.6))
for y in (54.5,65.5):cutcyl(lid,(246,y,69.8),1.4,6.2)
cutbox(lid,(234,278,69.9),(241,286,72.1))
mag=box('R3_door_magnet',(234.5,278.5,70),(240.5,285.5,72),door,METAL);moving(mag)
box('R3_D3_closed_reed',(235,273,64),(241,289,67),elec,BLACK)
box('R3_D4_stowed_reed',(14.5,148,7),(18.8,164,11),elec,BLACK)
cutcyl(lid,(9,156,72.4),1.55,3.2)
lid['kind']='machined_part';lid['material']='8 mm machined polymer or aluminium; blind threaded keeper attachment'

# Rotate the 5065 horizontal bolt from +Y to +X and lower it into the electronics bay.
# New mapping: x=y+182, y=276-x, z=z-30. Tip X242 locked / X237.7 retracted.
M=Matrix.Translation((182,276,-30))@Matrix.Rotation(-math.pi/2,4,'Z')
for name in latch_names:
    o=bpy.data.objects[name]
    o.animation_data_clear();o.matrix_world=M@o.matrix_world
bpy.data.objects['잠금_S1_후퇴확인_NC'].location+=Vector((2,18,0))
bpy.data.objects['잠금_S2_전진확인_NO'].location+=Vector((-11,-23.6,0))
slug=bpy.data.objects['잠금_가로잠금핀_4p3mm']
slug.name='R3_lock_bolt_4p3mm'
rest=slug.location.copy()
bracket=box('R3_lock_base',(205,44,12),(238,77,20),lock,METAL,'metal_fabrication')
for x in (213.5,231.5):
    for y in (49.2,70.8):
        cutcyl(bracket,(x,y,11),1.7,10)
        cutcyl(bpy.data.objects['잠금_5065_프레임'],(x,y,19),1.7,4)
        cyl('R3_lock_M3',(x,y,19),1.5,3,c=hw)
# A rigid long keeper stays beside (not through) the weighing tray.
keeper=box('R3_door_keeper',(239,52,20.5),(240.5,68,70.8),lock,METAL,'metal_fabrication')
cutbox(keeper,(238,56.4,22.8),(242,63.6,34.2))
boolean(keeper,box('FLANGE',(239,52,70),(250,68,71.5),cuts,METAL),'UNION')
for y in (54.5,65.5):
    cutcyl(keeper,(246,y,69),1.4,4)
    s=cyl('R3_keeper_blind_M2p5',(246,y,69.8),1.25,5.8);moving(s)
    h=cyl('R3_keeper_inside_head',(246,y,67.8),2.35,2);moving(h)
moving(keeper)
keeper['material']='1.5 mm stainless steel; bend radius, strength and withdrawal force require bench verification'
keeper['attachment']='underside-only M2.5 screws, 2 mm unbroken outer lid skin'
wire('R3_S1_adjustable_lever',[(239,90,23),(238.4,68,23.7),(238.4,60,24)],METAL,.28)
wire('R3_S2_adjustable_lever',[(239,40,23),(239,51,24),(241.4,60,24)],METAL,.28)
# Cover the below-deck latch and route its service pull to an internal-only loop.
wire('R3_internal_manual_release',[(234,57,24),(228,82,25),(210,93,25)],PURPLE,.5)

# Fixed protective deck; sensing tray floats above this, with only one load path.
deck=box('R3_fixed_electronics_deck',(22,6,41),(243,318,44),storage,BLUE,'sheet_fabrication')
cutbox(deck,(110.5,143,40),(151,176,45))
cutbox(deck,(237.8,50.8,40),(243.1,69.2,45))
tray=box('R3_single_weighing_tray',tuple(P['tray_min_mm']),tuple(P['tray_max_mm']),scale,METAL,'sheet_fabrication')
tray['load_path']='tray -> spacer -> free end of L6D -> fixed end -> base; no four-point FSR supports'
tray['material']='3 mm flat aluminium, flush fasteners; removable washable liner must move with tray'
tray['measuring_part']=True
label('R3_A4_label','ONE TRAY / A4',(75,255,51.05),7)
# Non-contact peripheral stops protect against accidental pressing, not a claimed load rating.
for x,y in [(29,28),(232.5,28),(29,306),(232.5,306)]:
    stop=cyl('R3_overload_stop',(x,y,44),2.5,3.2,c=storage,color=BLACK,kind='print')
    stop['nominal_gap_mm']=.8;stop['setup']='Adjust after measuring actual full-load deflection; do not touch during weighing.'
# Deck and front panel fasteners can only be accessed after opening and lifting the tray.
for x,y in [(27,82),(238,82),(27,242),(238,242)]:
    b=box('R3_deck_support',(x-3,y-4,6),(x+3,y+4,41),shell,SHELL,'print')
    cutcyl(b,(x,y,33),1.4,9);cutcyl(deck,(x,y,40),1.7,5)
    cyl('R3_deck_inside_M3',(x,y,36),1.5,8)
    cyl('R3_deck_head',(x,y,44),2.8,2.4)

# L6D single-point sensor: the platform is below the rated 250 x 350 mm envelope.
fixed=box('R3_cell_fixed_pedestal',(112.75,42,6),(148.75,67,14),scale,METAL,'metal_fabrication')
cell=box('R3_L6D_3kg_envelope',(115.75,42,14),(145.75,172,36),scale,METAL)
# Simplified beam window is for identification only; never manufacture this purchased sensor.
cutbox(cell,(114,74,18),(147,140,32))
cell['supplier_drawing_required']=True
cell['part']='Zemic L6D-C3-3kg-0.40B; actual beam/holes supplied by manufacturer'
spacer=box('R3_cell_loaded_spacer',(115.75,147,36),(145.75,172,48),scale,METAL,'metal_fabrication')
spacer['measuring_part']=True
# 2015 manufacturer drawing: M6 through, 12 mm from ends, transverse pitch 15 mm.
# Confirm the current purchased revision before machining; cable exits fixed end.
for x in (123.25,138.25):
    y=160
    cutcyl(tray,(x,y,47),3.2,5)
    cutcyl(tray,(x,y,49.5),5.5,2)
    cutcyl(spacer,(x,y,35),3.2,14)
    cutcyl(cell,(x,y,13),2.5,24)
    s=cyl('R3_cell_loaded_M6',(x,y,30),3,20,c=hw)
    s['measuring_part']=True;s['supplier_drawing_required']=True
    h=cyl('R3_cell_flush_head',(x,y,49.55),5.3,1.40,c=hw)
    boolean(s,h,'UNION')
for x in (123.25,138.25):
    y=54
    cutcyl(fixed,(x,y,5),3.2,10)
    cutcyl(base,(x,y,-1),3.2,8)
    cutcyl(cell,(x,y,13),2.5,24)
    # Bottom attachment is not a latch bypass; cover with robot mounting plate on deployment.
    s=cyl('R3_cell_fixed_M6',(x,y,1),3,20);s['supplier_drawing_required']=True
    cutcyl(base,(x,y,-.1),5.6,3)
    boolean(s,cyl('R3_cell_fixed_head',(x,y,.4),5.3,2.4),'UNION')

# Rebuilt lower-floor electronics. The latch occupies the front-right; cell is central.
pcb('R3_Arduino_Nano',(39,20,13),(18,45))
box('R3_Nano_USB_internal',(42,13,14.6),(54,23,22),color=METAL)
pcb('R3_HX711_5V_carrier',(45,117,13),(34,21))
pcb('R3_D24V10F5',(161,25,13),(18,13))
pcb('R3_MOSFET_driver',(166,80,13),(31,28))
erase('R3_MOSFET_driver_IC')
box('R3_IRLZ44N',(170,86,14.6),(180,90.5,32),color=BLACK)
box('R3_F1_T1A',(166,15,12),(189,24,20),color=BLACK)
box('R3_SB310',(159,46,13),(171,51,18),color=BLACK)
pcb('R3_BLE_3V3',(39,273,13),(27,13))
pcb('R3_LDO_level_shift',(39,237,13),(25,18))
box('R3_D12_electronics_seated',(30,298,18),(43,304,25),color=BLACK)
box('R3_A3_panel_closed',(67,6,29),(80,12,35),color=BLACK)
pcb('R3_power_sense_100k33k',(166,119,13),(25,14))
for x,y in [(157,57),(180,57)]:cyl('R3_bulk_cap',(x,y,13),3.5,9,c=elec,color=BLACK)
cyl('R3_D1_flyback',(239,28,27),1.3,5,'Y',elec,BLACK)
for n,p in [('J1_COIL',(200,85,12)),('J2_LATCH',(199,120,12)),('J3_CELL',(82,119,12)),('J4_DECK',(49,218,12))]:
    box(n,p,(p[0]+18,p[1]+7,p[2]+9),wires,PURPLE)
# A reserved, unpopulated battery option is not a fabricated power-source claim.
res=box('R3_optional_power_space',(92,218,8),(227,291,33),info,(.14,.23,.30,1),'annotation')
res.display_type='WIRE';res.hide_render=True
label('R3_power_label','POWER OPTION / NOT FITTED',(100,247,9),5)
wire('R3_CELL_SHIELDED',[(117,61,24),(104,61,23),(96,130,23),(79,129,17)],BLUE,.65)
wire('R3_5V_to_HX',[(165,30,18),(154,68,24),(87,78,24),(68,120,18)],ORANGE,.5)
wire('R3_HX_CLOCK_DATA',[(55,55,18),(67,85,23),(63,118,18)],BLUE,.45)
wire('R3_12V_fused',[(178,12,23),(177,17,23),(163,47,22),(200,81,25)],RED,.6)
wire('R3_COIL_pair',[(200,87,23),(230,90,25),(235,65,27)],RED,.7)
wire('R3_GND_star',[(166,47,18),(161,76,23),(78,77,23),(58,57,18)],BLACK,.65)
wire('R3_BLE_UART',[(50,61,18),(33,71,23),(33,268,23),(50,274,18)],PURPLE,.5)
wire('R3_PANEL_switch',[(72,10,31),(72,30,25),(56,37,18)],PURPLE,.4)
wire('R3_D3_wire',[(204,122,23),(235,140,32),(237,279,32),(238,280,65)],PURPLE,.4)
cutcyl(deck,(238,280,40),1.2,6)

# Concealed front fasteners. No external USB hole or externally removable latch screws.
panel=box('R3_captive_front_panel',(24.4,1,8),(240.6,5,39),shell,SHELL,'print')
for x in (29,235):
    boolean(panel,box('EAR',(x-3,4,32),(x+3,20,39),cuts,SHELL),'UNION')
    cutcyl(panel,(x,16,31),1.4,9)
    cutcyl(deck,(x,16,40),1.7,5)
    cyl('R3_panel_inside_M3',(x,16,33),1.5,11,c=hw)
    cyl('R3_panel_inside_head',(x,16,44),2.8,2.4,c=hw)
cutcyl(panel,(178,0,23),4.2,7,'Y')
cyl('R3_12V_jack',(178,2,23),4,11,'Y',elec,BLACK)
cutcyl(panel,(68,0,24),1.6,7,'Y')
cyl('R3_status_LED',(68,1.5,24),1.5,5,'Y',elec,GREEN)
panel['service']='Open lid; remove weighing tray and internal deck fasteners; remove two vertical panel screws. Power off before unplugging.'

# Deliberate unlock-on-close cycle; a spring-cam closure is not assumed or animated.
for a in list(bpy.data.actions):
    if a.users==0:bpy.data.actions.remove(a)
rig.animation_data_clear();sc.timeline_markers.clear()
for f,angle,z in [(1,0,74),(24,0,74),(48,-90,74),(72,-90,10.2),(96,-90,74),(120,0,74),(144,0,74)]:
    rig.location=(9,0,z);rig.rotation_euler=(0,math.radians(angle),0)
    rig.keyframe_insert(data_path='location',frame=f);rig.keyframe_insert(data_path='rotation_euler',frame=f)
for f,delta in [(1,0),(16,0),(24,-4.3),(48,-4.3),(55,0),(96,0),(108,-4.3),(120,-4.3),(132,0),(144,0)]:
    slug.location=rest+Vector((delta,0,0));slug.keyframe_insert(data_path='location',frame=f)
for action in bpy.data.actions:
    for layer in action.layers:
        for strip in layer.strips:
            for bag in strip.channelbags:
                for fc in bag.fcurves:
                    for k in fc.keyframe_points:k.interpolation='LINEAR'
for name,frame in [('LOCKED',1),('UNLOCK',24),('UPRIGHT',48),('STOWED',72),('LIFT',96),('HOLD_TO_CLOSE',108),('CLOSED',120),('RELOCKED',144)]:
    sc.timeline_markers.new(name,frame=frame)
sc.frame_set(1);sc.frame_start=1;sc.frame_end=144

# Native file + metric GLB + fabrication/reference exports.
materials={}
for o in sc.objects:
    if o.type not in ('MESH','CURVE','FONT'):continue
    key=tuple(round(x,3) for x in o.color)
    if key not in materials:
        m=bpy.data.materials.new('R3_Material_'+str(len(materials)));m.diffuse_color=key;m.use_nodes=True
        bs=next((n for n in m.node_tree.nodes if n.type=='BSDF_PRINCIPLED'),None)
        if bs is None:
            bs=m.node_tree.nodes.new('ShaderNodeBsdfPrincipled')
            out=next((n for n in m.node_tree.nodes if n.type=='OUTPUT_MATERIAL'),None) or m.node_tree.nodes.new('ShaderNodeOutputMaterial')
            m.node_tree.links.new(bs.outputs['BSDF'],out.inputs['Surface'])
        bs.inputs['Base Color'].default_value=key;bs.inputs['Roughness'].default_value=.5
        if key==METAL:bs.inputs['Metallic'].default_value=.45
        materials[key]=m
    o.data.materials.clear();o.data.materials.append(materials[key])
sc['revision']='R3-A4 / double floor / underfloor horizontal latch'
sc['dimensions_mm']='255 x 324 x 78';sc['minimum_item_target']='A4 80gsm ~4.99g; requires bench qualification'
sc['power']='regulated external 12V; internal battery not fitted'
sc['fsr_positions_mm']='[]';sc['fsr_stack']='removed; L6D + HX711'
sc['frames']='1 locked; 24 release; 48 upright; 72 stowed; 108 release to close; 144 locked'
sc['lock_stroke_mm']=4.3
for fn in ['build_r3.py','design_parameters.json']:
    tx=bpy.data.texts.get(fn) or bpy.data.texts.new(fn);tx.clear();tx.write((ROOT/fn).read_text(encoding='utf8'))
sc.unit_settings.system='METRIC';sc.unit_settings.scale_length=.001
sc.render.engine='BLENDER_WORKBENCH';sc.display.shading.color_type='MATERIAL'
for screen in bpy.data.screens:
    for area in screen.areas:
        if area.type=='VIEW_3D':
            area.spaces.active.clip_end=10000;area.spaces.active.region_3d.view_distance=490
            area.spaces.active.region_3d.view_location=(127,162,55)
for name in ['STL_print','fabrication']:(ROOT/name).mkdir(exist_ok=True)
for o in list(sc.objects):
    if o.type=='MESH' and o.get('kind') in ('print','sheet_fabrication','metal_fabrication','machined_part'):
        bpy.ops.object.select_all(action='DESELECT');o.select_set(True);bpy.context.view_layer.objects.active=o
        sub='STL_print' if o.get('kind')=='print' else 'fabrication'
        bpy.ops.wm.stl_export(filepath=str(ROOT/sub/(o.name+'.stl')),export_selected_objects=True,use_scene_unit=False)
physical=[o for o in sc.objects if o.type in ('MESH','CURVE') and o.get('kind')!='annotation']
root=bpy.data.objects.new('METRES_root',None);sc.collection.objects.link(root)
tops=[o for o in physical+[rig] if o.parent is None]
for o in tops:o.parent=root
root.scale=(.001,.001,.001);sc.unit_settings.scale_length=1
bpy.ops.object.select_all(action='DESELECT')
for o in physical+[root,rig]:o.select_set(True)
bpy.ops.export_scene.gltf(filepath=str(ROOT/'smart_lock_box_r3.glb'),export_format='GLB',use_selection=True,export_animations=True,export_yup=True,export_extras=True)
from merge_glb_animations import merge
merge(ROOT/'smart_lock_box_r3.glb')
root.scale=(1,1,1)
for o in tops:o.parent=None
erase(root);sc.unit_settings.scale_length=.001;sc.frame_set(1)
bpy.context.view_layer.update()
rows=[]
for o in sc.objects:
    if o.type!='MESH':continue
    pts=[o.matrix_world@Vector(p) for p in o.bound_box]
    rows.append({'name':o.name,'kind':o.get('kind','reference'),'min_mm':[min(p[i] for p in pts) for i in range(3)],
                 'max_mm':[max(p[i] for p in pts) for i in range(3)],'moving_with_door':bool(o.get('moving_with_door'))})
(ROOT/'object_inventory.json').write_text(json.dumps(rows,ensure_ascii=False,indent=2),encoding='utf8')
with (ROOT/'placement.csv').open('w',encoding='utf-8-sig',newline='') as f:
    w=csv.writer(f);w.writerow(['name','kind','xmin','ymin','zmin','xmax','ymax','zmax'])
    for r in rows:w.writerow([r['name'],r['kind'],*r['min_mm'],*r['max_mm']])
bpy.ops.object.select_all(action='DESELECT');lid.select_set(True);bpy.context.view_layer.objects.active=lid
bpy.ops.wm.save_as_mainfile(filepath=str(ROOT/'smart_lock_box_r3.blend'),compress=True)
print('R3_BUILD_COMPLETE',len(rows))
