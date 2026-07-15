FROM amazoncorretto:17-alpine
RUN apk add --no-cache font-noto-cjk
WORKDIR /app

COPY build/libs/*-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-XX:+UseG1GC", \
    "-Djava.awt.headless=true", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-Duser.timezone=Asia/Seoul", \
    "-jar", "app.jar"]
