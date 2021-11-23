# Copyright (c) 2020-2021 CertifAI Sdn. Bhd.

# ----------------------------- Deploy Classifai into Docker Container--------------------------------------------------

# Step 1: Move this docker file into classifai-uberjar/target
#
# Step 2: Change the ENV parameters below to different name or path accordingly. Remove the ENV parameter that not
#         required Noted that datapath, labelpath and configpath are paths that use to access the files in the docker
#         container that shared from the local machine.
#
# Step 3: Modify the arguments in startDocker.bat or startDocker.sh
#         Make sure to mount volume using -v in order to share the dataset folder and database from local machine
#         into docker container before running the bat or sh file.
#
# Settings:
# Build project: The ENV parameter required are port, projectname, projecttype, datapath, labelpath(optional)
# Import project: The ENV parameter required are port, datapath, configpath
#
# ----------------------------------------------------------------------------------------------------------------------
FROM adoptopenjdk:14.0.2_12-jdk-hotspot

ARG jar=classifai-uberjar-dev.jar

WORKDIR /classifai
COPY $jar /classifai/$jar

ENV JAVA_HOME=/mnt/c/'Program Files'/AdoptOpenJDK/jdk
ENV port=9999
ENV projectname=project
ENV projecttype=boundingbox
ENV datapath=/data
ENV labelpath=/data/label.txt
ENV configpath=/data/project.json

EXPOSE $PORT

CMD ["sh", "-c", "java -jar classifai-uberjar-dev.jar --unlockdb --docker --port=$port --projectname=$projectname \
        --projecttype=$projecttype --datapath=$datapath --labelpath=$labelpath --configpath=$configpath"]