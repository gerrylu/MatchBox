var g_iAppFrom = 0;
var g_iAppCount = 0;
var MAX_APP_NAME_LEN = 10;

var g_iAppCurrentStep = 0;

function GetID(name){
	var s = name.replace(/\./g,"");
	return s;
}

function GetAppList(action) {
	if (g_iAppCurrentStep < MAX_ITEM_PER_REQUEST) {
		++g_iAppCurrentStep;
	}

	var iTo = g_iAppFrom + g_iAppCurrentStep;
	var sReq = "AppList.xml?from=" + g_iAppFrom + "&to=" + iTo;
	sReq += "&action=" + action;
	LOG4MB(sReq);
	StartAjaxRequest(sReq,"xml");
}

function OnClickRemvoeApp(sAppName) {
	if(!confirm(sLConfirmeDelete + '<' + sAppName +'>? ')){
		return;
	}
	
	var sReq = "AppList.xml?action=removeapp&appname=" + sAppName;
	StartAjaxRequest(sReq,"xml");
	
	var sID = "#" + GetID(sAppName);
	$(sID).remove();
}

/*

 */
function AppGenLink(sName, sPackageName, sVersionName) {
	var sSubName = decodeURIComponent(sName);
	sSubName = sSubName.sub(MAX_APP_NAME_LEN);
	var sLink = '<dl class="filelist" id="' + GetID(sPackageName) + '">';

	sLink += "<dt><img width='48' height='48' src=\"?action=getappicon&appname="
			+ sName + "\" title=\"" + decodeURIComponent(sPackageName) + " V" + decodeURIComponent(sVersionName) + "\"></dt>";
	
	sLink += "<dd>";
	sLink += sSubName;
	sLink += "</dd>";
	
	sLink += '<a href="#" class="aHasBk sprite sprite-delete" onclick="OnClickRemvoeApp(\'' + sPackageName + '\');\"></a>';

	sLink += '</dl>';

	return sLink;
}

function AppXmlParser(xml) {
	g_iAppCount = $(xml).find("AppCount").text();
	
	$(xml).find("App").each(
			function(i) {
				var fLink = AppGenLink($(this).find("Name").text(), $(this)
						.find("PackageName").text(), $(this)
						.find("VersionName").text());
				$("#app-list").append(fLink);
				++g_iAppFrom;
			});

	if (g_iAppFrom < g_iAppCount) {
		g_iAppFrom += 1;
		GetAppList("getapplist");
		return;
	}
	
	$("#app-list").InitJscroll();
}

function OnRefreshApp(){
	g_iAppFrom = 0;
	g_iAppCurrentStep = 0;
	GetAppList("refreshapplist");
	
	$("#app-list").html("");
}

function OnAppInit() {
	g_iAppFrom = 0;
	g_iAppCurrentStep = 0;
	
	GetAppList("getapplist");
	
	$("#app-list").html("");
	
	$("#BTN-REFRESH-APP").html(sLRefresh);
		
	$("#app-div").fadeIn(1000);
	
}

function OnAppClose() {
	$("#app-div").hide();

}

var appPageAdapter = new PageAdapter();
appPageAdapter.iPageIndex = APP_PAGE_INDEX;
appPageAdapter.OnInit = function() {
	OnAppInit();
};
appPageAdapter.OnClose = function() {
	OnAppClose();
};
appPageAdapter.XmlParser = function(xml) {
	AppXmlParser(xml);
};

PageFuncList.push(appPageAdapter);
