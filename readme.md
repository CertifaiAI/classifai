<p align="center">
<img src="metadata/logo/Classifai_Logo_Horizontal_Dark.jpg" width="450" height="110" style="float:left"/>
</p>                                                                                   


<h3 align="center">
Data Annotation Platform for AI Training
</h3>

**Classifai** is the more comprehensive open-source data annotation platform.  
It supports the labelling of various data types with multi labelled outputs for AI model training. 

<p align="center">
  <img align="middle" src="metadata/classifai_workflow_0_0.gif"/>
</p>

Data labelling tasks are challenging due to a few factors:
- long hours of human workforce to label the data manually, 
- off-the-shelves toolkit which cannot fulfill the use cases needs.
- frustration processes to convert data to supported format types for labelling work

We aim to solve these in classifai to provide significant value to the data science workflow.

## Features
- Platform-agnostic
- Asynchronous API - Fast and speedy response
- In-memory Java-based database
- Conversion of conventional data types to preferred formats
- Current supported data type formats  
<center>
  
| Images        | Documents|Video|Tabular|Voice|
|:-------------:|:-------------:|:-------------:|:-------------:|:-------------:|
|jpg, JPEG, JPG| pdf |Coming soon... |Coming soon... |Coming soon... |
|png, PNG| | | | | |
|tif, tiff| | | | | |  
|bmp| | | | | |  

</center>

- [Still WIP. Stay tune with these features coming up below]   
  - **AI in the Loop** - Deep Learning assistant for labelling task to reclaim valuable time for meaningful ML/DL tasks.  
  - Data management


## Strengths of the tool
- Scale data labelling operations to massive real world dataset
- Cut costly data labelling services
- Aesthetic and intuitive UI interface, to make the work fun to do!
- Reclaim valuable time from inefficient data labelling, technical team can focus on more meaningful ML/DL tasks.

## Quick Tour

## How to Run 

Classifai supports the following Operating Systems.  




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

##

