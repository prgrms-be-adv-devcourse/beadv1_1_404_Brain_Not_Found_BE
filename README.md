## ✏️ 프로젝트 개요
아이돌·애니메이션 굿즈를 수집하는 이용자를 위해, 신뢰할 수 있는 거래 환경과 안전한 결제·정산 시스템을 제공하는 굿즈 전용 중고거래 플랫폼입니다.

## 📪 배포 링크
### [Gooream](https://gooream.chlab.org/)

## 📆 개발 기간
| 구분           | 기간                              | 주요 내용                                                                                        | 비고              |
| ------------ |---------------------------------| -------------------------------------------------------------------------------------------- | --------------- |
| **총 개발 기간**      | **2025.11.03 (월) ~ 2025.12.18 (월)** | **기획 → 설계 → 개발 → 배포 → 통합 테스트 전 과정 수행**                                                           |    |
| 사전 기획·설계     | 2025.11.03 (월) ~ 2025.11.06 (목) | 프로젝트 주제 선정, 요구사항 정리, 팀 역할 분담, API 명세 정의, ERD 설계, 전체 서비스 플로우 정의                               | 코드 컨벤션 수립       |
| 핵심 기능 개발     | 2025.11.07 (금) ~ 2025.11.17 (월) | 회원/인증, 상품, 장바구니, 주문·결제, 예치금, 정산 모듈 개발, Kafka 이벤트 구현, Spring Batch 정산 처리, Elasticsearch 검색 구현 | 테스트 코드 및 디버깅 병행 |
| 배포 환경 통합 테스트 | 2025.11.14 (금) ~ 2025.11.18 (화) | 팀 단위 통합 테스트, API Gateway 연동, 실배포 환경 기능 연동 테스트, Nginx Reverse Proxy 및 SSL 설정                  | AWS EC2 배포      |
| 인프라 개선·리팩토링  | 2025.11.26 (금) ~ 2025.11.30 (일) | Kubernetes 환경 구축, 도메인 리팩토링                                                                   | 클러스터 환경 구성      |
| 핵심 도메인 통합    | 2025.12.01 (월) ~ 2025.12.09 (화) | 주요 모듈 병합(cart-product / payment-settlement-deposit), 조회 구조 정리, 추천 기능 구현, 배포 자동화              | CI/CD 구성        |
| 기능 안정화       | 2025.12.10 (수) ~ 2025.12.18 (목) | 기능 안정화, 동시성·정합성 보완, 통합 테스트                                                                   | 마무리 단계          |

## 👨‍💻 개발인원 및 역할
|                                          PO                                          |                                        Infra                                         |                                          BE                                          |                                          BE                                           |                                          BE                                          |
|:------------------------------------------------------------------------------------:|:------------------------------------------------------------------------------------:|:------------------------------------------------------------------------------------:|:-------------------------------------------------------------------------------------:|:------------------------------------------------------------------------------------:|
| <img src="https://avatars.githubusercontent.com/u/83849667?v=4" width=175 alt="이승현"> | <img src="https://avatars.githubusercontent.com/u/94231335?v=4" width=175 alt="김예성"> | <img src="https://avatars.githubusercontent.com/u/48897023?v=4" width=175 alt="김호남"> | <img src="https://avatars.githubusercontent.com/u/117821255?v=4" width=175 alt="신호영"> | <img src="https://avatars.githubusercontent.com/u/80530044?v=4" width=175 alt="김민석"> |
|                          [이승현](https://github.com/Dianuma)                           |                          [김예성](https://github.com/yuio7279)                          |                       [김호남](https://github.com/tigerpoint123)                        |                        [신호영](https://github.com/Signalzero94)                         |                        [김민석](https://github.com/98minseok)                        |
|                  프로젝트 일정 관리,<br/> 예치금 및 정산 설계,<br/> Kafka·Batch 설계                   |            Docker·Kubernetes 환경 구성,<br/> AWS EC2 배포,<br/> CI/CD 파이프라인 구축             |                                     주문 모듈 구현,<br/> 외부 PG 결제 연동,<br/> 주문·결제 트랜잭션 정합성 보장                                      |                             상품 도메인,<br/> Elasticsearch 검색,<br/> AI 기반 상품 추천                             |                               회원·인증/인가,<br/> JWT·Redis 토큰 관리,<br/> API Gateway 연동                                |

## 🔊 프로젝트 주요 기능

### 회원 및 인증
- 이메일 및 소셜 로그인(Google / Naver) 지원
- JWT 기반 인증·인가 및 Spring Security 적용
- 기기별 로그인 상태 관리
- Redis 기반 Refresh Token 관리로 인증 성능 및 안정성 확보

### 상품 관리 및 검색
- 상품 등록·수정·삭제·상태 변경 기능 제공
- S3 기반 상품 이미지 업로드
- Elasticsearch 기반 상품 검색
  - 키워드 검색, 가격대 및 판매자 유형 필터링
  - 이벤트 기반 색인 동기화로 검색 데이터 정합성 유지
- 장바구니를 통한 다건 상품 묶음 구매 지원

### 거래 및 결제
- 상품 상세 페이지 및 장바구니 기반 주문·결제 흐름
- 예치금 결제 및 외부 결제 API(Toss) 연동
- 주문·결제·재고 흐름에 비관적 락 적용으로 동시성 및 데이터 정합성 보장
- Kafka 기반 이벤트 처리 및 Outbox 패턴 적용

### 정산 시스템
- Spring Batch 기반 월 단위 판매자 정산 처리
- 정산 실패·재시도·보상 시나리오를 고려한 정산 로직 구현

### 개인화 상품 추천
- 사용자 조회·검색·장바구니 이력 데이터 수집
- Redis 기반 단기 관심사, RDB 기반 장기 관심사 관리
- Vector DB(Qdrant)를 활용한 임베딩 데이터 저장
- LLM 기반 추천 쿼리 생성 및 개인화 상품 추천 제공

### 인프라 및 운영 환경
- Docker 기반 컨테이너화 및 AWS EC2 배포
- Kubernetes 도입을 통한 무중단 배포 및 롤링 업데이트
- API Gateway 및 Nginx Reverse Proxy 기반 트래픽 분산 및 보안 강화
- GitHub Actions 기반 CI/CD 파이프라인 구축
- GitHub Secrets를 활용한 환경 변수 및 민감 정보 관리

## 🖥 System Design
### Architecture Diagram
![아키텍처 다이어그램](assets/images/gooream_architecture_diagram.png)

### ERD
![ERD](assets/images/gooream_erd.png)

## 🛠 Tech Stack
- ### **Development**  
  <img src="https://img.shields.io/badge/IntelliJ%20IDEA-000000?style=for-the-badge&logo=intellijidea&logoColor=white">  
  <img src="https://img.shields.io/badge/Java-007396?style=for-the-badge&logo=OpenJDK&logoColor=white"> 
  <img src="https://img.shields.io/badge/Gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white">  
  <img src="https://img.shields.io/badge/Spring%20Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white">
  <img src="https://img.shields.io/badge/Spring%20Cloud-6DB33F?style=for-the-badge&logo=spring&logoColor=white">  

- ### **Database & Cache**  
  <img src="https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white"> 
  <img src="https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white">  
  <img src="https://img.shields.io/badge/JPA-59666C?style=for-the-badge&logo=hibernate&logoColor=white"> 
  <img src="https://img.shields.io/badge/QueryDSL-2C3E50?style=for-the-badge">

- ### **Event Streaming & Messaging**  
  <img src="https://img.shields.io/badge/Apache%20Kafka-231F20?style=for-the-badge&logo=apachekafka&logoColor=white">

- ### **Batch Processing**  
  <img src="https://img.shields.io/badge/Spring%20Batch-6DB33F?style=for-the-badge&logo=spring&logoColor=white">

- ### **Search & Indexing**  
  <img src="https://img.shields.io/badge/Elasticsearch-005571?style=for-the-badge&logo=elasticsearch&logoColor=white">

- ### **AI / Recommendation**  
  <img src="https://img.shields.io/badge/Spring%20AI-6DB33F?style=for-the-badge"> 
  <img src="https://img.shields.io/badge/Qdrant-FF6F00?style=for-the-badge">

- ### **Authentication & Security**  
  <img src="https://img.shields.io/badge/Spring%20Security-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white"> 
  <img src="https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white"> 
  <img src="https://img.shields.io/badge/OAuth2-EB5424?style=for-the-badge">

- ### **Docs & API Testing**  
  <img src="https://img.shields.io/badge/Swagger-85EA2D?style=for-the-badge&logo=swagger&logoColor=white"> 
  <img src="https://img.shields.io/badge/Postman-FF6C37?style=for-the-badge&logo=postman&logoColor=white">

- ### **Infra & Deployment**  
  <img src="https://img.shields.io/badge/AWS%20EC2-FF9900?style=for-the-badge&logo=amazonaws&logoColor=white">
  <img src="https://img.shields.io/badge/Nginx-009639?style=for-the-badge&logo=nginx&logoColor=white">  
  <img src="https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white">
  <img src="https://img.shields.io/badge/Docker%20Compose-2496ED?style=for-the-badge&logo=docker&logoColor=white">   
  <img src="https://img.shields.io/badge/GitHub%20Actions-2088FF?style=for-the-badge&logo=githubactions&logoColor=white"> 
  <img src="https://img.shields.io/badge/Kubernetes-326CE5?style=for-the-badge&logo=kubernetes&logoColor=white">

- ### **Collaboration Tools**  
  <img src="https://img.shields.io/badge/GitHub-181717?style=for-the-badge&logo=github&logoColor=white"> <img src="https://img.shields.io/badge/Slack-4A154B?style=for-the-badge&logo=slack&logoColor=white"> <img src="https://img.shields.io/badge/Notion-000000?style=for-the-badge&logo=notion&logoColor=white">

