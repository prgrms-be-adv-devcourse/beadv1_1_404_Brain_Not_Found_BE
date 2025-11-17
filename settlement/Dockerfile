FROM gradle:8.10.2-jdk21 AS builder

WORKDIR /app

COPY build.gradle settings.gradle gradlew /app/
COPY gradle /app/gradle/

# 권한 설정
RUN chmod +x gradlew

# 의존성만 먼저 빌드
RUN ./gradlew dependencies --no-daemon
COPY core /app/core/
COPY settlement /app/settlement/
# 빌드 (배포 시 -x 옵션 제거 필수!)
# 저장 경로 app/settlement/build/libs/settlement-<version>.jar
RUN ./gradlew :settlement:bootJar -x test --no-daemon --stacktrace


FROM azul/zulu-openjdk:21-latest
WORKDIR /app

RUN apt-get update && \
    apt-get install -y curl

# 8. 빌드된 jar파일 최상위로 복사
COPY --from=builder /app/settlement/build/libs/*.jar app.jar
# 9. 외부 포트 노출
EXPOSE 8080

# x. 컨테이너 시작 명령
ENTRYPOINT ["java", "-jar", "app.jar"]


