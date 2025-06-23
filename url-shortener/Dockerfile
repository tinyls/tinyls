FROM maven:3.9.9-eclipse-temurin-21-alpine AS deps

WORKDIR /app
COPY pom.xml .

RUN mvn go-offline:resolve-dependencies


FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder

WORKDIR /app
COPY --from=deps /root/.m2/repository /root/.m2/repository
COPY . /app

RUN mvn package -DskipTests


FROM maven:3.9.9-eclipse-temurin-21-alpine AS dev

WORKDIR /app
COPY --from=deps /root/.m2 /root/.m2
COPY . /app
RUN mvn compile -DskipTests

ENTRYPOINT [ "mvn", "spring-boot:run" ]


FROM eclipse-temurin:21-jdk-alpine AS release

LABEL maintainer="Mounish Pedagandham <mounish.pedagandham@gmail.com>"

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

RUN addgroup --system app && adduser -S -s /bin/false -G app app
RUN chown -R app:app /app

ENV SPRING_OUTPUT_ANSI_ENABLED=ALWAYS

USER app
EXPOSE 8000

CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"] 