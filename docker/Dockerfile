FROM adoptopenjdk:14-jdk-openj9

ENV DISPLAY :11
ENV JAVA_HOME /opt/jdk
ENV PATH ${PATH}:${JAVA_HOME}/bin

RUN apk-get libxext6 libxrender-dev

WORKDIR /
ADD classifai-uberjar-1.0-SNAPSHOT-dev.jar classifai.jar
EXPOSE 9999
# CMD java -jar classifai.jar --unlockdb=true --port=9999 -Dvertx.disableFileCaching=true

ENTRYPOINT ["java", "-jar", "classifai.jar", "--unlockdb=true", "--port=9999", "-Dvertx.disableFileCaching=true"]