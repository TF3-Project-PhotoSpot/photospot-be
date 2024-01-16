FROM openjdk:17

COPY ./build/libs/*.jar ./photospot.jar

ENTRYPOINT ["java", "-jar", "photospot.jar"]
