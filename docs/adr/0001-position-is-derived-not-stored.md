# Position은 저장하지 않고 좌석에서 파생한다

Player/HandPlayer에 Position 필드를 두지 않고, 매번 `seat` + `buttonSeat` + `playerCount`로 계산한다(`HandRecord.getPosition`).

버튼은 핸드마다 옮겨가므로, 같은 좌석의 Position은 핸드마다 달라진다. Position을 저장하면 버튼·인원 수와 어긋날 여지가 생기고 정합성 유지 비용이 커진다. Seat을 단일 진실의 원천으로 두고 Position은 순수 함수로 파생시켜, 저장 데이터의 모순 가능성을 없앤다.

되돌리기 어려운 이유: 저장 스키마와 다수 화면이 "Position은 파생"이라는 전제에 의존한다. 미래의 독자가 Player에 position 필드가 없는 것을 보고 의아해할 수 있어 기록해 둔다.
