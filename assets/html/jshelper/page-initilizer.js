var EA_RET_OK = 0;
var EA_RET_FAILED = 1;
var EA_RET_FILE_EXIST = 2;
var EA_RET_FILE_NOT_EXIST = 3;
var EA_RET_AUTH_FAILED = 4;
var EA_RET_INVALID_SMS_CONTENT = 5;
var EA_RET_INVALID_PHONE_NO = 6;
var EA_RET_END_OF_FILE = 7;
var EA_RET_UNKONW_REQ = 8;
var EA_RET_INVALID_EMAIL = 9;
var EA_RET_BUSY_BACKUP = 10;
var EA_RET_ACCESS_DENINED = 11;
var EA_RET_QUERY_STATE_LATER = 12;
var EA_RET_SAVE_FILE_FAILED = 13;
var EA_RET_NEED_OP_ON_PHONE = 14;
var EA_RET_NO_EXTERNAL_STOREAGE = 15;

var SYS_PAGE_INDEX = 0;
var SMS_THREAD_PAGE_INDEX = 1;
var SMS_CHAT_PAGE_INDEX = 2;
var CONTACT_PAGE_INDEX = 3;
var CALL_PAGE_INDEX = 4;
var PHOTO_PAGE_INDEX = 5;
var VIDEO_PAGE_INDEX = 6;
var MUSIC_PAGE_INDEX = 7;
var FILE_PAGE_INDEX = 8;
var APP_PAGE_INDEX = 9;
var BACKUP_PAGE_INDEX = 10;
var OPTION_PAGE_INDEX = 11;


/*
 * function GetCurrentFileName() { var strUrl=window.location.href; var
 * arrUrl=strUrl.split("/"); var strPage=arrUrl[arrUrl.length-1]; return
 * strPage; }
 */

function OnChangeLang(langType) {
	SetFavLangType(langType);

	/*
	 * var sReq = "LangManager.xml?langtype=" + langType; StartAjaxRequest(sReq,
	 * "xml");
	 */

	InitLangString();
	InitTemplate();
	EnterPage(g_iPageIndex);

}

function SetLangLink() {
	var iLangType = GetLangType();
	var sHtml = "";
	for ( var i = 0; i < LangInfo.length; ++i) {
		if (i == iLangType) {
			// continue;
		}

		sHtml += '<li style="margin-right:10px">' + LangInfo[i][0]
				+ '<a href="#"  class="aHasBk sprite ' + LangInfo[i][2]
				+ '" onclick="OnChangeLang(' + i + ')"></a></li>';
	}

	$("#nav-lang").html(sHtml);
	// zvMenu("nav-lang", 0);// 下拉菜单
}

function SelectMainNav() {
	GenHeader();
}

function GenHeader() {
	var sHtml = "";

	var iPageIndex = g_iPageIndex;
	if (iPageIndex == 2) {
		iPageIndex = 1; // a patch for sms chat,not elegant but simple:)
	}

	
	for ( var i = 0; i < PageNav.length; ++i) {
		if (PageNav[i][1] == "") {
			continue;
		}
	
		sHtml += "<li>";
		sHtml += '<a href="#" ';
		var sImg = '<em class="aHasBk sprite sprite-';
		sImg += PageNav[i][0];
		sImg = sImg.replace("div","menu");
		
		if (iPageIndex == PageNav[i][2]) {
			sImg += '-sel';
			sHtml += 'class="current  button-transition" ';
		}else{
			sHtml += 'class="button-transition" ';
		}
		sImg += '"></em>';
		
		sHtml += "onclick=\"EnterPage(" + PageNav[i][2] + ");\">";
		sHtml += sImg;
		sHtml += "<span>" + PageNav[i][1] + "</span>";
		sHtml += "</a></li>"
	}
	
	$("#Main-Menu").html(sHtml);

}

function OnAbout(){
	$("#ABOUT-APP-NAME").html(sLAppName);
	$.colorbox({
		inline : true,
		href : '#ABOUT-DIV'
	});
}

function OnStartog(){
	g_iEnableDebug = 1;
	
	LOG4MB("Start Loging...");
}

function SetTitle() {
	document.title = sLAppName;

	for ( var i = 0; i < PageNav.length; ++i) {
		if (g_iPageIndex == PageNav[i][2]) {
			document.title = sLAppName + "-------" + PageNav[i][1];
			break;
		}
	}
}

function InitTemplate() {
	GenHeader();
	SetTitle();
}

function PageAdapter() {
	this.iPageIndex = 0;
	this.OnInit = null;
	this.OnClose = null;
	this.OnLangChange = null;
	this.XmlParser = null;
}

var PageFuncList = new Array();
var g_iPageIndex = 0;

function GetPageAdapter(pageIndex) {
	for ( var i = 0; i < PageFuncList.length; ++i) {
		var pageAdapter = PageFuncList[i];
		if (pageAdapter.iPageIndex == pageIndex) {
			return pageAdapter;
		}
	}

	return null;
}

function EnterPage(pageIndex) {
	LOG4MB("pageIndex=" + pageIndex);
	var nextPageAdapter = GetPageAdapter(pageIndex);
	if (nextPageAdapter == null) {
		ShowAlert("No page found!!! index is " + pageIndex);
		return;
	}

	if (g_iPageIndex != pageIndex) {
		var prePageAdapter = GetPageAdapter(g_iPageIndex);
		if (prePageAdapter != null) {
			if (prePageAdapter.OnClose != null) {
				prePageAdapter.OnClose();
			}
		}
	}

	g_iPageIndex = pageIndex;
	if (nextPageAdapter.OnInit != null) {
		nextPageAdapter.OnInit();
	}

	g_iCurPageAdapter = nextPageAdapter;

	SelectMainNav();

	SetTitle();
}

function HideSubDiv() {
	for ( var i = 0; i < PageNav.length; ++i) {
		$("#" + PageNav[i][0]).hide();
	}
}

/*width:1100px;height:650px;*/
function SetUIOffset(){
	var sh = getHeight();
	var	sw = getWidth();
	
	var uw = 1100;
	var uh = 650;
	
	if (sh > uh){
		var offset = (sh - uh)/2;
		$("#frame-div").css('margin-top',offset + "px");
	}else{
		$("#frame-div").css('margin-top',"0px");
	}
}

function OnInit() {
	InitLangString();

	HideSubDiv();
	
	g_iCurPageAdapter = GetPageAdapter(SYS_PAGE_INDEX);

	InitTemplate();
	SetUIOffset();
	
	$("#ENABLE-LOG-BTN").html(sLSendLog);
	
	EnterPage(g_iCurPageAdapter.iPageIndex);
}

$(document).ready(function() {
	OnInit();
});
