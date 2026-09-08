"""Rebuild and validate the R3 package. Does not access any robot/serial port."""
from pathlib import Path
import argparse,subprocess,sys,json,re,hashlib,datetime
ROOT=Path(__file__).resolve().parent
V=ROOT/'validation';V.mkdir(exist_ok=True)
p=argparse.ArgumentParser(description=__doc__)
p.add_argument('--blender',required=True);p.add_argument('--arduino-cli',required=True)
p.add_argument('--compiler',required=True,help='g++/clang++ or a portable zig executable')
p.add_argument('--rebuild',action='store_true');a=p.parse_args()
def run(name,cmd):
    r=subprocess.run([str(x) for x in cmd],cwd=ROOT,capture_output=True,text=True,encoding='utf8',errors='replace')
    log=r.stdout+r.stderr;(V/(name+'.log')).write_text(log,encoding='utf8')
    if r.returncode:raise RuntimeError(name+' failed; inspect validation/'+name+'.log\n'+log[-2000:])
    print(name,'PASS',flush=True);return log
if a.rebuild:
    run('build',[a.blender,'-b','--python',ROOT/'build_r3.py'])
    if not (ROOT/'smart_lock_box_r3.blend').is_file():raise RuntimeError('No rebuilt native file')
for name in ('geometry.json','static_intersections.json'):
    q=V/name
    if q.exists():q.unlink()
run('geometry_and_static',[a.blender,'-b','--python',ROOT/'audit_static.py'])
geometry=json.loads((V/'geometry.json').read_text(encoding='utf8'))
static=json.loads((V/'static_intersections.json').read_text(encoding='utf8'))
assert geometry['geometry_pass'] and static['pass']
run('exports',[sys.executable,ROOT/'validate_exports.py'])
fw=ROOT/'firmware/smart_lock_box'
crypto=[fw/'src/Crypto'/name for name in ('SHA256.cpp','Hash.cpp','Crypto.cpp')]
cc=[a.compiler,'c++'] if Path(a.compiler).name.lower().startswith('zig') else [a.compiler]
counts={}
for name in ('core_tests','sketch_tests'):
    exe=V/(name+('.exe' if sys.platform=='win32' else ''))
    run(name+'_compile',cc+['-std=c++17','-O2','-Wno-nullability-completeness','-I',ROOT/'tests/fakes',ROOT/'tests'/(name+'.cpp'),*crypto,'-o',exe])
    log=run(name,[exe]);m=re.search(r'RESULT checks=(\d+) failed=(\d+)',log)
    assert m and int(m[2])==0;counts[name]=int(m[1])
log=run('protocol_tests',[sys.executable,'-m','unittest','discover','-s',ROOT/'tests','-p','test_*.py'])
assert 'OK' in log;counts['python_protocol_tests']=int(re.search(r'Ran (\d+) tests',log)[1])
build=V/'avr_build'
log=run('avr_compile',[a.arduino_cli,'compile','--fqbn','arduino:avr:nano:cpu=atmega328','--warnings','all','--build-path',build,fw])
flash=int(re.search(r'Sketch uses (\d+) bytes',log)[1]);ram=int(re.search(r'Global variables use (\d+) bytes',log)[1])
critical=['smart_lock_box_r3.blend','smart_lock_box_r3.glb','design_parameters.json','build_r3.py','verify_geometry.py','audit_static.py','tools/box_client.py']
critical += [str(x.relative_to(ROOT)).replace('\\','/') for x in fw.rglob('*') if x.is_file()]
summary={'revision':'R3-A4','tested_at_utc':datetime.datetime.now(datetime.timezone.utc).isoformat(),
 'cad_and_software_pass':True,'physical_hardware_pass':None,'physical_paper_detection_tested':False,
 'release_motion_samples':geometry['sampled_release_poses'],'animation_frames':144,
 'locked_contact_degrees':geometry['locked_first_contact_deg'],
 'unexpected_static_intersections':len(static['unexpected']),
 'firmware_host_checks':counts,'arduino_board':'arduino:avr:nano:cpu=atmega328',
 'flash_bytes':flash,'static_ram_bytes':ram,'stack_headroom_bytes':2048-ram,
 'note':'Headroom is a link-time allocation, not a measured runtime stack high-water mark. I/O in sketch tests is mocked.',
 'hardware_release_blockers':['5 g/A4 repeated physical detection and drift','actual load cell/switch/actuator installation','payload and retention-force rating','thermal and low-voltage operation','watchdog/BOD on actual Nano','robot motion adapter and mounting/head sweep','power source or protected battery selection'],
 'sha256':{name:hashlib.sha256((ROOT/name).read_bytes()).hexdigest() for name in critical}}
(V/'summary.json').write_text(json.dumps(summary,ensure_ascii=False,indent=2),encoding='utf8')
print(json.dumps({k:v for k,v in summary.items() if k!='sha256'},ensure_ascii=False,indent=2))
