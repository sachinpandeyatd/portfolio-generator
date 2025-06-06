FROM eclipse-temurin:21-jdk-jammy AS builder


# Set Maven version
ARG MAVEN_VERSION=3.9.6
ARG USER_HOME_DIR="/root"
# SHA512 checksum for Maven binary (good for security, find on Apache Maven download page)
ARG SHA=8119096899595NPMe0177022bbe2f4044747c3f38075730d202494911186780786A0191AAA3B087F25776C54A7B5FDE84B634CD8240212CCB79477B576023F0C7
# Base URL for Maven downloads
ARG BASE_URL=https://apache.osuosl.org/maven/maven-3/${MAVEN_VERSION}/binaries


RUN apt-get update && \
    apt-get install -y wget && \
    mkdir -p /usr/share/maven /usr/share/maven/ref && \
    wget -q -O /tmp/apache-maven.tar.gz ${BASE_URL}/apache-maven-${MAVEN_VERSION}-bin.tar.gz && \
    echo "${SHA}  /tmp/apache-maven.tar.gz" | sha512sum -c - && \
    tar -xzf /tmp/apache-maven.tar.gz -C /usr/share/maven --strip-components=1 && \
    rm -f /tmp/apache-maven.tar.gz && \
    ln -s /usr/share/maven/bin/mvn /usr/bin/mvn


WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn package -DskipTests -B

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=builder /app/target/*.jar application.jar

LABEL maintainer="33sachinedu@gmail.com"
LABEL description="Spring Boot backend for AI Portfolio Generator"

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/application.jar"]