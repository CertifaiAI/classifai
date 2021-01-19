FROM openjdk:14-oracle

ENV DISPLAY :11
ARG JAR=classifai-uberjar-dev.jar
ARG PORT=9999

WORKDIR /
ADD $JAR $JAR

EXPOSE $PORT
CMD ["java","-jar","classifai-uberjar-dev.jar","--unlockdb", "--port=$PORT"]