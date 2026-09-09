"""Review images from actual mechanical model; no generated illustration."""
import bpy,math
from pathlib import Path
from mathutils import Vector
ROOT=Path(__file__).resolve().parent
bpy.ops.wm.open_mainfile(filepath=str(ROOT/'smart_lock_box_r3_lift.blend'))
sc=bpy.context.scene;sc.render.engine='CYCLES';sc.cycles.samples=20;sc.cycles.use_denoising=True
sc.cycles.device='CPU';sc.render.threads_mode='FIXED';sc.render.threads=10
world=bpy.data.worlds.new('Lift_review_world');sc.world=world;world.use_nodes=True
bg=next((n for n in world.node_tree.nodes if n.type=='BACKGROUND'),None)
bg.inputs['Color'].default_value=(.8,.86,.92,1);bg.inputs['Strength'].default_value=.7
for name,loc,power,size in [('Key',(250,-200,650),1000000,380),('Fill',(-250,250,450),650000,420),('Rim',(420,520,500),750000,300)]:
    d=bpy.data.lights.new(name,'AREA');d.energy=power;d.shape='DISK';d.size=size
    o=bpy.data.objects.new(name,d);sc.collection.objects.link(o);o.location=loc
    o.rotation_euler=(Vector((127,162,50))-o.location).to_track_quat('-Z','Y').to_euler()
d=bpy.data.cameras.new('L_Review_Camera');cam=bpy.data.objects.new('L_Review_Camera',d);sc.collection.objects.link(cam)
sc.camera=cam;d.type='ORTHO';d.clip_end=10000
sc.render.resolution_x=1440;sc.render.resolution_y=1080;sc.render.resolution_percentage=100;sc.view_settings.view_transform='AgX'
def reset():
    for o in sc.objects:o.hide_render=o.get('kind')=='annotation'
def shot(n,loc,target,scale):
    cam.location=loc;cam.rotation_euler=(Vector(target)-cam.location).to_track_quat('-Z','Y').to_euler();d.ortho_scale=scale
    bpy.context.view_layer.update();q=cam.rotation_euler.to_quaternion();right=q@Vector((1,0,0));up=q@Vector((0,1,0))
    pts=[o.matrix_world@Vector(p) for o in sc.objects if o.type in ['MESH','CURVE'] and not o.hide_render for p in o.bound_box]
    xs=[p.dot(right) for p in pts];ys=[p.dot(up) for p in pts]
    cam.location+=right*((max(xs)+min(xs))/2-cam.location.dot(right))+up*((max(ys)+min(ys))/2-cam.location.dot(up))
    d.ortho_scale=max(max(xs)-min(xs),(max(ys)-min(ys))*sc.render.resolution_x/sc.render.resolution_y)*1.12
    sc.render.filepath=str(ROOT/'drawings'/n);bpy.ops.render.render(write_still=True)
reset();sc.frame_set(108)
shot('01_raised.png',(580,-650,510),(122,163,117),445)
reset();sc.frame_set(1)
shot('02_closed.png',(580,-650,480),(127,163,52),450)
reset();sc.frame_set(72)
for o in sc.objects:
    if o.parent and o.parent.name=='RIG_도어_Y축회전_하강':o.hide_render=True
    if o.name.startswith(('L_front_wall','L_captive','L_fixed_guard','R3_single_weighing_tray','L_moving_skirt','L_A4')):o.hide_render=True
shot('03_internal_drive.png',(540,-520,610),(130,158,36),455)
reset();sc.frame_set(72)
for o in sc.objects:
    if o.type in ['CAMERA','LIGHT']:continue
    o.hide_render=True
    if o.name.startswith(('L_PQ12','L_output','L_slider','L_MGN7','L_slide_spacer','L_POM','L_623','L_roller','L_cradle','L_LM6','L_6mm','L_cell_fixed','R3_L6D','R3_cell_loaded')):o.hide_render=False
shot('04_mechanism_down.png',(410,-380,330),(132,178,35),360)
sc.frame_set(108)
shot('05_mechanism_up.png',(410,-380,330),(132,178,48),360)
reset();sc.frame_set(108)
shot('06_front_elevation.png',(127,-650,134),(127,162,134),410)
reset();sc.frame_set(108)
bpy.ops.wm.save_as_mainfile(filepath=str(ROOT/'review_scene.blend'),compress=True)
print('RENDERS_COMPLETE')
