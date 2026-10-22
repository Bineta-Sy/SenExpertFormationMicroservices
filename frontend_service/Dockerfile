# ─────────────────────────────────────────────
# Étape 1 : Build Maven
# ─────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests -B

# ─────────────────────────────────────────────
# Étape 2 : Runtime Wildfly 32 (Jakarta EE 10)
# ─────────────────────────────────────────────
FROM quay.io/wildfly/wildfly:32.0.1.Final-jdk17

# Copier le WAR dans le dossier de déploiement automatique de Wildfly
COPY --from=build /app/target/frontend_service.war /opt/jboss/wildfly/standalone/deployments/

# Port HTTP Wildfly
EXPOSE 8080

# Démarrage en mode standalone avec binding sur toutes les interfaces
CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0"]
