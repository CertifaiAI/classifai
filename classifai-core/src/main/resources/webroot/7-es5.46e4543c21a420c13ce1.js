!function(){function e(e,t){if(!(e instanceof t))throw new TypeError("Cannot call a class as a function")}function t(e,t){for(var n=0;n<t.length;n++){var o=t[n];o.enumerable=o.enumerable||!1,o.configurable=!0,"value"in o&&(o.writable=!0),Object.defineProperty(e,o.key,o)}}function n(e,n,o){return n&&t(e.prototype,n),o&&t(e,o),e}(window.webpackJsonp=window.webpackJsonp||[]).push([[7],{RN8A:function(t,o,a){"use strict";a.r(o),a.d(o,"DataSetLayoutModule",function(){return pe});var i=a("ofXK"),r=a("XIp8"),c=a("SxV6"),s=a("lJxs"),l=a("eIep"),b=a("5+tZ"),d=a("/uUt"),u=a("1G5W"),m=a("XNiG"),p=a("l5mm"),g=a("z6cu"),h=a("3Pt+"),f=a("fXoL"),v=a("tyNb"),j=a("LY9J"),w=a("F7l1"),M=a("14na"),P=a("I7yr"),x=a("1F7/"),_=a("x2Se"),y=a("sYmb");function O(e,t){if(1&e){var n=f.Nb();f.Kb(0),f.Mb(1,"div",5),f.Mb(2,"div",6),f.Tb("click",function(){return f.hc(n),f.Vb(2).displayModal()}),f.Ib(3,"img",7),f.Mb(4,"label",8),f.oc(5),f.Wb(6,"translate"),f.Lb(),f.Lb(),f.Lb(),f.Jb()}if(2&e){var o=f.Vb(),a=o.index,i=o.$implicit;f.xb(1),f.yb("data-index",a),f.xb(2),f.ac("src",i.src,f.jc),f.xb(2),f.pc(f.Xb(6,3,i.name))}}function L(e,t){if(1&e){var n=f.Nb();f.Mb(0,"div",9),f.Tb("click",function(){f.hc(n);var e=f.Vb().$implicit;return f.Vb().onClickButton(e.id)}),f.Ib(1,"img",10),f.Mb(2,"label",11),f.oc(3),f.Wb(4,"translate"),f.Lb(),f.Lb()}if(2&e){var o=f.Vb(),a=o.$implicit;f.yb("data-index",o.index),f.xb(1),f.lc(a.style),f.ac("src",a.src,f.jc),f.xb(2),f.pc(f.Xb(4,5,a.name))}}function C(e,t){if(1&e&&(f.Kb(0),f.mc(1,O,7,5,"ng-container",3),f.mc(2,L,5,7,"ng-template",null,4,f.nc),f.Jb()),2&e){var n=t.index,o=f.fc(3);f.xb(1),f.ac("ngIf",0===n)("ngIfElse",o)}}var I,S,k=((S=function(){function t(){var n=this;e(this,t),this.menuSchema=[{src:"../../../assets/icons/add.svg",id:"newProject",name:"menuName.newProject"},{src:"../../../assets/icons/import.svg",id:"importProject",name:"menuName.importProject",style:"width: 1.3vw; padding: 0.3vw;"},{src:"../../../assets/icons/project.svg",id:"myProject",name:"menuName.myProject"},{src:"../../../assets/icons/starred.svg",id:"starred",name:"menuName.starred"},{src:"../../../assets/icons/history.svg",id:"recent",name:"menuName.recent"},{src:"../../../assets/icons/trash.svg",id:"trash",name:"menuName.trash"}],this._onCreate=new f.n,this._onImport=new f.n,this.displayModal=function(){n._onCreate.emit(!0)},this.onClickButton=function(e){"importProject"===e?n._onImport.emit():console.log("This feature is not available yet")}}return n(t,[{key:"ngOnInit",value:function(){}}]),t}()).\u0275fac=function(e){return new(e||S)},S.\u0275cmp=f.Bb({type:S,selectors:[["data-set-side-menu"]],outputs:{_onCreate:"_onCreate",_onImport:"_onImport"},decls:3,vars:1,consts:[[1,"dataset-sidemenu-container"],[4,"ngFor","ngForOf"],[1,"horizontal-line"],[4,"ngIf","ngIfElse"],["otherMenu",""],[1,"new-project-container"],[1,"new-project-btn",3,"click"],[1,"add-icon",3,"src"],[1,"new-project-txt"],[1,"current-project-btn",3,"click"],[1,"project-icon",3,"src"],[1,"current-project-txt"]],template:function(e,t){1&e&&(f.Mb(0,"div",0),f.mc(1,C,4,2,"ng-container",1),f.Lb(),f.Ib(2,"div",2)),2&e&&(f.xb(1),f.ac("ngForOf",t.menuSchema))},directives:[i.j,i.k],pipes:[y.c],styles:[".dataset-sidemenu-container[_ngcontent-%COMP%]{display:flex;flex-wrap:wrap;flex-direction:column;width:16vw}.new-project-container[_ngcontent-%COMP%]{margin-bottom:5vh;margin-left:2vw}.new-project-btn[_ngcontent-%COMP%]{padding:1vw;border-radius:5vh;background-color:#525353;border:none;color:#fff;outline:none;cursor:pointer;display:flex;justify-content:space-around;align-items:center;min-width:10vw;max-width:10vw;min-height:4vh;max-height:4vh}.new-project-btn[_ngcontent-%COMP%]:hover{background-color:#393838}.add-icon[_ngcontent-%COMP%]{min-height:inherit;max-height:inherit}.new-project-txt[_ngcontent-%COMP%]{border:none;background:none;outline:none;cursor:pointer;font-size:2vh;color:#fff;text-align:start;white-space:nowrap}.current-project-btn[_ngcontent-%COMP%]{color:#fff;cursor:pointer;display:flex;align-items:center;border-radius:5vh;padding:1vh 1vw;margin-left:2vw;min-width:10vw;max-width:10vw;min-height:5vh;max-height:5vh;flex:1 1 100%}.current-project-btn[_ngcontent-%COMP%]:hover{background-color:#525353}.project-icon[_ngcontent-%COMP%]{min-height:4vh;max-height:4vh;flex:1 1 10%}.current-project-txt[_ngcontent-%COMP%]{border:none;background:none;outline:none;cursor:pointer;font-size:2vh;color:#fff;white-space:nowrap;flex:1 1 90%;text-align:left;padding-left:20px}.horizontal-line[_ngcontent-%COMP%]{width:12vw;background-color:#393838;min-height:.3vh;max-height:.3vh;margin:auto;border:.0625rem solid #000}"]}),S),N=((I=function(){function t(){e(this,t)}return n(t,[{key:"ngOnInit",value:function(){}}]),t}()).\u0275fac=function(e){return new(e||I)},I.\u0275cmp=f.Bb({type:I,selectors:[["data-set-header"]],decls:6,vars:3,consts:[[1,"dataset-header-container"],[1,"label"],[1,"dataset-icon-container"]],template:function(e,t){1&e&&(f.Mb(0,"div",0),f.Mb(1,"label",1),f.oc(2),f.Wb(3,"translate"),f.Lb(),f.Mb(4,"div",2),f.Ib(5,"div"),f.Lb(),f.Lb()),2&e&&(f.xb(2),f.pc(f.Xb(3,1,"datasetHeader.datasetManagement")))},pipes:[y.c],styles:[".dataset-header-container[_ngcontent-%COMP%]{display:flex;justify-content:space-around;align-items:center;padding:1vw;min-width:80vw;max-width:80vw}.label[_ngcontent-%COMP%]{flex:1 1 80%;background:none;font-size:2.5vh;color:#fff;white-space:nowrap;min-height:inherit;max-height:inherit}.dataset-icon-container[_ngcontent-%COMP%]{display:flex;justify-content:space-between;align-items:center;flex:1 1 20%}.dataset-icon[_ngcontent-%COMP%]{flex:1 1 3%;min-width:2vw;max-width:2vw;cursor:pointer}.dataset-icon[_ngcontent-%COMP%]:hover{border-radius:5vh;background-color:#393838}.dataset-select[_ngcontent-%COMP%]{min-height:4vh;max-height:4vh;font-size:2vh;min-width:7vw;max-width:7vw;-moz-text-align-last:center;background:#000;color:#fff;border:.1vw solid;text-align-last:center}.dataset-select[_ngcontent-%COMP%]:focus, .dataset-select[_ngcontent-%COMP%]:hover{background:#393838}.dataset-select[_ngcontent-%COMP%]:focus, .dataset-select[_ngcontent-%COMP%]:hover, select[_ngcontent-%COMP%]{-moz-appearance:none;-webkit-appearance:none}option[_ngcontent-%COMP%]{background:#000;text-align:center}"]}),I);function F(e,t){1&e&&(f.Kb(0),f.Mb(1,"div",2),f.Mb(2,"div",3),f.Mb(3,"div",4),f.Mb(4,"label",5),f.oc(5),f.Wb(6,"translate"),f.Lb(),f.Lb(),f.Mb(7,"div"),f.Mb(8,"label",6),f.oc(9),f.Wb(10,"translate"),f.Lb(),f.Lb(),f.Lb(),f.Lb(),f.Jb()),2&e&&(f.xb(5),f.pc(f.Xb(6,2,"datasetCard.fetchingProject")),f.xb(4),f.pc(f.Xb(10,4,"datasetCard.pleaseWait")))}function W(e,t){1&e&&(f.Kb(0),f.Mb(1,"label",22),f.oc(2),f.Wb(3,"translate"),f.Lb(),f.Jb()),2&e&&(f.xb(2),f.qc(" ",f.Xb(3,1,"datasetCard.uploading")," "))}function D(e,t){1&e&&(f.Kb(0),f.Mb(1,"label",24),f.oc(2),f.Wb(3,"translate"),f.Lb(),f.Jb()),2&e&&(f.xb(2),f.pc(f.Xb(3,1,"datasetCard.new")))}function T(e,t){1&e&&(f.Kb(0),f.Mb(1,"label",25),f.oc(2),f.Wb(3,"translate"),f.Lb(),f.Jb()),2&e&&(f.xb(2),f.pc(f.Xb(3,1,"datasetCard.available")))}function X(e,t){1&e&&(f.Kb(0),f.Mb(1,"label",26),f.oc(2),f.Wb(3,"translate"),f.Lb(),f.Jb()),2&e&&(f.xb(2),f.pc(f.Xb(3,1,"datasetCard.opened")))}function E(e,t){if(1&e&&(f.Kb(0,23),f.mc(1,D,4,3,"ng-container",16),f.mc(2,T,4,3,"ng-container",16),f.mc(3,X,4,3,"ng-container",16),f.Jb()),2&e){var n=f.Vb().$implicit;f.ac("ngSwitch",n),f.xb(1),f.ac("ngIf",n.is_new),f.xb(1),f.ac("ngIf",!n.is_new&&!n.is_loaded),f.xb(1),f.ac("ngIf",!n.is_new&&n.is_loaded)}}function z(e,t){if(1&e){var n=f.Nb();f.Kb(0),f.Mb(1,"span"),f.Mb(2,"div",27),f.Mb(3,"div",28),f.Tb("click",function(){f.hc(n);var e=f.Vb().$implicit;return f.Vb(3).onRenameProject(e.project_name)}),f.oc(4),f.Wb(5,"translate"),f.Lb(),f.Mb(6,"div",28),f.Tb("click",function(){f.hc(n);var e=f.Vb().$implicit;return f.Vb(3).onDeleteProject(e.project_name)}),f.oc(7),f.Wb(8,"translate"),f.Lb(),f.Lb(),f.Lb(),f.Jb()}2&e&&(f.xb(4),f.qc(" ",f.Xb(5,2,"datasetCard.renameProject")," "),f.xb(3),f.qc(" ",f.Xb(8,4,"datasetCard.deleteProject")," "))}var R=function(e){return[e]};function q(e,t){if(1&e){var n=f.Nb();f.Kb(0),f.Mb(1,"div",9),f.Tb("dblclick",function(){f.hc(n);var e=t.index,o=t.$implicit;return f.Vb(3).onOpenProject(e,o)}),f.Mb(2,"div",10),f.Mb(3,"div"),f.mc(4,W,4,3,"ng-container",0),f.mc(5,E,4,4,"ng-template",null,11,f.nc),f.Lb(),f.Mb(7,"div",12),f.Mb(8,"div",13),f.Tb("click",function(){f.hc(n);var e=t.$implicit;return f.Vb(3).onStarred(e,!e.is_starred)})("dblclick",function(e){return f.hc(n),f.Vb(3).onDblClickStopPropagate(e)}),f.Ib(9,"img",14),f.Lb(),f.Mb(10,"div",13),f.Tb("click",function(){f.hc(n);var e=t.index;return f.Vb(3).onDisplayMore(e)})("dblclick",function(e){return f.hc(n),f.Vb(3).onDblClickStopPropagate(e)}),f.Ib(11,"img",15),f.Lb(),f.mc(12,z,9,6,"ng-container",16),f.Lb(),f.Lb(),f.Mb(13,"div",17),f.Mb(14,"label",18),f.Mb(15,"div",19),f.oc(16),f.Wb(17,"translate"),f.Lb(),f.Lb(),f.Lb(),f.Mb(18,"div",20),f.Mb(19,"label",18),f.Mb(20,"div",21),f.oc(21),f.Lb(),f.Lb(),f.Lb(),f.Mb(22,"div",20),f.Mb(23,"label",18),f.Mb(24,"div",21),f.oc(25),f.Wb(26,"translate"),f.Lb(),f.Lb(),f.Lb(),f.Lb(),f.Jb()}if(2&e){var o=t.$implicit,a=t.index,i=f.fc(6),r=f.Vb(3);f.xb(1),f.yb("data-index",a),f.xb(3),f.ac("ngIf",r.isExactIndex(a)&&r._jsonSchema.isUploading)("ngIfElse",i),f.xb(5),f.ac("src",f.dc(17,R,o.is_starred?r.starredActiveIcon:r.starredInactiveIcon),f.jc),f.xb(3),f.ac("ngIf",r.isExactIndex(a)),f.xb(2),f.ac("title",o.created_date),f.xb(2),f.rc(" ",f.Xb(17,13,"datasetCard.created")," ",o.created_date," "),f.xb(3),f.ac("title",o.project_name),f.xb(2),f.qc(" ",o.project_name," "),f.xb(2),f.ac("title","Total Photo: "+o.total_uuid),f.xb(2),f.rc(" ",f.Xb(26,15,"datasetCard.totalPhoto")," ",o.total_uuid," ")}}function J(e,t){if(1&e&&(f.Kb(0),f.Mb(1,"div",2),f.mc(2,q,27,19,"ng-container",8),f.Lb(),f.Jb()),2&e){var n=f.Vb(2);f.xb(2),f.ac("ngForOf",n._jsonSchema.projects)}}function V(e,t){1&e&&(f.Mb(0,"div",2),f.Mb(1,"div",3),f.Mb(2,"div",4),f.Mb(3,"label",5),f.oc(4),f.Wb(5,"translate"),f.Lb(),f.Lb(),f.Mb(6,"div"),f.Mb(7,"label",6),f.oc(8),f.Wb(9,"translate"),f.Lb(),f.Lb(),f.Lb(),f.Lb()),2&e&&(f.xb(4),f.pc(f.Xb(5,2,"datasetCard.noProject")),f.xb(4),f.pc(f.Xb(9,4,"datasetCard.createNew")))}function B(e,t){if(1&e&&(f.mc(0,J,3,1,"ng-container",0),f.mc(1,V,10,6,"ng-template",null,7,f.nc)),2&e){var n=f.fc(2),o=f.Vb();f.ac("ngIf",o._jsonSchema.projects.length>0)("ngIfElse",n)}}var U,H=((U=function(){function t(n){var o=this;e(this,t),this._cd=n,this._onClick=new f.n,this._onStarred=new f.n,this._onDelete=new f.n,this._onRename=new f.n,this.starredActiveIcon="../../../assets/icons/starred_active.svg",this.starredInactiveIcon="../../../assets/icons/starred.svg",this.cardSchema={clickIndex:-1},this.previousProjectLength=0,this.conditionalDisableProject=function(e){return e.is_loaded?"disabled":"enabled"},this.conditionalDisableClickEvent=function(e){return e},this.onOpenProject=function(e,t){var n=t.project_name;!o.isExactIndex(e)&&o._onClick.emit(n)},this.onDisplayMore=function(){var e=arguments.length>0&&void 0!==arguments[0]?arguments[0]:o.cardSchema.clickIndex,t=o.cardSchema.clickIndex;o.cardSchema={clickIndex:t===e?-1:e}},this.onCloseDisplay=function(){o.cardSchema.clickIndex=-1},this.onStarred=function(e,t){var n=e.project_name;o._jsonSchema.projects=o._jsonSchema.projects.map(function(e){return e.project_name===n?(e.is_starred=t,e):e}),o._onStarred.emit({projectName:n,starred:t})},this.isExactIndex=function(e){return e===o.cardSchema.clickIndex},this.onDblClickStopPropagate=function(e){return e.stopPropagation()}}return n(t,[{key:"ngOnInit",value:function(){}},{key:"onRenameProject",value:function(e){this._onRename.emit({shown:!0,projectName:e}),this.onCloseDisplay()}},{key:"onDeleteProject",value:function(e){this._onDelete.emit(e),this.onCloseDisplay()}},{key:"ngOnChanges",value:function(e){!e._jsonSchema.currentValue.isUploading&&this.onDisplayMore(),this._jsonSchema.projects.length!==this.previousProjectLength&&(this.cardSchema.clickIndex=-1),this.previousProjectLength=this._jsonSchema.projects.length}}]),t}()).\u0275fac=function(e){return new(e||U)(f.Hb(f.h))},U.\u0275cmp=f.Bb({type:U,selectors:[["data-set-card"]],inputs:{_jsonSchema:"_jsonSchema"},outputs:{_onClick:"_onClick",_onStarred:"_onStarred",_onDelete:"_onDelete",_onRename:"_onRename"},features:[f.vb],decls:3,vars:2,consts:[[4,"ngIf","ngIfElse"],["showCardBody",""],[1,"card-layout-container","scroll","fade-in"],[1,"no-project-card-container"],[1,"no-project-title-padding"],[1,"no-project-title"],[1,"no-project-subtitle"],["noProject",""],[4,"ngFor","ngForOf"],[1,"card-container",3,"dblclick"],[1,"card-header-style"],["newLabel",""],[1,"card-icon-container"],[3,"click","dblclick"],[1,"card-icon-style",3,"src"],["src","../../../assets/icons/more.svg",1,"card-icon-style","enabled"],[4,"ngIf"],[1,"card-title-style"],[3,"title"],[1,"card-title-txt"],[1,"project-name-style"],[1,"project-info"],[1,"project-status-uploading"],[3,"ngSwitch"],[1,"project-status-new"],[1,"project-status-available"],[1,"project-status-opened"],[1,"popup-container","enabled"],[1,"popup-label",3,"click"]],template:function(e,t){if(1&e&&(f.mc(0,F,11,6,"ng-container",0),f.mc(1,B,3,2,"ng-template",null,1,f.nc)),2&e){var n=f.fc(2);f.ac("ngIf",t._jsonSchema.isFetching)("ngIfElse",n)}},directives:[i.k,i.j,i.m],pipes:[y.c],styles:['@keyframes fade-in{0%{opacity:0}to{opacity:1}}@-webkit-keyframes fade-in{0%{opacity:0}to{opacity:1}}.fade-in[_ngcontent-%COMP%]{animation:fadeIn 1.5s ease;-webkit-animation:fadeIn 1.5s ease;-moz-animation:fadeIn ease 1.5s;-o-animation:fadeIn ease 1.5s;-ms-animation:fadeIn ease 1.5s}.card-layout-container[_ngcontent-%COMP%]{width:80vw;display:flex;flex-wrap:wrap;padding:0 0 0 1vw;overflow-y:scroll;position:relative;height:80vh}.scroll[_ngcontent-%COMP%]::-webkit-scrollbar-track{box-shadow:inset 0 0 6px rgba(0,0,0,.3);border-radius:10px;background-color:#000}.scroll[_ngcontent-%COMP%]::-webkit-scrollbar{width:.5vw}.scroll[_ngcontent-%COMP%]::-webkit-scrollbar-thumb{border-radius:10px;box-shadow:inset 0 0 6px rgba(0,0,0,.3);background-color:#525353}.card-container[_ngcontent-%COMP%]{min-width:11vw;max-width:11vw;min-height:30vh;max-height:30vh;border-style:solid;font-size:2.2vh;background:#2e2d2d;position:relative}.card-container[_ngcontent-%COMP%]:hover{background:#404040}.card-container[_ngcontent-%COMP%]:before{content:"";display:block;height:100%;position:absolute;top:0;left:0;width:.3vw;background-color:#363636}.enabled[_ngcontent-%COMP%]{cursor:pointer}.disabled[_ngcontent-%COMP%]{cursor:not-allowed}.project-status-new[_ngcontent-%COMP%]{background-color:#f59221}.project-status-available[_ngcontent-%COMP%], .project-status-new[_ngcontent-%COMP%]{color:#f5f5f5;font-size:1.5vh;padding:.3vh 1.5vw .6vh 1vw}.project-status-available[_ngcontent-%COMP%]{background-color:#92c91b}.project-status-opened[_ngcontent-%COMP%]{background-color:#258fc0}.project-status-opened[_ngcontent-%COMP%], .project-status-uploading[_ngcontent-%COMP%]{color:#f5f5f5;font-size:1.5vh;padding:.3vh 1.5vw .6vh 1vw}.project-status-uploading[_ngcontent-%COMP%]{background-color:#f5219d}.card-icon-container[_ngcontent-%COMP%]{display:flex}.card-icon-style[_ngcontent-%COMP%]{min-width:1.5vw;max-width:1.5vw;min-height:3vh;max-height:3vh}.card-icon-style[_ngcontent-%COMP%]:hover{border-radius:5vh;background-color:#393838}.card-header-style[_ngcontent-%COMP%]{margin-top:1vh;display:flex;flex-direction:row;justify-content:space-between;position:relative}.card-title-style[_ngcontent-%COMP%]{margin-left:1vw}.card-title-txt[_ngcontent-%COMP%]{color:#656667;font-size:1.3vh;white-space:nowrap;overflow:hidden;text-overflow:ellipsis}.project-name-style[_ngcontent-%COMP%]{margin-left:1vw;padding:1vh 0 0}.project-info[_ngcontent-%COMP%]{color:#dbdbda;font-size:2vh;white-space:nowrap;overflow:hidden;text-overflow:ellipsis}.tag-container[_ngcontent-%COMP%]{display:flex;flex-direction:row;border-radius:10vw;background:#363636;width:4.5vw;padding:.2vw .3vh;cursor:pointer;outline:none;border:none;margin:.8vh 0 0 1vw}.tag-img[_ngcontent-%COMP%]{min-height:2vh;max-height:2vh;min-width:1vw;max-width:1vw;margin-left:.35vw}.tag-txt[_ngcontent-%COMP%]{font-size:1.2vh;color:#fff;padding:.2vh 0 0 .3vw;cursor:pointer}.popup-container[_ngcontent-%COMP%]{overflow:hidden;position:absolute;margin:4vh 0 0 -10vw;padding:.5vw;border-radius:.2vw;box-shadow:0 2px 5px 0 rgba(var(--shadow-rgb),.26),0 2px 10px 0 rgba(var(--shadow-rgb),.16);transform-origin:left top;transform:scale(1);opacity:1;white-space:nowrap;background:#fff;font-size:1.5vh;-webkit-animation:appear .35s ease-in 1;animation:appear .35s ease-in 1}@-webkit-keyframes appear{0%{opacity:0;transform:translateY(-10px)}}@keyframes appear{0%{opacity:0;transform:translateY(-10px)}}.popup-label[_ngcontent-%COMP%]{font-size:2vh;padding:.5vw}.popup-label[_ngcontent-%COMP%]:hover{background:#e9e9e9}.no-project-title[_ngcontent-%COMP%]{font-size:4vh;color:#fff;white-space:nowrap}.no-project-title-padding[_ngcontent-%COMP%]{padding:2vw}.no-project-subtitle[_ngcontent-%COMP%]{font-size:2vh;color:#bebebe;white-space:nowrap}.no-project-card-container[_ngcontent-%COMP%]{display:flex;flex-direction:column;justify-content:center;align-items:center;background-color:#1f1f1f;width:100%}']}),U),K=a("44N4"),$=a("TJKd"),A=["refProjectName"],G=["labeltextfilename"],Y=["refNewProjectName"],Z=["jsonImportProjectFile"],Q=["jsonImportProjectFilename"];function ee(e,t){if(1&e&&(f.Mb(0,"div",33),f.Mb(1,"p",34),f.oc(2),f.Lb(),f.Lb()),2&e){var n=f.Vb();f.xb(2),f.qc(" ",n.isImageUploading?"Uploading the Images. Please Wait...":"Selection Window is Opened"," ")}}function te(e,t){1&e&&(f.Mb(0,"span"),f.Mb(1,"small",36),f.oc(2),f.Wb(3,"translate"),f.Lb(),f.Lb()),2&e&&(f.xb(2),f.qc(" ",f.Xb(3,1,"projectNameExist")," "))}function ne(e,t){1&e&&(f.Mb(0,"span"),f.Mb(1,"small",36),f.oc(2),f.Wb(3,"translate"),f.Lb(),f.Lb()),2&e&&(f.xb(2),f.qc(" ",f.Xb(3,1,"projectNameRequired")," "))}function oe(e,t){if(1&e&&(f.Kb(0),f.Mb(1,"div",35),f.mc(2,te,4,3,"span",12),f.mc(3,ne,4,3,"span",12),f.Lb(),f.Jb()),2&e){var n=f.Vb(),o=null,a=null;f.xb(2),f.ac("ngIf",null==(o=n.form.get("projectName"))?null:o.getError("exist")),f.xb(1),f.ac("ngIf",null==(a=n.form.get("projectName"))?null:a.getError("required"))}}function ae(e,t){1&e&&(f.Mb(0,"span"),f.Mb(1,"small",36),f.oc(2),f.Wb(3,"translate"),f.Lb(),f.Lb()),2&e&&(f.xb(2),f.qc(" ",f.Xb(3,1,"projectNameExist")," "))}function ie(e,t){1&e&&(f.Mb(0,"span"),f.Mb(1,"small",36),f.oc(2),f.Wb(3,"translate"),f.Lb(),f.Lb()),2&e&&(f.xb(2),f.qc(" ",f.Xb(3,1,"projectNameRequired")," "))}function re(e,t){if(1&e&&(f.Kb(0),f.Mb(1,"div",35),f.mc(2,ae,4,3,"span",12),f.mc(3,ie,4,3,"span",12),f.Lb(),f.Jb()),2&e){var n=f.Vb(),o=null,a=null;f.xb(2),f.ac("ngIf",null==(o=n.renameForm.get("newProjectName"))?null:o.getError("exist")),f.xb(1),f.ac("ngIf",null==(a=n.renameForm.get("newProjectName"))?null:a.getError("required"))}}function ce(e,t){if(1&e&&(f.Kb(0),f.Mb(1,"div",37),f.Mb(2,"span"),f.Ib(3,"small",31),f.Lb(),f.Lb(),f.Jb()),2&e){var n=f.Vb();f.xb(1),f.ac("ngClass",n.spanClass),f.xb(2),f.ac("innerHTML",n.modalSpanMessage,f.ic)}}var se,le,be,de=[{path:"",component:(se=function(){function t(n,o,a,i,f,v,j){var w=this;e(this,t),this._fb=n,this._router=o,this._dataSetService=a,this._spinnerService=i,this._imgLblModeService=f,this._languageService=v,this._modalService=j,this.onChangeSchema={currentThumbnailIndex:-1,thumbnailName:"",totalNumThumbnail:0,status:void 0},this.projectList={projects:[],isUploading:!1,isFetching:!1},this.inputProjectName="",this.newInputProjectName="",this.selectedProjectName="",this.oldProjectName="",this.labelTextUpload=[],this.subject$=new m.a,this.thumbnailList=[],this.labelList=[],this.unsubscribe$=new m.a,this.isLoading=!1,this.isOverlayOn=!1,this.isImageUploading=!1,this.isProjectLoading=!1,this.imgLblMode=null,this.modalSpanMessage="",this.spanClass="",this.modalIdCreateProject="modal-create-project",this.modalIdRenameProject="modal-rename-project",this.modalIdImportProject="modal-import-project",this.modalIdDeleteProject="modal-delete-project",this.createProjectModalBodyStyle={minHeight:"35vh",maxHeight:"35vh",minWidth:"31vw",maxWidth:"31vw",margin:"15vw 71vh",overflow:"none"},this.renameProjectModalBodyStyle={minHeight:"23vh",maxHeight:"23vh",minWidth:"31vw",maxWidth:"31vw",margin:"15vw 71vh",overflow:"none"},this.importProjectModalBodyStyle={minHeight:"15vh",maxHeight:"25vh",minWidth:"31vw",maxWidth:"31vw",margin:"15vw 71vh",overflow:"none"},this.deleteProjectBodyStyle={minHeight:"10vh",maxHeight:"15vh",minWidth:"31vw",maxWidth:"31vw",margin:"15vw 71vh",overflow:"none"},this.showProjectList=function(){w.projectList.isFetching=!0,w._dataSetService.getProjectList().pipe(Object(c.a)()).subscribe(function(e){var t=e.content;if(t){var n=Object(r.a)(t).map(function(e){return Object.assign(Object.assign({},e),{created_date:w.formatDate(e.created_date)})});w.projectList=Object.assign(Object.assign({},w.projectList),{projects:n,isFetching:!1})}})},this.formatDate=function(e){var t=new Date(e),n=["Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"].find(function(e,n){return n===t.getMonth()||void 0});return n?"".concat(n,"-").concat(t.getDate(),"-").concat(t.getFullYear()):"Error"},this.createFormControls=function(){w.form=w._fb.group({projectName:["",h.l.required]})},this.renameFormControls=function(){w.renameForm=w._fb.group({newProjectName:["",h.l.required]})},this.onChange=function(e){w.inputProjectName=e},this.onChangeRename=function(e){w.newInputProjectName=e},this.toggleModalDisplay=function(e){w._labelTextFilename.nativeElement.innerHTML="",w.labelTextUpload=[],e&&w.form.reset(),e?w._modalService.open(w.modalIdCreateProject):w._modalService.close(w.modalIdCreateProject)},this.toggleRenameModalDisplay=function(e){if(e){var t=e.shown,n=e.projectName;t?(w.renameForm.reset(),w._modalService.open(w.modalIdRenameProject)):w._modalService.close(w.modalIdRenameProject),w.oldProjectName=n}else w._modalService.close(w.modalIdRenameProject)},this.toggleImportProjectModalDisplay=function(e){w.modalSpanMessage="",e?w._modalService.open(w.modalIdImportProject):w._modalService.close(w.modalIdImportProject)},this.onSelectImportProjectJson=function(){w.toggleImportProjectModalDisplay(!0);var e=w._dataSetService.importStatus();w._dataSetService.importProject().pipe(Object(c.a)(),Object(s.a)(function(e){return e.message})).subscribe(function(t){var n=!1;Object(p.a)(500).pipe(Object(l.a)(function(){return e}),Object(c.a)(function(e){return w.isOverlayOn=0===e.message,1!==e.message&&4!==e.message||(n=!0),n})).subscribe(function(e){console.log("RRS",e),console.log(e.error_message.replace(/(\r\n|\n|\r)/gm,"<br>")),w.modalSpanMessage=e.error_message.replace(/(\r\n|\n|\r)/gm,"<br>"),w.processIsSuccess(1!==e.message),w.showProjectList()})})},this.processIsSuccess=function(e){w.spanClass=e?"validation-success":"validation-error"},this.importProject=function(){w.toggleImportProjectModalDisplay(!0)},this.onStarred=function(e){var t=e.projectName,n=e.starred;w._dataSetService.updateProjectStatus(t,n,"star").pipe(Object(c.a)()).subscribe(function(e){var t=e.message;return console.log(t)},function(e){return w.projectList={isUploading:w.projectList.isUploading,isFetching:w.projectList.isFetching,projects:w.projectList.projects.map(function(e){return e.project_name===t?Object.assign(Object.assign({},e),{is_starred:!1}):e})}})},this.onSubmit=function(e,t){var n,o,a;w.form.markAllAsTouched(),e?w.inputProjectName?w.projectList.projects&&w.projectList.projects.find(function(e){return e&&e.project_name===w.inputProjectName})?(null===(n=w.form.get("projectName"))||void 0===n||n.setErrors({exist:!0}),w._refProjectName.nativeElement.focus()):(w.createProject(w.inputProjectName),w.selectedProjectName=null===(o=w.form.get("projectName"))||void 0===o?void 0:o.value):(null===(a=w.form.get("projectName"))||void 0===a||a.setErrors({required:!0}),w._refProjectName.nativeElement.focus()):t&&w.startProject(t)},this.onOpenImportProject=function(e,t){console.log("BUTTON IMPORT CLICKED")},this.startProject=function(e){w._router.navigate(["imglabel/"+w.imgLblMode],{state:{projectName:e}})},this.createProject=function(e){var t=w._dataSetService.createNewProject(e),n=w._dataSetService.updateLabelList(e,w.labelTextUpload),o=w._dataSetService.localUploadStatus(e),a=0;w.projectList=Object.assign(Object.assign({},w.projectList),{isUploading:!0}),w.subjectSubscription=w.subject$.pipe(Object(c.a)(),Object(b.a)(function(){return t}),Object(b.a)(function(){return n}),Object(b.a)(function(e){return function(e){var t=e.message;return 5===t||1!==t&&0!==t?Object(g.a)(function(e){return console.error(e),w.projectList=Object.assign(Object.assign({},w.projectList),{isUploading:!1}),e}):Object(p.a)(500).pipe(Object(b.a)(function(){return o}),Object(c.a)(function(e){var t=e.message;return w.isOverlayOn=0===t||2===t,w.isImageUploading=2===t,4===t||1===t}))}(e)})).subscribe(function(e){4===e.message&&w.toggleModalDisplay(!1),w.isProjectLoading=!0,(a=e?--a:a)<1&&(w.projectList=Object.assign(Object.assign({},w.projectList),{isUploading:!1}))},function(e){},function(){w.isProjectLoading=!1,w.showProjectList()}),w.subject$.next()},this.renameProject=function(e,t){w._dataSetService.renameProject(e,t).pipe(Object(c.a)(),Object(s.a)(function(e){return e.message})).subscribe(function(t){1===t&&(w._languageService._translate.get("renameSuccess").subscribe(function(t){alert(e+" "+t)}),w.showProjectList(),w.toggleRenameModalDisplay())})},this.deleteProject=function(e){w._dataSetService.deleteProject(e).pipe(Object(c.a)(),Object(s.a)(function(e){return e.message})).subscribe(function(t){1===t&&(w._languageService._translate.get("deleteSuccess").subscribe(function(t){w.modalSpanMessage=e+" "+t,w._modalService.open(w.modalIdDeleteProject)}),w.showProjectList())})},this.keyDownEvent=function(e){"Escape"===e.key&&w.toggleRenameModalDisplay()&&w.toggleModalDisplay(!1)},this._imgLblModeService.imgLabelMode$.pipe(Object(d.a)()).subscribe(function(e){return w.imgLblMode=e}),this._spinnerService.returnAsObservable().pipe(Object(u.a)(this.unsubscribe$)).subscribe(function(e){return w.isLoading=e}),this.createFormControls(),this.renameFormControls(),this._languageService.initializeLanguage("data-set-page",["data-set-page-en","data-set-page-cn","data-set-page-ms"])}return n(t,[{key:"ngOnInit",value:function(){this.showProjectList()}},{key:"onSubmitRename",value:function(){var e,t,n,o=this;this.renameForm.markAllAsTouched(),this.newInputProjectName?this.projectList.projects&&this.projectList.projects.find(function(e){return e?e.project_name===o.newInputProjectName:null})?(null===(e=this.renameForm.get("newProjectName"))||void 0===e||e.setErrors({exist:!0}),this._refProjectName.nativeElement.focus()):(this.renameProject(this.oldProjectName,this.newInputProjectName),this.selectedProjectName=null===(t=this.renameForm.get("newProjectName"))||void 0===t?void 0:t.value):(null===(n=this.renameForm.get("newProjectName"))||void 0===n||n.setErrors({required:!0}),this._refProjectName.nativeElement.focus())}},{key:"importLabelFile",value:function(){var e=this,t=this._dataSetService.importLabelFileStatus();this._dataSetService.importLabelFile().pipe(Object(c.a)(),Object(s.a)(function(e){return e.message})).subscribe(function(n){var o=!1;Object(p.a)(500).pipe(Object(l.a)(function(){return t}),Object(c.a)(function(t){return e.isOverlayOn=0===t.message,4===t.message&&(e._labelTextFilename.nativeElement.innerHTML=t.label_file_path.replace(/^.*[\\\/]/,""),e.labelTextUpload=t.label_list),1!==t.message&&4!==t.message||(o=!0),o})).subscribe(function(t){e.showProjectList()})})}},{key:"onWindowClose",value:function(e){e.preventDefault(),this.isProjectLoading&&(e.returnValue="Are you sure you want to leave this page?")}},{key:"ngOnDestroy",value:function(){this.unsubscribe$.next(),this.unsubscribe$.complete()}}]),t}(),se.\u0275fac=function(e){return new(e||se)(f.Hb(h.b),f.Hb(v.a),f.Hb(j.a),f.Hb(w.a),f.Hb(M.a),f.Hb(P.a),f.Hb(x.a))},se.\u0275cmp=f.Bb({type:se,selectors:[["data-set-layout"]],viewQuery:function(e,t){var n;1&e&&(f.tc(A,!0),f.tc(G,!0),f.tc(Y,!0),f.tc(Z,!0),f.tc(Q,!0)),2&e&&(f.ec(n=f.Ub())&&(t._refProjectName=n.first),f.ec(n=f.Ub())&&(t._labelTextFilename=n.first),f.ec(n=f.Ub())&&(t._refNewProjectName=n.first),f.ec(n=f.Ub())&&(t._jsonImportProjectFile=n.first),f.ec(n=f.Ub())&&(t._jsonImportProjectFilename=n.first))},hostBindings:function(e,t){1&e&&f.Tb("keydown",function(e){return t.keyDownEvent(e)},!1,f.gc)("beforeunload",function(e){return t.onWindowClose(e)},!1,f.gc)},decls:82,vars:68,consts:[["class","overlay",4,"ngIf"],[3,"_onChange"],[1,"upper-container"],[3,"_onCreate","_onImport"],[3,"_jsonSchema","_onClick","_onStarred","_onDelete","_onRename"],[3,"id","modalBodyStyle","modalTitle","scrollable"],[3,"formGroup"],[1,"content-container"],[1,"new-project-container"],[1,"label"],["type","text","placeholder","Enter project name","formControlName","projectName",1,"input-style",3,"value","input"],["refProjectName",""],[4,"ngIf"],[1,"select-file-container"],[1,"label","label-file"],["type","button",1,"button-style","choose-file-button",3,"click"],[1,"file-name-container"],[1,"filename"],["labeltextfilename",""],[1,"horizontal-line"],[1,"model-button-container"],["type","submit",1,"button-style","create-btn",3,"click"],["type","text","placeholder","Enter new project name","formControlName","newProjectName",1,"input-style",3,"value","input"],["refNewProjectName",""],[1,"choose-file-btn"],["type","button",1,"input-id",3,"click"],["jsonImportProjectFilename",""],[1,"tooltip"],[1,"tooltiptitle"],[1,"tooltiptext"],[1,"validation-success"],[1,"error-msg",3,"innerHTML"],[3,"_loading"],[1,"overlay"],[2,"margin-top","40vh","color","rgb(255, 255, 255, 0.9)","text-align","center","font-size","3vh"],[1,"validation"],[1,"error-msg"],[3,"ngClass"]],template:function(e,t){var n,o;(1&e&&(f.mc(0,ee,3,1,"div",0),f.Ib(1,"page-header",1),f.Mb(2,"div",2),f.Mb(3,"data-set-side-menu",3),f.Tb("_onCreate",function(e){return t.toggleModalDisplay(e)})("_onImport",function(){return t.importProject()}),f.Lb(),f.Mb(4,"div"),f.Ib(5,"data-set-header"),f.Mb(6,"data-set-card",4),f.Tb("_onClick",function(e){return t.onSubmit(!1,e)})("_onStarred",function(e){return t.onStarred(e)})("_onDelete",function(e){return t.deleteProject(e)})("_onRename",function(e){return t.toggleRenameModalDisplay(e)}),f.Lb(),f.Lb(),f.Lb(),f.Mb(7,"modal",5),f.Wb(8,"translate"),f.Mb(9,"form",6),f.Mb(10,"div",7),f.Mb(11,"div",8),f.Mb(12,"label",9),f.oc(13),f.Wb(14,"translate"),f.Lb(),f.Mb(15,"input",10,11),f.Tb("input",function(e){return t.onChange(e.target.value)}),f.Lb(),f.Lb(),f.mc(17,oe,4,2,"ng-container",12),f.Ib(18,"br"),f.Mb(19,"div",13),f.Mb(20,"label",14),f.oc(21),f.Wb(22,"translate"),f.Lb(),f.Mb(23,"button",15),f.Tb("click",function(){return t.importLabelFile()}),f.oc(24),f.Wb(25,"translate"),f.Lb(),f.Lb(),f.Mb(26,"div",16),f.Ib(27,"label",17,18),f.Lb(),f.Ib(29,"div",19),f.Mb(30,"div",20),f.Mb(31,"button",21),f.Tb("click",function(){return t.onSubmit(!0)}),f.oc(32),f.Wb(33,"translate"),f.Lb(),f.Lb(),f.Lb(),f.Lb(),f.Lb(),f.Mb(34,"modal",5),f.Wb(35,"translate"),f.Mb(36,"form",6),f.Mb(37,"div",7),f.Mb(38,"div",8),f.Mb(39,"label",9),f.oc(40),f.Wb(41,"translate"),f.Lb(),f.Mb(42,"input",22,23),f.Tb("input",function(e){return t.onChangeRename(e.target.value)}),f.Lb(),f.Lb(),f.mc(44,re,4,2,"ng-container",12),f.Ib(45,"div",19),f.Mb(46,"div",20),f.Mb(47,"button",21),f.Tb("click",function(){return t.onSubmitRename()}),f.oc(48),f.Wb(49,"translate"),f.Lb(),f.Lb(),f.Lb(),f.Lb(),f.Lb(),f.Mb(50,"modal",5),f.Wb(51,"translate"),f.Ib(52,"br"),f.Mb(53,"form",6),f.Mb(54,"div",7),f.Mb(55,"div",13),f.Mb(56,"label",14),f.oc(57),f.Wb(58,"translate"),f.Lb(),f.Mb(59,"label",24),f.oc(60),f.Wb(61,"translate"),f.Mb(62,"button",25),f.Tb("click",function(){return t.onSelectImportProjectJson()}),f.oc(63),f.Lb(),f.Lb(),f.Ib(64,"label",17,26),f.Lb(),f.Mb(66,"div",27),f.Mb(67,"div",28),f.oc(68),f.Wb(69,"translate"),f.Lb(),f.Mb(70,"span",29),f.oc(71),f.Wb(72,"translate"),f.Lb(),f.Lb(),f.mc(73,ce,4,2,"ng-container",12),f.Ib(74,"div",19),f.Lb(),f.Lb(),f.Lb(),f.Mb(75,"modal",5),f.Wb(76,"translate"),f.Ib(77,"br"),f.Mb(78,"div",7),f.Mb(79,"div",30),f.Ib(80,"p",31),f.Lb(),f.Lb(),f.Lb(),f.Ib(81,"spinner",32)),2&e)&&(f.ac("ngIf",t.isOverlayOn),f.xb(1),f.ac("_onChange",t.onChangeSchema),f.xb(5),f.ac("_jsonSchema",t.projectList),f.xb(1),f.ac("id",t.modalIdCreateProject)("modalBodyStyle",t.createProjectModalBodyStyle)("modalTitle",f.Xb(8,40,"createNewProject"))("scrollable",!1),f.xb(2),f.ac("formGroup",t.form),f.xb(4),f.qc("",f.Xb(14,42,"newProjectName")," "),f.xb(2),f.ac("value",t.inputProjectName),f.xb(2),f.ac("ngIf",null==(n=t.form.get("projectName"))?null:n.touched),f.xb(4),f.qc("",f.Xb(22,44,"labelListFile")," "),f.xb(3),f.qc(" ",f.Xb(25,46,"chooseFile")," "),f.xb(8),f.qc(" ",f.Xb(33,48,"createButton")," "),f.xb(2),f.ac("id",t.modalIdRenameProject)("modalBodyStyle",t.renameProjectModalBodyStyle)("modalTitle",f.Xb(35,50,"renameProject"))("scrollable",!1),f.xb(2),f.ac("formGroup",t.renameForm),f.xb(4),f.qc("",f.Xb(41,52,"newProjectName")," "),f.xb(2),f.ac("value",t.inputProjectName),f.xb(2),f.ac("ngIf",null==(o=t.renameForm.get("newProjectName"))?null:o.touched),f.xb(4),f.qc(" ",f.Xb(49,54,"updateButton")," "),f.xb(2),f.ac("id",t.modalIdImportProject)("modalBodyStyle",t.importProjectModalBodyStyle)("modalTitle",f.Xb(51,56,"menuName.importProject"))("scrollable",!1),f.xb(3),f.ac("formGroup",t.form),f.xb(4),f.qc("",f.Xb(58,58,"importJson")," "),f.xb(3),f.qc(" ",f.Xb(61,60,"chooseFile")," "),f.xb(3),f.qc(" ","Import"," "),f.xb(5),f.pc(f.Xb(69,62,"configFileInfoTitle")),f.xb(3),f.pc(f.Xb(72,64,"configFileInfoDest")),f.xb(2),f.ac("ngIf",t.modalSpanMessage.trim()),f.xb(2),f.ac("id",t.modalIdDeleteProject)("modalBodyStyle",t.deleteProjectBodyStyle)("modalTitle",f.Xb(76,66,"deleteProject"))("scrollable",!1),f.xb(5),f.ac("innerHTML",t.modalSpanMessage,f.ic),f.xb(1),f.ac("_loading",t.isLoading))},directives:[i.k,_.a,k,N,H,K.a,h.n,h.g,h.d,h.a,h.f,h.c,$.a,i.i],pipes:[y.c],styles:[".upper-container[_ngcontent-%COMP%]{display:flex;margin-top:5vh}.model[_ngcontent-%COMP%]{z-index:1000;padding-top:10vh;top:0;width:100%;height:100%;overflow:auto;background-color:transparent;scrollbar-width:none;position:fixed;background:rgba(0,0,0,.7)}.model-content[_ngcontent-%COMP%]{background-color:#525353;padding:1vw;border:solid;max-width:30vw;min-width:30vw;border-radius:1vw;margin:15vh auto auto}.content-container[_ngcontent-%COMP%]{margin-left:1.3vw}.content-header[_ngcontent-%COMP%]{color:#fff;font-size:3vh}.new-project-container[_ngcontent-%COMP%]{display:flex;flex-direction:row;margin:3vh 0 0;align-items:baseline}.label[_ngcontent-%COMP%]{margin-right:1vw}.input-style[_ngcontent-%COMP%], .label[_ngcontent-%COMP%]{color:#fff;font-size:2vh}.input-style[_ngcontent-%COMP%]{border-radius:2vw;border:none;outline:none;background-color:#363636;min-width:11vw;max-width:11vw;min-height:4vh;max-height:4vh;padding:0 1vw}.validation[_ngcontent-%COMP%]{color:red}.validation-error[_ngcontent-%COMP%]{margin:1px;color:red}.validation-success[_ngcontent-%COMP%]{margin:1px;color:green}.error-msg[_ngcontent-%COMP%]{font-size:2vh}.select-file-container[_ngcontent-%COMP%]{display:flex;flex-direction:row;margin:0}.file-name-container[_ngcontent-%COMP%]{display:flex;flex-direction:row;margin:1.5vh 0 0 19vh;min-height:4vh}.input[_ngcontent-%COMP%]{color:#7fffd4}.horizontal-line[_ngcontent-%COMP%]{background-color:#fff;min-height:.3vh;max-height:.3vh;margin:2vh auto}.model-button-container[_ngcontent-%COMP%]{display:flex;flex-direction:row-reverse;padding:.5vw}.button-style[_ngcontent-%COMP%]{padding:1vh 1.5vw;border-radius:1vh;border:none;outline:none;color:#fff;cursor:pointer;font-size:2vh}.choose-file-button[_ngcontent-%COMP%]{background-color:#444}.create-btn[_ngcontent-%COMP%]{background-color:#169887}.cancel-btn[_ngcontent-%COMP%]{background-color:#444;margin-right:.7vw}.choose-file-btn[_ngcontent-%COMP%]{font-size:2vh;text-decoration:none;background-color:#444;color:#fff;padding:.5vh 1vw;border:none;border-radius:1vh;margin-right:1vw;cursor:pointer}.label-file[_ngcontent-%COMP%]{padding-top:.5vh}.input-id[_ngcontent-%COMP%]{font-size:2vh;display:none}.filename[_ngcontent-%COMP%]{font-size:2vh;color:#fff;overflow:hidden;text-overflow:ellipsis}.overlay[_ngcontent-%COMP%]{z-index:3000;position:absolute;background-color:rgba(0,0,0,.9);width:99.9vw;height:99.7vh;cursor:not-allowed}.tooltip[_ngcontent-%COMP%]{position:relative;display:inline-block;border-bottom:1px dotted #000;font-size:15px;text-decoration:underline;margin-top:1vh}.tooltiptitle[_ngcontent-%COMP%]{opacity:.3}.tooltip[_ngcontent-%COMP%]   .tooltiptext[_ngcontent-%COMP%]{visibility:hidden;width:240px;background-color:#000;color:#fff;text-align:center;border-radius:6px;padding:5px 0;position:absolute;z-index:1;top:100%;left:50%;margin-left:-60px}.tooltip[_ngcontent-%COMP%]:hover   .tooltiptext[_ngcontent-%COMP%]{visibility:visible}"]}),se)}],ue=((le=function t(){e(this,t)}).\u0275mod=f.Fb({type:le}),le.\u0275inj=f.Eb({factory:function(e){return new(e||le)},imports:[[v.d.forChild(de)]]}),le),me=a("KZX/"),pe=((be=function t(){e(this,t)}).\u0275mod=f.Fb({type:be}),be.\u0275inj=f.Eb({factory:function(e){return new(e||be)},imports:[[i.b,me.a,ue]]}),be)}}])}();