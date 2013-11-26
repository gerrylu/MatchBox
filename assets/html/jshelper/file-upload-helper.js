var UpFileList = new Array();
var g_iCurFilePos = 0;

function OnFileSelected() {
	if (UpFileList.length > 1){
		alert("only support upload one file for now:)");
		return;
	}
	
	g_iCurFilePos = 0;
	var file = document.getElementById('file2Upload').files[0];
	UpFileList.push(file);
	fileSelected();
}

function fileSelected() {
	for ( var i = 0; i < UpFileList.length; ++i) {
		var file = UpFileList[i];
		var fileSize = 0;
		if (file.size > 1024 * 1024) {
			fileSize = (Math.round(file.size * 100 / (1024 * 1024)) / 100)
					.toString()
					+ 'MB';
		} else {
			fileSize = (Math.round(file.size * 100 / 1024) / 100).toString()
					+ 'KB';
		}

		FileUploadEventListener("FILE-SELECTED", file.name + ":" + fileSize);
	}
}


function uploadFile() {
	if ($.browser.msie) {
		ShowAlert(sLNotRunOnIE);
		return;
	}

	var fd = new FormData();
	fd.append("fileToUpload", UpFileList[g_iCurFilePos]);
	
	var path = checkPath(GetCurrentFolder() + "//" + UpFileList[g_iCurFilePos].name);
	var xhr = new XMLHttpRequest();
	try {
		xhr.upload.addEventListener("progress", _uploadProgress, false);
		xhr.addEventListener("load", _uploadComplete, false);
		xhr.addEventListener("error", _uploadFailed, false);
		xhr.addEventListener("abort", _uploadCanceled, false);
	
		xhr.open("POST", path);
		xhr.send(fd);
		
		xhr.onreadystatechange = function(){ 
			if (4 == xhr.readyState){
				var html = xhr.responseText;
				var ret = HtmlGetRetCode(xhr.responseText);
				if (ret > 0){
					$("#UPLOAD_INFO").html(ParseErrNo(ret));
					return;
				}
				
				var err = HtmlGetException(html);
				if (err != null){
					$("#UPLOAD_INFO").html(err);
				}
			} 
		};
			
	} catch (e) {
		$("#UPLOAD_INFO").html(e.toLocaleString());
	}
}

function _uploadProgress(evt) {
	if (evt.lengthComputable) {
		FileUploadEventListener("PROGRESS", Math.round(evt.loaded * 100
				/ evt.total));
		return;
	}

	FileUploadEventListener("PROGRESS", -1);
}

function _uploadComplete(evt) {
	FileUploadEventListener("COMPLETE", UpFileList[g_iCurFilePos].name);
	//if (g_iCurFilePos >= Files.length) 
	{
		UpFileList.length = 0;
		g_iCurFilePos = 0;
		return;
	}

	++g_iCurFilePos;
	uploadFile();
}

function _uploadFailed(evt) {
	FileUploadEventListener("FAILED", evt.target.responseText);
	LOG4MB("FAILED:" + evt.target.responseText);
	UpFileList.length = 0;
	//++g_iCurFilePos;
}

function _uploadCanceled(evt) {
	FileUploadEventListener("CANCELED", evt.target.responseText);
	UpFileList.length = 0;
	//g_iCurFilePos;
}

function OnFileDroped(evt) {
	LOG4MB("ok works");
	evt.stopPropagation();
	evt.preventDefault();
	var files = evt.dataTransfer.files; // FileList object.

	for ( var i = 0; i < files.length; ++i) {
		UpFileList.push(files[i]);
	}

	fileSelected();
}

function handleDragOver(evt) {
	evt.stopPropagation();
	evt.preventDefault();
	evt.dataTransfer.dropEffect = 'copy'; // Explicitly show this is a copy.
}

function InitDragAndDrop(dropArea) {
	// alert("initDrag:" + dropArea);
	var dropZone = document.getElementById(dropArea);
	dropZone.addEventListener('dragover', handleDragOver, false);
	dropZone.addEventListener('drop', OnFileDroped, false);

	// alert("initDrag done:" + dropArea);
}
