# 스마트 잠금 상자 설계

이번 수정본은 **R3-A4**입니다. 상부 칸막이를 없애고, 단일 적재판 아래에 전원·회로·로드셀·잠금장치를 둔 이중 바닥으로 변경했습니다.

- [R3 편집 모델 및 개폐 애니메이션](smart_lock_box_r3/smart_lock_box_r3.blend)
- [R3 배치·회로 설명서 PDF](smart_lock_box_r3/assembly_and_circuit_r3.pdf)
- [R3 전체 패키지 ZIP](smart_lock_box_r3_package.zip)
- [설계 변경·사용법·검증 한계](smart_lock_box_r3/README.md)
- [실행한 검증 결과](smart_lock_box_r3/validation/summary.json)

CAD 이동 632자세·애니메이션 144프레임, C++ 검사 235개, 통신 테스트 10개 및 Nano 컴파일을 통과했습니다. **A4 한 장의 실제 검출, 강도, 발열, 테미 장착은 아직 실물 검증 전입니다.**

외형은 A4 폭을 확보하기 위해 255×324×78 mm로 변경했습니다. 전원은 안정화 12 V 입력 기준이며 내부 배터리는 미선정입니다. 기존 `smart_lock_box.blend`, `assembly_and_circuit.pdf`, 두 원본 ZIP은 보존했습니다.
