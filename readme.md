# Intellibel

Intellibel is a deep learning powered data annotator toolkit.  

Data labelling tasks are challenging due to a few factors:
- long hours of human workforce to label the data manually, 
- frustration processes which further complicate the labelling work
- technology/product which cannot fulfill the use cases needs.

We aim to solve these in Intellibel to provide significant value to the data science workflow.

## Features
- **AI in the Loop** - Deep Learning assistant for labelling task to reclaim valuable time for meaningful ML/DL tasks.
- Data alteration with built in Computer Vision features
- One click retraining of models after labelling  
- Platform-agnostic (Currently tested on Mac and Windows)
- Asynchronous - Fast and speedy response
- In-memory Java-based database with JDBC driver

## Strengths of the tool
- Scale data labelling operations to massive real world dataset  
- Cut costly data labelling services
- Reclaim valuable time from inefficient data labelling, technical team can focus on more meaningful ML/DL tasks.

## How to Run

## On Mac/Linux
Run  
```
./startIntellibel.sh --port=8888
```

## On Windows
Run  
```
startIntellibel.bat --port=8888
```

## Alternatively
```
./mvnw -Puberjar -Dmaven.test.skip=true clean package  
cd intellibel-uberjar/target  
java -jar intellibel-uberjar-1.0-SNAPSHOT-dev.jar  
```

