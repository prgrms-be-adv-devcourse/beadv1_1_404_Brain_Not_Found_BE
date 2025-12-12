FROM gradle:8.10.2-jdk21 AS builder

WORKDIR /app

COPY build.gradle settings.gradle gradlew /app/
COPY gradle /app/gradle/

# 권한 설정
RUN chmod +x gradlew

# 의존성만 먼저 빌드
RUN ./gradlew dependencies --no-daemon
COPY core /app/core/
COPY order /app/order/
# 저장 경로 app/order/build/libs/order-<version>.jar
RUN ./gradlew clean :order:bootJar -x test --no-daemon --stacktrace


FROM azul/zulu-openjdk:21-latest
WORKDIR /app

#RUN apt-get update && \
#    apt-get install -y curl

# 8. 빌드된 jar파일 최상위로 복사
COPY --from=builder /app/order/build/libs/*.jar app.jar
# 9. 외부 포트 노출
EXPOSE 8082

# x. 컨테이너 시작 명령
ENTRYPOINT ["java", "-jar", "app.jar"]


