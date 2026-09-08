"""Render the actual R3 Blender geometry, including a removable reference A4 sheet."""
import bpy,math
from pathlib import Path
from mathutils import Vector
ROOT=Path(__file__).resolve().parent
bpy.ops.wm.open_mainfile(filepath=str(ROOT/'smart_lock_box_r3.blend'))
sc=bpy.context.scene;sc.render.engine='CYCLES';sc.cycles.samples=24;sc.cycles.use_denoising=True
sc.cycles.device='CPU';sc.render.threads_mode='FIXED';sc.render.threads=8
if sc.world is None:sc.world=bpy.data.worlds.new('R3_World')
sc.world.use_nodes=True
bg=next((n for n in sc.world.node_tree.nodes if n.type=='BACKGROUND'),None)
if bg is None:
    bg=sc.world.node_tree.nodes.new('ShaderNodeBackground')
    out=sc.world.node_tree.nodes.new('ShaderNodeOutputWorld');sc.world.node_tree.links.new(bg.outputs[0],out.inputs['Surface'])
bg.inputs['Color'].default_value=(.81,.86,.91,1);bg.inputs['Strength'].default_value=.7
for name,loc,power,size in [('Key',(250,-200,600),900000,380),('Fill',(-250,250,400),600000,420),('Rim',(420,520,400),600000,300)]:
    d=bpy.data.lights.new(name,'AREA');d.energy=power;d.shape='DISK';d.size=size
    o=bpy.data.objects.new(name,d);sc.collection.objects.link(o);o.location=loc
    o.rotation_euler=(Vector((127,162,40))-o.location).to_track_quat('-Z','Y').to_euler()
d=bpy.data.cameras.new('R3_Review_Camera');camera=bpy.data.objects.new('R3_Review_Camera',d);sc.collection.objects.link(camera)
sc.camera=camera;d.type='ORTHO';d.clip_end=10000
sc.render.resolution_x=1280;sc.render.resolution_y=960;sc.render.resolution_percentage=100
sc.view_settings.view_transform='AgX'
def reset():
    for o in sc.objects:o.hide_render=o.get('kind')=='annotation'
def shot(name,loc,target,zoom):
    camera.location=loc;camera.rotation_euler=(Vector(target)-camera.location).to_track_quat('-Z','Y').to_euler()
    bpy.context.view_layer.update();q=camera.rotation_euler.to_quaternion()
    right=q@Vector((1,0,0));up=q@Vector((0,1,0))
    pts=[o.matrix_world@Vector(p) for o in sc.objects if o.type in ('MESH','CURVE') and not o.hide_render for p in o.bound_box]
    xs=[p.dot(right) for p in pts];ys=[p.dot(up) for p in pts]
    camera.location+=right*((max(xs)+min(xs))/2-camera.location.dot(right))+up*((max(ys)+min(ys))/2-camera.location.dot(up))
    d.ortho_scale=max(max(xs)-min(xs),(max(ys)-min(ys))*sc.render.resolution_x/sc.render.resolution_y)*1.12
    sc.render.filepath=str(ROOT/'drawings'/name);bpy.ops.render.render(write_still=True)
reset();sc.frame_set(48)
shot('01_single_tray_open.png',(590,-620,520),(122,155,126),505)
# Reference A4, 210 x 297, clear of all fixed walls. Not exported as a physical component.
bpy.ops.mesh.primitive_cube_add(size=1,location=(130.75,162,51.1))
paper=bpy.context.object;paper.name='A4_reference_only';paper.dimensions=(210,297,.1)
mat=bpy.data.materials.new('Paper');mat.diffuse_color=(.94,.95,.93,1);paper.data.materials.append(mat)
reset();sc.frame_set(48)
shot('02_a4_fit.png',(350,-410,640),(127,161,90),490)
bpy.data.objects.remove(paper,do_unlink=True)
reset();sc.frame_set(1)
for o in sc.objects:
    if o.get('moving_with_door') or o.name in ('R3_single_weighing_tray','R3_fixed_electronics_deck','R3_front_wall','R3_front_top_seal','R3_captive_front_panel'):
        o.hide_render=True
shot('03_lower_electronics.png',(460,-460,650),(127,164,35),450)
reset();sc.frame_set(1)
for o in sc.objects:
    o.hide_render=True
    if o.type=='LIGHT' or o.name.startswith(('R3_cell','R3_L6D','R3_single_weighing_tray')):o.hide_render=False
shot('04_single_point_load_path.png',(410,-270,160),(130,165,32),380)
reset();sc.frame_set(1)
for o in sc.objects:
    o.hide_render=True
    if o.type=='LIGHT' or o.name.startswith(('R3_lock_','잠금_5065','잠금_S','R3_door_keeper','R3_keeper_','R3_S1_','R3_S2_')):o.hide_render=False
shot('05_underfloor_latch.png',(350,190,170),(228,67,46),120)
reset();sc.frame_set(72)
shot('06_stowed_elevation.png',(127,-600,128),(127,162,128),370)
reset();sc.frame_set(1)
for o in sc.objects:
    if o.get('moving_with_door'):o.hide_render=True
# Exploded inspection illustration: +70 mm tray, +25 mm fixed deck. Native model remains assembled.
deck=bpy.data.objects['R3_fixed_electronics_deck'];tray=bpy.data.objects['R3_single_weighing_tray']
deck.location.z+=25;tray.location.z+=70
shot('07_double_floor_exploded.png',(530,-650,350),(127,162,70),480)
deck.location.z-=25;tray.location.z-=70
# Save a useful camera and open pose in a separate review scene, not over native animation start.
reset();sc.frame_set(1)
camera.location=(590,-620,520);camera.rotation_euler=(Vector((122,155,126))-camera.location).to_track_quat('-Z','Y').to_euler();d.ortho_scale=505
bpy.ops.wm.save_as_mainfile(filepath=str(ROOT/'review_scene.blend'),compress=True)
print('R3_RENDERS_COMPLETE')
