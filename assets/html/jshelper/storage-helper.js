var g_iFileTotalPage = 0;
var g_iFileStartItem = 0;
var FILE_ITEM_PER_PAGE = 10;

var g_sCurrentFolder = "/";
var g_sRootFolder = "/";

var FILE_TYPE_TEXT = 1;
var FILE_TYPE_AUDIO = 2;
var FILE_TYPE_IMAGE = 3;
var FILE_TYPE_VIDEO = 4;
var FILE_TYPE_FOLDER = 5;
var MAX_FILE_NAME_LEN = 10;

//var uploader;

function checkPath(sPath){
	sPath = sPath.replace("////","/");
	sPath = sPath.replace("///","/");
	sPath = sPath.replace("//","/");
	return sPath;
}

function StorageXmlParser(xml) {
	FolderXmlParser(xml);
	GenCurrentFolderLink();
}

function GetFileList(sFolderName) {

	var sReq = "FolderList.xml?action=getfolderlist&file=" + sFolderName;
	sReq += "&" + "from=" + g_iFileStartItem;
	//sReq += "&to=" + (g_iFileStartItem + FILE_ITEM_PER_PAGE);
	StartAjaxRequest(sReq,"xml");
}

function GetRootFolder(){
	var sReq = "FolderList.xml?action=getexternalrootfolder";
	StartAjaxRequest(sReq,"xml");
}

function GetRealPath(path){
	return checkPath(g_sRootFolder + "/" + path);
}

function OnClickEnterFolder(sFolderName) {
	g_iFileStartItem = 0;
	GetFileList(checkPath(g_sCurrentFolder + "//" + sFolderName));

}

function OnClickEnterFolderEx(sParentFolder) {
	g_iFileStartItem = 0;
	GetFileList(checkPath(sParentFolder));
}

/*
 * <li><a href="index.html">my document >></a></li> <li class="current-tab"><a
 * href="style-demo.html">picture >></a></li> <li><a
 * href="style-demo.html">photos</a></li>
 */

function GenCurrentFolderLink() {
	var folders = g_sCurrentFolder.split("/");
	LOG4MB("g_sCurrentFolder:" + g_sCurrentFolder);
	var sLinks = "";
	var sFolderPath = "/";
	for ( var i = 0; i < folders.length; ++i) {
		var sFolderName = folders[i];
		if (sFolderName.length < 1) {
			if (i == 0) {
				sFolderName = "/";
			} else {
				continue;
			}

		}
		sFolderPath += folders[i];
		
		var sClass = 'class="pathItem  button-transition';
		if (i == folders.length - 1){
			sClass += ' path-current';
		}
		sClass += '"';

		sLinks += '<a href="#" ' +  sClass + ' onclick=OnClickEnterFolderEx(\"' + sFolderPath
				+ '");>' + sFolderName + '</a>';
		
		sFolderPath += "/";
	}

	$("#FolderPath").html(sLinks);
}

/*
	<dl class="filelist">
							<dt>
								<a href="#"><img src="images/ico-08.png" alt="Õº∆¨√Ë ˆ" /></a>
							</dt>
							<dd>
								<a href="#">œ‡∆¨</a>
							</dd>
						</dl>
 */
function FolderGenLink(sFileName, iSize, iType) {
	var sSubFileName = decodeURIComponent(sFileName);
	sSubFileName = sSubFileName.sub(MAX_FILE_NAME_LEN);
	var sTitle = ' title = "' + decodeURIComponent(sFileName) + '"';
	var sImageClass = 'class="aHasBk sprite sprite-other-file"';
	var sPageLink = '<a href="?action=getfile&file=' +  GetRealPath(g_sCurrentFolder + "/" + sFileName) +'" target="_blank"';
	if (iType == FILE_TYPE_FOLDER) {
		sImageClass = 'class="aHasBk sprite sprite-folder"';
		sPageLink = '<a href="#" onclick="OnClickEnterFolder(\'' + sFileName + '\');"';
	} else if (iType == FILE_TYPE_IMAGE) {
		sImageClass = 'class="aHasBk sprite sprite-photo"';
	} else if (iType == FILE_TYPE_AUDIO) {
		sImageClass = 'class="aHasBk sprite sprite-music"';
	} else if (iType == FILE_TYPE_VIDEO) {
		sImageClass = 'class="aHasBk sprite sprite-video"';
	} else if (iType == FILE_TYPE_TEXT) {
		sImageClass = 'class="aHasBk sprite sprite-doc"';
	}
	//sImagLink += sTitle + '/>';
	sPageLink += sImageClass + '>';
	
	var sLink = '<dl class="filelist">';
	sLink += '<dt>' + sPageLink + '</a>' + '</dt>';
	sLink += '<dd>' + sSubFileName + '</dd>';
	sLink += '<dd><input type="checkbox" name="FileCB" value="' + decodeURIComponent(sFileName) + '"/></dd>'; 
	
	sLink += "</dl>";
	
	//LOG4MB(sLink);

	return sLink;
}

var firstTime = 1;
function FolderXmlParser(xml) {
	var retVal = $(xml).find("EARetCode");
	if (retVal.text().length >= 1) {
		var retcode = retVal.text();
		if (parseInt(retcode) == EA_RET_END_OF_FILE){
			return;
		}
		
		ShowAlert(ParseErrNo(retcode));
		return;
	}
	
	retVal = $(xml).find("RootPath");
	if (retVal.text().length > 1) {
		g_sRootFolder = retVal.text();
		g_sRootFolder += "/";
		
		$("#FileList").html("");
		GetFileList(g_sCurrentFolder);
		
		return;
	}
	
	retVal = $(xml).find("Refresh");
	if (retVal.text().length > 1) {
		LOG4MB(retVal);
		OnRefreshContent();
		return;
	} 

	var TotalCount = $(xml).find("TotalCount").text();

	var cFolder = $(xml).find("CurrentFolder");
	if (cFolder.text() != g_sCurrentFolder || g_iFileStartItem == 0) {
		// current folder has been changed already,so start from first page
		g_iFileStartItem = 0;
		g_sCurrentFolder = cFolder.text();
		$("#FileList").html("");
	}
	
	if (parseInt(TotalCount) == 0){
		$("#FileList").html("");
		return;
	}
	
	$(xml).find("Folder").each(
			function(i) {
				var fLink = FolderGenLink($(this).find("Name").text(), $(this)
						.find("Size").text(), $(this).find("Type").text());
				$("#FileList").append(fLink);
				
				g_iFileStartItem += 1;
			});
	
	if (g_iFileStartItem < parseInt(TotalCount)){
		GetFileList(g_sCurrentFolder);
		return;
	}
	
	$("#FileList").InitJscroll();
}




function OnSumbitFile(id, filename) {
	uploader.setParams({
		"file" : g_sCurrentFolder
	});
}

function OnDeleteFile(){
	var check_obj = $("input:checked[name='FileCB']");
	var sFileList = "";

	var sFiles = "";
	for ( var i = 0; i < check_obj.length; i++) {
		if (i > 0){
			sFileList += ",";
			sFiles += ",";
		}
		
		sFileList += checkPath(GetCurrentFolder() + "//" + check_obj.get(i).value);
		sFiles += check_obj.get(i).value;
	}
	
	if(confirm(sLConfirmeDelete + '<' + sFiles +'>?')){
		var sReq = "FolderList.xml?action=removefile&filelist=" + encodeURIComponent(sFileList);
		StartAjaxRequest(sReq,"xml");
	}
}

function createFileNow(){
	var name = $("#CREATE-FOLDER-NAME").val();
	
	var sReq = "FolderList.xml?action=createfile&file=" + checkPath(GetCurrentFolder() + "//" + name);
	StartAjaxRequest(sReq,"xml");
	
	$.colorbox.close();
}

function OnCreateFile(){
	$("#TIPS-FOLDER-NAME").html(sLFileName);
	$("#CREATE-FOLDER-NOW-BTN").html(sLNewFolder);
	$.colorbox({
		inline : true,
		href : '#CREATE-FILE-DIV'
	});
}

function ReNameNow(){
	var sFileName = $("#RENAME-FOLDER-NAME").val();
	var sNewFileName = $("#RENAME-FOLDER-NAME-NEW").val();
	
	if (sFileName == sNewFileName){
		return;
	}
	
	var sReq = "FolderList.xml?action=renamefile&file=" + checkPath(GetCurrentFolder() + "//" + sFileName);
	sReq += '&newpath=' + checkPath(GetCurrentFolder() + "//" + sNewFileName);
	StartAjaxRequest(sReq,"xml");
	
	$.colorbox.close();
}


function OnRenameFile(){
	$("#TIPS-RE-NAME").html(sLFileName);
	$("#TIPS-RE-NAME-NEW").html(sLNewFileName);
	$("#RENAME-FOLDER-NOW-BTN").html(sLReNameFile);
	
	var check_obj = $("input:checked[name='FileCB']");
	
	if( check_obj.length > 0) {
		var sFileName = check_obj.get(0).value;
		//LOG4MB(sFileName);
		$("#RENAME-FOLDER-NAME").val(sFileName);
		$("#RENAME-FOLDER-NAME-NEW").val(sFileName);
	}
	else {
		ShowAlert(sLSelectFile);
		return;
	}
	
	$.colorbox({
		inline : true,
		href : '#RENAME-FILE-DIV'
	});
}

function OnRefreshContent(){
	g_iFileStartItem = 0;
	GetFileList(g_sCurrentFolder);
	
	$("#FileList").html("");
	
}

function GetCurrentFolder(){
	return g_sCurrentFolder;
}

function ShowUploadDiv(){
	if ($.browser.msie) {
		ShowAlert(sLNotRunOnIE);
		return;
	}
	
	$("#UPLOAD_INFO").html("");
	$("#UP-DROP-AREA").html(sLDragAndDrop);
	$.colorbox({
		inline : true,
		href : '#UPLOAD-FILE-DIV'
	});
}

function OnStorageInit() {
	$("#BTN-UPLOADFILE").html(sLUpload);
	$("#BTN-REFRESH").html(sLRefresh);
	$("#BTN-DELETE").html(sLDeleteFile);
	$("#BTN-CREATE").html(sLNewFolder);
	$("#BTN-RENAME").html(sLReNameFile);
		
	$("#FileList").html("");
	
	g_iFileTotalPage = 0;
	g_iFileStartItem = 0;

	g_sCurrentFolder = "/";
	GetRootFolder();
	
	$("#FolderPath").html("");

	$("#file-div").fadeIn(1000);
	
	if (!$.browser.msie) {
		InitDragAndDrop("UP-DROP-AREA");
	}
}

function FileUploadEventListener(evt,arg){
	if (evt == "FILE-SELECTED"){
		var name = arg.split(":")[0];
		var size = arg.split(":")[1];
		$("#UP-DROP-AREA").append('<p>' + name + ":" + size + '</p>');
		$("#UPLOAD_INFO").html(name + " : " + size);
	}
	else if (evt == "PROGRESS"){
		LOG4MB("PROGRESS:" + arg);
		$("#UPLOAD_INFO").html(arg + "%");
	}
	else if (evt == "COMPLETE"){
		$("#UPLOAD_INFO").html("100%");
		$("#UP-DROP-AREA").html(sDragAndDrop);
	}
	else if (evt == "FAILED"){
		$("#UPLOAD_INFO").html(arg + "FAILED");
	}
	else if (evt == "CANCELED"){
		$("#UPLOAD_INFO").html(arg + "CANCELED");
	}
	
	
}

function OnStorageClose() {
	$("#UPLOADFILE").html("");
	$("#pager").html("");
	$("#SubContent").html("");

	$("#CurrentFolderLinks").html("");

	$("#file-div").hide();

}

var StoragePageAdapter = new PageAdapter();
StoragePageAdapter.iPageIndex = FILE_PAGE_INDEX;
StoragePageAdapter.OnInit = function() {
	OnStorageInit();
};
StoragePageAdapter.OnClose = function() {
	OnStorageClose();
};
StoragePageAdapter.XmlParser = function(xml) {
	StorageXmlParser(xml);
};

PageFuncList.push(StoragePageAdapter);
