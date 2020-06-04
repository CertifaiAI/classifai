
## Two Databases

### Portfolio Database
 
- **projectid** integer identity primary key
- **uuidlist** clob  

### Project Database  
- **uuid** integer identity primary key  
- **projectid** integer
- **imgpath** varchar(255)  
- **bndbox** varchar(2000) - a list contains a dictionary  
    - when init: ServerConfig.EMPTY_ARRAY
- **imgX** integer - origin 0
    - when init: 0
- **imgY** integer - origin 0
    - when init: 0
- **imgW** double - current width
    - when init: 0
- **imgH** double - current height
    - when init: 0
- **imgOriW** integer -  original width
- **imgOriH** integer - original height  
 
## Other parameters  
imgthumbnail (resize to database)  
imgsrc     