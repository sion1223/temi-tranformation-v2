"""Render the generated assembly itself, not an illustrative generated image."""
import bpy
from pathlib import Path
from mathutils import Vector

ROOT=Path(__file__).resolve().parent
bpy.ops.wm.open_mainfile(filepath=str(ROOT/'smart_lock_box_r4.blend'))
sc=bpy.context.scene;sc.render.engine='CYCLES';sc.cycles.samples=12
sc.cycles.use_denoising=True;sc.cycles.device='CPU'
sc.render.threads_mode='FIXED';sc.render.threads=4
sc.world=bpy.data.worlds.new('R4_studio');sc.world.use_nodes=True
sc.world.node_tree.nodes.get('Background').inputs['Color'].default_value=(.80,.86,.93,1)
sc.world.node_tree.nodes.get('Background').inputs['Strength'].default_value=.6
for name,loc,power,size in [('Key',(300,-400,650),1600000,400),('Fill',(-400,100,450),1000000,400),('Rim',(300,500,450),1000000,350)]:
    d=bpy.data.lights.new(name,'AREA');d.energy=power;d.shape='DISK';d.size=size
    o=bpy.data.objects.new(name,d);sc.collection.objects.link(o);o.location=loc
    o.rotation_euler=(Vector((120,160,70))-o.location).to_track_quat('-Z','Y').to_euler()
camdata=bpy.data.cameras.new('R4_review');cam=bpy.data.objects.new('R4_review',camdata)
sc.collection.objects.link(cam);sc.camera=cam;camdata.type='ORTHO';camdata.clip_end=10000
sc.render.resolution_x=1200;sc.render.resolution_y=960;sc.render.resolution_percentage=100
sc.view_settings.view_transform='AgX';sc.render.image_settings.file_format='PNG'
(ROOT/'drawings').mkdir(exist_ok=True)
def shot(name,frame,loc,target,scale,cutaway=False,tray_only=False):
    sc.frame_set(frame)
    for o in sc.objects:
        o.hide_render=o.get('kind')=='annotation' or o.type=='FONT'
        if cutaway and (o.name in ('도어_본체','R4_A4_stack_reference','R3_front_wall','R3_captive_front_panel','R3_front_top_seal','R3_fixed_electronics_deck') or 'column_guard' in o.name):
            o.hide_render=True
        if tray_only and o.type not in ('CAMERA','LIGHT'):
            o.hide_render=o.name not in ('R4_paper_tray','R4_front_paper_lip','R4_rear_paper_lip')
    cam.location=loc;cam.rotation_euler=(Vector(target)-cam.location).to_track_quat('-Z','Y').to_euler();camdata.ortho_scale=scale
    sc.render.filepath=str(ROOT/'drawings'/name);bpy.ops.render.render(write_still=True)
shot('01_open_raised.png',745,(530,-580,470),(115,150,130),665)
shot('02_closed.png',1,(530,-580,400),(120,155,65),575)
shot('03_front_low_left_high_right.png',745,(125,-650,135),(125,160,135),545)
shot('04_drive_cutaway.png',745,(500,-640,340),(120,130,70),580,True)
shot('05_low_stop_detail.png',745,(-160,-330,340),(126,160,108),420,tray_only=True)
print('R4_RENDERS_COMPLETE')
