"""Render every PDF page and audit text bounds; never mark hardware rows passed."""
from pathlib import Path
import json,csv
import pymupdf
ROOT=Path(__file__).resolve().parent
out=ROOT/'validation/pdf_pages';out.mkdir(exist_ok=True)
doc=pymupdf.open(ROOT/'assembly_and_circuit_r3.pdf')
assert len(doc)==8
issues=[]
for i,page in enumerate(doc):
    assert len(page.get_text().strip())>100
    for block in page.get_text('dict')['blocks']:
        if block['type']!=0:continue
        for line in block['lines']:
            for span in line['spans']:
                x0,y0,x1,y1=span['bbox']
                if x0<0 or y0<0 or x1>page.rect.width+.1 or y1>page.rect.height+.1:issues.append([i+1,span['text']])
    page.get_pixmap(matrix=pymupdf.Matrix(1.2,1.2)).save(out/f'page_{i+1:02}.png')
assert not issues,issues
with (ROOT/'hardware_measurements.csv').open(encoding='utf-8-sig',newline='') as f:rows=list(csv.DictReader(f))
assert len(rows)==180
assert all(r['result']=='NOT_RUN' for r in rows)
result={'pdf_pages':len(doc),'text_out_of_page_bounds':issues,'all_pages_rendered':True,
        'physical_test_rows':len(rows),'physical_rows_all_not_run':True}
(ROOT/'validation/documents.json').write_text(json.dumps(result,indent=2),encoding='utf8')
print(json.dumps(result,indent=2))
