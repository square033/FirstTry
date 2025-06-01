🛒 까치 마트 스마트 쇼핑 앱
까치 마트는 얼굴 인식 기반 로그인, RFID 기반 상품 인식, QR 결제, 실시간 영수증 생성 등을 통합한 스마트 마트 안드로이드 애플리케이션입니다.

🔍 주요 기능 (Features)
기능	설명
👤 얼굴 인식 로그인	PyQt 기반 시스템과 연동된 Firebase 회원정보를 바탕으로 로그인
📱 전화번호 기반 로그인	전화번호로 간편 로그인 가능 (Firebase와 연동)
🛒 RFID 기반 장바구니	물건을 RFID 리더에 태그하면 장바구니에 실시간으로 추가됨
📍 매장 내 위치 표시	MapActivity를 통해 비콘 기반 사용자의 위치를 매장 맵에 표시
📷 QR 결제 기능	계산 완료 시 QR 영수증이 생성되고, 이를 스캔하여 결제 진행
📜 전자 영수증 확인	ReceiptActivity에서 영수증을 JSON 기반으로 확인 가능
🙋 마이페이지	사용자 정보, 적립 포인트, 구매 이력 등 확인 가능

🖼️ 메인화면 UI (activity_main.xml)
(직접 캡처한 UI가 있다면 여기에 삽입)

앱 상단: 까치 마트 타이틀

로그인 전 상태: 로그인 및 회원가입 버튼 표시

로그인 후 상태: 사용자의 이름 표시 및 마이페이지 버튼 활성화

하단 메뉴: QR 스캔, 지도 보기, 결제하기 등 핵심 기능 진입 버튼 존재

🗂️ 프로젝트 구조
perl
복사
편집
app/
├── src/
│   └── main/
│       ├── java/                 # Kotlin/Java 소스 코드
│       ├── res/
│       │   ├── layout/           # activity_main.xml 외 다양한 UI 레이아웃
│       │   ├── drawable/        # 아이콘 및 이미지
│       │   ├── values/          # 색상, 문자열 등 리소스 정의
│       └── AndroidManifest.xml  # 앱 구성 정보
├── build.gradle.kts             # 프로젝트 설정
├── google-services.json         # Firebase 연동
🚀 실행 방법 (Build & Run)
Android Studio에서 프로젝트 열기

google-services.json을 app/에 위치시켜 Firebase 연동 완료

실행 전:

MainActivity → 앱 시작점

LoginActivity, EasyPayActivity, MapActivity, QRScanActivity 등 필요 시 연결 확인

Run ▶ 버튼 클릭하여 에뮬레이터 또는 실기기에서 테스트

🛠 기술 스택
Android (Kotlin 기반)

Firebase Realtime Database + Authentication

QR Code: zxing

Beacon 기반 실내 위치 측정 (Android Bluetooth API)

PyQt 기반 얼굴 인식 시스템과 연동 가능 (FaceNet, pickle DB 사용)

📌 주요 액티비티 설명
Activity	설명
MainActivity	앱 메인 화면, 로그인 상태에 따라 UI 변경
LoginActivity, FullPhoneLoginActivity	전화번호 기반 인증
MapActivity	비콘 위치 기반 지도 뷰
QRScanActivity	QR 영수증 스캔 및 결제
PaymentActivity	결제 완료 → QR 생성 및 정보 Firebase 저장
ReceiptActivity	결제 정보 시각화
MypageActivity	사용자 개인 정보 확인 (이름, 적립금, 구매 기록 등)

📷 화면 예시
필요한 경우, 주요 화면들의 스크린샷을 삽입하거나 .screenshots/ 폴더 생성

메인화면

로그인 화면

장바구니 및 결제화면

QR 스캔 화면

마이페이지

🧩 연동 시스템 (외부 구성 요소)
Raspberry Pi 기반 PyQt 얼굴 인식 시스템

face_database.pkl 및 phone_map.pkl 공유

QR 코드 기반 사용자 인증

RFID 리더기: HID 입력 방식, 상품 UID 입력을 통해 상품 추가

모바일 앱과 실시간 정보 공유: Firebase를 통한 양방향 동기화

🔒 참고 및 주의사항
얼굴 인식 시스템과 연동 시 반드시 Firebase 사용자 정보와 pickle DB를 동기화할 것

QR 영수증은 JSON 형식 기반이며, 모바일 앱 외부에서 직접 디코딩도 가능

실내 위치 측정은 BLE 환경에서 정확도가 달라질 수 있음
