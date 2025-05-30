FROM docker.io/library/eclipse-temurin:21-jdk-alpine@sha256:cafcfad1d9d3b6e7dd983fa367f085ca1c846ce792da59bcb420ac4424296d56 AS builder

WORKDIR /src/eventsphere

COPY . .
RUN chmod +x gradlew
RUN ./gradlew dependencies --no-daemon


COPY src ./src


RUN ./gradlew bootJar --no-daemon


FROM docker.io/library/eclipse-temurin:21-jre-alpine@sha256:4e9ab608d97796571b1d5bbcd1c9f430a89a5f03fe5aa6c093888ceb6756c502 AS runner

ARG USER_NAME=eventsphere
ARG USER_UID=1000
ARG USER_GID=${USER_UID}


RUN addgroup -g ${USER_GID} ${USER_NAME} \
    && adduser -h /opt/eventsphere -D -u ${USER_UID} -G ${USER_NAME} ${USER_NAME}

USER ${USER_NAME}
WORKDIR /opt/eventsphere
COPY --from=builder --chown=${USER_UID}:${USER_GID} /src/eventsphere/build/libs/*.jar app.jar

EXPOSE 8080


ENTRYPOINT ["java"]
CMD ["-jar", "app.jar"]