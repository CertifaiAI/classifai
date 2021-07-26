FROM adoptopenjdk:14.0.2_12-jdk-hotspot

ARG jar=classifai-uberjar-dev.jar

WORKDIR /classifai
COPY $jar /classifai/$jar

ENV JAVA_HOME=/mnt/c/'Program Files'/AdoptOpenJDK/jdk
ENV port=9999
ENV projectname=project5
ENV projecttype=boundingbox
ENV datapath=/usr/PNG
ENV labelpath=/usr/PNG/label.txt

EXPOSE $PORT

CMD ["sh", "-c", "java -jar classifai-uberjar-dev.jar --unlockdb --docker --port=$port --projectname=$projectname --projecttype=$projecttype --datapath=$datapath --labelpath=$labelpath"]