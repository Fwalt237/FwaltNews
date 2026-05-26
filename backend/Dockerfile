FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle gradle.properties ./

COPY module-web/build.gradle ./module-web/

RUN chmod +x gradlew
RUN ./gradlew dependencies --no-daemon
COPY . .
RUN chmod +x gradlew
RUN ./gradlew :module-web:bootJar -x test

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

COPY --from=build /app/module-web/build/libs/*.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]



