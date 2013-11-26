var g_iOptionFrom = 0;
var g_iOptionCount = 0;
var MAX_Option_NAME_LEN = 10;

var g_iOptionCurrentStep = 0;

function GetID(name){
	var s = name.replace(/\./g,"");
	return s;
}

function GetOptionList(action) {
	if (g_iOptionCurrentStep < MAX_ITEM_PER_REQUEST) {
		++g_iOptionCurrentStep;
	}

	var iTo = g_iOptionFrom + g_iOptionCurrentStep;
	var sReq = "OptionList.xml?from=" + g_iOptionFrom + "&to=" + iTo;
	sReq += "&action=" + action;
	LOG4MB(sReq);
	StartAjaxRequest(sReq,"xml");
}

function OnClickRemvoeOption(sOptionName) {
	if(!confirm(sLConfirmeDelete + '<' + sOptionName +'>? ')){
		return;
	}
	
	var sReq = "OptionList.xml?action=removeOption&Optionname=" + sOptionName;
	StartAjaxRequest(sReq,"xml");
	
	var sID = "#" + GetID(sOptionName);
	$(sID).remove();
}

/*

 */
function OptionGenLink(sName, sPackageName, sVersionName) {
	var sSubName = decodeURIComponent(sName);
	sSubName = sSubName.sub(MAX_Option_NAME_LEN);
	var sLink = '<dl class="filelist" id="' + GetID(sPackageName) + '">';

	sLink += "<dt><img width='48' height='48' src=\"?action=getOptionicon&Optionname="
			+ sName + "\" title=\"" + decodeURIComponent(sPackageName) + " V" + decodeURIComponent(sVersionName) + "\"></dt>";
	
	sLink += "<dd>";
	sLink += sSubName;
	sLink += "</dd>";
	
	sLink += '<a href="#" class="aHasBk sprite sprite-delete" onclick="OnClickRemvoeOption(\'' + sPackageName + '\');\"></a>';

	sLink += '</dl>';

	return sLink;
}

function OptionXmlParser(xml) {
	g_iOptionCount = $(xml).find("OptionCount").text();
	
	$("#Option-list").InitJscroll();
	$(xml).find("Option").each(
			function(i) {
				var fLink = OptionGenLink($(this).find("Name").text(), $(this)
						.find("PackageName").text(), $(this)
						.find("VersionName").text());
				$("#Option-list").Optionend(fLink);
				++g_iOptionFrom;
			});

	if (g_iOptionFrom < g_iOptionCount) {
		g_iOptionFrom += 1;
		GetOptionList("getOptionlist");
		return;
	}
	
	$("#Option-list").Optionend('<div class="clear"></div>');
	$("#Option-list").tinyscrollbar();
}

function OnRefreshOption(){
	g_iOptionFrom = 0;
	g_iOptionCurrentStep = 0;
	GetOptionList("refreshOptionlist");
	
	$("#Option-list").html("");
}

function OnOptionInit() {
	g_iOptionFrom = 0;
	g_iOptionCurrentStep = 0;
	
	GetOptionList("getOptionlist");
	
	$("#Option-list").html("");
	
	$("#BTN-REFRESH-Option").html(sLRefresh);
		
	$("#option-div").fadeIn(1000);
	
}

function OnOptionClose() {
	$("#option-div").hide();

}

var optionPageAdapter = new PageAdapter();
optionPageAdapter.iPageIndex = OPTION_PAGE_INDEX;
optionPageAdapter.OnInit = function() {
	OnOptionInit();
};
optionPageAdapter.OnClose = function() {
	OnOptionClose();
};
optionPageAdapter.XmlParser = function(xml) {
	OptionXmlParser(xml);
};

PageFuncList.push(optionPageAdapter);
