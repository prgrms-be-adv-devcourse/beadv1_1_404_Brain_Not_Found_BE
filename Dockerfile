# 1. 베이스 이미지
FROM azul/zulu-openjdk:21-latest

RUN apt-get update && apt-get install -y curl

# 2. 작업 디렉토리
WORKDIR /app


# 3. Gradle 빌드 결과 복사
COPY build/libs/*.jar app.jar

# 4. 컨테이너 시작 명령
ENTRYPOINT ["java", "-jar", "app.jar"]
