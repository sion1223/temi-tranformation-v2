"""Write an order-oriented BOM. Prices are checked page values, not quotations."""
from pathlib import Path
import csv,json
from urllib.parse import quote_plus
ROOT=Path(__file__).resolve().parent
rows=[]
def add(ref,name,qty,option,url,source='',usd='',status='판매 페이지 확인 / 배송·최종 재고는 주문 시 확인',model=''):
    rows.append(dict(reference=ref,part=name,quantity=qty,order_option=option,purchase_url=url,technical_source=source,unit_usd=usd,status=status,model_objects=model,checked_date='2026-09-09'))
add('M1','Actuonix PQ12-100-12-S',1,'100:1 / 12V / S형 / 20mm. R형 6V나 P형으로 임의 대체 금지','https://www.actuonix.com/pq12-100-12-s','https://www.actuonix.com/assets/images/datasheets/ActuonixPQ12Datasheet.pdf',65,'페이지 In Stock 13개 표시; 20mm 명목 스트로크 중 18mm를 설계에 사용','L_PQ12_*')
add('M1-C','Actuonix PQ12 Cable Adapter with S Extension Cable',1,'PQ12 S 전용 케이블 어댑터 포함 묶음','https://www.actuonix.com/cable-adapter-s-bundle',usd=6,model='L_motor_cable')
add('U-M','Pololu DRV8876 carrier #4036',1,'15.2×17.8mm / 12V 양방향 구동. 출고 2A 제한을 그대로 사용하지 않음','https://www.pololu.com/product/4036','https://www.pololu.com/file/0J1882/drv887x-single-brushed-dc-motor-driver-carrier-dimensions.pdf',8.15,'판매 페이지 확인, backorder 가능 표시; 즉시 재고 수량 미확인','L_DRV8876*')
add('GR1..2','RobotDigg 440C MGN7 rail + MGN7C carriage',2,'SS_MGN7-200L / 200mm / 표준 MGN7C 블록 1개씩. MGN7H 제외','https://www.robotdigg.com/product/840/440C-SUS-MGN7-linear-rail-with-carriage',model='L_MGN7_200_rail_* / L_MGN7C_*')
add('BR1..4','623ZZ miniature ball bearing',4,'내경3 / 외경10 / 폭4mm / ZZ 양면 실드. V623·F623 대체 금지','https://us.misumi-ec.com/blog/ball-bearings/miniature/623-ball-bearings-3x10x4mm-z-zz-2rs/',model='L_623ZZ_*')
add('GB1..4','RobotDigg LM6UU linear bearing',4,'내경6 / 외경12 / 길이19mm. LM6LUU 35mm 제외','https://www.robotdigg.com/product/42',model='L_LM6UU_*')
add('GS1','Hardened 6mm smooth rod',1,'Ø6×300mm 1개 구입 → 68mm 4개 절단·양끝 면취. 절단 손실 포함','https://www.robotdigg.com/product/1047/6mm-smooth-rods-267mm%2C-300mm%2C-400mm-and-500mm',model='L_6mm_shaft_*')
add('LC1','Zemic L6D-C3-3kg-0.40B',1,'반드시 C3 / 3kg / 0.40B. 판매 페이지에 용량 옵션이 없으면 정확한 형번 확인 후 주문','https://www.dscbalances.com/products/zemic-l6d-aluminium-single-point-load-cell-oiml-approved-3kg-50kg','https://www.zemiceurope.com/media/Documentation/L6D_Datasheet.pdf',30,'L6D 판매·장바구니·재고 표시 확인. 정확한 3kg 옵션 재고는 확인 필요; 제조사 직접 견적도 가능','R3_L6D_3kg_envelope')
add('U-HX','Adafruit HX711 #5974',1,'25.5×23×12.1mm / 10SPS / A채널 gain128 / VCC와 VIO 모두 5V','https://www.adafruit.com/product/5974','https://learn.adafruit.com/adafruit-hx711-24-bit-adc/pinouts',9.95,model='L_HX711*')
add('U1','Arduino Nano A000005',1,'ATmega328P 5V / 18×45mm. 아래쪽 핀 높이 포함 배치 여유 확인','https://store.arduino.cc/products/arduino-nano','https://docs.arduino.cc/resources/datasheets/A000005-datasheet.pdf',model='L_Nano*')
add('U-BLE','Adafruit Bluefruit LE UART Friend #2479',1,'21×32×5mm / UART / 5V 입력 허용 / CTS·RTS 배선','https://www.adafruit.com/products/2479','https://learn.adafruit.com/introducing-the-adafruit-bluefruit-le-uart-friend?view=all',17.5,model='L_BLE*')
add('U-IO','Adafruit MCP23017 breakout #5346',1,'43×18×5mm / 5V / I2C 주소0x20 / 센서 입력 확장','https://www.adafruit.com/product/5346',usd=5.95,model='L_MCP23017*')
add('U5','Pololu D24V10F5 #2831',1,'5V 1A / 17.78×12.7×3.56mm / 12V 입력','https://www.pololu.com/product/2831','https://www.pololu.com/product/2831/specs',12.95,model='L_D24V10F5*')
add('L1','Adafruit lock solenoid #5065',1,'12V / 약350mA / R3 잠금 형상 유지','https://www.adafruit.com/product/5065','https://cdn-shop.adafruit.com/product-files/5065/5065_C16212.pdf',7.5,model='잠금_5065_* / R3_lock_bolt_4p3mm')
add('S1..10','D2F-01L SPDT lever microswitch',10,'PC pin / 12.8×5.8×6.5mm 본체 / 좌우 상·하 4 + 래치2 + 덮개2 + 패널·보호판2','https://www.digikey.com/en/products/detail/omron-electronics-inc-emc-div/D2F-01L/83264','https://components.omron.com/sites/default/files/datasheet_pdf/B036-E1.pdf',1.44,model='L_DOWN_* / L_UP_* / L_latch_* / L_lid_* / L_panel_seated / L_deck_seated')
# Commodity items have exact order specifications, but search links are explicitly not verified listings.
def commodity(ref,name,qty,option,query,model=''):
    add(ref,name,qty,option,'https://www.aliexpress.com/w/wholesale-'+quote_plus(query).replace('+','-')+'.html',status='규격품 검색 링크; 특정 판매자·재고·품질 확인 전',model=model)
commodity('Q1','IRLZ44NPBF',1,'TO-220 / logic-level N-MOSFET / 5V gate','IRLZ44NPBF','L_IRLZ44N')
commodity('D1','1N4007',1,'DO-41 / 솔레노이드 코일 플라이백 전용','1N4007 diode')
commodity('D0','SB310',1,'3A 100V Schottky / 입력 역극성 보호','SB310 diode')
commodity('F1','T1A 250V 5x20 fuse + holder',1,'메인 12V 가지 / DC 차단 정격 확인','T1A fuse 5x20 holder','L_FUSE_MAIN')
commodity('F2','T315mA 250V 5x20 fuse + holder',1,'모터 가지 / DC 차단 정격·기동 내량 실측','T315mA fuse 5x20 holder','L_FUSE_MOTOR')
commodity('R-set','금속피막 1% 0.25W 저항',1,'220Ω×1;47kΩ×1;10kΩ×4;100kΩ×1;33kΩ×1;1kΩ×3;866Ω×1 (DRV VREF 설정)','metal film resistor 1 percent kit')
commodity('C-set','전해·세라믹 커패시터',1,'470uF25V×1;47uF10V×1;10uF10V×1;100nF50V×6','capacitor assortment 25V 470uF 100nF')
commodity('PCB','2.54mm perfboard',1,'30×55mm 절단 / 하부 전원·잠금 회로','2.54mm perfboard','L_power_and_latch_board')
commodity('J1','DC-022 5.5x2.1mm panel jack',1,'중앙+ / 12V 1A 이상 / 장착 외경8mm 확인','DC 022 5.5 2.1 panel jack','L_12V_jack')
commodity('W1','JST-XH 2.54 connectors and silicone wire',1,'2P×3세트;4P×3세트;센서10개용3P×10세트;22AWG전원·28AWG신호','JST XH 2.54 connector kit')
commodity('W2','Shielded flexible load cell cable',1,'4심+실드 / 최소0.5m / 여유루프 및 고정클립','4 core shielded flexible cable')
commodity('PS1','12V 2A regulated external adapter',1,'KC인증 제품 / 5.5×2.1mm 중앙+ / 220V AC 입력. 상자 내부 배터리 아님','12V 2A power supply 5.5 2.1')
commodity('H1','M2 fasteners',1,'레일용M2×6 28개;캐리지용M2×6 8개(실효나사물림2.5mm);스위치용M2×12 20개+너트','M2 metric screw assortment')
commodity('H2','M3 fasteners',1,'PQ12 부속 우선;램프M3×8 8개;보호판M3×10 4개;패널M3×14 2개;샤프트클램프M3×16 8개;조절·예비M3 12개','M3 metric screw assortment')
commodity('H3','3mm shoulder bolts and washers',4,'롤러 축 Ø3 매끈한 어깨 / 유효어깨16mm / 623 내륜만 체결 / 외륜 비접촉','3mm shoulder screw 16mm')
commodity('H4','M6 load cell fasteners',4,'고정단2·가동단2 / R3 구멍피치 / 물림·카운터보어는 납품 도면 대조','M6 low head socket screw')
commodity('H5','M2.5 keeper screws',2,'R3 블라인드 체결 / 바깥면 관통 금지','M2.5 socket screw')
with (ROOT/'BOM.csv').open('w',encoding='utf-8-sig',newline='') as f:
    w=csv.DictWriter(f,fieldnames=list(rows[0]));w.writeheader();w.writerows(rows)
(ROOT/'sources/procurement_sources.json').write_text(json.dumps(rows,ensure_ascii=False,indent=2),encoding='utf8')
lines=['# R3-LIFT 주문 부품표','','확인일: 2026-09-09. 구매를 실행하지 않았다. USD 가격은 페이지 표시값이며 배송·관세·환율·가공비를 포함하지 않는다. 정확한 옵션을 먼저 맞춘다.','','|Ref|구매 부품|수량|주문 옵션|확인 가격|','|---|---|---:|---|---|']
for r in rows:
    price='$'+str(r['unit_usd']) if r['unit_usd']!='' else '확인 필요'
    lines.append(f"|{r['reference']}|[{r['part']}]({r['purchase_url']})|{r['quantity']}|{r['order_option']}|{price}|")
lines+=['','**조달 구분:** M1~S1..10은 실제 판매/제조사 페이지를 확인했다. 표준 소모품은 규격을 지정한 AliExpress 검색 링크이며, 특정 판매 물건을 검증했다는 뜻이 아니다. 특히 LC1은 L6D 판매 페이지는 있으나 3kg 옵션을 구매 전에 확인해야 한다. MGN7·LM6UU·623ZZ는 AliExpress에서도 동일 규격을 검색할 수 있지만, 치수·재질이 다른 대체품을 그대로 쓰면 안 된다.','','**별도 가공품:** 케이스·적재판·프레임·램프·브래킷·가드는 아래 `FABRICATION.md`와 `parts/`를 따른다. 이들은 완제품 구매 부품이 아니다. 3D 프린트 출력만으로 금속 프레임과 축을 대체하지 않는다.','','[가공 및 조립](FABRICATION.md) · [회로·동작 조건](CONTROL_AND_WIRING.md) · [전체 CSV](BOM.csv)']
(ROOT/'PURCHASE_LIST.md').write_text('\n'.join(lines)+'\n',encoding='utf8')
print('BOM rows',len(rows))
