# 🛒 까치 마트 스마트 쇼핑 앱 (SmartCart App)

**전화번호 조회 기반 로그인, RFID 상품 인식, QR 결제, 실시간 영수증 생성 등 다양한 기능을 통합한 스마트 마트 안드로이드 애플리케이션입니다.**

---

## 1. 주요 기능 (Features)

| 기능 | 설명 | 기타 | 
|------|------|------|
| 👤 얼굴 인식  UI와 회원 정보 연동 | PyQt 기반 시스템과 연동, Firebase 등록 정보로 로그인 처리 | (FaceNet, pickle DB 사용) Firebase Realtime Database + Authentication | 
| 📞 전화번호 로그인 | 전화번호 뒷 4자리로 로그인하며 중복 시, 전체 전화번호입력으로 로그인 가능 | 
| 🛒 RFID 장바구니 | RFID 리더기 태깅으로 장바구니 자동 업데이트 | 
| 🗺️ 매장 지도 | `MapActivity`를 통해 사용자 위치를 매장 맵에 표시 | Beacon 기반 실내 위치 측정 (Android Bluetooth API) | 
| 📷 QR 결제 | 결제 시 QR 영수증 생성 → 스캔하여 결제 완료 | QR Code: zxing | 
| 📜 전자 영수증 | `ReceiptActivity`에서 JSON 기반 영수증 정보 확인 | | 
| 🙋 마이페이지 | 사용자 정보, 포인트, 구매 이력 등 확인 가능 | | 

---

## 2. 메인 화면 구성 (activity_main.xml)

- 상단 : 앱 타이틀 `까치 마트`
- 로그인 전 : 로그인 / 회원가입 버튼
- 로그인 후 : 사용자 이름 + 마이페이지 진입 버튼
- 하단 메뉴 :  
  `QR 스캔`, `지도 보기`, `결제하기` 진입 버튼 배치

---

## 3. 프로젝트 구조

```plaintext
SmartCart/
├── app/
│   └── src/
│       └── main/
│           ├── java/                  # Kotlin/Java 소스 코드
│           ├── res/
│           │   ├── layout/            # activity_main.xml 등 UI 정의
│           │   ├── drawable/          # 이미지 리소스
│           │   └── values/            # 색상, 문자열 등 리소스 정의
│           └── AndroidManifest.xml    # 앱 구성 정의
├── build.gradle.kts                   # 프로젝트 설정
├── google-services.json               # Firebase 연동 키
└── settings.gradle.kts
```

---

## 4. 주요 액티비티 설명
### 1) Activity	설명

| Activity 이름              | 설명                                                                 |
|---------------------------|----------------------------------------------------------------------|
| `MainActivity`            | 앱 메인 화면. 로그인 여부에 따라 UI가 동적으로 변경됨                |
| `LoginActivity`           | 전화번호를 통한 로그인 처리 (간단 로그인)                             |
| `FullPhoneLoginActivity`  | 전화번호 + 비밀번호 기반 로그인 처리 (보안 강화 버전)                |
| `MapActivity`             | 비콘 기반으로 사용자의 실내 위치를 지도 상에 표시                    |
| `QRScanActivity`          | QR 영수증을 스캔하여 결제 처리                                       |
| `PaymentActivity`         | 장바구니 결제 → QR 코드 생성 → 결제 정보 Firebase에 저장              |
| `ReceiptActivity`         | 결제 내역 및 전자 영수증을 시각적으로 확인 가능                       |
| `MypageActivity`          | 사용자 개인 정보, 적립금, 구매 이력 등을 열람 가능                    |

---

## 5. 실행 방법 (Build & Run)
### 1) Android Studio에서 프로젝트 열기
### 2) Run ▶ 버튼 클릭하여 에뮬레이터 또는 실기기에서 테스트

---

## 6. 참고 및 주의사항
### 얼굴 인식 시스템과 연동 시 반드시 Firebase 사용자 정보와 pickle DB를 동기화할 것
### QR 영수증은 JSON 형식 기반이며, 모바일 앱 외부에서 직접 디코딩도 가능
### 실내 위치 측정은 BLE 환경에서 정확도가 달라질 수 있음
