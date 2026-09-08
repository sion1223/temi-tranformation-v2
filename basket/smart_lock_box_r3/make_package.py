"""Create an integrity-checked deliverable, excluding runtimes/build caches and stale backups."""
from pathlib import Path
import zipfile,hashlib,json,re
ROOT=Path(__file__).resolve().parent
def include(p):
    rel=p.relative_to(ROOT)
    if rel.parts[0]=='vendor' or '__pycache__' in rel.parts:return False
    if any(part in ('avr_build','pdf_pages') for part in rel.parts):return False
    if p.suffix.lower() in ('.exe','.o','.obj','.pyc','.elf','.bin'):return False
    if re.search(r'\.blend\d+$',p.name):return False
    return p.name!='package_manifest.json'
files=sorted(p for p in ROOT.rglob('*') if p.is_file() and include(p))
manifest={'revision':'R3-A4','physical_tests':'NOT_RUN',
          'files':{p.relative_to(ROOT).as_posix():{'sha256':hashlib.sha256(p.read_bytes()).hexdigest(),'bytes':p.stat().st_size} for p in files}}
(ROOT/'package_manifest.json').write_text(json.dumps(manifest,ensure_ascii=False,indent=2),encoding='utf8')
files.append(ROOT/'package_manifest.json')
target=ROOT.parent/'smart_lock_box_r3_package.zip'
with zipfile.ZipFile(target,'w',zipfile.ZIP_DEFLATED,compresslevel=7) as z:
    for p in files:z.write(p,p.relative_to(ROOT).as_posix())
with zipfile.ZipFile(target) as z:
    assert z.testzip() is None
    for name,m in manifest['files'].items():assert hashlib.sha256(z.read(name)).hexdigest()==m['sha256'],name
print(json.dumps({'archive':str(target),'file_count':len(files),'bytes':target.stat().st_size,
                  'sha256':hashlib.sha256(target.read_bytes()).hexdigest(),'integrity_verified':True},ensure_ascii=False,indent=2))
