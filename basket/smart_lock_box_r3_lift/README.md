# R3-LIFT — 모터 내장형 승강 바닥

R4는 폐기했다. **R3의 단일 A4 적재판·로드셀·잠금·수동 수직 격납 도어를 기준으로**, 하부 회로 공간 안에 모터가 완전히 들어가는 승강 바닥으로 다시 설계했다. 사용자 승인에 따라 높이를 78→108mm로 늘렸으며 폭·깊이는 유지한다. R3 원본 파일은 변경하지 않았다.

![상승 상태](drawings/01_raised.png)

- [편집 모델 및 동작 애니메이션](smart_lock_box_r3_lift.blend): 108프레임에서 열림·상승. 1프레임은 닫힘.
- [공유용 GLB](smart_lock_box_r3_lift.glb): 미터 단위, 개폐·승강 통합 애니메이션.
- **[주문 부품리스트와 구매 링크](PURCHASE_LIST.md)** · [엑셀에서 열 수 있는 CSV](BOM.csv)
- [실제 모델의 내부 구동부](drawings/03_internal_drive.png) · [하강 기구](drawings/04_mechanism_down.png) · [상승 기구](drawings/05_mechanism_up.png)
- [부품별 가공 참고 STL](parts/) · [가공·조립 치수](FABRICATION.md) · [회로·제어 연결](CONTROL_AND_WIRING.md)
- [실행한 검사 결과](validation/geometry.json) · [제작 전 실물 확인](HARDWARE_ACCEPTANCE.md)

|항목|이번 설계|
|---|---|
|닫힘 외형|**255×324×108mm**|
|적재판|R3의 **213.5×308×3mm** 알루미늄 판|
|A4|210×297mm, 평평하게 놓을 공간 유지|
|적재판 상면|하강 Z81 → 상승 Z108, **27mm 수평 상승**|
|닫힌 수납 높이|도어 밑면100 − 적재판 상면81 = **19mm**|
|구동 모터|**Actuonix PQ12-100-12-S 1개**, 12V, 100:1|
|모터 본체 위치|X114~135.5 / Y208~244.5 / **Z12.5~27.5mm**|
|모터·로드·연결 링크|전체 동작에서 X22~243 / Y6~318 / Z6~41의 하부 공간 안|
|변환 기구|하부 슬라이더18mm → 1.5:1 경사면 → 바닥27mm|
|안내·지지|MGN7C 수평 레일2개, 623ZZ 롤러4개, Ø6 수직축4개 + LM6UU4개|
|고정 전장 보호판|Z71~74mm, 움직이는 셀·축용 통과구|
|덮개|R3 방식으로 사람이 열고 수직 격납. **자동 승강은 적재 바닥에 적용**|
|설계 적재 목표|종이 **500g**. 허용하중 인증값이 아니며 실물 시험 전|

부품 근거는 [Actuonix 제품과 규격서](https://www.actuonix.com/pq12-100-12-s), [RobotDigg MGN7](https://www.robotdigg.com/product/840/440C-SUS-MGN7-linear-rail-with-carriage), [LM6UU](https://www.robotdigg.com/product/42), [Zemic L6D](https://www.zemiceurope.com/media/Documentation/L6D_Datasheet.pdf)다. 모터·베어링·보드 등은 도면 크기에 맞춘 구매품 외형 모델이며 내부 구조를 재현한 제조사 CAD는 아니다.

하중은 `적재판 → L6D 가동단 → L6D 고정단 → 승강 프레임 → 롤러 → 슬라이더 → 바닥` 순서로 전달된다. 가이드와 액추에이터가 계량판을 직접 지지하지 않는다. **이동 중 무게 판단을 무효화하고, 하강 정지 후 안정화한 값만 종이 검출에 사용**한다. 케이블 당김·가이드 마찰·판 휨·A4 한 장 검출은 실물 검증 대상이다.

500g 종이와 판·셀·프레임·배선 질량 여유를 합친 명목 이동 질량은 약1.73kg다. 경사면 변환과25% 손실 여유를 적용한 계산 추력은 약31.9N으로, 액추에이터50N 최대 추력 대비 약1.57배 여유다. 최대 추력은 연속 운전 보증이 아니다. 제조사20% 듀티 제한을 지키며, 6초 통전 시 최소24초 휴지하도록 제어한다. 수평 정지 유지 여유는 별도로 검사 결과에 기록했다.

기하 검사는 240프레임의 모터 내부 수용·수평 승강·도어 순서, 움직이는 메시와 다른 계통의 표면 교차를 검사한다. 베어링/레일과 액추에이터 내부 조인트4종만 이름별로 허용한다. 고정부끼리의 모든 접촉, 메시 안에 완전히 포함된 물체, 공차·강도·발열·배선 굴곡 시험을 포함한 완성품 인증은 아니다.

**제작 상태:** 모델·구매 목록·가공 참고 형상·제어 배선 설계까지 완료한 시제품안이다. 새 승강 제어를 실제 Nano에 통합하거나 구동 시험한 상태는 아니다. 기존 R3 `.hex`를 그대로 올려도 승강하지 않는다. 내부 배터리와 테미 전원 포트 연결은 포함하지 않았고, 외부 안정화12V 입력을 기준으로 한다.

재생성:

ZIP의 모델은 단독으로 열 수 있다. 빌드 스크립트 재실행에는 저장소의 `basket/smart_lock_box_r3` 기준 폴더와 Blender, 패키지 검사에는 Python의 numpy/Pillow가 필요하다.

```text
blender -b --python basket/smart_lock_box_r3_lift/build_lift.py
blender -b --python basket/smart_lock_box_r3_lift/audit_lift.py
blender -b --python basket/smart_lock_box_r3_lift/render_lift.py
python basket/smart_lock_box_r3_lift/make_bom.py
python basket/smart_lock_box_r3_lift/package_review.py
```

R3는 Blender5.2 저장본이고 이번 재생성은4.5.3 LTS를 사용했다. 읽을 때 버전 경고가 발생하므로 재사용 메시 외곽을 R3 inventory와 대조했으며 결과는 `validation/source_import.json`에 기록했다. R3 원본 SHA-256은 `parameters.json`에 남겼다. R4 이력은 Git의 마지막 R4 커밋 `fa6051f`에 보존되며 현재 설계·부품에는 사용하지 않는다.
