FROM maven:3.9.5-openjdk-21 AS build

WORKDIR /app

COPY pom.xml .

COPY src ./src

RUN mvn clean package -DskipTests

FROM openjdk:21-jre-slim

WORKDIR /app

COPY --from=build target/*.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]