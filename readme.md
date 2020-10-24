<img src="metadata/logo/Classifai_Logo_Horizontal_Dark.jpg" width="280" height="65" style="float:left"/>
                                                                                  
  
  

Classifai is a deep learning powered data annotator toolkit.  

Data labelling tasks are challenging due to a few factors:
- long hours of human workforce to label the data manually, 
- frustration processes which further complicate the labelling work
- off-the-shelves product which cannot fulfill the use cases needs.

We aim to solve these in classifai to provide significant value to the data science workflow.

<p align="center">
  <img align="middle" src="metadata/Classifai.gif" width="700" height="425"/>
</p>



## Features
- Platform-agnostic (Currently tested on Mac and Windows)
- Asynchronous - Fast and speedy response
- In-memory Java-based database with JDBC driver
- [Still WIP. Stay tune with these features coming up below]   
  - Data alteration with built in Computer Vision features
  - **AI in the Loop** - Deep Learning assistant for labelling task to reclaim valuable time for meaningful ML/DL tasks.  
  - One click retraining of models after labelling  

## Strengths of the tool
- Scale data labelling operations to massive real world dataset
- Cut costly data labelling services
- Aesthetic and intuitive UI interface, to make the work fun to do!
- Reclaim valuable time from inefficient data labelling, technical team can focus on more meaningful ML/DL tasks.

## How to Run  

## Prerequisites  
Install [Java JDK 8](https://www.oracle.com/java/technologies/javase/javase-jdk8-downloads.html) according on your platforms. 


## On Mac/Linux
Run  
```
./startClassifai.sh --port=8888 --unlockdb=false  
```

## On Windows
Run  
```
startClassifai.bat --port=8888 --unlockdb=false  
```

## Alternatively
```
./mvnw -Puberjar -Dmaven.test.skip=true clean package  
cd classifai-uberjar/target  
java -jar classifai-uberjar-1.0-SNAPSHOT-dev.jar --port=8888 --unlockdb=false  
```

This repo is tested on both Windows and Mac in current stage.  
Do submit issues or contact us at _helloannotation@certifai.ai_.  

