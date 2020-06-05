PUT http://localhost:8080/createproject/:projectname  
PUT http://localhost:8080/selectproject/:projectname

PUT http://localhost:8080/updatelabel/:projectname

GET http://localhost:8080/project/:projectname  
GET http://localhost:8080/projects  
    
GET http://localhost:8080/select?projectname={projectname}&filetype={file/folder}  
GET http://localhost:8080/selectstatus/:projectname

GET http://localhost:8080/thumbnail?projectname={projectname}&uuid={uuid}  
GET http://localhost:8080/imgsrc?projectname={projectname}&uuid={uuid}

PUT http://localhost:8080/update?projectname={projectname}&uuid={uuid}

----------------------------------------------------------------------------

**Functionality:**  
Create new project with project name

PUT http://localhost:8080/createproject/:projectname  

**projectname**: String  
Example: http://localhost:8080/createproject/helloworld  

**filetype**
- file
- folder

**request payload**  
null

**return payload**
- key: 
    - string: 
        - message

- value:   
    - integer:   
        - 0 (fail)
        - 1 (success)  

----------------------------------------------------------------------------

**Functionality:**  
Select existing project with project name

PUT http://localhost:8080/selectproject/:projectname  

**projectname**: String  
**request payload**  
null

**return payload**
- key: 
    - string: 
        - message

- value:   
    - integer:   
        - 0 (fail)
        - 1 (success)  
        
- key:
    - string:   
        - uuidlist
    - string:
        - labellist  
  
- value:     
    - string:     
        - array of uuids
        - []
    - string:
        - array of labels
        - []

----------------------------------------------------------------------------
**Functionality:**  
Select existing project with project name

PUT http://localhost:8080/updatelabel/:projectname Example: http://localhost:8080/updatelabel/:helloworld

**projectname**: String 
 
**request payload**  
- key:
    - string:
        - labellist
        
- value:
    - jsonarray:
        -["label1", "label2", "label3"]

**return payload**
- key: 
    - string: 
        - message

- value:   
    - integer:   
        - 0 (fail)
        - 1 (success)  
        
----------------------------------------------------------------------------

**Functionality:**  
Get list of items from project name

GET http://localhost:8080/project/:projectname 

**projectname**: String  
Example: http://localhost:8080/project/:projectname

**request payload**  
null

**return payload**
- key: 
    - string: 
        - message

- value:   
    - integer:   
        - 0 (fail)
        - 1 (success)  
        
- key:
    - string:   
        - uuidlist  
  
- value:     
    - string:     
        - array of uuids
        - []
 
----------------------------------------------------------------------------

**Functionality:**  
Get a list of project names from all existing project names

GET http://localhost:8080/projects    

**request payload**  
null

**return payload**
- key:
    - string:
        - content
        
- value:
    - array of project name
    
----------------------------------------------------------------------------

**Functionality:**  
Open folder window to select root folder contains data  
Alternately, open file window to select multiple files  

GET http://localhost:8080/select?projectname={projectname}&filetype={file/folder}  
Example:  
- http://localhost:8080/select/projectname=helloworld&filetype=file 

**filetype**
- file
- folder

**request payload**  
null

**return payload**
- key: 
    - string: 
        - message

- value:   
    - integer:   
        - 0 (fail)
        - 1 (success)  
        - 2 (window opened / database updating)  //FIXTHIS

----------------------------------------------------------------------------
**Functionality:**  
Check  
1. window status (open/close)
2. files/folder is selected. If yes, return array of (files/folder)

GET http://localhost:8080/selectstatus/:projectname

Example: http://localhost:8080/selectstatus/helloworld

**request payload**  
null  

**return payload**  
- key:
    - string:   
        - message  
  
- value:     
    - integer:     
        - 0 (window open)
        - 1 (window close and no UUIDs created)  
        - 2 (window close and UUIDs creating)
        - 3 (window close and UUIDs created)  
        - 4 (error)
         
- key:
    - string: (if message == 3)  
        - uuidlist  
  
- value:     
    - string:     
        - array of uuids
        - []  

----------------------------------------------------------------------------
**Functionality:**  
Get  
1. Get thumbnail of image (scaled image to 100x100)
2. All other metadata of the requested projectname and uuid  

GET http://localhost:8080/thumbnail?projectname={projectname}&uuid={uuid}     

Example: http://localhost:8080/thumbnail?projectname=helloworld&uuid=12345

**request payload**  
null  

**return payload**  
 - **message** integer (0 - failed, 1 - success)
 - **uuid** integer
 - **projectname** varchar(255)  
 - **imgpath** varchar(255)  
 - **bndbox** varchar(2000)
 - **imgX** integer
 - **imgY** integer
 - **imgW** double
 - **imgH** double
 - **imgOriW** integer
 - **imgOriH** integer
 - **imgthumbnail**  string
  
 

----------------------------------------------------------------------------
**Functionality:**  
Get base64 image of the requested project name and uuid

GET http://localhost:8080/imgsrc?projectname={projectname}&uuid={uuid}     

Example: http://localhost:8080/imgsrc?projectname=helloworld&uuid=12345

**request payload**  
null        
       
**return payload**    
    - **imgsrc**  string     
        
----------------------------------------------------------------------------
**Functionality:**  
Update bounding boxes for a requested project name and uuid  

PUT http://localhost:8080/update?projectname={projectname}&uuid={uuid}  

Example: http://localhost:8080/update?projectname=helloworld&uuid=12345   

**request payload**  
 - **bndbox** varchar(2000)
 - **imgX** integer
 - **imgY** integer
 - **imgW** double
 - **imgH** double
 - **imgOriW** integer
 - **imgOriH** integer
  
**return payload**
- key: 
    - string: 
        - message

- value:   
    - integer:   
        - 0 (fail)
        - 1 (success)
----------------------------------------------------------------------------
**Error Handling:**  
Message failed comes with **errorcode** and **errormessage**  

For example:  
{
  "message": 0,  
  "errorcode": 1,  
  "errormessage": "Project name is not registered in database"  
}  

Error code can be referenced in <em>ErrorCodes.java</em>