# 1122 전체 모듈 설정 변경 정리

## 1. 작업 기준 주요 상태
- settlement 제외 모든 모듈 **로컬 실행 확인**
- **Kafka 토픽 초기화 확인**
- **Elasticsearch 실행 확인**
- 이벤트 수신 체크: 미진행
- API 테스트: 미진행

---

# 전달사항

## 1. 로컬 환경 변수 관리 방식 변경
- `.env` 파일을 스프링에서 읽지 못해 로컬 환경변수 적용이 불가능한 문제 → **각자 PC에 직접 환경변수를 등록하는 방식으로 전환**
- `application.yml` 파일은 민감정보를 적지않아도 되기에 **GitHub에 공유 가능**
- `env.template`, `setup-env.bat` 두 파일을 **root 프로젝트 바로 아래** 위치시키기  
  (두 파일은 GitHub에 공유되지 않도록 주의)

### 로컬 환경 변수 설정
env.tamplate에서 각자의 로컬환경에 맞춘 환경변수를 설정해주세요.
- H2 사용 시  
  - `DB_USER`, `DB_PASSWORD` 부분 설정 (예: ` DB_USER=sa`)
- MySQL 사용 시  
  - `MYSQL_USER`, `MYSQL_PASSWORD` 설정
- 이후 `setup-env.bat` → 우클릭 → 실행 → 환경변수 자동 등록  
  이후 **IntelliJ 재실행 필수

---

## 2. 스프링 설정 분리 기준
- 기존 `application.yml` 과 `application-docker.yml` 에서 → 로컬(application.yml, application-local.yml), 운영(application-dev.yml, application-prod.yml)로 명확하게 분리.
- 즉 개인별 핵심 로컬 설정은 **application-local.yml** 에 작성
- application.yml은 로컬과 운영 모든 환경에 공통 적용됩니다.

### 주의사항
- `application-local.yml` 수정 시 **주석으로 무엇을 추가했는지 명시**
- dev 환경: **도커 기반**
- prod 환경: **EC2 배포 환경**
- 따라서 dev/prod 설정은 인프라 담당이 관리

---

## 3. 도커 환경 포트 규칙 통일
기존:  
- 예시) `8083:8080`

결정:  
- **로컬과 동일하게 1:1 매칭**  
  → `8083:8083`

수정 후 `.env` 파일 재배포 예정

---

## 4. 로컬 환경에서 Gateway, Discovery 미사용
- 로컬에서는 불필요한 필터 관련 작업을 줄이기 위해 **Gateway/Discovery를 끄기로 결정**
- API 테스트 편의성 증가 목적

---

## 5. Kafka 관련 설정은 수정하지 않음
- Kafka 설정 구조가 복잡하여 수정 보류  
- 필요 시 추후 점검 예정

---

## 6. 소셜 로그인 응답 형식 롤백
- 임시로 사용하던 **쿠키 기반 토큰 저장 로직 삭제**
- 원래 방식인 JSON 응답 복구  
  - `accessToken`
  - `refreshToken`

---

## 7. 문서 내용에 오타 또는 이상한 부분 있을 수 있음
- 팀원 확인 요청

---

# 문제상황 및 해결 요약

## 1. `.env` 파일 미인식으로 인한 빌드 실패
- 스프링이 로컬 `.env` 를 읽지 못함 → 환경변수 값 null
- 해결:
  - 환경별 yml 분리
  - 로컬 전용 환경변수 세팅 도구 제공 (`env.template` + `setup-env.bat`)

---

## 2. products 모듈 로컬 실행 불가
원인:
1. DB 설정 불일치  
2. Elasticsearch 미기동  
3. deposit 모듈과 포트 충돌

### 조치:
- deposit 포트 8085 → **8086 변경**
- product_db MySQL 포트 3311 → **3306 통일**
- 로컬 MySQL 기동 여부 필수 체크
- `products` 모듈은 **H2가 아닌 MySQL 사용 중**

### 체크리스트
Windows 기준:
- Win + R → `services.msc`  
  → MySQL 서비스 실행 여부 확인
- MySQL Workbench 또는 cmd로 DB 존재 여부 확인:
  ```cmd
  mysql -u <YourUserName> -p
  SHOW DATABASES;

### 로컬에서 Elasticsearch 실행 필요한 경우
- products 모듈에서 docker compose -f docker-compose-es.yml up -d
- 연결 포트: 9200
- 브라우저에서 http://localhost:9200 → JSON 응답 나오면 정상

### 카프카 필요한 경우 ( 토픽 초기화만 확인했습니다.)
- core 모듈에서 docker compose -f docker-compose-es.yml up -d
- 연결 포트: 9092
- 아래의 명령어를 실행
``` terminal
 docker exec -it kafka-1 /bin/bash
 cd /opt/kafka/bin
 ./kafka-topics.sh --bootstrap-server localhost:9092 --list
```
 - 토픽 목록이 출력되면 정상 실행


