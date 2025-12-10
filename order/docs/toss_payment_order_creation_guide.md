# 토스 결제 주문 생성 가이드

이 문서는 토스 결제를 통해 주문을 생성하는 실제 사용 방법을 단계별로 설명합니다.

---

## 사전 준비

1. **서버 실행 확인**
   - Order 서버가 `http://localhost:8082`에서 실행 중이어야 합니다
   - Payment 서버가 `http://localhost:8087`에서 실행 중이어야 합니다

2. **필수 헤더**
   - 모든 API 요청에 `X-User-Code` 헤더가 필요합니다
   - 예: `X-User-Code: USER-001`

---

## 🎯 가장 쉬운 방법: 주문 생성 폼 페이지 사용

### 주문 생성 폼 페이지 접속

**URL**: `http://localhost:8082/orders/create-form`

**접속 방법**:
1. 브라우저를 열고 위 URL로 접속합니다
2. 주문 정보를 입력합니다:
   - 장바구니 코드
   - 구매자 이름
   - 배송지 주소
   - 주문 유형 (온라인/오프라인)
   - 상품 정보 (상품 코드, 수량, 가격)
   - 총 결제 금액 (자동 계산됨)
3. "주문하기" 버튼을 클릭합니다
4. 자동으로 토스 결제 페이지로 이동합니다

**장점**:
- ✅ curl 명령어나 Postman 없이 브라우저에서 바로 사용 가능
- ✅ 직관적인 UI로 쉽게 주문 생성 가능
- ✅ 상품 추가/제거 기능 제공
- ✅ 총 결제 금액 자동 계산

---

## 단계별 주문 생성 절차 (API 직접 호출)

### 1단계: 주문 생성 API 호출

**URL**: `POST http://localhost:8082/api/orders/cartItems`

**요청 방법**:
- 브라우저에서 직접 호출할 수 없으므로, 프론트엔드 애플리케이션이나 API 테스트 도구(Postman, curl 등)를 사용해야 합니다
- **또는 위의 주문 생성 폼 페이지를 사용하세요!**

**요청 예시 (curl)**:
```bash
curl -X POST http://localhost:8082/api/orders/cartItems \
  -H "Content-Type: application/json" \
  -H "X-User-Code: USER-001" \
  -d '{
    "cartCode": "CART-001",
    "name": "홍길동",
    "address": "서울시 강남구 테헤란로 123",
    "products": [
      {
        "productCode": "PROD-001",
        "quantity": 2,
        "price": 50000,
        "image": "https://example.com/image.jpg"
      }
    ],
    "totalPrice": 100000,
    "orderType": "ONLINE",
    "paidType": "TOSS_PAYMENT",
    "paymentKey": null
  }'
```

**중요 사항**:
- `paidType`은 반드시 `"TOSS_PAYMENT"`로 설정해야 합니다
- `paymentKey`는 `null`로 설정합니다 (결제 완료 후 토스에서 전달됩니다)

**결과**:
- 주문이 생성되고 자동으로 결제 페이지로 리다이렉트됩니다
- 리다이렉트 URL: `http://localhost:8082/orders/payment?orderId={주문ID}&orderName={주문명}&amount={금액}`

---

### 2단계: 결제 페이지 접속

**URL**: `http://localhost:8082/orders/payment?orderId={주문ID}&orderName={주문명}&amount={금액}`

**접속 방법**:
- 1단계에서 주문 생성 API를 호출하면 자동으로 이 페이지로 리다이렉트됩니다
- 또는 브라우저에서 직접 URL을 입력하여 접속할 수 있습니다

**예시 URL**:
```
http://localhost:8082/orders/payment?orderId=1&orderName=주문번호:%20ORDER-20240101-001&amount=100000
```

**페이지에서 확인할 수 있는 정보**:
- 주문번호
- 상품명
- 결제금액
- 결제 수단 선택 UI (토스 결제 위젯)

---

### 3단계: 결제 진행

**위치**: 결제 페이지 (`/orders/payment`)

**절차**:
1. 결제 페이지에서 결제 수단을 선택합니다 (카드, 계좌이체 등)
2. 결제 정보를 입력합니다
3. "결제하기" 버튼을 클릭합니다
4. 토스 결제 서버에서 결제를 처리합니다

**결제 성공 시**:
- 자동으로 `/api/orders/payment/success`로 리다이렉트됩니다
- URL 예시: `http://localhost:8082/api/orders/payment/success?paymentKey={결제키}&orderId={주문코드}&amount={금액}`

**결제 실패 시**:
- 자동으로 `/api/orders/payment/fail`로 리다이렉트됩니다
- URL 예시: `http://localhost:8082/api/orders/payment/fail?errorCode={에러코드}&errorMessage={에러메시지}&orderId={주문코드}`

---

### 4단계: 결제 완료 처리 (자동)

**URL**: `http://localhost:8082/api/orders/payment/success?paymentKey={결제키}&orderId={주문코드}&amount={금액}`

**동작**:
- 이 URL은 토스 결제 서버에서 자동으로 호출됩니다
- 서버에서 결제를 검증하고 주문 상태를 `COMPLETED`로 변경합니다
- 처리 완료 후 성공 페이지로 리다이렉트됩니다

**리다이렉트 대상**:
- `http://localhost:8082/orders/payment/success-page?orderId={주문코드}&amount={금액}`

---

### 5단계: 결제 성공 페이지 확인

**URL**: `http://localhost:8082/orders/payment/success-page?orderId={주문코드}&amount={금액}`

**접속 방법**:
- 4단계에서 자동으로 리다이렉트됩니다
- 또는 브라우저에서 직접 접속할 수 있습니다

**페이지에서 확인할 수 있는 정보**:
- 주문번호
- 결제금액
- 주문 완료 메시지

---

## 결제 실패 시 절차

### 결제 실패 콜백 (자동)

**URL**: `http://localhost:8082/api/orders/payment/fail?errorCode={에러코드}&errorMessage={에러메시지}&orderId={주문코드}`

**동작**:
- 토스 결제 서버에서 자동으로 호출됩니다
- 실패 페이지로 리다이렉트됩니다

**리다이렉트 대상**:
- `http://localhost:8082/orders/payment/fail-page?errorCode={에러코드}&errorMessage={에러메시지}&orderId={주문코드}`

---

### 결제 실패 페이지 확인

**URL**: `http://localhost:8082/orders/payment/fail-page?errorCode={에러코드}&errorMessage={에러메시지}&orderId={주문코드}`

**접속 방법**:
- 결제 실패 시 자동으로 리다이렉트됩니다
- 또는 브라우저에서 직접 접속할 수 있습니다

**페이지에서 확인할 수 있는 정보**:
- 에러 코드
- 에러 메시지
- 주문번호 (있는 경우)

---

## 전체 흐름 요약

```
1. 주문 생성
   POST http://localhost:8082/api/orders/cartItems
   ↓ (자동 리다이렉트)

2. 결제 페이지
   GET http://localhost:8082/orders/payment?orderId=...&orderName=...&amount=...
   ↓ (사용자가 결제 진행)

3. 결제 성공 콜백 (자동)
   GET http://localhost:8082/api/orders/payment/success?paymentKey=...&orderId=...&amount=...
   ↓ (자동 리다이렉트)

4. 결제 성공 페이지
   GET http://localhost:8082/orders/payment/success-page?orderId=...&amount=...
```

---

## 실제 사용 예시

### 시나리오: 장바구니에서 주문하기

1. **사용자가 장바구니에서 "주문하기" 버튼 클릭**
   - 프론트엔드에서 `POST /api/orders/cartItems` 호출
   - `paidType: "TOSS_PAYMENT"` 설정

2. **자동으로 결제 페이지로 이동**
   - 브라우저가 `/orders/payment?orderId=1&orderName=...&amount=100000`로 리다이렉트

3. **사용자가 결제 정보 입력 및 결제 진행**
   - 결제 페이지에서 카드 정보 입력
   - "결제하기" 버튼 클릭

4. **결제 완료 후 성공 페이지 표시**
   - 자동으로 `/orders/payment/success-page`로 이동
   - 주문 완료 메시지 확인

---

## 주의사항

1. **주문 생성 API는 브라우저에서 직접 호출할 수 없습니다**
   - 프론트엔드 애플리케이션이나 API 테스트 도구를 사용해야 합니다
   - 브라우저에서 직접 `POST` 요청을 보낼 수 없습니다

2. **리다이렉트는 자동으로 처리됩니다**
   - 주문 생성 후 자동으로 결제 페이지로 이동합니다
   - 결제 완료 후 자동으로 성공 페이지로 이동합니다

3. **결제 키는 자동으로 생성됩니다**
   - 주문 생성 시 `paymentKey`는 `null`로 설정합니다
   - 실제 결제 키는 토스 결제 위젯에서 생성되어 콜백으로 전달됩니다

4. **주문 상태 확인**
   - 주문 생성 직후: `CREATED` (결제 대기)
   - 결제 완료 후: `COMPLETED` (주문 완료)
   - 결제 실패 시: `FAILED` (주문 실패)

---

## 문제 해결

### 결제 페이지가 표시되지 않는 경우

1. **서버가 실행 중인지 확인**
   ```bash
   # Order 서버 포트 확인
   netstat -an | grep 8082
   ```

2. **URL 확인**
   - 정확한 URL 형식: `http://localhost:8082/orders/payment?orderId={숫자}&orderName={문자열}&amount={숫자}`
   - 쿼리 파라미터가 올바르게 인코딩되었는지 확인

3. **로그 확인**
   - Order 서버 로그에서 에러 메시지 확인

### 결제가 완료되지 않는 경우

1. **토스 결제 설정 확인**
   - `application-local.yml`의 `payment.widgetClientKey` 확인
   - 테스트 환경에서는 기본값 사용 가능

2. **콜백 URL 확인**
   - `payment.successUrl`과 `payment.failUrl`이 올바르게 설정되었는지 확인

3. **주문 상태 확인**
   - 주문 상세 조회 API로 주문 상태 확인
   - `GET http://localhost:8082/api/orders/{orderCode}/details`

---

## 관련 URL 목록

| 단계 | URL | 메서드 | 설명 |
|------|-----|--------|------|
| 주문 생성 | `/api/orders/cartItems` | POST | 주문 생성 (토스 결제) |
| 결제 페이지 | `/orders/payment` | GET | 토스 결제 위젯 페이지 |
| 결제 성공 콜백 | `/api/orders/payment/success` | GET | 결제 완료 처리 (자동) |
| 결제 실패 콜백 | `/api/orders/payment/fail` | GET | 결제 실패 처리 (자동) |
| 성공 페이지 | `/orders/payment/success-page` | GET | 결제 성공 안내 페이지 |
| 실패 페이지 | `/orders/payment/fail-page` | GET | 결제 실패 안내 페이지 |

---

## 빠른 테스트 방법

### 방법 1: 주문 생성 폼 페이지 사용 (가장 쉬움! ⭐)

1. **브라우저에서 주문 생성 폼 페이지 열기**
   - URL: `http://localhost:8082/orders/create-form`
   
2. **주문 정보 입력**
   - 장바구니 코드: `CART-001` (기본값 입력됨)
   - 구매자 이름: `홍길동` (기본값 입력됨)
   - 배송지 주소: `서울시 강남구 테헤란로 123` (기본값 입력됨)
   - 주문 유형: `온라인` 선택
   - 상품 정보 (기본값이 이미 입력되어 있음):
     - 상품 코드: `PROD-001`
     - 수량: `1`
     - 가격: `10000`
   - 총 결제 금액: 자동 계산됨
   
3. **"주문하기" 버튼 클릭**
   - 자동으로 토스 결제 페이지로 이동합니다
   - 결제 페이지에서 테스트 결제 진행

**장점**: 
- ✅ curl이나 Postman 없이 브라우저에서 바로 사용 가능!
- ✅ 직관적인 UI로 쉽게 주문 생성
- ✅ 상품 추가/제거 기능 제공
- ✅ 총 결제 금액 자동 계산

---

### 방법 2: Postman을 사용한 테스트

1. **Postman 열기**
2. **새 요청 생성**
   - Method: `POST`
   - URL: `http://localhost:8082/api/orders/cartItems`
   - Headers:
     - `Content-Type: application/json`
     - `X-User-Code: USER-001`
   - Body (raw JSON):
     ```json
     {
       "cartCode": "CART-001",
       "name": "홍길동",
       "address": "서울시 강남구 테헤란로 123",
       "products": [
         {
           "productCode": "PROD-001",
           "quantity": 1,
           "price": 10000,
           "image": ""
         }
       ],
       "totalPrice": 10000,
       "orderType": "ONLINE",
       "paidType": "TOSS_PAYMENT",
       "paymentKey": null
     }
     ```
3. **요청 전송**
4. **응답에서 Location 헤더 확인** (302 리다이렉트)
5. **Location 헤더의 URL을 브라우저에서 열기**
6. **결제 페이지에서 테스트 결제 진행**

---

## 추가 정보

- **서버 포트**: 8082 (Order 서버)
- **기본 도메인**: `http://localhost:8082`
- **필수 헤더**: `X-User-Code` (모든 API 요청에 필요)

