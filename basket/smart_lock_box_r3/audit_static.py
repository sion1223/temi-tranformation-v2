"""Inventory all mesh intersections at assembly rest. Review intentional threaded/mated joints explicitly."""
import sys,json
from pathlib import Path
ROOT=Path(__file__).resolve().parent
sys.path.insert(0,str(ROOT))
import verify_geometry as v
rows=[]
gs={o.name:v.geom(o) for o in v.objects}
for i,a in enumerate(v.objects):
    for b in v.objects[i+1:]:
        if v.intersects(gs[a.name],gs[b.name]):rows.append([a.name,b.name])
def accepted(a,b):
    pair={a,b}
    if a.startswith('R3_deck_support') and b.startswith('R3_deck_inside_M3'):return 'M3 thread engagement (thread envelope)'
    if 'R3_captive_front_panel' in pair and any(n.startswith('R3_panel_inside_M3') for n in pair):return 'M3 thread engagement (thread envelope)'
    if '도어_본체' in pair and any(n.startswith('도어_숄더볼트') for n in pair):return 'Original shoulder bolt threaded engagement'
    if 'R3_L6D_3kg_envelope' in pair and any(n.startswith(('R3_cell_loaded_M6','R3_cell_fixed_M6')) for n in pair):return 'M6 thread engagement in manufacturer sensor'
    if '잠금_5065_프레임' in pair and (pair & {'잠금_5065_코일','잠금_5065_플랜지','R3_lock_bolt_4p3mm'}):return 'Internal parts of one purchased solenoid; envelope-only CAD'
    return None
review=[{'pair':r,'reason':accepted(*r) or accepted(*reversed(r))} for r in rows]
unexpected=[r for r in review if not r['reason']]
report={'all_pairs':review,'unexpected':unexpected,'pass':not unexpected,
        'scope':'Rest assembly, mesh surfaces/containment. Only explicit threaded joints and internal solenoid envelopes accepted.'}
(ROOT/'validation/static_intersections.json').write_text(json.dumps(report,ensure_ascii=False,indent=2),encoding='utf8')
print('STATIC_PAIRS',len(rows))
for r in rows:print(' / '.join(r))
if unexpected:raise RuntimeError('Unresolved static assembly intersections')
