"""Render the real 220 mm print plate layout for visual review."""
import json
from pathlib import Path
import bpy
from PIL import Image

ROOT = Path(__file__).resolve().parent
bpy.ops.wm.open_mainfile(filepath=str(ROOT/'print_ready/smart_lock_box_r4_print.blend'))
sc = bpy.data.scenes['PRINT_PLATES_220mm']
bpy.context.window.scene = sc
rows = json.loads((ROOT/'print_ready/print_manifest.json').read_text())['parts']
ink = bpy.data.materials.new('Dimension text ink')
ink.diffuse_color = (.015,.025,.04,1)
ink.use_nodes = True
ink.node_tree.nodes['Principled BSDF'].inputs['Base Color'].default_value = (.015,.025,.04,1)
for mat in list(bpy.data.materials):
    if mat.name.startswith('PETG_'):
        color = mat.diffuse_color[:]
        mat.use_nodes = True
        mat.node_tree.nodes['Principled BSDF'].inputs['Base Color'].default_value = color
        mat.node_tree.nodes['Principled BSDF'].inputs['Roughness'].default_value = .7
for row in rows:
    label = bpy.data.objects[row['id']+'_label'].data
    label.body = row['id']+'  '+ ' x '.join(str(round(x,1)) for x in row['dimensions_mm'])+' mm'
    label.size = 10
    label.materials.clear()
    label.materials.append(ink)
for o in sc.objects:
    if o.type == 'CURVE':
        o.data.bevel_depth = .25
        o.data.materials.clear()
        o.data.materials.append(ink)
sc.render.engine = 'CYCLES'
sc.cycles.samples = 8
sc.cycles.use_denoising = True
sc.render.threads_mode = 'FIXED'
sc.render.threads = 4
sc.world = bpy.data.worlds.new('Print studio')
sc.world.use_nodes = True
sc.world.node_tree.nodes['Background'].inputs['Color'].default_value = (.9,.93,.96,1)
sc.world.node_tree.nodes['Background'].inputs['Strength'].default_value = .8
lamp = bpy.data.lights.new('Print softbox', 'AREA')
lamp.energy = 80000000
lamp.size = 2500
obj = bpy.data.objects.new('Print softbox', lamp)
sc.collection.objects.link(obj)
obj.location = (850,1200,4000)
cam = bpy.data.cameras.new('Print overview')
cam.type = 'ORTHO'
cam.ortho_scale = 2750
cam.clip_end = 10000
obj = bpy.data.objects.new('Print overview', cam)
sc.collection.objects.link(obj)
obj.location = (845,1240,4500)
sc.camera = obj
sc.render.resolution_x = 1600
sc.render.resolution_y = 2400
sc.render.resolution_percentage = 100
sc.render.image_settings.file_format = 'PNG'
sc.render.filepath = str(ROOT/'drawings/06_print_plates.png')
bpy.ops.render.render(write_still=True)
Image.open(sc.render.filepath).convert('RGB').save(ROOT/'drawings/06_print_plates.webp',quality=90)
