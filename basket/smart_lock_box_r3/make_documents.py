"""Generate the Korean PDF, BOM and blank hardware acceptance data sheet."""
from pathlib import Path
import json,csv,math,sys
from reportlab.pdfgen import canvas
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
from reportlab.lib.colors import HexColor
ROOT=Path(__file__).resolve().parent
FONT=Path(sys.argv[1]) if len(sys.argv)>1 else Path('C:/Windows/Fonts/malgun.ttf')
pdfmetrics.registerFont(TTFont('KR',str(FONT)))
W,H=842,595
C=canvas.Canvas(str(ROOT/'assembly_and_circuit_r3.pdf'),pagesize=(W,H))
C.setTitle('R3-A4 스마트 잠금 상자 — 단일 수납·이중 바닥·회로 및 검증')
C.setAuthor('R3 design review')
INK='#17384A';MUTED='#52717B';TEAL='#087F8C';PALE='#EFF5F6';RED='#AF3F31';LINE='#CEDDE2'
page=0
def text(x,y,s,size=10,color=INK):C.setFillColor(HexColor(color));C.setFont('KR',size);C.drawString(x,H-y,str(s))
def line(x,y,xx,yy,color=LINE,width=1):C.setStrokeColor(HexColor(color));C.setLineWidth(width);C.line(x,H-y,xx,H-yy)
def rect(x,y,w,h,color=PALE):C.setFillColor(HexColor(color));C.rect(x,H-y-h,w,h,stroke=0,fill=1)
def para(x,y,s,width,size=10,leading=16,color=INK):
    yy=y
    for part in s.split('\n'):
        row=''
        for char in part:
            if row and pdfmetrics.stringWidth(row+char,'KR',size)>width:text(x,yy,row,size,color);yy+=leading;row=''
            row+=char
        if row:text(x,yy,row,size,color);yy+=leading
    return yy
def new(title,sub):
    global page
    if page:C.showPage()
    page+=1;rect(0,0,W,H,'#FFFFFF');rect(0,0,10,H,TEAL)
    text(34,30,'SMART LOCK BOX  /  R3-A4  /  2026-09-09',9,TEAL)
    text(34,61,title,23);text(34,84,sub,9,MUTED)
    line(34,557,808,557);text(34,578,'CAD·소프트웨어 검사 완료 / 실제 A4 검출·강도·발열·테미 장착은 실물 검수 대상',8,MUTED);text(777,578,f'{page:02}',9,MUTED)
def image(name,x,y,w,h):C.drawImage(str(ROOT/'drawings'/name),x,H-y-h,w,h,preserveAspectRatio=True,anchor='c',mask='auto')
def table(x,y,width,heads,rows,widths,size=9,rowh=29):
    rect(x,y,width,rowh,TEAL);xx=x
    for h,w in zip(heads,widths):text(xx+7,y+18,h,size,'#FFFFFF');xx+=w
    for i,row in enumerate(rows):
        yy=y+(i+1)*rowh;rect(x,yy,width,rowh,'#EDF4F6' if i%2==0 else '#FFFFFF');xx=x
        for value,w in zip(row,widths):para(xx+7,yy+17,str(value),w-14,size,12);xx+=w
def box(x,y,w,h,title,sub=''):
    rect(x,y,w,h);text(x+9,y+20,title,11,TEAL)
    if sub:para(x+9,y+37,sub,w-18,9,13)
def arrow(x,y,xx,yy):
    line(x,y,xx,yy,TEAL,1.5);a=math.atan2(yy-y,xx-x)
    for t in (a+.5,a-.5):line(xx,yy,xx-7*math.cos(t),yy-7*math.sin(t),TEAL,1.5)

new('A4 한 장을 위한 단일 수납·이중 바닥','상부 칸막이 제거 / 아래층 전원·회로·로드셀·잠금 / 원본 보존 후 별도 수정')
image('01_single_tray_open.png',34,104,380,280);image('07_double_floor_exploded.png',426,104,380,280)
box(34,401,245,80,'단일 적재판','213.5 × 308 mm, 상부 깊이 19 mm\nA4가 고정벽에 닿지 않도록 배치')
box(290,401,248,80,'아래 전장층','로드셀 + HX711, Nano, 전원 보호\n가로 솔레노이드는 적재판 아래로 이동')
box(549,401,257,80,'외형 255 × 324 × 78 mm','A4 폭 확보로 기존보다 12 mm 넓음\n배터리는 미선정, 안정화 12 V 기준')
para(34,511,'A4 80 g/㎡ 한 장은 계산상 4.9896 g. 모델과 합성 신호 시험은 실제 종이 검출 성능을 보증하지 않는다. 오른쪽 그림은 적재판 +70 mm, 보호판 +25 mm를 띄운 구조 설명용 분해도다.',770,10,16,RED)

new('하중은 단일점 로드셀로만 전달','FSR 12개와 MUX, 상부 칸막이 제거 / 전장 보호판과 적재판을 분리')
image('04_single_point_load_path.png',34,104,430,288)
table(480,109,326,['층','Z 범위 / 역할'],[
 ('적재판','48~51 / 알루미늄 3 mm'),('비접촉 틈','44~48 / 정상 상태 4 mm'),('고정 보호판','41~44 / 전장 덮개'),
 ('가동 스페이서','36~48 / 보호판 개구 통과'),('L6D 센서','14~36 / 3 kg 총 하중 기준'),('고정 받침·바닥','6~14 / 기구 하중 전달')],[95,231],rowh=34)
para(34,420,'선정 기준: Zemic L6D-C3-3kg-0.40B + HX711. 플랫폼 213.5×308 mm는 센서 권장 250×350 mm 이내다. 3 kg는 적재판·라이너·스페이서 무게를 포함한 센서 정격이다. 허용 물품 무게는 미정이며 펌웨어 1,000 g 한도는 임시 제한이다.',770)
para(34,487,'과부하 스토퍼의 초기 틈은 0.8 mm. 실물에서 처짐과 코너 하중을 측정해 심을 조정한다. 고정벽·접착제·전선·덮개가 적재판을 받치면 안 된다. 제조사 도면과 구매품의 M6 구멍, 체결 토크, 하중 방향을 확인한 뒤 가공한다.',770,10,16,RED)

new('잠금장치와 정비 체결부를 아래층에 배치','위쪽 수납면은 한 장의 평판 / 키퍼만 오른쪽 틈으로 내려옴')
image('05_underfloor_latch.png',34,110,395,300)
para(448,123,'핀 방향: X축\n잠김 끝: X242 / 해제 끝: X237.7\n키퍼: X239~240.5\n스트로크: 4.3 mm / 명목 해제 틈: 1.3 mm\n잠긴 상승 차단: 약 0.2°\n키퍼 위치 ±0.3 mm 조합 27개도 검사',350,11,23)
para(448,296,'초기 수정은 3.25°에서 벽 충돌을 검출했다. 오른쪽 벽 안에 회전 여유 포켓을 추가한 뒤 전체 경로를 재검사했다. 여유 포켓 바깥 벽은 7 mm지만 재료 강도/잠금 인장 시험을 대신하지 않는다.',350,10,17)
para(34,437,'체결: 키퍼 M2.5 나사는 도어 밑면에서 접근, 바깥 상면 2 mm 재료 보존. 전면 패널 나사는 내부 수직 체결, 외부 USB 개구 제거. 패널과 전장 보호판은 별도 접점으로 감시한다.',770)
para(34,495,'닫기: 정지 → 인증된 CLOSE → 후퇴 확인 → 수동으로 도어 내리기 → 닫힘/전진 확인. 스프링 캠을 도어 자중으로 누를 수 있다고 가정하지 않는다. 실제 레버 형상과 0.3 mm 이상 후퇴 여유를 실측해야 한다.',770,10,16,RED)

new('전원·솔레노이드 구동 회로','기준 입력: 안정화 12 V / 내부에는 저전압 DC 회로만 배치 / 배터리 직결 금지')
box(34,110,138,61,'J0 12 V 입력','외부 안정화 DC');arrow(172,140,196,140)
box(196,110,140,61,'F1 + D0','T1A → SB310');arrow(336,140,365,140)
box(365,110,189,61,'V12_PROTECTED','470 µF / 25 V → GND');arrow(554,140,587,140)
box(587,110,219,61,'솔레노이드 +','5065 12 V / 코일 - → Drain')
arrow(449,171,449,218);box(359,219,206,62,'D24V10F5','VIN 12 V → VOUT 5 V')
arrow(359,250,313,250);box(34,219,279,62,'Nano 5V / BLE용 3.3V LDO','Nano VIN 미연결 / USB VBUS 중복급전 방지')
arrow(565,250,602,250);box(602,219,204,62,'HX711 아날로그 5 V','페라이트 + 10 µF + 100 nF')
table(34,314,772,['연결','점대점 배선'],[
 ('코일 귀환','코일 - → IRLZ44N Drain, Source → STAR_GND'),
 ('게이트','D7 → 220 Ω → Gate, Gate → 47 kΩ → GND'),
 ('역기전력 다이오드','1N4007 캐소드 띠 → 코일 +, 애노드 → 코일 -/Drain'),
 ('전압 감시 A6','보호 버스 → 100 kΩ / 33 kΩ 분압 → 1 kΩ → A6, A6 100 nF → GND'),
 ('그라운드','코일 전류와 센서 GND를 분리 배선하고 전원부 공통점에서 결합')],[145,627],rowh=33)
para(34,535,'3S 보호 팩/BMS는 충전기·12 V 안정화 장치가 아니다. 배터리 자립형은 선정 팩 + 전용 충전 + 승강압 + 체결/발열 검증이 추가로 필요하다.',770,8.5,13,RED)

new('로드셀·HX711·Nano 핀 연결','아날로그 신호선은 차폐 / HX711 10 SPS·채널 A·gain 128 / 보드의 실제 AVDD 구성 확인')
table(34,108,370,['로드셀 / HX711','연결'],[
 ('L6D 빨강 / 검정','E+ = 아날로그 5 V / E- = GND'),('L6D 초록 / 흰색','INA+ / INA- (각 100 Ω 직렬)'),
 ('입력 차동 필터','INA+ ↔ INA- 사이 100 nF'),('HX711 전원','VSUP/DVDD 5 V, AVDD 깨끗한 5 V'),
 ('외부 AVDD 방식','VFB GND / BASE NC / AGND 공통점'),('RATE / XI / XO','GND / GND / NC'),
 ('DOUT / PD_SCK','Nano D8 / D9'),('실드','아날로그 GND에 한쪽 끝만 접속')],[136,234],rowh=35,size=8.5)
table(425,108,381,['Nano','용도'],[
 ('D2 / D5','핀 후퇴 / 전진 확인'),('D3 / D4','도어 닫힘 / 격납 리드'),('D6 / D7','상태 LED / MOSFET Gate'),
 ('D8 / D9','HX711 데이터 / 클록'),('D10 / D11','BLE RX / TX 분압 1 kΩ + 2 kΩ'),
 ('D12 / A3','전장 보호판 / 전면 패널 닫힘'),('A6','보호 12 V 버스 전압 감시'),('5V / GND','안정화 5 V 입력 / 공통점')],[93,288],rowh=35,size=8.5)
para(34,450,'D2/D3/D4/D5/D12/A3: INPUT_PULLUP, 접점 반대쪽 GND. LOW일 때만 해당 상태가 확인된 것으로 해석한다. NC/NO 명칭만으로 판정하지 않고 실제 위치와 단선 상태를 확인한다. 패널 열림은 고장으로 유지하며 다시 닫았다고 자동 해제하지 않는다.',772)
para(34,513,'J1: 코일 2P / J2: 잠금·리드 8P / J3: 셀·실드 5P / J4: 보호판·패널 4P. 정확한 핀 순서와 배선 규칙은 netlist.md를 따른다. 회로 명세이며 제작 완료된 PCB 파일은 아니다.',772,9,14,RED)

new('펌웨어·인증·보정 동작','소프트웨어는 실제 Nano용으로 컴파일 / 보드 업로드 및 로봇 주행은 수행하지 않음')
table(34,109,772,['조건','동작'],[
 ('전원 인가','코일 OFF, EEPROM 영점·감도 검사. 자동 영점 없음. 센싱 초기 15초 대기'),
 ('정지·측정','정지 1초 + 새 20샘플, 창 내 최대-최소 ≤0.8 g, ADC 350 ms 이내 최신'),
 ('적재 판단','ON 3 g / OFF 1 g + 유지시간. 잡음·이동·범위 초과면 weight_valid=0'),
 ('해제·닫기','최신 정지/정상 스위치/전압/고장 없음, 최대 2초 통전 후 최소 6초 휴지'),
 ('인증','CHAL verb → nonce → AUTH HMAC-SHA256. 1회·2초·채널·명령 결합'),
 ('상태 응답','응답도 같은 nonce로 HMAC. 오래된 상태나 평문 STATUS로 이동 허가 금지'),
 ('물리 정비','키 등록 및 빈 판→500 g 보정은 열린 도어/패널의 USB 정비 상태에서만')],[135,637],rowh=40,size=9)
para(34,450,'부팅 번호는 EEPROM 두 슬롯에 검증 후 사용한다. 키는 256비트 OS 난수로 만들고 패키지에 넣지 않는다. 평문 PIN 명령을 제거했으며 상태 기밀성/BLE 링크 암호화까지 구현한 것은 아니다.',770)
para(34,511,'실제 테미 앱은 참조 클라이언트와 별도 연동해야 한다. 실제 속도/이동 상태로 정지 heartbeat를 만들고, 이동 전에는 새 ready_to_travel, 이동 중에는 containment_ok를 검사한다.',770,10,16,RED)

summary=json.loads((ROOT/'validation/summary.json').read_text(encoding='utf8')) if (ROOT/'validation/summary.json').exists() else None
new('검증 결과와 실물 승인 조건','README 주장 대신 모델·소스·기록을 대조 / 합성 신호를 실제 종이 시험으로 표기하지 않음')
rows=[('해제 이동',f"{summary['release_motion_samples'] if summary else 632}자세, 도어 간섭 0건"),('실제 애니메이션','144프레임, 핀/도어 동시 검사, 간섭 0건'),
      ('잠김/공차','차단 약 0.2°, 키퍼 ±0.3 mm 조합 27개'),('상판 분리','0~0.6 mm 가정 하강, 고정판·스토퍼 간섭 0건'),
      ('정지 조립','명시한 나사 체결·구매품 내부 외형 외 미해결 간섭 0건'),
      ('펌웨어','C++ 207 + 실제 스케치 I/O 모의 28 + 통신 10 검사 통과'),
      ('AVR 컴파일',f"Flash {summary['flash_bytes'] if summary else '-'} B / 정적 RAM {summary['static_ram_bytes'] if summary else '-'} B")]
table(34,108,772,['컴퓨터에서 확인','결과'],rows,[137,635],rowh=33,size=9)
para(34,402,'실물 미검증: 5 g 분동 9지점×10회, A4 한 장 9배치×10회 적재·회수, 30분 영점 드리프트, 물건을 둔 재부팅, 센서 단선/쇼트, 저전압·발열, 실제 잠금 인장, watchdog 복구, 로봇 헤드/장착 간섭.',770,11,19,RED)
para(34,468,'격납 도어는 상자 위 174.2 mm 돌출한다. 외형 폭이 12 mm 증가했다. 최대 적재 무게와 목표 잠금 인장하중은 미정이다. 테스트 표의 NOT_RUN을 실측 없이 PASS로 바꾸면 안 된다.',770,10,16)
para(34,515,'재검증: run_validation.py. 상세 근거: validation/summary.json, geometry.json, static_intersections.json, exports.json. 실물 절차: HARDWARE_ACCEPTANCE.md, hardware_measurements.csv.',770,9,14)

new('제작·정비와 출처','구매품은 외형 모델 / 판재·금속 가이드·키퍼는 형상 참고 / 실제 구매 도면을 우선')
para(34,119,'제작 전 확인\n• 324 mm 길이 부품은 일반 220 mm 출력 베드에 들어가지 않는다. 큰 평판은 절삭/판재로 검토하고 포켓·장착부는 가공 조건을 맞춘다.\n• 금속 가이드 상단의 1 mm 구간은 강도 검증 대상이다. 단순히 금속으로 바꿨다고 하중 등급이 생기지 않는다.\n• 5065 스위치 레버와 장착 공간은 조정용 외형이다. 실제 스위치 작동점과 최소 후퇴 여유를 시험한다.\n• 적재판 나사를 풀기 전에 로드셀 가동단을 비틀거나 전선을 잡아당기지 않는다. 정비 후 보정·반복성 시험을 한다.',770,11,21)
text(34,316,'주요 근거와 다시 확인할 자료',14,TEAL)
sources=[('Adafruit 5065 / 제조 규격서','https://cdn-shop.adafruit.com/product-files/5065/5065_C16212.pdf'),
 ('Zemic L6D / Rev12 정격','https://www.zemiceurope.com/media/Documentation/L6D_Datasheet.pdf'),
 ('Zemic 제조사 Rev8 치수 도면 보관본','https://www.imajteknik.net/uploads/datasheet-l6d.pdf'),
 ('AVIA HX711 데이터시트','https://cdn.sparkfun.com/datasheets/Sensors/ForceFlex/hx711_english.pdf'),
 ('HMAC 구현 / 시험 벡터','https://rweather.github.io/arduinolibs/classSHA256.html')]
for i,(name,url) in enumerate(sources):
    y=348+i*37;text(34,y,name,10);text(34,y+15,url,8.3,MUTED)
    C.linkURL(url,(34,H-y-17,800,H-y+9),relative=0)
C.save()

BOM=[
 ('U1','Arduino Nano ATmega328P 5 V',1,'검증한 AVR core 1.8.8. watchdog 복구 가능한 부트로더를 실제 확인'),
 ('LC1','Zemic L6D-C3-3kg-0.40B',1,'총 하중 3 kg, 플랫폼 250×350 mm. 실제 형상·M6·출구 확인'),
 ('U2','HX711 carrier, external AVDD 5 V configuration',1,'10 SPS / A gain128. 내부 4.3 V 모듈을 5 V로 오인하지 않음'),
 ('L1','Adafruit 5065 / HD2728L-12-28A',1,'12 V 솔레노이드, 4.3 mm 모델 스트로크. 스프링·스위치 실측'),
 ('U3','Pololu D24V10F5',1,'12→5 V, Nano 5V 입력'),('Q1','IRLZ44N',1,'5 V 게이트 논리레벨 MOSFET'),
 ('D0','SB310',1,'입력 역극성 보호'),('D1','1N4007',1,'코일 플라이백, 띠=코일 +'),('F1','T1A DC fuse + holder',1,'입력 퓨즈. 허용 전압·차단용량 확인'),
 ('J0','12 V panel connector',1,'안정화 외부 12 V, 입력 공급원은 사용자 미선정'),
 ('S1,S2','Low force lever microswitch',2,'후퇴/전진; 긴 레버 조정 외형, 정확한 구매품 미선정'),
 ('S3,S4','Reed switch + magnet',2,'도어/격납, 실제 감지거리 확인'),('S5,S6','Panel/deck installed switch',2,'정상 장착시 GND 접점 닫힘, 단선시 고장'),
 ('U4','3.3 V UART BLE module',1,'실물 미선정, 27×13 mm 예약 외형'),('U5','MCP1700-3302 or specified 3.3 V LDO',1,'5 V 입력, 입출력 각 1 µF'),
 ('RG,RPD','220 Ω / 47 kΩ','각 1','게이트 직렬/풀다운'),('RV1,RV2','100 kΩ / 33 kΩ 1%','각 1','A6 전압 분압'),
 ('R1K','1 kΩ',4,'A6 직렬 / LED / BLE RX 분압 상단 / 예비'),('RB','2 kΩ',1,'BLE RX 분압 하단'),
 ('RHX','100 Ω matched',2,'HX711 차동 입력 각 1개'),('FB1','Low DCR ferrite bead',1,'HX711/셀 아날로그 5 V'),
 ('C12','470 µF 25 V',1,'보호 12 V 버스'),('C5','47 µF',1,'5 V 버스'),('CHX','10 µF + 100 nF','각 1','AVDD 근접'),
 ('CDIFF','100 nF',1,'HX711 INA+~INA-'),('CADC','100 nF',1,'A6 GND 필터'),('CBYP','100 nF',4,'보드 바이패스'),('CLDO','1 µF',2,'LDO 입출력'),
 ('LED','Status LED',1,'D6 / 직렬 1 kΩ'),('J1..J4','Keyed connector pairs','4 세트','2P 코일, 8P 상태, 5P 셀, 4P 탬퍼'),
 ('P1','213.5×308×3 mm aluminium tray',1,'상면 나사 플러시, 모서리 디버링; 사하중 약 0.53 kg'),
 ('P2','Fixed electronics protection deck',1,'Z41~44, 셀 스페이서/키퍼 개구'),('K1','1.5 mm stainless keeper',1,'밑면 체결, 절곡·인장 시험 필요'),
 ('G1,G2','Metal door guides',2,'기존 가이드 형상. 상단 1 mm 구간 강도 미검증'),('LID','8 mm machined door',1,'도어 밑면 블라인드 키퍼 체결'),
 ('MECH','Base, wall parts, spacers, stops, screws','1 세트','STL/형상참고. 큰 부품은 출력기/가공 방식 결정 필요'),
 ('HARNESS','Shielded cell cable + coil/signal harness','1 세트','셀 신호/코일 전류 분리; 정비 여장 및 그로밋'),
 ('BATTERY','NOT SELECTED / NOT FITTED',0,'배터리 요구 시 보호팩+전용충전+12 V 승강압 및 체결 검증 추가')]
with (ROOT/'BOM.csv').open('w',encoding='utf-8-sig',newline='') as f:
    w=csv.writer(f);w.writerow(['reference','part','quantity','procurement_note']);w.writerows(BOM)
# Never overwrite actual measurements on regeneration.
target=ROOT/'hardware_measurements.csv'
if not target.exists():
    with target.open('w',encoding='utf-8-sig',newline='') as f:
        w=csv.writer(f);w.writerow(['test','position','x_center_mm','y_center_mm','repeat','reference_mass_g','measured_g','stable_pp_g','detect_delay_s','removed_detected','result'])
        for kind,xs,ys in [('A4_flat',[129.5,130.75,132],[157,162,167]),('5g_point',[35,130.75,226],[20,162,304])]:
            for pos,(x,y) in enumerate(( (x,y) for x in xs for y in ys),1):
                for rep in range(1,11):w.writerow([kind,pos,x,y,rep,'','','','','','NOT_RUN'])
print('PDF_BOM_AND_ACCEPTANCE_SHEET_WRITTEN')
