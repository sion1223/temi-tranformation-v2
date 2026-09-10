"""Derive real millimetre STL solids and a plate-layout blend from the R4 blend.

Run in Blender's Python with numpy, trimesh and manifold3d installed, or:
  python prepare_print.py   # Python 3.11 + bpy==4.5.3
No electronic or purchased envelopes are ever selected for STL export.
Large parts receive 8 mm stepped adhesive lap joints with 0.15 mm clearance.
"""
import csv
import hashlib
import itertools
import json
from pathlib import Path

import bpy
import manifold3d as md
import numpy as np
import trimesh
from mathutils import Matrix, Vector, Quaternion

ROOT = Path(__file__).resolve().parent
OUT = ROOT / 'print_ready'
STL = OUT / 'STL_print'
STL.mkdir(parents=True, exist_ok=True)
for p in STL.glob('*.stl'):
    p.unlink()

BED = (220, 220, 250)
USABLE = 216.0                 # 2 mm reserved along each bed edge
LAP = 8.0
GAP = .15
ALIASES = {'셸_격납슬롯내벽': 'slot_inner_wall', '도어_본체': 'lid'}
METAL_ONLY = ('6mm_lift_shaft', '8mm_axle', 'carry_handle_',
              'keyed_lid_hub_', 'lid_hub_arm_', 'low_pickup_ear_',
              'weighing_cradle', 'lid_stop_arm', 'open_stop_adjuster',
              'R3_cell_', 'R3_lock_base', 'R3_door_keeper')


def electronic(o):
    groups = {c.name for c in o.users_collection}
    return bool(groups & {'05_전장', '07_배선_커넥터', '12_R4_electronics'}) or (
        '14_R4_routes' in groups and 'belt' not in o.name) or any(
        s in o.name for s in ('_wire', '_signal_route', '_power_route',
                              '_supply_route', '_linear_feedback_',
                              '_HOME_', '_UPPER_', 'R4_LID_OPEN_switch'))


def printable(o):
    return (o.type == 'MESH' and not electronic(o)
            and o.get('kind') in ('print', 'sheet_fabrication', 'metal_fabrication')
            and not any(s in o.name for s in METAL_ONLY))


def to_trimesh(solid):
    # Collapse coplanar Boolean ancestry before triangulation. Otherwise a
    # stepped cut can contain a zero-area T-junction triangle in float32 STL.
    m = solid.as_original().simplify(1e-5).to_mesh64()
    mesh = trimesh.Trimesh(np.asarray(m.vert_properties)[:, :3],
                           np.asarray(m.tri_verts), process=True)
    mesh.merge_vertices(digits_vertex=5)
    mesh.update_faces(mesh.nondegenerate_faces(height=1e-7))
    mesh.update_faces(mesh.unique_faces())
    mesh.remove_unreferenced_vertices()
    return mesh


def to_manifold(mesh, name):
    mesh.merge_vertices(digits_vertex=5)
    mesh.remove_unreferenced_vertices()
    if not mesh.is_watertight or not mesh.is_winding_consistent:
        raise ValueError(f'{name}: source is not a closed, consistently wound solid')
    solid = md.Manifold(md.Mesh(np.asarray(mesh.vertices, dtype=np.float32),
                               np.asarray(mesh.faces, dtype=np.uint32)))
    if solid.status() != md.Error.NoError or solid.volume() <= 0:
        raise ValueError(f'{name}: {solid.status()}, volume={solid.volume()}')
    return solid


def object_mesh(o):
    o.data.calc_loop_triangles()
    return trimesh.Trimesh(
        [tuple(o.matrix_world @ v.co) for v in o.data.vertices],
        [tuple(t.vertices) for t in o.data.loop_triangles], process=True)


def orient(mesh):
    """24 right-angle orientations; shortest height, then largest bed contact."""
    candidates = []
    for perm in itertools.permutations(range(3)):
        for signs in itertools.product((-1, 1), repeat=3):
            r = np.eye(3)[list(perm)] * np.array(signs)[:, None]
            if np.linalg.det(r) < .9:
                continue
            vertices = mesh.vertices @ r.T
            lo, hi = vertices.min(axis=0), vertices.max(axis=0)
            triangles = vertices[mesh.faces]
            bottom = np.all(abs(triangles[:, :, 2] - lo[2]) < .01, axis=1)
            contact = float(mesh.area_faces[bottom].sum())
            candidates.append(((round(float(hi[2]-lo[2]), 3), -contact), r, lo))
    _, r, lo = min(candidates, key=lambda x: x[0])
    transform = np.eye(4)
    transform[:3, :3] = r
    transform[:3, 3] = -lo
    result = mesh.copy()
    result.apply_transform(transform)
    return result, transform


def half(solid, axis, position, positive):
    n = [0., 0., 0.]
    n[axis] = 1. if positive else -1.
    return solid.trim_by_plane(n, position if positive else -position)


def lap_split(solid, axis, centre, joint_z=None):
    bounds = to_trimesh(solid).bounds
    middle_z = float((bounds[0, 2] + bounds[1, 2]) / 2) if joint_z is None else joint_z
    # Complementary stepped surfaces; adhesive fills the deliberate small gap.
    a = half(solid, axis, centre-LAP/2-GAP/2, False) + half(
        half(solid, axis, centre+LAP/2-GAP/2, False), 2, middle_z-GAP/2, False)
    b = half(solid, axis, centre+LAP/2+GAP/2, True) + half(
        half(solid, axis, centre-LAP/2+GAP/2, True), 2, middle_z+GAP/2, True)
    return a, b


def tile(solid, joint_z=None):
    todo = [(solid, [])]
    ready = []
    while todo:
        item, joins = todo.pop(0)
        bounds = to_trimesh(item).bounds
        spans = bounds[1] - bounds[0]
        axis = int(np.argmax(spans[:2]))
        if spans[axis] <= USABLE + .001:
            ready.append((item, joins))
            continue
        centre = float((bounds[1, axis] + bounds[0, axis]) / 2)
        joint = {'axis_in_print_frame': axis, 'centre_mm': centre,
                 'lap_mm': LAP, 'clearance_mm': GAP,
                 'step_z_mm': joint_z if joint_z is not None else float((bounds[0,2]+bounds[1,2])/2)}
        for piece in lap_split(item, axis, centre, joint_z):
            for component in piece.decompose():
                if component.volume() <= .01:
                    raise ValueError('A cut produced an unprintable tiny fragment')
                todo.append((component, joins+[joint]))
    return ready


bpy.ops.wm.open_mainfile(filepath=str(ROOT/'smart_lock_box_r4.blend'))
sc = bpy.context.scene
sc.frame_set(1)
bpy.context.view_layer.update()
sources, exclusions = {}, []
for o in sc.objects:
    if printable(o):
        sources[o.name] = object_mesh(o)
    elif o.type in ('MESH', 'CURVE'):
        reason = ('circuit_or_wiring' if electronic(o) else
                  'metal_hardware_keep_metal' if any(s in o.name for s in METAL_ONLY)
                  else 'purchased_or_reference_envelope')
        exclusions.append({'object': o.name, 'kind': o.get('kind', ''), 'reason': reason})

# Fuse the paper tray's front/rear fences into its printed body: no loose walls.
tray_names = ('R4_paper_tray', 'R4_front_paper_lip', 'R4_rear_paper_lip')
tray_solid = md.Manifold()
for name in tray_names:
    tray_solid = tray_solid + to_manifold(sources.pop(name), name)
sources['R4_paper_tray_with_end_fences'] = to_trimesh(tray_solid)

# Also provide an assembled, animated mechanical copy with the circuits removed.
removed = []
for o in list(sc.objects):
    if electronic(o) or o.get('kind') in ('annotation', 'reference_only') or o.type == 'FONT':
        removed.append(o.name)
        bpy.data.objects.remove(o, do_unlink=True)
sc['circuits_removed'] = True
sc['print_instructions'] = 'This scene includes real metal/purchased hardware. Print only STL_print or PRINT_PLATES.'
sc.frame_set(745)
bpy.ops.wm.save_as_mainfile(filepath=str(OUT/'r4_mechanical_no_circuits.blend'), compress=True)

rows, meshes, source_checks = [], [], []
for source_name, source in sorted(sources.items()):
    oriented, transform = orient(source)
    original = to_manifold(oriented, source_name)
    parts = []
    # A raised fence must not move the floor's lap up into empty space.
    joint_z = 1.5 if source_name == 'R4_paper_tray_with_end_fences' else None
    for component in original.decompose():
        parts.extend(tile(component, joint_z))
    combined = md.Manifold()
    for part, _ in parts:
        combined = combined + part
    outside = (combined - original).volume()
    lost = max(0., original.volume() - combined.volume())
    if outside > .05 or lost > original.volume() * .025:
        raise ValueError(f'{source_name}: excessive split change: outside={outside}, lost={lost}')
    source_checks.append({'object': source_name, 'parts': len(parts),
                          'original_mm3': original.volume(), 'added_mm3': outside,
                          'joint_clearance_removed_mm3': lost})
    for j, (part, joints) in enumerate(parts, 1):
        mesh = to_trimesh(part)
        shift = mesh.bounds[0].copy()
        mesh.apply_translation(-shift)
        name = ALIASES.get(source_name, source_name).replace('.', '_').replace('-', 'm')
        name += f'__{j:02d}'
        if not mesh.is_watertight or not mesh.is_winding_consistent or mesh.volume <= 0:
            raise ValueError(f'{name}: invalid STL solid')
        if np.any(mesh.extents > np.array([USABLE, USABLE, BED[2]])+.001):
            raise ValueError(f'{name}: exceeds usable bed')
        path = STL/(name+'.stl')
        mesh.export(path, file_type='stl')
        # Read the actual delivered STL, including float32 serialization.
        delivered = trimesh.load_mesh(path, process=True)
        if not delivered.is_watertight or not delivered.is_winding_consistent or delivered.volume <= 0:
            raise ValueError(f'{name}: STL read-back failed')
        to_source = np.linalg.inv(transform)
        shift_matrix = np.eye(4)
        shift_matrix[:3, 3] = shift
        to_source = to_source @ shift_matrix
        row = {'id': f'P{len(rows)+1:03d}', 'file': path.name, 'source': source_name,
               'quantity': 1, 'dimensions_mm': [round(float(v), 3) for v in delivered.extents],
               'volume_mm3': round(float(delivered.volume), 3), 'watertight': True,
               'winding_consistent': True, 'connected_solids': 1,
               'bed_contact_z_mm': round(float(delivered.bounds[0, 2]), 6),
               'material': 'PETG prototype', 'joints': joints,
               'support': 'lap/overhang supports from build plate as needed' if joints else 'inspect overhangs in slicer',
               'print_to_assembly_mm': to_source.tolist(),
               'sha256': hashlib.sha256(path.read_bytes()).hexdigest()}
        rows.append(row)
        meshes.append((mesh, row))
        print(row['id'], name, row['dimensions_mm'], flush=True)

bpy.ops.wm.read_factory_settings(use_empty=True)
sc = bpy.context.scene
sc.name = 'PRINT_PLATES_220mm'
sc.unit_settings.system = 'METRIC'
sc.unit_settings.scale_length = .001
sc['units'] = 'All STL coordinates are millimetres. Import at 100 percent scale.'
sc['bed_mm'] = list(BED)
sc['layout'] = 'Each Pxxx collection is ONE 220 x 220 mm plate, not one giant build.'
sc['joint'] = '8 mm stepped adhesive laps / 0.15 mm clearance; dry-fit and bond, finish paper surface flush.'
sc['physical_print_tested'] = False
assembly = bpy.data.scenes.new('ASSEMBLED_PRINTED_PARTS')
assembly.unit_settings.system = 'METRIC'
assembly.unit_settings.scale_length = .001
assembly['excludes'] = 'Electronics, motors, screws, shafts, metal-only load-path parts'
palette = [(0.06,.48,.57,1),(.91,.41,.10,1),(.44,.58,.69,1),(.24,.42,.58,1)]
materials = []
for k, color in enumerate(palette):
    mat = bpy.data.materials.new('PETG_'+str(k))
    mat.diffuse_color = color
    materials.append(mat)
for i, (mesh, row) in enumerate(meshes):
    collection = bpy.data.collections.new(row['id']+'_'+Path(row['file']).stem)
    sc.collection.children.link(collection)
    data = bpy.data.meshes.new(Path(row['file']).stem)
    data.from_pydata(mesh.vertices.tolist(), [], mesh.faces.tolist())
    data.materials.append(materials[i % len(materials)])
    obj = bpy.data.objects.new(row['id']+'_'+Path(row['file']).stem, data)
    collection.objects.link(obj)
    dx, dy = (i % 7)*245, (i // 7)*255
    obj.location = (dx+(220-mesh.extents[0])/2, dy+(220-mesh.extents[1])/2, 0)
    obj['print_id'] = row['id']
    obj['source_object'] = row['source']
    obj['kind'] = 'print'
    obj['stl_file'] = 'STL_print/'+row['file']
    obj['supports'] = row['support']
    # Bed outline is a curve without thickness, never an exported solid.
    curve = bpy.data.curves.new(row['id']+'_bed_outline', 'CURVE')
    curve.dimensions = '3D'
    spline = curve.splines.new('POLY')
    spline.points.add(3)
    for p, xy in zip(spline.points, ((dx,dy),(dx+220,dy),(dx+220,dy+220),(dx,dy+220))):
        p.co = (*xy,0,1)
    spline.use_cyclic_u = True
    bed = bpy.data.objects.new(row['id']+'_220mm_bed', curve)
    collection.objects.link(bed)
    label = bpy.data.curves.new(row['id']+'_label', 'FONT')
    label.body = row['id']+'  '+Path(row['file']).stem
    label.size = 7
    text = bpy.data.objects.new(row['id']+'_label', label)
    collection.objects.link(text)
    text.location = (dx,dy-12,0)
    assembled = bpy.data.objects.new(obj.name, data)
    assembly.collection.objects.link(assembled)
    assembled.matrix_world = Matrix(row['print_to_assembly_mm'])
    assembled['kind'] = 'print'
    assembled['print_id'] = row['id']
for screen in bpy.data.screens:
    for area in screen.areas:
        if area.type == 'VIEW_3D':
            space = area.spaces.active
            space.clip_end = 20000
            space.shading.color_type = 'MATERIAL'
            space.region_3d.view_distance = 2300
            space.region_3d.view_location = (840, (len(rows)//7)*127, 0)
            space.region_3d.view_rotation = Quaternion((1,0,0,0))
            space.region_3d.view_perspective = 'ORTHO'
bpy.ops.wm.save_as_mainfile(filepath=str(OUT/'smart_lock_box_r4_print.blend'), compress=True)

report = {'pass': True, 'source_blend_sha256': hashlib.sha256((ROOT/'smart_lock_box_r4.blend').read_bytes()).hexdigest(),
          'software': {'bpy': bpy.app.version_string, 'trimesh': trimesh.__version__},
          'units': 'mm', 'bed_mm': list(BED), 'reserved_edge_mm': 2,
          'stl_count': len(rows), 'source_part_count': len(sources),
          'all_stl_readback_watertight': True, 'all_grounded_and_within_bed': True,
          'objects_removed_from_mechanical_blend': removed,
          'non_printed_objects': exclusions, 'source_reconstruction': source_checks,
          'parts': rows, 'physical_print_tested': False,
          'scope': 'Closed positive solids, serialized STL read-back, nominal build envelope, split volume conservation. No slicer G-code, stress, deflection, adhesion or physical fit certification.'}
(OUT/'print_manifest.json').write_text(json.dumps(report, ensure_ascii=False, indent=2)+'\n', encoding='utf-8')
with (OUT/'PRINT_LIST.csv').open('w', newline='', encoding='utf-8-sig') as f:
    w = csv.writer(f, lineterminator='\n')
    w.writerow(['ID','STL','source','quantity','X_mm','Y_mm','Z_mm','material','joining'])
    for r in rows:
        w.writerow([r['id'],r['file'],r['source'],1,*r['dimensions_mm'],r['material'],
                    '8 mm adhesive lap; 0.15 mm clearance' if r['joints'] else 'one piece'])
with (OUT/'NON_PRINTED_PARTS.csv').open('w', newline='', encoding='utf-8-sig') as f:
    w = csv.DictWriter(f, fieldnames=['object','kind','reason'], lineterminator='\n')
    w.writeheader()
    w.writerows(exclusions)
print('PRINT_PREPARATION_COMPLETE', len(rows), 'validated STL solids', flush=True)
