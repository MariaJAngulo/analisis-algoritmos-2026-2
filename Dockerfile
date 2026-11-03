# Etapa 1: compilar y empaquetar el JAR con Maven
FROM maven:3.9-eclipse-temurin-21 AS construccion
WORKDIR /proyecto
COPY pom.xml .
COPY src ./src
RUN mvn -q -DskipTests package

# Etapa 2: imagen final, solo con Java y lo necesario para ejecutar
FROM eclipse-temurin:21-jre
WORKDIR /aplicacion
COPY --from=construccion /proyecto/target/app.jar app.jar
COPY datos ./datos
# Render entrega el puerto en la variable PORT; el servidor la lee solo.
# El plan gratuito tiene 512 MB: limitamos el heap para no pasarnos.
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=70"
EXPOSE 8081
CMD ["java", "-jar", "app.jar"]
