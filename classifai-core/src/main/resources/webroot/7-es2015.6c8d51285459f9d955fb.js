(window.webpackJsonp=window.webpackJsonp||[]).push([[7],{RN8A:function(t,e,n){"use strict";n.r(e),n.d(e,"DataSetLayoutModule",function(){return nt});var i=n("ofXK"),a=n("XIp8"),o=n("SxV6"),c=n("5+tZ"),r=n("lJxs"),s=n("/uUt"),l=n("1G5W"),b=n("XNiG"),d=n("cp0P"),h=n("HDdC"),p=n("3N8a");const g=new(n("IjjT").a)(p.a);var u=n("DH7j");function m(t=0,e=g){var n;return n=t,(Object(u.a)(n)||!(n-parseFloat(n)+1>=0)||t<0)&&(t=0),e&&"function"==typeof e.schedule||(e=g),new h.a(n=>(n.add(e.schedule(f,t,{subscriber:n,counter:0,period:t})),n))}function f(t){const{subscriber:e,counter:n,period:i}=t;e.next(n),this.schedule({subscriber:e,counter:n+1,period:i},i)}var v=n("z6cu"),j=n("3Pt+"),_=n("fXoL"),w=n("tyNb"),x=n("LY9J"),M=n("F7l1"),O=n("14na"),C=n("I7yr"),L=n("x2Se"),P=n("sYmb");function y(t,e){if(1&t){const t=_.Nb();_.Kb(0),_.Mb(1,"div",5),_.Mb(2,"div",6),_.Tb("click",function(){return _.gc(t),_.Vb(2).displayModal()}),_.Ib(3,"img",7),_.Mb(4,"label",8),_.lc(5),_.Wb(6,"translate"),_.Lb(),_.Lb(),_.Lb(),_.Jb()}if(2&t){const t=_.Vb(),e=t.index,n=t.$implicit;_.xb(1),_.yb("data-index",e),_.xb(2),_.ac("src",n.src,_.hc),_.xb(2),_.mc(_.Xb(6,3,n.name))}}function k(t,e){if(1&t&&(_.Mb(0,"div",9),_.Ib(1,"img",10),_.Mb(2,"label",11),_.lc(3),_.Wb(4,"translate"),_.Lb(),_.Lb()),2&t){const t=_.Vb(),e=t.$implicit;_.yb("data-index",t.index),_.xb(1),_.ac("src",e.src,_.hc),_.xb(2),_.mc(_.Xb(4,3,e.name))}}function S(t,e){if(1&t&&(_.Kb(0),_.jc(1,y,7,5,"ng-container",3),_.jc(2,k,5,5,"ng-template",null,4,_.kc),_.Jb()),2&t){const t=e.index,n=_.ec(3);_.xb(1),_.ac("ngIf",0===t)("ngIfElse",n)}}let I=(()=>{class t{constructor(){this.menuSchema=[{src:"../../../assets/icons/add.svg",name:"menuName.newProject"},{src:"../../../assets/icons/project.svg",name:"menuName.myProject"},{src:"../../../assets/icons/starred.svg",name:"menuName.starred"},{src:"../../../assets/icons/history.svg",name:"menuName.recent"},{src:"../../../assets/icons/trash.svg",name:"menuName.trash"}],this._onClick=new _.n,this.displayModal=()=>{this._onClick.emit(!0)}}ngOnInit(){}}return t.\u0275fac=function(e){return new(e||t)},t.\u0275cmp=_.Bb({type:t,selectors:[["data-set-side-menu"]],outputs:{_onClick:"_onClick"},decls:3,vars:1,consts:[[1,"dataset-sidemenu-container"],[4,"ngFor","ngForOf"],[1,"horizontal-line"],[4,"ngIf","ngIfElse"],["otherMenu",""],[1,"new-project-container"],[1,"new-project-btn",3,"click"],[1,"add-icon",3,"src"],[1,"new-project-txt"],[1,"current-project-btn"],[1,"project-icon",3,"src"],[1,"current-project-txt"]],template:function(t,e){1&t&&(_.Mb(0,"div",0),_.jc(1,S,4,2,"ng-container",1),_.Lb(),_.Ib(2,"div",2)),2&t&&(_.xb(1),_.ac("ngForOf",e.menuSchema))},directives:[i.j,i.k],pipes:[P.c],styles:[".dataset-sidemenu-container[_ngcontent-%COMP%]{display:flex;flex-wrap:wrap;flex-direction:column;width:16vw}.new-project-container[_ngcontent-%COMP%]{margin-bottom:5vh;margin-left:2vw}.new-project-btn[_ngcontent-%COMP%]{padding:1vw;border-radius:5vh;background-color:#525353;border:none;color:#fff;outline:none;cursor:pointer;display:flex;justify-content:space-around;align-items:center;min-width:10vw;max-width:10vw;min-height:4vh;max-height:4vh}.new-project-btn[_ngcontent-%COMP%]:hover{background-color:#393838}.add-icon[_ngcontent-%COMP%]{min-height:inherit;max-height:inherit}.new-project-txt[_ngcontent-%COMP%]{border:none;background:none;outline:none;cursor:pointer;font-size:2vh;color:#fff;text-align:start;white-space:nowrap}.current-project-btn[_ngcontent-%COMP%]{color:#fff;cursor:pointer;display:flex;align-items:center;border-radius:5vh;padding:1vh 1vw;margin-left:2vw;min-width:10vw;max-width:10vw;min-height:5vh;max-height:5vh;flex:1 1 100%}.current-project-btn[_ngcontent-%COMP%]:hover{background-color:#525353}.project-icon[_ngcontent-%COMP%]{min-height:4vh;max-height:4vh;flex:1 1 10%}.current-project-txt[_ngcontent-%COMP%]{border:none;background:none;outline:none;cursor:pointer;font-size:2vh;color:#fff;white-space:nowrap;flex:1 1 90%;text-align:left;padding-left:20px}.horizontal-line[_ngcontent-%COMP%]{width:12vw;background-color:#393838;min-height:.3vh;max-height:.3vh;margin:auto;border:.0625rem solid #000}"]}),t})(),N=(()=>{class t{constructor(){}ngOnInit(){}}return t.\u0275fac=function(e){return new(e||t)},t.\u0275cmp=_.Bb({type:t,selectors:[["data-set-header"]],decls:6,vars:3,consts:[[1,"dataset-header-container"],[1,"label"],[1,"dataset-icon-container"]],template:function(t,e){1&t&&(_.Mb(0,"div",0),_.Mb(1,"label",1),_.lc(2),_.Wb(3,"translate"),_.Lb(),_.Mb(4,"div",2),_.Ib(5,"div"),_.Lb(),_.Lb()),2&t&&(_.xb(2),_.mc(_.Xb(3,1,"datasetHeader.datasetManagement")))},pipes:[P.c],styles:[".dataset-header-container[_ngcontent-%COMP%]{display:flex;justify-content:space-around;align-items:center;padding:1vw;min-width:80vw;max-width:80vw}.label[_ngcontent-%COMP%]{flex:1 1 80%;background:none;font-size:2.5vh;color:#fff;white-space:nowrap;min-height:inherit;max-height:inherit}.dataset-icon-container[_ngcontent-%COMP%]{display:flex;justify-content:space-between;align-items:center;flex:1 1 20%}.dataset-icon[_ngcontent-%COMP%]{flex:1 1 3%;min-width:2vw;max-width:2vw;cursor:pointer}.dataset-icon[_ngcontent-%COMP%]:hover{border-radius:5vh;background-color:#393838}.dataset-select[_ngcontent-%COMP%]{min-height:4vh;max-height:4vh;font-size:2vh;min-width:7vw;max-width:7vw;-moz-text-align-last:center;background:#000;color:#fff;border:.1vw solid;text-align-last:center}.dataset-select[_ngcontent-%COMP%]:focus, .dataset-select[_ngcontent-%COMP%]:hover{background:#393838}.dataset-select[_ngcontent-%COMP%]:focus, .dataset-select[_ngcontent-%COMP%]:hover, select[_ngcontent-%COMP%]{-moz-appearance:none;-webkit-appearance:none}option[_ngcontent-%COMP%]{background:#000;text-align:center}"]}),t})();function T(t,e){1&t&&(_.Kb(0),_.Mb(1,"div",2),_.Mb(2,"div",3),_.Mb(3,"div",4),_.Mb(4,"label",5),_.lc(5),_.Wb(6,"translate"),_.Lb(),_.Lb(),_.Mb(7,"div"),_.Mb(8,"label",6),_.lc(9),_.Wb(10,"translate"),_.Lb(),_.Lb(),_.Lb(),_.Lb(),_.Jb()),2&t&&(_.xb(5),_.mc(_.Xb(6,2,"datasetCard.fetchingProject")),_.xb(4),_.mc(_.Xb(10,4,"datasetCard.pleaseWait")))}function F(t,e){1&t&&(_.Kb(0),_.Mb(1,"label",22),_.lc(2),_.Wb(3,"translate"),_.Lb(),_.Jb()),2&t&&(_.xb(2),_.nc(" ",_.Xb(3,1,"datasetCard.uploading")," "))}function z(t,e){1&t&&(_.Kb(0),_.Mb(1,"label",24),_.lc(2),_.Wb(3,"translate"),_.Lb(),_.Jb()),2&t&&(_.xb(2),_.mc(_.Xb(3,1,"datasetCard.new")))}function E(t,e){1&t&&(_.Kb(0),_.Mb(1,"label",25),_.lc(2),_.Wb(3,"translate"),_.Lb(),_.Jb()),2&t&&(_.xb(2),_.mc(_.Xb(3,1,"datasetCard.available")))}function U(t,e){1&t&&(_.Kb(0),_.Mb(1,"label",26),_.lc(2),_.Wb(3,"translate"),_.Lb(),_.Jb()),2&t&&(_.xb(2),_.mc(_.Xb(3,1,"datasetCard.notAvailable")))}function X(t,e){if(1&t&&(_.Kb(0,23),_.jc(1,z,4,3,"ng-container",16),_.jc(2,E,4,3,"ng-container",16),_.jc(3,U,4,3,"ng-container",16),_.Jb()),2&t){const t=_.Vb().$implicit;_.ac("ngSwitch",t),_.xb(1),_.ac("ngIf",t.is_new),_.xb(1),_.ac("ngIf",!t.is_new&&!t.is_loaded),_.xb(1),_.ac("ngIf",!t.is_new&&t.is_loaded)}}function D(t,e){if(1&t){const t=_.Nb();_.Kb(0),_.Mb(1,"span"),_.Mb(2,"div",27),_.Mb(3,"div",28),_.Tb("click",function(){_.gc(t);const e=_.Vb(),n=e.index,i=e.$implicit;return _.Vb(3).onUploadContent(n,i.project_name)}),_.lc(4),_.Wb(5,"translate"),_.Lb(),_.Mb(6,"div",28),_.Tb("click",function(){_.gc(t);const e=_.Vb(),n=e.index,i=e.$implicit;return _.Vb(3).onUploadContent(n,i.project_name,"file")}),_.lc(7),_.Wb(8,"translate"),_.Lb(),_.Lb(),_.Lb(),_.Jb()}2&t&&(_.xb(4),_.nc(" ",_.Xb(5,2,"datasetCard.uploadFolder")," "),_.xb(3),_.nc(" ",_.Xb(8,4,"datasetCard.uploadPhotos")," "))}const W=function(t){return[t]};function $(t,e){if(1&t){const t=_.Nb();_.Kb(0),_.Mb(1,"div",9),_.Tb("dblclick",function(){_.gc(t);const n=e.index,i=e.$implicit;return _.Vb(3).onOpenProject(n,i)}),_.Mb(2,"div",10),_.Mb(3,"div"),_.jc(4,F,4,3,"ng-container",0),_.jc(5,X,4,4,"ng-template",null,11,_.kc),_.Lb(),_.Mb(7,"div",12),_.Mb(8,"div",13),_.Tb("click",function(){_.gc(t);const n=e.$implicit,i=_.Vb(3);return i.conditionalDisableClickEvent(n.is_loaded)?null:i.onStarred(n,!n.is_starred)}),_.Ib(9,"img",14),_.Lb(),_.Mb(10,"div",13),_.Tb("click",function(){_.gc(t);const n=e.$implicit,i=e.index,a=_.Vb(3);return a.conditionalDisableClickEvent(n.is_loaded)?null:a.onDisplayMore(i)}),_.Ib(11,"img",15),_.Lb(),_.jc(12,D,9,6,"ng-container",16),_.Lb(),_.Lb(),_.Mb(13,"div",17),_.Mb(14,"label",18),_.Mb(15,"div",19),_.lc(16),_.Wb(17,"translate"),_.Lb(),_.Lb(),_.Lb(),_.Mb(18,"div",20),_.Mb(19,"label",18),_.Mb(20,"div",21),_.lc(21),_.Lb(),_.Lb(),_.Lb(),_.Mb(22,"div",20),_.Mb(23,"label",18),_.Mb(24,"div",21),_.lc(25),_.Wb(26,"translate"),_.Lb(),_.Lb(),_.Lb(),_.Lb(),_.Jb()}if(2&t){const t=e.$implicit,n=e.index,i=_.ec(6),a=_.Vb(3);_.xb(1),_.ac("ngClass",a.conditionalDisableProject(t)),_.yb("data-index",n),_.xb(3),_.ac("ngIf",a.isExactIndex(n)&&a._jsonSchema.isUploading)("ngIfElse",i),_.xb(3),_.ac("ngClass",a.conditionalDisableProject(t)),_.xb(2),_.ac("src",_.cc(19,W,t.is_starred?a.starredActiveIcon:a.starredInactiveIcon),_.hc),_.xb(3),_.ac("ngIf",a.isExactIndex(n)),_.xb(2),_.ac("title",t.created_date),_.xb(2),_.oc(" ",_.Xb(17,15,"datasetCard.created")," ",t.created_date," "),_.xb(3),_.ac("title",t.project_name),_.xb(2),_.nc(" ",t.project_name," "),_.xb(2),_.ac("title","Total Photo: "+t.total_uuid),_.xb(2),_.oc(" ",_.Xb(26,17,"datasetCard.totalPhoto")," ",t.total_uuid," ")}}function J(t,e){if(1&t&&(_.Kb(0),_.Mb(1,"div",2),_.jc(2,$,27,21,"ng-container",8),_.Lb(),_.Jb()),2&t){const t=_.Vb(2);_.xb(2),_.ac("ngForOf",t._jsonSchema.projects)}}function V(t,e){1&t&&(_.Mb(0,"div",2),_.Mb(1,"div",3),_.Mb(2,"div",4),_.Mb(3,"label",5),_.lc(4),_.Wb(5,"translate"),_.Lb(),_.Lb(),_.Mb(6,"div"),_.Mb(7,"label",6),_.lc(8),_.Wb(9,"translate"),_.Lb(),_.Lb(),_.Lb(),_.Lb()),2&t&&(_.xb(4),_.mc(_.Xb(5,2,"datasetCard.noProject")),_.xb(4),_.mc(_.Xb(9,4,"datasetCard.createNew")))}function K(t,e){if(1&t&&(_.jc(0,J,3,1,"ng-container",0),_.jc(1,V,10,6,"ng-template",null,7,_.kc)),2&t){const t=_.ec(2),e=_.Vb();_.ac("ngIf",e._jsonSchema.projects.length>0)("ngIfElse",t)}}let A=(()=>{class t{constructor(){this._onClick=new _.n,this._onUpload=new _.n,this._onStarred=new _.n,this.starredActiveIcon="../../../assets/icons/starred_active.svg",this.starredInactiveIcon="../../../assets/icons/starred.svg",this.cardSchema={clickIndex:-1},this.conditionalDisableProject=({is_loaded:t})=>t?"disabled":"enabled",this.conditionalDisableClickEvent=t=>t,this.onOpenProject=(t,{project_name:e,is_loaded:n})=>{!n&&!this.isExactIndex(t)&&this._onClick.emit(e)},this.onUploadContent=(t,e,n)=>{this.cardSchema={clickIndex:t},this._onUpload.emit({projectName:e,fileType:null!=n?n:"folder"})},this.onDisplayMore=(t=this.cardSchema.clickIndex)=>{const{clickIndex:e}=this.cardSchema;this.cardSchema={clickIndex:e===t?-1:t}},this.onStarred=(t,e)=>{const{project_name:n}=t;this._jsonSchema.projects=this._jsonSchema.projects.map(t=>t.project_name===n?(t.is_starred=e,t):t),this._onStarred.emit({projectName:n,starred:e})},this.isExactIndex=t=>t===this.cardSchema.clickIndex}ngOnInit(){}ngOnChanges(t){const{isUploading:e}=t._jsonSchema.currentValue;!e&&this.onDisplayMore()}}return t.\u0275fac=function(e){return new(e||t)},t.\u0275cmp=_.Bb({type:t,selectors:[["data-set-card"]],inputs:{_jsonSchema:"_jsonSchema"},outputs:{_onClick:"_onClick",_onUpload:"_onUpload",_onStarred:"_onStarred"},features:[_.vb],decls:3,vars:2,consts:[[4,"ngIf","ngIfElse"],["showCardBody",""],[1,"card-layout-container","scroll","fade-in"],[1,"no-project-card-container"],[1,"no-project-title-padding"],[1,"no-project-title"],[1,"no-project-subtitle"],["noProject",""],[4,"ngFor","ngForOf"],[1,"card-container",3,"ngClass","dblclick"],[1,"card-header-style"],["newLabel",""],[1,"card-icon-container",3,"ngClass"],[3,"click"],[1,"card-icon-style",3,"src"],["src","../../../assets/icons/more.svg",1,"card-icon-style"],[4,"ngIf"],[1,"card-title-style"],[3,"title"],[1,"card-title-txt"],[1,"project-name-style"],[1,"project-info"],[1,"project-status-uploading"],[3,"ngSwitch"],[1,"project-status-new"],[1,"project-status-available"],[1,"project-status-not-available"],[1,"popup-container"],[1,"popup-label",3,"click"]],template:function(t,e){if(1&t&&(_.jc(0,T,11,6,"ng-container",0),_.jc(1,K,3,2,"ng-template",null,1,_.kc)),2&t){const t=_.ec(2);_.ac("ngIf",e._jsonSchema.isFetching)("ngIfElse",t)}},directives:[i.k,i.j,i.i,i.m],pipes:[P.c],styles:['@keyframes fade-in{0%{opacity:0}to{opacity:1}}@-webkit-keyframes fade-in{0%{opacity:0}to{opacity:1}}.fade-in[_ngcontent-%COMP%]{animation:fadeIn 1.5s ease;-webkit-animation:fadeIn 1.5s ease;-moz-animation:fadeIn ease 1.5s;-o-animation:fadeIn ease 1.5s;-ms-animation:fadeIn ease 1.5s}.card-layout-container[_ngcontent-%COMP%]{width:80vw;display:flex;flex-wrap:wrap;padding:0 0 0 1vw;overflow-y:scroll;position:relative;height:80vh}.scroll[_ngcontent-%COMP%]::-webkit-scrollbar-track{box-shadow:inset 0 0 6px rgba(0,0,0,.3);border-radius:10px;background-color:#000}.scroll[_ngcontent-%COMP%]::-webkit-scrollbar{width:.5vw}.scroll[_ngcontent-%COMP%]::-webkit-scrollbar-thumb{border-radius:10px;box-shadow:inset 0 0 6px rgba(0,0,0,.3);background-color:#525353}.card-container[_ngcontent-%COMP%]{min-width:11vw;max-width:11vw;min-height:30vh;max-height:30vh;border-style:solid;font-size:2.2vh;background:#2e2d2d;position:relative}.card-container[_ngcontent-%COMP%]:hover{background:#404040}.card-container[_ngcontent-%COMP%]:before{content:"";display:block;height:100%;position:absolute;top:0;left:0;width:.3vw;background-color:#363636}.enabled[_ngcontent-%COMP%]{cursor:pointer}.disabled[_ngcontent-%COMP%]{cursor:not-allowed}.project-status-new[_ngcontent-%COMP%]{background-color:#f59221}.project-status-available[_ngcontent-%COMP%], .project-status-new[_ngcontent-%COMP%]{color:#f5f5f5;font-size:1.5vh;padding:.3vh 1.5vw .6vh 1vw}.project-status-available[_ngcontent-%COMP%]{background-color:#4bf521}.project-status-not-available[_ngcontent-%COMP%]{background-color:#f52121}.project-status-not-available[_ngcontent-%COMP%], .project-status-uploading[_ngcontent-%COMP%]{color:#f5f5f5;font-size:1.5vh;padding:.3vh 1.5vw .6vh 1vw}.project-status-uploading[_ngcontent-%COMP%]{background-color:#f5219d}.card-icon-container[_ngcontent-%COMP%]{display:flex}.card-icon-style[_ngcontent-%COMP%]{min-width:1.5vw;max-width:1.5vw;min-height:3vh;max-height:3vh}.card-icon-style[_ngcontent-%COMP%]:hover{border-radius:5vh;background-color:#393838}.card-header-style[_ngcontent-%COMP%]{margin-top:1vh;display:flex;flex-direction:row;justify-content:space-between;position:relative}.card-title-style[_ngcontent-%COMP%]{margin-left:1vw}.card-title-txt[_ngcontent-%COMP%]{color:#656667;font-size:1.3vh;white-space:nowrap;overflow:hidden;text-overflow:ellipsis}.project-name-style[_ngcontent-%COMP%]{margin-left:1vw;padding:1vh 0 0}.project-info[_ngcontent-%COMP%]{color:#dbdbda;font-size:2vh;white-space:nowrap;overflow:hidden;text-overflow:ellipsis}.tag-container[_ngcontent-%COMP%]{display:flex;flex-direction:row;border-radius:10vw;background:#363636;width:4.5vw;padding:.2vw .3vh;cursor:pointer;outline:none;border:none;margin:.8vh 0 0 1vw}.tag-img[_ngcontent-%COMP%]{min-height:2vh;max-height:2vh;min-width:1vw;max-width:1vw;margin-left:.35vw}.tag-txt[_ngcontent-%COMP%]{font-size:1.2vh;color:#fff;padding:.2vh 0 0 .3vw;cursor:pointer}.popup-container[_ngcontent-%COMP%]{overflow:hidden;position:absolute;margin:4vh 0 0 -10vw;padding:.5vw;border-radius:.2vw;box-shadow:0 2px 5px 0 rgba(var(--shadow-rgb),.26),0 2px 10px 0 rgba(var(--shadow-rgb),.16);transform-origin:left top;transform:scale(1);opacity:1;white-space:nowrap;background:#fff;font-size:1.5vh;-webkit-animation:appear .35s ease-in 1;animation:appear .35s ease-in 1}@-webkit-keyframes appear{0%{opacity:0;transform:translateY(-10px)}}@keyframes appear{0%{opacity:0;transform:translateY(-10px)}}.popup-label[_ngcontent-%COMP%]{font-size:2vh;padding:.5vw}.popup-label[_ngcontent-%COMP%]:hover{background:#e9e9e9}.no-project-title[_ngcontent-%COMP%]{font-size:4vh;color:#000;white-space:nowrap}.no-project-title-padding[_ngcontent-%COMP%]{padding:2vw}.no-project-subtitle[_ngcontent-%COMP%]{font-size:2vh;color:#656667;white-space:nowrap}.no-project-card-container[_ngcontent-%COMP%]{display:flex;flex-direction:column;justify-content:center;align-items:center;background-color:#c8c6c6;width:100%}']}),t})();var H=n("TJKd");const B=["refProjectName"],Y=["labeltextfile"],q=["labeltextfilename"];function G(t,e){1&t&&(_.Mb(0,"span"),_.Mb(1,"small",28),_.lc(2),_.Wb(3,"translate"),_.Lb(),_.Lb()),2&t&&(_.xb(2),_.nc(" ",_.Xb(3,1,"projectNameExist")," "))}function R(t,e){1&t&&(_.Mb(0,"span"),_.Mb(1,"small",28),_.lc(2),_.Wb(3,"translate"),_.Lb(),_.Lb()),2&t&&(_.xb(2),_.nc(" ",_.Xb(3,1,"projectNameRequired")," "))}function Z(t,e){if(1&t&&(_.Kb(0),_.Mb(1,"div",27),_.jc(2,G,4,3,"span",14),_.jc(3,R,4,3,"span",14),_.Lb(),_.Jb()),2&t){const t=_.Vb();let e=null,n=null;_.xb(2),_.ac("ngIf",null==(e=t.form.get("projectName"))?null:e.getError("exist")),_.xb(1),_.ac("ngIf",null==(n=t.form.get("projectName"))?null:n.getError("required"))}}const Q=[{path:"",component:(()=>{class t{constructor(t,e,n,i,h,p,g){this._fb=t,this._cd=e,this._router=n,this._dataSetService=i,this._spinnerService=h,this._imgLblModeService=p,this._languageService=g,this.onChangeSchema={currentThumbnailIndex:-1,thumbnailName:"",totalNumThumbnail:0,status:void 0},this.projectList={projects:[],isUploading:!1,isFetching:!1},this.inputProjectName="",this.selectedProjectName="",this.labelTextUpload=[],this.displayModal=!1,this.subject$=new b.a,this.thumbnailList=[],this.labelList=[],this.unsubscribe$=new b.a,this.isLoading=!1,this.imgLblMode=null,this.getProjectList=()=>{this.projectList.isFetching=!0,this._dataSetService.getProjectList().pipe(Object(o.a)()).subscribe(({content:t})=>{if(t){const e=Object(a.a)(t).map(t=>Object.assign(Object.assign({},t),{created_date:this.formatDate(t.created_date)}));this.projectList.projects=[...e],this.projectList.isFetching=!1}})},this.formatDate=t=>{const e=new Date(t),n=["Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"].find((t,n)=>n===e.getMonth()||void 0);return n?`${n}-${e.getDate()}-${e.getFullYear()}`:"Error"},this.createFormControls=()=>{this.form=this._fb.group({projectName:["",j.l.required]})},this.onChange=t=>{this.inputProjectName=t},this.toggleModalDisplay=t=>{t&&this.form.reset(),this.displayModal=t,setTimeout(()=>this._refProjectName.nativeElement.focus())},this.onFileChange=({target:{files:t}})=>{var e,n;const i=null===(n=null===(e=this._labelTextFile.nativeElement.files)||void 0===e?void 0:e.item(0))||void 0===n?void 0:n.name;this._labelTextFilename.nativeElement.innerHTML=void 0===i?"":i;const a=new FileReader;if(t&&t.length){const e=t.item(0);a.onload=()=>{this._cd.markForCheck()},a.onloadend=()=>{const t=a.result.split("\n");if(t.length>0){const e=t.reduce((t,e)=>{const n=e.replace(/[^A-Z0-9]+/gi,"").toLowerCase();return t.push(n),t},[]);this.labelTextUpload=[],this.labelTextUpload.push(...e)}},e&&a.readAsText(e)}},this.onStarred=({projectName:t,starred:e})=>{this._dataSetService.updateProjectStatus(t,e,"star").pipe(Object(o.a)()).subscribe(({message:t})=>console.log(t),e=>this.projectList={isUploading:this.projectList.isUploading,isFetching:this.projectList.isFetching,projects:this.projectList.projects.map(e=>e.project_name===t?Object.assign(Object.assign({},e),{is_starred:!1}):e)})},this.onSubmit=(t,e)=>{var n,i,a;this.form.markAllAsTouched(),t?this.inputProjectName?this.projectList.projects&&this.projectList.projects.find(t=>t?t.project_name===this.inputProjectName:null)?(null===(n=this.form.get("projectName"))||void 0===n||n.setErrors({exist:!0}),this._refProjectName.nativeElement.focus()):(this.createProject(this.inputProjectName),this.selectedProjectName=null===(i=this.form.get("projectName"))||void 0===i?void 0:i.value):(null===(a=this.form.get("projectName"))||void 0===a||a.setErrors({required:!0}),this._refProjectName.nativeElement.focus()):e&&this.startProject(e)},this.startProject=t=>{this.selectedProjectName=t;const e=this._dataSetService.checkProjectStatus(t),n=this._dataSetService.updateProjectLoadStatus(t),i=this._dataSetService.checkExistProjectStatus(t),a=this._dataSetService.getThumbnailList;this.subjectSubscription=this.subject$.pipe(Object(c.a)(()=>Object(d.a)([e])),Object(o.a)(([{message:t,content:e}])=>{this.projectList={isUploading:this.projectList.isUploading,isFetching:this.projectList.isFetching,projects:this.projectList.projects.map(t=>t.project_name===e[0].project_name?Object.assign(Object.assign({},e[0]),{created_date:t.created_date}):t)};const{is_loaded:n}=e[0];return 1===t&&!n}),Object(c.a)(([{message:t}])=>t?Object(d.a)([n,i]):[]),Object(c.a)(([{},{message:e,uuid_list:n,label_list:r}])=>2===e?(this.labelList=[...r],n.length>0?n.map(e=>a(t,e)):[]):m(500).pipe(Object(c.a)(()=>i),Object(o.a)(({message:t})=>2===t),Object(c.a)(({uuid_list:e,label_list:n})=>(this.labelList=[...n],e.length>0?e.map(e=>a(t,e)):[])))),Object(c.a)(t=>t)).subscribe(t=>{this.thumbnailList=[...this.thumbnailList,t]},t=>{},()=>{this._router.navigate(["imglabel/"+this.imgLblMode],{state:{thumbnailList:this.thumbnailList,projectName:t,labelList:this.labelList}}),this._spinnerService.hideSpinner()}),this.subject$.next()},this.uploadThumbnail=({projectName:t=this.inputProjectName,fileType:e})=>{const n=this._dataSetService.localUploadThumbnail(t,e),i=this._dataSetService.localUploadStatus(t),a=this._dataSetService.getThumbnailList;let r=0;const s=({message:e})=>5!==e&&1===e?m(500).pipe(Object(c.a)(()=>i),Object(o.a)(({message:t})=>4===t||1===t),Object(c.a)(({uuid_list:e,message:n})=>{const i=4===n&&e.length>0?e.map(e=>a(t,e)):[];return this.projectList=Object.assign(Object.assign({},this.projectList),i.length>0?{isUploading:!0}:{isUploading:!1}),r=i.length,i}),Object(c.a)(t=>t)):Object(v.a)(t=>(console.error(t),this.projectList=Object.assign(Object.assign({},this.projectList),{isUploading:!1}),t));this.projectList=Object.assign(Object.assign({},this.projectList),{isUploading:!0}),this.subjectSubscription=this.subject$.pipe(Object(o.a)(),Object(c.a)(()=>n),Object(c.a)(t=>s(t))).subscribe(t=>{r=t?--r:r,r<1&&(this.projectList=Object.assign(Object.assign({},this.projectList),{isUploading:!1}))},t=>{},()=>{this.getProjectList()}),this.subject$.next()},this.createProject=t=>{const e=this._dataSetService.createNewProject(t),n=this._dataSetService.updateLabelList(t,this.labelTextUpload);e.pipe(Object(o.a)(),Object(r.a)(({message:t})=>t),Object(c.a)(()=>n)).subscribe(({message:t})=>{1===t&&(this.getProjectList(),this.toggleModalDisplay(!1))})},this.keyDownEvent=({key:t})=>{"Escape"===t&&this.displayModal&&this.toggleModalDisplay(!1)},this._imgLblModeService.imgLabelMode$.pipe(Object(s.a)()).subscribe(t=>this.imgLblMode=t),this._spinnerService.returnAsObservable().pipe(Object(l.a)(this.unsubscribe$)).subscribe(t=>this.isLoading=t),this.createFormControls(),this._languageService.initializeLanguage("data-set-page",["data-set-page-en","data-set-page-cn","data-set-page-ms"])}ngOnInit(){this.getProjectList()}ngOnDestroy(){this.unsubscribe$.next(),this.unsubscribe$.complete()}}return t.\u0275fac=function(e){return new(e||t)(_.Hb(j.b),_.Hb(_.h),_.Hb(w.a),_.Hb(x.a),_.Hb(M.a),_.Hb(O.a),_.Hb(C.a))},t.\u0275cmp=_.Bb({type:t,selectors:[["data-set-layout"]],viewQuery:function(t,e){if(1&t&&(_.pc(B,!0),_.pc(Y,!0),_.pc(q,!0)),2&t){let t;_.dc(t=_.Ub())&&(e._refProjectName=t.first),_.dc(t=_.Ub())&&(e._labelTextFile=t.first),_.dc(t=_.Ub())&&(e._labelTextFilename=t.first)}},hostBindings:function(t,e){1&t&&_.Tb("keydown",function(t){return e.keyDownEvent(t)},!1,_.fc)},decls:41,vars:25,consts:[[3,"_onChange"],[1,"upper-container"],[3,"_onClick"],[3,"_jsonSchema","_onClick","_onStarred","_onUpload"],[3,"hidden"],[1,"model"],[3,"formGroup"],[1,"model-content"],[1,"content-container"],[1,"content-header"],[1,"new-project-container"],[1,"label"],["type","text","placeholder","Enter project name","formControlName","projectName",1,"input-style",3,"value","input"],["refProjectName",""],[4,"ngIf"],[1,"select-folder-container"],[1,"label","label-file"],[1,"choose-file-btn"],["type","file","accept",".txt",1,"input-id",3,"change"],["labeltextfile",""],[1,"filename"],["labeltextfilename",""],[1,"horizontal-line"],[1,"model-button-container"],["type","submit",1,"button-style","create-btn",3,"click"],[1,"button-style","cancel-btn",3,"click"],[3,"_loading"],[1,"validation"],[1,"error-msg"]],template:function(t,e){if(1&t&&(_.Ib(0,"page-header",0),_.Mb(1,"div",1),_.Mb(2,"data-set-side-menu",2),_.Tb("_onClick",function(t){return e.toggleModalDisplay(t)}),_.Lb(),_.Mb(3,"div"),_.Ib(4,"data-set-header"),_.Mb(5,"data-set-card",3),_.Tb("_onClick",function(t){return e.onSubmit(!1,t)})("_onStarred",function(t){return e.onStarred(t)})("_onUpload",function(t){return e.uploadThumbnail(t)}),_.Lb(),_.Lb(),_.Lb(),_.Mb(6,"div",4),_.Mb(7,"div",5),_.Mb(8,"form",6),_.Mb(9,"div",7),_.Mb(10,"div",8),_.Mb(11,"h1",9),_.lc(12),_.Wb(13,"translate"),_.Lb(),_.Mb(14,"div",10),_.Mb(15,"label",11),_.lc(16),_.Wb(17,"translate"),_.Lb(),_.Mb(18,"input",12,13),_.Tb("input",function(t){return e.onChange(t.target.value)}),_.Lb(),_.Lb(),_.jc(20,Z,4,2,"ng-container",14),_.Mb(21,"div",15),_.Mb(22,"label",16),_.lc(23),_.Wb(24,"translate"),_.Lb(),_.Mb(25,"label",17),_.lc(26),_.Wb(27,"translate"),_.Mb(28,"input",18,19),_.Tb("change",function(t){return e.onFileChange(t)}),_.Lb(),_.Lb(),_.Ib(30,"label",20,21),_.Lb(),_.Ib(32,"div",22),_.Mb(33,"div",23),_.Mb(34,"button",24),_.Tb("click",function(){return e.onSubmit(!0)}),_.lc(35),_.Wb(36,"translate"),_.Lb(),_.Mb(37,"button",25),_.Tb("click",function(){return e.toggleModalDisplay(!1)}),_.lc(38),_.Wb(39,"translate"),_.Lb(),_.Lb(),_.Lb(),_.Lb(),_.Lb(),_.Lb(),_.Lb(),_.Ib(40,"spinner",26)),2&t){let t=null;_.ac("_onChange",e.onChangeSchema),_.xb(5),_.ac("_jsonSchema",e.projectList),_.xb(1),_.ac("hidden",!e.displayModal),_.xb(2),_.ac("formGroup",e.form),_.xb(4),_.mc(_.Xb(13,13,"createNewProject")),_.xb(4),_.nc("",_.Xb(17,15,"newProjectName")," "),_.xb(2),_.ac("value",e.inputProjectName),_.xb(2),_.ac("ngIf",null==(t=e.form.get("projectName"))?null:t.touched),_.xb(3),_.nc("",_.Xb(24,17,"labelListFile")," "),_.xb(3),_.nc(" ",_.Xb(27,19,"chooseFile"),""),_.xb(9),_.nc(" ",_.Xb(36,21,"createButton")," "),_.xb(3),_.nc(" ",_.Xb(39,23,"cancelButton")," "),_.xb(2),_.ac("_loading",e.isLoading)}},directives:[L.a,I,N,A,j.n,j.g,j.d,j.a,j.f,j.c,i.k,H.a],pipes:[P.c],styles:[".upper-container[_ngcontent-%COMP%]{display:flex;margin-top:5vh}.model[_ngcontent-%COMP%]{z-index:1000;padding-top:10vh;top:0;width:100%;height:100%;overflow:auto;background-color:transparent;scrollbar-width:none;position:fixed;background:rgba(0,0,0,.7)}.model-content[_ngcontent-%COMP%]{background-color:#525353;padding:1vw;border:solid;max-width:30vw;min-width:30vw;border-radius:1vw;margin:15vh auto auto}.content-container[_ngcontent-%COMP%]{margin-left:1.3vw}.content-header[_ngcontent-%COMP%]{color:#fff;font-size:3vh}.new-project-container[_ngcontent-%COMP%]{display:flex;flex-direction:row;margin:3vh 0 0;align-items:baseline}.label[_ngcontent-%COMP%]{margin-right:1vw}.input-style[_ngcontent-%COMP%], .label[_ngcontent-%COMP%]{color:#fff;font-size:2vh}.input-style[_ngcontent-%COMP%]{border-radius:2vw;border:none;outline:none;background-color:#363636;min-width:11vw;max-width:11vw;min-height:4vh;max-height:4vh;padding:0 1vw}.validation[_ngcontent-%COMP%]{color:red}.error-msg[_ngcontent-%COMP%]{font-size:2vh}.select-folder-container[_ngcontent-%COMP%]{display:flex;flex-direction:row;margin:2vh 0 0}.input[_ngcontent-%COMP%]{color:#7fffd4}.horizontal-line[_ngcontent-%COMP%]{background-color:#fff;min-height:.3vh;max-height:.3vh;margin:2vh auto}.model-button-container[_ngcontent-%COMP%]{display:flex;flex-direction:row-reverse;padding:1vw}.button-style[_ngcontent-%COMP%]{padding:1vh 1.5vw;border-radius:1vh;border:none;outline:none;color:#fff;cursor:pointer;font-size:2vh}.create-btn[_ngcontent-%COMP%]{background-color:#169887}.cancel-btn[_ngcontent-%COMP%]{background-color:#444;margin-right:.7vw}.choose-file-btn[_ngcontent-%COMP%]{font-size:2vh;text-decoration:none;background-color:#444;color:#fff;padding:.5vh 1vw;border:none;border-radius:1vh;margin-right:1vw;cursor:pointer}.label-file[_ngcontent-%COMP%]{padding-top:.5vh}.input-id[_ngcontent-%COMP%]{font-size:2vh;display:none}.filename[_ngcontent-%COMP%]{font-size:2vh;padding-top:.5vh;color:#fff}"]}),t})()}];let tt=(()=>{class t{}return t.\u0275mod=_.Fb({type:t}),t.\u0275inj=_.Eb({factory:function(e){return new(e||t)},imports:[[w.d.forChild(Q)]]}),t})();var et=n("KZX/");let nt=(()=>{class t{}return t.\u0275mod=_.Fb({type:t}),t.\u0275inj=_.Eb({factory:function(e){return new(e||t)},imports:[[i.b,et.a,tt]]}),t})()}}]);