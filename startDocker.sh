# Copyright (c) 2020-2021 CertifAI Sdn. Bhd.

#!/bin/bash

# Before Running Docker :
# Step 1: Move the attached docker file to classifai-uberjar/target
# Step 2: Change the ENV parameters accordingly
# Step 3: Edit the mount volume paths in the docker run command below

# Example of docker commands:

# Build project with label: docker run -v C:\Users\ken\Desktop\image:/data -v C:\Users\ken\Desktop\image\label.txt:/data/label.txt -v C:\Users\ken\.classifai:/root/.classifai -p 9999:9999 classifai
# Import project: docker run -v C:\Users\ken\Desktop\image:/data -v C:\Users\ken\Desktop\image\project.json:/data/project.json  -v C:\Users\ken\.classifai:/root/.classifai -p 9999:9999 classifai

# If the previous project need to deploy together into docker, remember to mount the previous volume
# Exp: previous project path at /pic and current path at /data, so add -v C:\Users\ken\Desktop\picture:/pic -v C:\Users\ken\Desktop\image:/data to the docker run command
# If want to focus on current project, no need mount previous projects volume

docker build ./classifai-uberjar/target -t classifai

docker run -v /mnt/c/Users/ken/Desktop/image:/data -v /mnt/c/Users/ken/.classifai:/root/.classifai -p 9999:9999 classifai