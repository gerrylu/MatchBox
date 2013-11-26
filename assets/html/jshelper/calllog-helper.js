var g_iCallTotalPage = 0;
var g_iCallStartItem = 0;
var CALL_ITEM_PER_PAGE = 15;

var CALL_TYPE_INCOMING = 0;
var CALL_TYPE_OUTGOING = 1;
var CALL_TYPE_MISSED = 2;

var sLTimeDay = "day";
var sLTimeDays = "days";
var sLTimeMinute = "minute";
var sLTimeMinutes = "minutes";
var sLTimeSecond = "second";
var sLTimeSeconds = "seconds";


function FormatDuration(iDuration) {
	var iDays = parseInt(iDuration / (60 * 60 * 24));
	if (iDays > 0) {
		iDuration -= (60 * 60 * 24) * iDays;
	}

	var iHours = parseInt(iDuration / (60 * 60));
	if (iHours > 0) {
		iDuration -= (60 * 60) * iDays;
	}

	var iMintues = parseInt(iDuration / 60);
	iDuration -= 60 * iMintues;

	var sDuration = "";
	if (iDays > 0) {
		if (iDays > 1) {
			sDuration += iDays + " " + sLTimeDays;
		} else {
			sDuration += iDays + " " + sLTimeDay;
		}
	}

	if (iMintues > 0) {
		if (iMintues > 1) {
			sDuration += iMintues + " " + sLTimeMinutes;
		} else {
			sDuration += iMintues + " " +  sLTimeMinute;
		}
	}

	if (iDuration > 0) {
		if (iDuration > 1) {
			sDuration += iDuration + " " +  sLTimeSeconds;
		} else {
			sDuration += iDuration + " " +  sLTimeSecond;
		}
	}

	return sDuration;
}

function CallLogXmlParser(htmlData) {
	var params = HtmlGetParameterList(htmlData);

	var RespType = HtmlGetParamsProperty(params, "RespType");
	LOG4MB("RespType:" + RespType);
	if (RespType == null) {
		var retCode = HtmlGetRetCode(htmlData);
		if (parseInt(retCode) == EA_RET_END_OF_FILE){
			return;
		}
		if (retCode != 0) {
			ShowAlert(ParseErrNo(retCode));
		}
		return;
	}
	
	if (RespType == "DeleteCallState"){
		var DeleteState = HtmlGetParamsProperty(params, "DeleteState");
		DeleteState = parseInt(DeleteState);
		if (DeleteState == 0){
			GetCallList();
			return;
		}
		//var CallID = HtmlGetParamsProperty(params, "DeleteID");
		
		ShowAlert(ParseErrNo(DeleteState));
		return;
	}

	if (RespType == "CallList") {
		var html = HtmlGetHtmlContent(htmlData);
		$("#call-list").html(html);
		
		$("#call-list").InitJscroll();

		var iTotalCount = HtmlGetParamsProperty(params, "CallTotalCount");
		
		GenCallPageLink(iTotalCount);
		
		return;
	}
}

function OnDeleteCall(id){
	var sReq = "CallLog.xml?action=deletecalllog&id=" + id;
	LOG4MB(sReq);
	
	StartAjaxRequest(sReq,"html");
}

function GetCallList() {
	var sReq = "CallLog.xml?action=getcallloglist&type=InComing";
	sReq += "&" + "from=" + g_iCallStartItem;
	sReq += "&to=" + (g_iCallStartItem + CALL_ITEM_PER_PAGE);
	LOG4MB(sReq);
	
	StartAjaxRequest(sReq,"html");
}

var CallPageNo = 0;
CallPageClick = function(pageclickednumber) {
	CallPageNo = pageclickednumber;
	// $("#pager").pager({ pagenumber: pageclickednumber, pagecount:
	// g_iCallTotalPage, buttonClickCallback: PageClick });
	g_iCallStartItem = (pageclickednumber - 1) * CALL_ITEM_PER_PAGE;

	GetCallList();

};

function GenCallPageLink(iTotalItemCount) {
	/*
	 * iTotalItemCount = 30; g_iCallStartItem = 1; iEndItem = 2;
	 */
	g_iCallTotalPage = parseInt(iTotalItemCount / CALL_ITEM_PER_PAGE);
	if (iTotalItemCount % CALL_ITEM_PER_PAGE > 0) {
		g_iCallTotalPage += 1;
	}

	$("#call-div").find("#pager").pager({
		pagenumber : CallPageNo,
		pagecount : g_iCallTotalPage,
		buttonClickCallback : CallPageClick
	});
}

function OnCallInit() {
	g_iCallTotalPage = 0;
	g_iCallStartItem = 0;
	CallPageNo = 0;

	GetCallList();
	
	$("#call-div").find("#pager").html("");
	$("#call-list").html("");

	$("#call-div").fadeIn(1000);
}

function OnCallClose() {
	
	$("#call-div").find("#pager").html("");

	$("#call-div").hide();
}

var callPageAdapter = new PageAdapter();
callPageAdapter.iPageIndex = CALL_PAGE_INDEX;
callPageAdapter.OnInit = function() {
	OnCallInit();
};
callPageAdapter.OnClose = function() {
	OnCallClose();
};
callPageAdapter.XmlParser = function(xml) {
	CallLogXmlParser(xml);
};

PageFuncList.push(callPageAdapter);
