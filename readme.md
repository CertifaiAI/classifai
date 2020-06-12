# Classifai

<p align="center">
  <img align="middle" src="metadata/sample.gif" width="680" height="425"/>
</p>

Classifai is a deep learning powered data annotator toolkit.  

Data labelling tasks are challenging due to a few factors:
- long hours of human workforce to label the data manually, 
- frustration processes which further complicate the labelling work
- off-the-shelves product which cannot fulfill the use cases needs.

We aim to solve these in classifai to provide significant value to the data science workflow.

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

## On Mac/Linux
Run  
```
./startClassifai.sh --port=8888
```

## On Windows
Run  
```
startClassifai.bat --port=8888
```

## Alternatively
```
./mvnw -Puberjar -Dmaven.test.skip=true clean package  
cd classifai-uberjar/target  
java -jar classifai-uberjar-1.0-SNAPSHOT-dev.jar  
```

