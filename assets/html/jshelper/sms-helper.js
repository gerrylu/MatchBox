
var MSG_TYPE_ALL = 0;
var MSG_TYPE_INBOX = 1;
var MSG_TYPE_SENT = 2;
var MSG_TYPE_DRAFT = 3;
var MSG_TYPE_OUTBOX = 4;
var MSG_TYPE_FAILED = 5;
var MSG_TYPE_QUEUED = 6;

// for thread list
var g_iThreadFrom = 0;
var g_iTCurrentStep = 0;

var g_iThreadTotalPage = 0;
var THREAD_ITEM_PER_PAGE = 10;
var MAX_ITEM_PER_REQUEST = 10;

var MAX_SMS_CONTENT_LEN = 50;

function OnShowChat(threadid){
	SetThreadID(threadid);
	EnterPage(SMS_CHAT_PAGE_INDEX);//sms-chat page	
}



function GetThreadList() {
	if (g_iTCurrentStep < MAX_ITEM_PER_REQUEST) {
		++g_iTCurrentStep;
	}

	var sReq = "SmsList.xml?from=" + g_iThreadFrom;// + '&to=' + iTo;
	sReq += '&action=getthreadlist';

	StartAjaxRequest(sReq,"html");
}

function OnDeleteThread(threadID){
	var sReq = "SmsList.xml?action=deletesms&threadid=" + threadID;
	StartAjaxRequest(sReq,"html");
}

function TrimSmsContent(){
	$(".pseudo-SmsContent").each(function (index,domEle){
		var content = $(domEle).html();
		if (content == null){
			return;
		}
		
		content = content.sub(MAX_SMS_CONTENT_LEN);
		$(domEle).html(content);
		
	} );  
}

/*
 * var THREAD_PER_REQUEST = 20; var iThreadFrom = 0; var iThreadCount = 0;
 */
function SmsThreadListParser(xml) {
	var params = HtmlGetParameterList(xml);
	var RespType = HtmlGetParamsProperty(params,"RespType");
	
	if (HtmlGetParamsProperty(params,"RespType") == "ThreadList"){
		var htmlData = HtmlGetHtmlContent(xml);
		$("#ThreadListDiv").html(htmlData);
		
		$("#ThreadListDiv").InitJscroll();
		TrimSmsContent();
		
		var threadCount = HtmlGetParamsProperty(params,"ThreadCount");
		threadCount = parseInt(threadCount);
		if (threadCount == 1){
			GetThreadList();
		}
		
		var totalCount = HtmlGetParamsProperty(params,"ThreadTotalCount");
		GenThreadPageLink(totalCount);
	}
	else if (RespType == "DeleteSmsState")
	{
		var DeleteState = HtmlGetParamsProperty(params, "DeleteState");
		DeleteState = parseInt(DeleteState);
		if (DeleteState == 0){
			//remove sms here
			//var sID = HtmlGetParamsProperty(params, "threadid");
			//remove thread on the page
			//ShowAlert("Operation Done");
			GetThreadList();
			return;
		}
		//var CallID = HtmlGetParamsProperty(params, "DeleteID");
		
		ShowAlert(ParseErrNo(DeleteState));
		return;
	}
}

var ThreadPageNo = 0;
ThreadPageClick = function(pageclickednumber) {
	ThreadPageNo = pageclickednumber;
	// $("#pager").pager({ pagenumber: pageclickednumber, pagecount:
	// g_iCallTotalPage, buttonClickCallback: PageClick });
	g_iThreadFrom = (pageclickednumber - 1) * THREAD_ITEM_PER_PAGE;

	GetThreadList();	
};

function GenThreadPageLink(iTotalItemCount) {
	g_iThreadTotalPage = parseInt(iTotalItemCount / THREAD_ITEM_PER_PAGE);
	if (iTotalItemCount % THREAD_ITEM_PER_PAGE > 0) {
		g_iThreadTotalPage += 1;
	}

	$("#sms-thread-div").find("#pager").pager({
		pagenumber : ThreadPageNo,
		pagecount : g_iThreadTotalPage,
		buttonClickCallback : ThreadPageClick
	});
}


function OnSmsInit() {
	// for thread list
	g_iThreadFrom = 0;

	// for converstation list
	g_iMsgFrom = 0;
	g_iMsgCount = 0;
	g_iThreadID = 0;
	g_iMsgReceived = 0;
	g_iMsgCurrentStep = 0;
	
	ThreadPageNo = 0;

	g_iContactCount = 0;
	
	$("#NEW-SMS-BTN").html(sLNewSMS);
		
	$("#ThreadListDiv").html("");
	GetThreadList();

	$("#sms-thread-div").fadeIn(1000);
	
	GenThreadPageLink(10);
}

function OnSmsClose() {
	$("#sms-thread-div").hide();
}


var smsPageAdapter = new PageAdapter();
smsPageAdapter.iPageIndex = SMS_THREAD_PAGE_INDEX;
smsPageAdapter.OnInit = function() {
	OnSmsInit();
};
smsPageAdapter.OnClose = function() {
	OnSmsClose();
};
smsPageAdapter.XmlParser = function(xml) {
	SmsThreadListParser(xml);
};

PageFuncList.push(smsPageAdapter);




