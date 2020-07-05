PUT http://localhost:{port}/createproject/:projectname  
GET http://localhost:{port}/selectproject/:projectname

GET http://localhost:{port}/selectproject/status/:projectname 

PUT http://localhost:{port}/updatelabel/:projectname 

GET http://localhost:{port}/project/:projectname  
GET http://localhost:{port}/projects  
    
GET http://localhost:{port}/select?projectname={projectname}&filetype={file/folder} 
GET http://localhost:{port}/selectstatus/:projectname 

GET http://localhost:{port}/thumbnail?projectname={projectname}&uuid={uuid} 
GET http://localhost:{port}/imgsrc?projectname={projectname}&uuid={uuid}

PUT http://localhost:{port}/update?projectname={projectname}&uuid={uuid}

----------------------------------------------------------------------------

**Functionality:**  
Create new project with project name

PUT http://localhost:{port}/createproject/:projectname  -> PUT http://localhost:{port}/createproject/imglbl/:projectnam

**projectname**: String  
Example: http://localhost:{port}/createproject/helloworld  

**request payload**  
null

**return payload**
- key:
    - string:
        - **message** integer (0 - failed, 1 - success)    
        - **content** list
    
- key: (if message == 0)
    - string:
        - **errormessage** string  

----------------------------------------------------------------------------

**Functionality:**  
Select existing project with project name

GET http://localhost:{port}/selectproject/:projectname  

**projectname**: String  

**request payload**  
null

**return payload**
- key: 
    - **message** integer (0 - failed, 1 - success)
----------------------------------------------------------------------------

**Functionality:**  
Return uuuid and label list of selected project

GET http://localhost:{port}/selectproject/status/:projectname  
      
**return payload**
- key: 
    - string: 
        - **message**

- value:   
    - integer:   
        - 0 (failure)
        - 1 (loading) 
        - 2 (success)
        - 3 (did not initiated)
        
        
- key: (if message == 1)  
  - string: 
      - **progress**  

- value:     
  - string:  
      - [current processing number, total number]           
                 
- key: (if message == 2)  
    - string:   
        - **uuidlist**
    - string:
        - **labellist**  
  
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

PUT http://localhost:{port}/updatelabel/:projectname Example: http://localhost:{port}/updatelabel/:helloworld

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

GET http://localhost:{port}/project/:projectname 

**projectname**: String  
Example: http://localhost:{port}/project/:projectname

**request payload**  
null

**return payload**
- key: 
    - string: 
        - **message** integer (0 - failed, 1 - success)
        - **uuidlist** array of uuids/[]  
 
----------------------------------------------------------------------------

**Functionality:**  
Get a list of project names from all existing project names

GET http://localhost:{port}/projects    

**request payload**  
null

**return payload**
- key:
    - string:
        - **message** integer (0 - failed, 1 - success)    
        - **content** list
    
- key: (if message == 0)
    - string:
        - **errormessage** string  
----------------------------------------------------------------------------

**Functionality:**  
Open folder window to select root folder contains data  
Alternately, open file window to select multiple files  

GET http://localhost:{port}/select?projectname={projectname}&filetype={file/folder}  
Example:  
- http://localhost:{port}/select/projectname=helloworld&filetype=file 

**filetype**
- file
- folder

**request payload**  
null

**return payload**
- key: 
    - string: 
        - **message** integer (0 - failed, 1 - success, 2 - window opened / database updating) //FIX THIS 
----------------------------------------------------------------------------
**Functionality:**  
Check  
1. window status (open/close)
2. files/folder is selected. If yes, return array of (files/folder)

GET http://localhost:{port}/selectstatus/:projectname

Example: http://localhost:{port}/selectstatus/helloworld

**request payload**  
null  

**return payload**  
- key:
    - **message**
  
- value:     
    - integer:     
        - 0 (window open)
        - 1 (window close and no UUIDs created)
        - 2 (window close and loading files)  
        - 3 (window close and database updating)
        - 4 (window close and database updated)  
        - 5 (error)
  
- key:
  - string: (if message == 2)  
      - progress (list)  

- value:     
  - string:  
      - [current processing number, total number]  
                 
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

GET http://localhost:{port}/thumbnail?projectname={projectname}&uuid={uuid}     

Example: http://localhost:{port}/thumbnail?projectname=helloworld&uuid=12345

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
 
- key: (if message == 0)
    - string:
        - **errormessage** string  
  
----------------------------------------------------------------------------
**Functionality:**  
Get base64 image of the requested project name and uuid

GET http://localhost:{port}/imgsrc?projectname={projectname}&uuid={uuid}     

Example: http://localhost:{port}/imgsrc?projectname=helloworld&uuid=12345

**request payload**  
null        
       
**return payload**
    - **message** integer (0 - failed, 1 - success)    
    - **imgsrc**  string  

- key: (if message == 0)
    - string:
        - **errormessage** string  
        
----------------------------------------------------------------------------
**Functionality:**  
Update bounding boxes for a requested project name and uuid  

PUT http://localhost:{port}/update?projectname={projectname}&uuid={uuid}  

Example: http://localhost:{port}/update?projectname=helloworld&uuid=12345   

**request payload**  
 - **bndbox** varchar(2000)
 - **imgX** integer
 - **imgY** integer
 - **imgW** double
 - **imgH** double
 - **imgOriW** integer
 - **imgOriH** integer
  
**return payload**
    - **message** integer (0 - failed, 1 - success)    

- key: (if message == 0)
    - string:
        - **errormessage** string 
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
