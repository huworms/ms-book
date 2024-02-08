#inicia con la imagen base que contiene Java runtime
FROM openjdk:17-jdk-slim as build

# se agregar el jar del microservicio al contenedor
COPY target/product-composite-service-0.0.1-SNAPSHOT.jar product-composite-service-0.0.1-SNAPSHOT.jar

#se ejecuta el microservicio
ENTRYPOINT ["java","-jar","/product-composite-service-0.0.1-SNAPSHOT.jar"]