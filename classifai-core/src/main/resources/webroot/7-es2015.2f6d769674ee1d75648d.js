(window.webpackJsonp=window.webpackJsonp||[]).push([[7],{RN8A:function(e,t,n){"use strict";n.r(t),n.d(t,"DataSetLayoutModule",function(){return ie});var i=n("ofXK"),a=n("XIp8"),o=n("SxV6"),c=n("lJxs"),r=n("eIep"),s=n("5+tZ"),l=n("/uUt"),b=n("1G5W"),d=n("XNiG"),h=n("l5mm"),p=n("cp0P"),g=n("z6cu"),m=n("3Pt+"),u=n("fXoL"),f=n("tyNb"),v=n("LY9J"),j=n("F7l1"),_=n("14na"),w=n("I7yr"),M=n("x2Se"),x=n("sYmb");function P(e,t){if(1&e){const e=u.Nb();u.Kb(0),u.Mb(1,"div",5),u.Mb(2,"div",6),u.Tb("click",function(){return u.gc(e),u.Vb(2).displayModal()}),u.Ib(3,"img",7),u.Mb(4,"label",8),u.mc(5),u.Wb(6,"translate"),u.Lb(),u.Lb(),u.Lb(),u.Jb()}if(2&e){const e=u.Vb(),t=e.index,n=e.$implicit;u.xb(1),u.yb("data-index",t),u.xb(2),u.ac("src",n.src,u.hc),u.xb(2),u.nc(u.Xb(6,3,n.name))}}function L(e,t){if(1&e){const e=u.Nb();u.Mb(0,"div",9),u.Tb("click",function(){u.gc(e);const t=u.Vb().$implicit;return u.Vb().onClickButton(t.id)}),u.Ib(1,"img",10),u.Mb(2,"label",11),u.mc(3),u.Wb(4,"translate"),u.Lb(),u.Lb()}if(2&e){const e=u.Vb(),t=e.$implicit;u.yb("data-index",e.index),u.xb(1),u.jc(t.style),u.ac("src",t.src,u.hc),u.xb(2),u.nc(u.Xb(4,5,t.name))}}function O(e,t){if(1&e&&(u.Kb(0),u.kc(1,P,7,5,"ng-container",3),u.kc(2,L,5,7,"ng-template",null,4,u.lc),u.Jb()),2&e){const e=t.index,n=u.ec(3);u.xb(1),u.ac("ngIf",0===e)("ngIfElse",n)}}let C=(()=>{class e{constructor(){this.menuSchema=[{src:"../../../assets/icons/add.svg",id:"newProject",name:"menuName.newProject"},{src:"../../../assets/icons/import.svg",id:"importProject",name:"menuName.importProject",style:"width: 1.3vw; padding: 0.3vw;"},{src:"../../../assets/icons/project.svg",id:"myProject",name:"menuName.myProject"},{src:"../../../assets/icons/starred.svg",id:"starred",name:"menuName.starred"},{src:"../../../assets/icons/history.svg",id:"recent",name:"menuName.recent"},{src:"../../../assets/icons/trash.svg",id:"trash",name:"menuName.trash"}],this._onCreate=new u.n,this._onImport=new u.n,this.displayModal=()=>{this._onCreate.emit(!0)},this.onClickButton=e=>{"importProject"===e?this._onImport.emit():console.log("This feature is not available yet")}}ngOnInit(){}}return e.\u0275fac=function(t){return new(t||e)},e.\u0275cmp=u.Bb({type:e,selectors:[["data-set-side-menu"]],outputs:{_onCreate:"_onCreate",_onImport:"_onImport"},decls:3,vars:1,consts:[[1,"dataset-sidemenu-container"],[4,"ngFor","ngForOf"],[1,"horizontal-line"],[4,"ngIf","ngIfElse"],["otherMenu",""],[1,"new-project-container"],[1,"new-project-btn",3,"click"],[1,"add-icon",3,"src"],[1,"new-project-txt"],[1,"current-project-btn",3,"click"],[1,"project-icon",3,"src"],[1,"current-project-txt"]],template:function(e,t){1&e&&(u.Mb(0,"div",0),u.kc(1,O,4,2,"ng-container",1),u.Lb(),u.Ib(2,"div",2)),2&e&&(u.xb(1),u.ac("ngForOf",t.menuSchema))},directives:[i.j,i.k],pipes:[x.c],styles:[".dataset-sidemenu-container[_ngcontent-%COMP%]{display:flex;flex-wrap:wrap;flex-direction:column;width:16vw}.new-project-container[_ngcontent-%COMP%]{margin-bottom:5vh;margin-left:2vw}.new-project-btn[_ngcontent-%COMP%]{padding:1vw;border-radius:5vh;background-color:#525353;border:none;color:#fff;outline:none;cursor:pointer;display:flex;justify-content:space-around;align-items:center;min-width:10vw;max-width:10vw;min-height:4vh;max-height:4vh}.new-project-btn[_ngcontent-%COMP%]:hover{background-color:#393838}.add-icon[_ngcontent-%COMP%]{min-height:inherit;max-height:inherit}.new-project-txt[_ngcontent-%COMP%]{border:none;background:none;outline:none;cursor:pointer;font-size:2vh;color:#fff;text-align:start;white-space:nowrap}.current-project-btn[_ngcontent-%COMP%]{color:#fff;cursor:pointer;display:flex;align-items:center;border-radius:5vh;padding:1vh 1vw;margin-left:2vw;min-width:10vw;max-width:10vw;min-height:5vh;max-height:5vh;flex:1 1 100%}.current-project-btn[_ngcontent-%COMP%]:hover{background-color:#525353}.project-icon[_ngcontent-%COMP%]{min-height:4vh;max-height:4vh;flex:1 1 10%}.current-project-txt[_ngcontent-%COMP%]{border:none;background:none;outline:none;cursor:pointer;font-size:2vh;color:#fff;white-space:nowrap;flex:1 1 90%;text-align:left;padding-left:20px}.horizontal-line[_ngcontent-%COMP%]{width:12vw;background-color:#393838;min-height:.3vh;max-height:.3vh;margin:auto;border:.0625rem solid #000}"]}),e})(),y=(()=>{class e{constructor(){}ngOnInit(){}}return e.\u0275fac=function(t){return new(t||e)},e.\u0275cmp=u.Bb({type:e,selectors:[["data-set-header"]],decls:6,vars:3,consts:[[1,"dataset-header-container"],[1,"label"],[1,"dataset-icon-container"]],template:function(e,t){1&e&&(u.Mb(0,"div",0),u.Mb(1,"label",1),u.mc(2),u.Wb(3,"translate"),u.Lb(),u.Mb(4,"div",2),u.Ib(5,"div"),u.Lb(),u.Lb()),2&e&&(u.xb(2),u.nc(u.Xb(3,1,"datasetHeader.datasetManagement")))},pipes:[x.c],styles:[".dataset-header-container[_ngcontent-%COMP%]{display:flex;justify-content:space-around;align-items:center;padding:1vw;min-width:80vw;max-width:80vw}.label[_ngcontent-%COMP%]{flex:1 1 80%;background:none;font-size:2.5vh;color:#fff;white-space:nowrap;min-height:inherit;max-height:inherit}.dataset-icon-container[_ngcontent-%COMP%]{display:flex;justify-content:space-between;align-items:center;flex:1 1 20%}.dataset-icon[_ngcontent-%COMP%]{flex:1 1 3%;min-width:2vw;max-width:2vw;cursor:pointer}.dataset-icon[_ngcontent-%COMP%]:hover{border-radius:5vh;background-color:#393838}.dataset-select[_ngcontent-%COMP%]{min-height:4vh;max-height:4vh;font-size:2vh;min-width:7vw;max-width:7vw;-moz-text-align-last:center;background:#000;color:#fff;border:.1vw solid;text-align-last:center}.dataset-select[_ngcontent-%COMP%]:focus, .dataset-select[_ngcontent-%COMP%]:hover{background:#393838}.dataset-select[_ngcontent-%COMP%]:focus, .dataset-select[_ngcontent-%COMP%]:hover, select[_ngcontent-%COMP%]{-moz-appearance:none;-webkit-appearance:none}option[_ngcontent-%COMP%]{background:#000;text-align:center}"]}),e})();function k(e,t){1&e&&(u.Kb(0),u.Mb(1,"div",2),u.Mb(2,"div",3),u.Mb(3,"div",4),u.Mb(4,"label",5),u.mc(5),u.Wb(6,"translate"),u.Lb(),u.Lb(),u.Mb(7,"div"),u.Mb(8,"label",6),u.mc(9),u.Wb(10,"translate"),u.Lb(),u.Lb(),u.Lb(),u.Lb(),u.Jb()),2&e&&(u.xb(5),u.nc(u.Xb(6,2,"datasetCard.fetchingProject")),u.xb(4),u.nc(u.Xb(10,4,"datasetCard.pleaseWait")))}function S(e,t){1&e&&(u.Kb(0),u.Mb(1,"label",22),u.mc(2),u.Wb(3,"translate"),u.Lb(),u.Jb()),2&e&&(u.xb(2),u.oc(" ",u.Xb(3,1,"datasetCard.uploading")," "))}function I(e,t){1&e&&(u.Kb(0),u.Mb(1,"label",24),u.mc(2),u.Wb(3,"translate"),u.Lb(),u.Jb()),2&e&&(u.xb(2),u.nc(u.Xb(3,1,"datasetCard.new")))}function N(e,t){1&e&&(u.Kb(0),u.Mb(1,"label",25),u.mc(2),u.Wb(3,"translate"),u.Lb(),u.Jb()),2&e&&(u.xb(2),u.nc(u.Xb(3,1,"datasetCard.available")))}function F(e,t){1&e&&(u.Kb(0),u.Mb(1,"label",26),u.mc(2),u.Wb(3,"translate"),u.Lb(),u.Jb()),2&e&&(u.xb(2),u.nc(u.Xb(3,1,"datasetCard.notAvailable")))}function T(e,t){if(1&e&&(u.Kb(0,23),u.kc(1,I,4,3,"ng-container",16),u.kc(2,N,4,3,"ng-container",16),u.kc(3,F,4,3,"ng-container",16),u.Jb()),2&e){const e=u.Vb().$implicit;u.ac("ngSwitch",e),u.xb(1),u.ac("ngIf",e.is_new),u.xb(1),u.ac("ngIf",!e.is_new&&!e.is_loaded),u.xb(1),u.ac("ngIf",!e.is_new&&e.is_loaded)}}function E(e,t){if(1&e){const e=u.Nb();u.Kb(0),u.Mb(1,"span"),u.Mb(2,"div",27),u.Mb(3,"div",28),u.Tb("click",function(){u.gc(e);const t=u.Vb().$implicit;return u.Vb(3).onRenameProject(t.project_name)}),u.mc(4),u.Wb(5,"translate"),u.Lb(),u.Mb(6,"div",28),u.Tb("click",function(){u.gc(e);const t=u.Vb().$implicit;return u.Vb(3).onDeleteProject(t.project_name)}),u.mc(7),u.Wb(8,"translate"),u.Lb(),u.Lb(),u.Lb(),u.Jb()}2&e&&(u.xb(4),u.oc(" ",u.Xb(5,2,"datasetCard.renameProject")," "),u.xb(3),u.oc(" ",u.Xb(8,4,"datasetCard.deleteProject")," "))}const D=function(e){return[e]};function X(e,t){if(1&e){const e=u.Nb();u.Kb(0),u.Mb(1,"div",9),u.Tb("dblclick",function(){u.gc(e);const n=t.index,i=t.$implicit;return u.Vb(3).onOpenProject(n,i)}),u.Mb(2,"div",10),u.Mb(3,"div"),u.kc(4,S,4,3,"ng-container",0),u.kc(5,T,4,4,"ng-template",null,11,u.lc),u.Lb(),u.Mb(7,"div",12),u.Mb(8,"div",13),u.Tb("click",function(){u.gc(e);const n=t.$implicit,i=u.Vb(3);return i.conditionalDisableClickEvent(n.is_loaded)?null:i.onStarred(n,!n.is_starred)}),u.Ib(9,"img",14),u.Lb(),u.Mb(10,"div",13),u.Tb("click",function(){u.gc(e);const n=t.$implicit,i=t.index,a=u.Vb(3);return a.conditionalDisableClickEvent(n.is_loaded)?null:a.onDisplayMore(i)}),u.Ib(11,"img",15),u.Lb(),u.kc(12,E,9,6,"ng-container",16),u.Lb(),u.Lb(),u.Mb(13,"div",17),u.Mb(14,"label",18),u.Mb(15,"div",19),u.mc(16),u.Wb(17,"translate"),u.Lb(),u.Lb(),u.Lb(),u.Mb(18,"div",20),u.Mb(19,"label",18),u.Mb(20,"div",21),u.mc(21),u.Lb(),u.Lb(),u.Lb(),u.Mb(22,"div",20),u.Mb(23,"label",18),u.Mb(24,"div",21),u.mc(25),u.Wb(26,"translate"),u.Lb(),u.Lb(),u.Lb(),u.Lb(),u.Jb()}if(2&e){const e=t.$implicit,n=t.index,i=u.ec(6),a=u.Vb(3);u.xb(1),u.ac("ngClass",a.conditionalDisableProject(e)),u.yb("data-index",n),u.xb(3),u.ac("ngIf",a.isExactIndex(n)&&a._jsonSchema.isUploading)("ngIfElse",i),u.xb(3),u.ac("ngClass",a.conditionalDisableProject(e)),u.xb(2),u.ac("src",u.cc(19,D,e.is_starred?a.starredActiveIcon:a.starredInactiveIcon),u.hc),u.xb(3),u.ac("ngIf",a.isExactIndex(n)),u.xb(2),u.ac("title",e.created_date),u.xb(2),u.pc(" ",u.Xb(17,15,"datasetCard.created")," ",e.created_date," "),u.xb(3),u.ac("title",e.project_name),u.xb(2),u.oc(" ",e.project_name," "),u.xb(2),u.ac("title","Total Photo: "+e.total_uuid),u.xb(2),u.pc(" ",u.Xb(26,17,"datasetCard.totalPhoto")," ",e.total_uuid," ")}}function z(e,t){if(1&e&&(u.Kb(0),u.Mb(1,"div",2),u.kc(2,X,27,21,"ng-container",8),u.Lb(),u.Jb()),2&e){const e=u.Vb(2);u.xb(2),u.ac("ngForOf",e._jsonSchema.projects)}}function W(e,t){1&e&&(u.Mb(0,"div",2),u.Mb(1,"div",3),u.Mb(2,"div",4),u.Mb(3,"label",5),u.mc(4),u.Wb(5,"translate"),u.Lb(),u.Lb(),u.Mb(6,"div"),u.Mb(7,"label",6),u.mc(8),u.Wb(9,"translate"),u.Lb(),u.Lb(),u.Lb(),u.Lb()),2&e&&(u.xb(4),u.nc(u.Xb(5,2,"datasetCard.noProject")),u.xb(4),u.nc(u.Xb(9,4,"datasetCard.createNew")))}function U(e,t){if(1&e&&(u.kc(0,z,3,1,"ng-container",0),u.kc(1,W,10,6,"ng-template",null,7,u.lc)),2&e){const e=u.ec(2),t=u.Vb();u.ac("ngIf",t._jsonSchema.projects.length>0)("ngIfElse",e)}}let R=(()=>{class e{constructor(){this._onClick=new u.n,this._onStarred=new u.n,this._onDelete=new u.n,this._onRename=new u.n,this.starredActiveIcon="../../../assets/icons/starred_active.svg",this.starredInactiveIcon="../../../assets/icons/starred.svg",this.cardSchema={clickIndex:-1},this.previousProjectLength=0,this.conditionalDisableProject=({is_loaded:e})=>e?"disabled":"enabled",this.conditionalDisableClickEvent=e=>e,this.onOpenProject=(e,{project_name:t,is_loaded:n})=>{!n&&!this.isExactIndex(e)&&this._onClick.emit(t)},this.onDisplayMore=(e=this.cardSchema.clickIndex)=>{const{clickIndex:t}=this.cardSchema;this.cardSchema={clickIndex:t===e?-1:e}},this.onCloseDisplay=()=>{this.cardSchema.clickIndex=-1},this.onStarred=(e,t)=>{const{project_name:n}=e;this._jsonSchema.projects=this._jsonSchema.projects.map(e=>e.project_name===n?(e.is_starred=t,e):e),this._onStarred.emit({projectName:n,starred:t})},this.isExactIndex=e=>e===this.cardSchema.clickIndex}ngOnInit(){}onRenameProject(e){this._onRename.emit({shown:!0,projectName:e}),this.onCloseDisplay()}onDeleteProject(e){this._onDelete.emit(e),this.onCloseDisplay()}ngOnChanges(e){const{isUploading:t}=e._jsonSchema.currentValue;!t&&this.onDisplayMore()}ngDoCheck(){this._jsonSchema.projects.length!==this.previousProjectLength&&(this.cardSchema.clickIndex=-1),this.previousProjectLength=this._jsonSchema.projects.length}}return e.\u0275fac=function(t){return new(t||e)},e.\u0275cmp=u.Bb({type:e,selectors:[["data-set-card"]],inputs:{_jsonSchema:"_jsonSchema"},outputs:{_onClick:"_onClick",_onStarred:"_onStarred",_onDelete:"_onDelete",_onRename:"_onRename"},features:[u.vb],decls:3,vars:2,consts:[[4,"ngIf","ngIfElse"],["showCardBody",""],[1,"card-layout-container","scroll","fade-in"],[1,"no-project-card-container"],[1,"no-project-title-padding"],[1,"no-project-title"],[1,"no-project-subtitle"],["noProject",""],[4,"ngFor","ngForOf"],[1,"card-container",3,"ngClass","dblclick"],[1,"card-header-style"],["newLabel",""],[1,"card-icon-container",3,"ngClass"],[3,"click"],[1,"card-icon-style",3,"src"],["src","../../../assets/icons/more.svg",1,"card-icon-style"],[4,"ngIf"],[1,"card-title-style"],[3,"title"],[1,"card-title-txt"],[1,"project-name-style"],[1,"project-info"],[1,"project-status-uploading"],[3,"ngSwitch"],[1,"project-status-new"],[1,"project-status-available"],[1,"project-status-not-available"],[1,"popup-container"],[1,"popup-label",3,"click"]],template:function(e,t){if(1&e&&(u.kc(0,k,11,6,"ng-container",0),u.kc(1,U,3,2,"ng-template",null,1,u.lc)),2&e){const e=u.ec(2);u.ac("ngIf",t._jsonSchema.isFetching)("ngIfElse",e)}},directives:[i.k,i.j,i.i,i.m],pipes:[x.c],styles:['@keyframes fade-in{0%{opacity:0}to{opacity:1}}@-webkit-keyframes fade-in{0%{opacity:0}to{opacity:1}}.fade-in[_ngcontent-%COMP%]{animation:fadeIn 1.5s ease;-webkit-animation:fadeIn 1.5s ease;-moz-animation:fadeIn ease 1.5s;-o-animation:fadeIn ease 1.5s;-ms-animation:fadeIn ease 1.5s}.card-layout-container[_ngcontent-%COMP%]{width:80vw;display:flex;flex-wrap:wrap;padding:0 0 0 1vw;overflow-y:scroll;position:relative;height:80vh}.scroll[_ngcontent-%COMP%]::-webkit-scrollbar-track{box-shadow:inset 0 0 6px rgba(0,0,0,.3);border-radius:10px;background-color:#000}.scroll[_ngcontent-%COMP%]::-webkit-scrollbar{width:.5vw}.scroll[_ngcontent-%COMP%]::-webkit-scrollbar-thumb{border-radius:10px;box-shadow:inset 0 0 6px rgba(0,0,0,.3);background-color:#525353}.card-container[_ngcontent-%COMP%]{min-width:11vw;max-width:11vw;min-height:30vh;max-height:30vh;border-style:solid;font-size:2.2vh;background:#2e2d2d;position:relative}.card-container[_ngcontent-%COMP%]:hover{background:#404040}.card-container[_ngcontent-%COMP%]:before{content:"";display:block;height:100%;position:absolute;top:0;left:0;width:.3vw;background-color:#363636}.enabled[_ngcontent-%COMP%]{cursor:pointer}.disabled[_ngcontent-%COMP%]{cursor:not-allowed}.project-status-new[_ngcontent-%COMP%]{background-color:#f59221}.project-status-available[_ngcontent-%COMP%], .project-status-new[_ngcontent-%COMP%]{color:#f5f5f5;font-size:1.5vh;padding:.3vh 1.5vw .6vh 1vw}.project-status-available[_ngcontent-%COMP%]{background-color:#4bf521}.project-status-not-available[_ngcontent-%COMP%]{background-color:#f52121}.project-status-not-available[_ngcontent-%COMP%], .project-status-uploading[_ngcontent-%COMP%]{color:#f5f5f5;font-size:1.5vh;padding:.3vh 1.5vw .6vh 1vw}.project-status-uploading[_ngcontent-%COMP%]{background-color:#f5219d}.card-icon-container[_ngcontent-%COMP%]{display:flex}.card-icon-style[_ngcontent-%COMP%]{min-width:1.5vw;max-width:1.5vw;min-height:3vh;max-height:3vh}.card-icon-style[_ngcontent-%COMP%]:hover{border-radius:5vh;background-color:#393838}.card-header-style[_ngcontent-%COMP%]{margin-top:1vh;display:flex;flex-direction:row;justify-content:space-between;position:relative}.card-title-style[_ngcontent-%COMP%]{margin-left:1vw}.card-title-txt[_ngcontent-%COMP%]{color:#656667;font-size:1.3vh;white-space:nowrap;overflow:hidden;text-overflow:ellipsis}.project-name-style[_ngcontent-%COMP%]{margin-left:1vw;padding:1vh 0 0}.project-info[_ngcontent-%COMP%]{color:#dbdbda;font-size:2vh;white-space:nowrap;overflow:hidden;text-overflow:ellipsis}.tag-container[_ngcontent-%COMP%]{display:flex;flex-direction:row;border-radius:10vw;background:#363636;width:4.5vw;padding:.2vw .3vh;cursor:pointer;outline:none;border:none;margin:.8vh 0 0 1vw}.tag-img[_ngcontent-%COMP%]{min-height:2vh;max-height:2vh;min-width:1vw;max-width:1vw;margin-left:.35vw}.tag-txt[_ngcontent-%COMP%]{font-size:1.2vh;color:#fff;padding:.2vh 0 0 .3vw;cursor:pointer}.popup-container[_ngcontent-%COMP%]{overflow:hidden;position:absolute;margin:4vh 0 0 -10vw;padding:.5vw;border-radius:.2vw;box-shadow:0 2px 5px 0 rgba(var(--shadow-rgb),.26),0 2px 10px 0 rgba(var(--shadow-rgb),.16);transform-origin:left top;transform:scale(1);opacity:1;white-space:nowrap;background:#fff;font-size:1.5vh;-webkit-animation:appear .35s ease-in 1;animation:appear .35s ease-in 1}@-webkit-keyframes appear{0%{opacity:0;transform:translateY(-10px)}}@keyframes appear{0%{opacity:0;transform:translateY(-10px)}}.popup-label[_ngcontent-%COMP%]{font-size:2vh;padding:.5vw}.popup-label[_ngcontent-%COMP%]:hover{background:#e9e9e9}.no-project-title[_ngcontent-%COMP%]{font-size:4vh;color:#000;white-space:nowrap}.no-project-title-padding[_ngcontent-%COMP%]{padding:2vw}.no-project-subtitle[_ngcontent-%COMP%]{font-size:2vh;color:#656667;white-space:nowrap}.no-project-card-container[_ngcontent-%COMP%]{display:flex;flex-direction:column;justify-content:center;align-items:center;background-color:#c8c6c6;width:100%}']}),e})();var $=n("TJKd");const J=["refProjectName"],V=["labeltextfile"],K=["labeltextfilename"],q=["refNewProjectName"];function B(e,t){if(1&e&&(u.Mb(0,"div",30),u.Mb(1,"p",31),u.mc(2),u.Lb(),u.Lb()),2&e){const e=u.Vb();u.xb(2),u.oc(" ",e.isImageUploading?"Uploading the Images. Please Wait...":"Selection Window is Opened"," ")}}function A(e,t){1&e&&(u.Mb(0,"span"),u.Mb(1,"small",33),u.mc(2),u.Wb(3,"translate"),u.Lb(),u.Lb()),2&e&&(u.xb(2),u.oc(" ",u.Xb(3,1,"projectNameExist")," "))}function H(e,t){1&e&&(u.Mb(0,"span"),u.Mb(1,"small",33),u.mc(2),u.Wb(3,"translate"),u.Lb(),u.Lb()),2&e&&(u.xb(2),u.oc(" ",u.Xb(3,1,"projectNameRequired")," "))}function G(e,t){if(1&e&&(u.Kb(0),u.Mb(1,"div",32),u.kc(2,A,4,3,"span",15),u.kc(3,H,4,3,"span",15),u.Lb(),u.Jb()),2&e){const e=u.Vb();let t=null,n=null;u.xb(2),u.ac("ngIf",null==(t=e.form.get("projectName"))?null:t.getError("exist")),u.xb(1),u.ac("ngIf",null==(n=e.form.get("projectName"))?null:n.getError("required"))}}function Y(e,t){1&e&&(u.Mb(0,"span"),u.Mb(1,"small",33),u.mc(2),u.Wb(3,"translate"),u.Lb(),u.Lb()),2&e&&(u.xb(2),u.oc(" ",u.Xb(3,1,"projectNameExist")," "))}function Z(e,t){1&e&&(u.Mb(0,"span"),u.Mb(1,"small",33),u.mc(2),u.Wb(3,"translate"),u.Lb(),u.Lb()),2&e&&(u.xb(2),u.oc(" ",u.Xb(3,1,"projectNameRequired")," "))}function Q(e,t){if(1&e&&(u.Kb(0),u.Mb(1,"div",32),u.kc(2,Y,4,3,"span",15),u.kc(3,Z,4,3,"span",15),u.Lb(),u.Jb()),2&e){const e=u.Vb();let t=null,n=null;u.xb(2),u.ac("ngIf",null==(t=e.renameForm.get("newProjectName"))?null:t.getError("exist")),u.xb(1),u.ac("ngIf",null==(n=e.renameForm.get("newProjectName"))?null:n.getError("required"))}}const ee=[{path:"",component:(()=>{class e{constructor(e,t,n,i,u,f,v){this._fb=e,this._cd=t,this._router=n,this._dataSetService=i,this._spinnerService=u,this._imgLblModeService=f,this._languageService=v,this.onChangeSchema={currentThumbnailIndex:-1,thumbnailName:"",totalNumThumbnail:0,status:void 0},this.projectList={projects:[],isUploading:!1,isFetching:!1},this.inputProjectName="",this.newInputProjectName="",this.selectedProjectName="",this.oldProjectName="",this.labelTextUpload=[],this.displayModal=!1,this.displayRenameProjectModal=!1,this.subject$=new d.a,this.thumbnailList=[],this.labelList=[],this.unsubscribe$=new d.a,this.isLoading=!1,this.isOverlayOn=!1,this.isImageUploading=!1,this.imgLblMode=null,this.getProjectList=()=>{this.projectList.isFetching=!0,this._dataSetService.getProjectList().pipe(Object(o.a)()).subscribe(({content:e})=>{if(e){const t=Object(a.a)(e).map(e=>Object.assign(Object.assign({},e),{created_date:this.formatDate(e.created_date)}));this.projectList.projects=[...t],this.projectList.isFetching=!1}})},this.formatDate=e=>{const t=new Date(e),n=["Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"].find((e,n)=>n===t.getMonth()||void 0);return n?`${n}-${t.getDate()}-${t.getFullYear()}`:"Error"},this.createFormControls=()=>{this.form=this._fb.group({projectName:["",m.l.required]})},this.renameFormControls=()=>{this.renameForm=this._fb.group({newProjectName:["",m.l.required]})},this.onChange=e=>{this.inputProjectName=e},this.onChangeRename=e=>{this.newInputProjectName=e},this.toggleModalDisplay=e=>{this._labelTextFilename.nativeElement.innerHTML="",this.labelTextUpload=[],e&&this.form.reset(),this.displayModal=e,setTimeout(()=>this._refProjectName.nativeElement.focus())},this.toggleRenameModalDisplay=e=>{e.shown&&this.renameForm.reset(),this.displayRenameProjectModal=e.shown,this.oldProjectName=e.projectName,setTimeout(()=>this._refNewProjectName.nativeElement.focus())},this.importProject=()=>{const e=this._dataSetService.importStatus();this._dataSetService.importProject().pipe(Object(o.a)(),Object(c.a)(({message:e})=>e)).subscribe(t=>{let n=!1;Object(h.a)(500).pipe(Object(r.a)(()=>e),Object(o.a)(e=>(this.isOverlayOn=0===e.message,1!==e.message&&4!==e.message||(n=!0),n))).subscribe(e=>{this.getProjectList()})})},this.onFileChange=({target:{files:e}})=>{var t,n;const i=null===(n=null===(t=this._labelTextFile.nativeElement.files)||void 0===t?void 0:t.item(0))||void 0===n?void 0:n.name;this._labelTextFilename.nativeElement.innerHTML=void 0===i?"":i;const a=new FileReader;if(e&&e.length){const t=e.item(0);a.onload=()=>{this._cd.markForCheck()},a.onloadend=()=>{const e=a.result.split("\n");if(e.length>0){const t=e.reduce((e,t)=>{const n=t.replace(/[^A-Z0-9]+/gi,"").toLowerCase();return e.push(n),e},[]);this.labelTextUpload=[],this.labelTextUpload.push(...t)}},t&&a.readAsText(t)}},this.onStarred=({projectName:e,starred:t})=>{this._dataSetService.updateProjectStatus(e,t,"star").pipe(Object(o.a)()).subscribe(({message:e})=>console.log(e),t=>this.projectList={isUploading:this.projectList.isUploading,isFetching:this.projectList.isFetching,projects:this.projectList.projects.map(t=>t.project_name===e?Object.assign(Object.assign({},t),{is_starred:!1}):t)})},this.onSubmit=(e,t)=>{var n,i,a;this.form.markAllAsTouched(),e?this.inputProjectName?this.projectList.projects&&this.projectList.projects.find(e=>e?e.project_name===this.inputProjectName:null)?(null===(n=this.form.get("projectName"))||void 0===n||n.setErrors({exist:!0}),this._refProjectName.nativeElement.focus()):(this.createProject(this.inputProjectName),this.selectedProjectName=null===(i=this.form.get("projectName"))||void 0===i?void 0:i.value,this.labelTextUpload=[],this._labelTextFilename.nativeElement.innerHTML=""):(null===(a=this.form.get("projectName"))||void 0===a||a.setErrors({required:!0}),this._refProjectName.nativeElement.focus()):t&&this.startProject(t)},this.startProject=e=>{this.selectedProjectName=e;const t=this._dataSetService.checkProjectStatus(e),n=this._dataSetService.updateProjectLoadStatus(e),i=this._dataSetService.checkExistProjectStatus(e),a=this._dataSetService.getThumbnailList;this.subjectSubscription=this.subject$.pipe(Object(s.a)(()=>Object(p.a)([t])),Object(o.a)(([{message:e,content:t}])=>{this.projectList={isUploading:this.projectList.isUploading,isFetching:this.projectList.isFetching,projects:this.projectList.projects.map(e=>e.project_name===t[0].project_name?Object.assign(Object.assign({},t[0]),{created_date:e.created_date}):e)};const{is_loaded:n}=t[0];return 1===e&&!n}),Object(s.a)(([{message:e}])=>e?Object(p.a)([n,i]):[]),Object(s.a)(([{},{message:t,uuid_list:n,label_list:c}])=>2===t?(this.labelList=[...c],n.length>0?n.map(t=>a(e,t)):[]):Object(h.a)(500).pipe(Object(s.a)(()=>i),Object(o.a)(({message:e})=>2===e),Object(s.a)(({uuid_list:t,label_list:n})=>(this.labelList=[...n],t.length>0?t.map(t=>a(e,t)):[])))),Object(s.a)(e=>e)).subscribe(e=>{this.thumbnailList=[...this.thumbnailList,e]},e=>{},()=>{this._router.navigate(["imglabel/"+this.imgLblMode],{state:{thumbnailList:this.thumbnailList,projectName:e,labelList:this.labelList}}),this._spinnerService.hideSpinner()}),this.subject$.next()},this.createProject=e=>{const t=this._dataSetService.createNewProject(e),n=this._dataSetService.updateLabelList(e,this.labelTextUpload),i=this._dataSetService.localUploadStatus(e),a=this._dataSetService.getThumbnailList;let c=0;const r=({message:t})=>5!==t&&1===t?Object(h.a)(500).pipe(Object(s.a)(()=>i),Object(o.a)(({message:e})=>(this.isOverlayOn=0===e||2===e,this.isImageUploading=2===e,4===e||1===e)),Object(s.a)(({uuid_list:t,message:n})=>{const i=4===n&&t.length>0?t.map(t=>a(e,t)):[];return this.projectList=Object.assign(Object.assign({},this.projectList),i.length>0?{isUploading:!0}:{isUploading:!1}),c=i.length,4===n&&this.toggleModalDisplay(!1),i}),Object(s.a)(e=>e)):Object(g.a)(e=>(console.error(e),this.projectList=Object.assign(Object.assign({},this.projectList),{isUploading:!1}),e));this.projectList=Object.assign(Object.assign({},this.projectList),{isUploading:!0}),this.subjectSubscription=this.subject$.pipe(Object(o.a)(),Object(s.a)(()=>t),Object(s.a)(()=>n),Object(s.a)(e=>r(e))).subscribe(e=>{c=e?--c:c,c<1&&(this.projectList=Object.assign(Object.assign({},this.projectList),{isUploading:!1}))},e=>{},()=>{this.getProjectList()}),this.subject$.next()},this.renameProject=(e,t)=>{this.displayRenameProjectModal=!1,this._dataSetService.renameProject(e,t).pipe(Object(o.a)(),Object(c.a)(({message:e})=>e)).subscribe(t=>{console.log(t),1==t&&(this._languageService._translate.get("renameSuccess").subscribe(t=>{alert(e+" "+t)}),this.getProjectList())})},this.deleteProject=e=>{this._dataSetService.deleteProject(e).pipe(Object(o.a)(),Object(c.a)(({message:e})=>e)).subscribe(t=>{this._languageService._translate.get("deleteSuccess").subscribe(t=>{alert(e+" "+t)}),this.getProjectList()})},this.keyDownEvent=({key:e})=>{"Escape"===e&&this.displayModal&&this.toggleModalDisplay(!1)},this._imgLblModeService.imgLabelMode$.pipe(Object(l.a)()).subscribe(e=>this.imgLblMode=e),this._spinnerService.returnAsObservable().pipe(Object(b.a)(this.unsubscribe$)).subscribe(e=>this.isLoading=e),this.createFormControls(),this.renameFormControls(),this._languageService.initializeLanguage("data-set-page",["data-set-page-en","data-set-page-cn","data-set-page-ms"])}ngOnInit(){this.getProjectList()}onSubmitRename(){var e,t,n;this.newInputProjectName?this.projectList.projects&&this.projectList.projects.find(e=>e?e.project_name===this.newInputProjectName:null)?(null===(e=this.renameForm.get("newProjectName"))||void 0===e||e.setErrors({exist:!0}),this._refProjectName.nativeElement.focus()):(this.renameProject(this.oldProjectName,this.newInputProjectName),this.selectedProjectName=null===(t=this.renameForm.get("newProjectName"))||void 0===t?void 0:t.value):(null===(n=this.renameForm.get("newProjectName"))||void 0===n||n.setErrors({required:!0}),this._refProjectName.nativeElement.focus())}ngOnDestroy(){this.unsubscribe$.next(),this.unsubscribe$.complete()}}return e.\u0275fac=function(t){return new(t||e)(u.Hb(m.b),u.Hb(u.h),u.Hb(f.a),u.Hb(v.a),u.Hb(j.a),u.Hb(_.a),u.Hb(w.a))},e.\u0275cmp=u.Bb({type:e,selectors:[["data-set-layout"]],viewQuery:function(e,t){if(1&e&&(u.qc(J,!0),u.qc(V,!0),u.qc(K,!0),u.qc(q,!0)),2&e){let e;u.dc(e=u.Ub())&&(t._refProjectName=e.first),u.dc(e=u.Ub())&&(t._labelTextFile=e.first),u.dc(e=u.Ub())&&(t._labelTextFilename=e.first),u.dc(e=u.Ub())&&(t._refNewProjectName=e.first)}},hostBindings:function(e,t){1&e&&u.Tb("keydown",function(e){return t.keyDownEvent(e)},!1,u.fc)},decls:65,vars:42,consts:[["class","overlay",4,"ngIf"],[3,"_onChange"],[1,"upper-container"],[3,"_onCreate","_onImport"],[3,"_jsonSchema","_onClick","_onStarred","_onDelete","_onRename"],[3,"hidden"],[1,"model"],[3,"formGroup"],[1,"model-content"],[1,"content-container"],[1,"content-header"],[1,"new-project-container"],[1,"label"],["type","text","placeholder","Enter project name","formControlName","projectName",1,"input-style",3,"value","input"],["refProjectName",""],[4,"ngIf"],[1,"select-folder-container"],[1,"label","label-file"],[1,"choose-file-btn"],["type","file","accept",".txt",1,"input-id",3,"change"],["labeltextfile",""],[1,"filename"],["labeltextfilename",""],[1,"horizontal-line"],[1,"model-button-container"],["type","submit",1,"button-style","create-btn",3,"click"],[1,"button-style","cancel-btn",3,"click"],["type","text","placeholder","Enter new project name","formControlName","newProjectName",1,"input-style",3,"value","input"],["refNewProjectName",""],[3,"_loading"],[1,"overlay"],[2,"margin-top","40vh","color","rgb(255, 255, 255, 0.9)","text-align","center","font-size","3vh"],[1,"validation"],[1,"error-msg"]],template:function(e,t){if(1&e&&(u.kc(0,B,3,1,"div",0),u.Ib(1,"page-header",1),u.Mb(2,"div",2),u.Mb(3,"data-set-side-menu",3),u.Tb("_onCreate",function(e){return t.toggleModalDisplay(e)})("_onImport",function(){return t.importProject()}),u.Lb(),u.Mb(4,"div"),u.Ib(5,"data-set-header"),u.Mb(6,"data-set-card",4),u.Tb("_onClick",function(e){return t.onSubmit(!1,e)})("_onStarred",function(e){return t.onStarred(e)})("_onDelete",function(e){return t.deleteProject(e)})("_onRename",function(e){return t.toggleRenameModalDisplay(e)}),u.Lb(),u.Lb(),u.Lb(),u.Mb(7,"div",5),u.Mb(8,"div",6),u.Mb(9,"form",7),u.Mb(10,"div",8),u.Mb(11,"div",9),u.Mb(12,"h1",10),u.mc(13),u.Wb(14,"translate"),u.Lb(),u.Mb(15,"div",11),u.Mb(16,"label",12),u.mc(17),u.Wb(18,"translate"),u.Lb(),u.Mb(19,"input",13,14),u.Tb("input",function(e){return t.onChange(e.target.value)}),u.Lb(),u.Lb(),u.kc(21,G,4,2,"ng-container",15),u.Mb(22,"div",16),u.Mb(23,"label",17),u.mc(24),u.Wb(25,"translate"),u.Lb(),u.Mb(26,"label",18),u.mc(27),u.Wb(28,"translate"),u.Mb(29,"input",19,20),u.Tb("change",function(e){return t.onFileChange(e)}),u.Lb(),u.Lb(),u.Ib(31,"label",21,22),u.Lb(),u.Ib(33,"div",23),u.Mb(34,"div",24),u.Mb(35,"button",25),u.Tb("click",function(){return t.onSubmit(!0)}),u.mc(36),u.Wb(37,"translate"),u.Lb(),u.Mb(38,"button",26),u.Tb("click",function(){return t.toggleModalDisplay(!1)}),u.mc(39),u.Wb(40,"translate"),u.Lb(),u.Lb(),u.Lb(),u.Lb(),u.Lb(),u.Lb(),u.Lb(),u.Mb(41,"div",5),u.Mb(42,"div",6),u.Mb(43,"form",7),u.Mb(44,"div",8),u.Mb(45,"div",9),u.Mb(46,"h1",10),u.mc(47),u.Wb(48,"translate"),u.Lb(),u.Mb(49,"div",11),u.Mb(50,"label",12),u.mc(51),u.Wb(52,"translate"),u.Lb(),u.Mb(53,"input",27,28),u.Tb("input",function(e){return t.onChangeRename(e.target.value)}),u.Lb(),u.Lb(),u.kc(55,Q,4,2,"ng-container",15),u.Ib(56,"div",23),u.Mb(57,"div",24),u.Mb(58,"button",25),u.Tb("click",function(){return t.onSubmitRename()}),u.mc(59),u.Wb(60,"translate"),u.Lb(),u.Mb(61,"button",26),u.Tb("click",function(){return t.toggleRenameModalDisplay(!1)}),u.mc(62),u.Wb(63,"translate"),u.Lb(),u.Lb(),u.Lb(),u.Lb(),u.Lb(),u.Lb(),u.Lb(),u.Ib(64,"spinner",29)),2&e){let e=null,n=null;u.ac("ngIf",t.isOverlayOn),u.xb(1),u.ac("_onChange",t.onChangeSchema),u.xb(5),u.ac("_jsonSchema",t.projectList),u.xb(1),u.ac("hidden",!t.displayModal),u.xb(2),u.ac("formGroup",t.form),u.xb(4),u.nc(u.Xb(14,22,"createNewProject")),u.xb(4),u.oc("",u.Xb(18,24,"newProjectName")," "),u.xb(2),u.ac("value",t.inputProjectName),u.xb(2),u.ac("ngIf",null==(e=t.form.get("projectName"))?null:e.touched),u.xb(3),u.oc("",u.Xb(25,26,"labelListFile")," "),u.xb(3),u.oc(" ",u.Xb(28,28,"chooseFile"),""),u.xb(9),u.oc(" ",u.Xb(37,30,"createButton")," "),u.xb(3),u.oc(" ",u.Xb(40,32,"cancelButton")," "),u.xb(2),u.ac("hidden",!t.displayRenameProjectModal),u.xb(2),u.ac("formGroup",t.renameForm),u.xb(4),u.nc(u.Xb(48,34,"renameProject")),u.xb(4),u.oc("",u.Xb(52,36,"newProjectName")," "),u.xb(2),u.ac("value",t.inputProjectName),u.xb(2),u.ac("ngIf",null==(n=t.renameForm.get("newProjectName"))?null:n.touched),u.xb(4),u.oc(" ",u.Xb(60,38,"updateButton")," "),u.xb(3),u.oc(" ",u.Xb(63,40,"cancelButton")," "),u.xb(2),u.ac("_loading",t.isLoading)}},directives:[i.k,M.a,C,y,R,m.n,m.g,m.d,m.a,m.f,m.c,$.a],pipes:[x.c],styles:[".upper-container[_ngcontent-%COMP%]{display:flex;margin-top:5vh}.model[_ngcontent-%COMP%]{z-index:1000;padding-top:10vh;top:0;width:100%;height:100%;overflow:auto;background-color:transparent;scrollbar-width:none;position:fixed;background:rgba(0,0,0,.7)}.model-content[_ngcontent-%COMP%]{background-color:#525353;padding:1vw;border:solid;max-width:30vw;min-width:30vw;border-radius:1vw;margin:15vh auto auto}.content-container[_ngcontent-%COMP%]{margin-left:1.3vw}.content-header[_ngcontent-%COMP%]{color:#fff;font-size:3vh}.new-project-container[_ngcontent-%COMP%]{display:flex;flex-direction:row;margin:3vh 0 0;align-items:baseline}.label[_ngcontent-%COMP%]{margin-right:1vw}.input-style[_ngcontent-%COMP%], .label[_ngcontent-%COMP%]{color:#fff;font-size:2vh}.input-style[_ngcontent-%COMP%]{border-radius:2vw;border:none;outline:none;background-color:#363636;min-width:11vw;max-width:11vw;min-height:4vh;max-height:4vh;padding:0 1vw}.validation[_ngcontent-%COMP%]{color:red}.error-msg[_ngcontent-%COMP%]{font-size:2vh}.select-folder-container[_ngcontent-%COMP%]{display:flex;flex-direction:row;margin:2vh 0 0}.input[_ngcontent-%COMP%]{color:#7fffd4}.horizontal-line[_ngcontent-%COMP%]{background-color:#fff;min-height:.3vh;max-height:.3vh;margin:2vh auto}.model-button-container[_ngcontent-%COMP%]{display:flex;flex-direction:row-reverse;padding:1vw}.button-style[_ngcontent-%COMP%]{padding:1vh 1.5vw;border-radius:1vh;border:none;outline:none;color:#fff;cursor:pointer;font-size:2vh}.create-btn[_ngcontent-%COMP%]{background-color:#169887}.cancel-btn[_ngcontent-%COMP%]{background-color:#444;margin-right:.7vw}.choose-file-btn[_ngcontent-%COMP%]{font-size:2vh;text-decoration:none;background-color:#444;color:#fff;padding:.5vh 1vw;border:none;border-radius:1vh;margin-right:1vw;cursor:pointer}.label-file[_ngcontent-%COMP%]{padding-top:.5vh}.input-id[_ngcontent-%COMP%]{font-size:2vh;display:none}.filename[_ngcontent-%COMP%]{font-size:2vh;padding-top:.5vh;color:#fff}.overlay[_ngcontent-%COMP%]{z-index:2000;position:absolute;background-color:rgba(0,0,0,.9);width:99.9vw;height:99.7vh;cursor:not-allowed}"]}),e})()}];let te=(()=>{class e{}return e.\u0275mod=u.Fb({type:e}),e.\u0275inj=u.Eb({factory:function(t){return new(t||e)},imports:[[f.d.forChild(ee)]]}),e})();var ne=n("KZX/");let ie=(()=>{class e{}return e.\u0275mod=u.Fb({type:e}),e.\u0275inj=u.Eb({factory:function(t){return new(t||e)},imports:[[i.b,ne.a,te]]}),e})()}}]);