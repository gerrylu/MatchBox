//for converstation list
var g_iMsgFrom = 0;
var g_iMsgCount = 0;
var g_iThreadID = 0;
var g_iMsgReceived = 0;
var g_iMsgCurrentStep = 0;

var g_iContactCount = 0;

var SMS_QUERY_INTERVAL = 500;
var MAX_CHAT_ITEM_ON_PAGE = 100;

function SetThreadID(threadid) {
	g_iThreadID = threadid;
	// alert(g_iThreadID);
}

function GetConversation() {
	var sReq = "SmsList.xml?action=getchat&threadid=" + g_iThreadID + "&from="	+ g_iMsgFrom;
	// alert(sReq);
	StartAjaxRequest(sReq, "html");
}


function GetPhoneNo(phoneEntry) {
	if (phoneEntry == null || phoneEntry == ""){
		return "";
	}
	
	if (phoneEntry.indexOf("<") >= 0){
		phoneEntry = phoneEntry.split("<")[1];	
	}
	
	if (phoneEntry.indexOf(">") >= 0){
		phoneEntry = phoneEntry.split(">")[0];	
	}
	
	if (phoneEntry == null || phoneEntry == ""){
		return "";
	}
		
	var phoneNo = "";
	for (var i = 0; i < phoneEntry.length; ++i){
		var c = phoneEntry.charAt(i);
		if (isNaN(c)){
			continue;
		}
		
		phoneNo += c;
	}
	
	if (isNaN(phoneNo)){
		return "";
	}
	
	return phoneNo;
}


var ckSmsStatusTask = new EATask();
ckSmsStatusTask.iTimerInterval = 2000;

function OnEaTimer(){
	var sReq = "SmsList.xml?action=getsendstate";
	StartAjaxRequest(sReq, "html",OnSmsState,OnQueryFailed);
}

//循环查询短信发送状态
var g_iEAQueryCount = 0;
function OnSmsState(data){
	var params = HtmlGetParameterList(data);
	var SendState = HtmlGetParamsProperty(params, "SendState");
	var idList = [];
	//idList.length = 0;
	
	if ( ('unkonw' == SendState) || (SendState == null) ){
		g_iEAQueryCount += 1;
		if (g_iEAQueryCount > 10){
			g_iEAQueryCount = 0;
			ShowAlert(sLSmsQueryFailed);
			ckSmsStatusTask.stop();
			clearSendStateOnPage();
		}
	}
	
	var stateAry = SendState.split("#");
	for ( var i = 0; i < stateAry.length; ++i) {
		if (stateAry[i].length < 1){
			continue;
		}
		
		var sID = stateAry[i].split("=")[0];
		if (sID == null || sID.length < 1) {
			continue;
		}
		
		idList.push( GetPhoneNo(sID) );
	}	
			
	if (ckSmsStateOnPage(idList)){
		return;
	}
	
	ckSmsStatusTask.stop();
}

function OnQueryFailed(data){
	ckSmsStatusTask.stop();
	clearSendStateOnPage();
	
	LOG4MB("OnQueryFailed");
}

function clearSendStateOnPage(){
	var objs = $("#SMS_CHAT_LIST").find('span');
	for ( var j = 0; j < objs.length; ++j) {
		var sID = objs.eq(j).attr('id');
		
		if (sID == null) {
			continue;
		}
		
		if ($("#"+sID).find('img') && $("#"+sID).find('img').html() != null){
			$("#"+sID).html("");
		}
	}
}

function ckSmsStateOnPage(idList){
	var needQueryAgain = false;
	var objs = $("#SMS_CHAT_LIST").find('span');
	for ( var j = 0; j < objs.length; ++j) {
		var sID = objs.eq(j).attr('id');
		
		if (sID == null) {
			continue;
		}
		
		for (var pos = 0; pos < idList.length;++pos){
			//这里使用indexOf进行判断主要是由于在回复短信的时候没有phone No
			if (idList[pos] != null  && idList[pos].indexOf(sID) >= 0){
				$("#"+sID).html("");
			}
		}
		
		if ($("#"+sID).find('img') && $("#"+sID).find('img').html() != null){
			needQueryAgain = true;
		}
	}
	
	$("#SMS_CHAT_LIST").tinyscrollbar_update('bottom');
	return needQueryAgain;
}

function PreProcessReceiver(recverList){
	var sPhones = recverList.split(",");
	var sList = "";
	for ( var i = 0; i < sPhones.length; ++i) {
		var phoneEntry = sPhones[i];
		if (phoneEntry == null || phoneEntry.length < 1){
			continue;
		}
		
		sList += GetPhoneNo(phoneEntry);
		sList += ",";
	}
	return sList;
}

function OnSendSms() {
	var bIsReply = true;
	if (g_iThreadID == 0){
		bIsReply = false;
	}
	
	var sContent = $("#SmsEditorContent").attr("value");
	
	var sReceiver = "";
	if (!bIsReply){
		sReceiver = GetSelectedContact();
		if ((sReceiver == null || sReceiver.length < 1) && (g_iThreadID == 0)) {
			ShowAlert(sLRecvEmpty);
			return;
		}
	}
	
	var sTimeStamp = GenID();
	
	var sReq = "SmsList.xml?action=sendsms&receiver="
			+ encodeURIComponent(PreProcessReceiver(sReceiver)) + "&threadid=" + g_iThreadID
			+ "&content=" + encodeURIComponent(sContent)
			+ "&SmsTimestamp=" + sTimeStamp;

	StartAjaxRequest(encodeURI(sReq), "html");

	var myDate = new Date().toLocaleString();
	
	
	if (!bIsReply){
		var sPhones = sReceiver.split(",");

		for ( var i = 0; i < sPhones.length; ++i) {
			var phoneEntry = sPhones[i];
			if (phoneEntry == null || phoneEntry.length < 1){
				continue;
			}
			var sHtml = "";//"<div>";
			sHtml += "<dl class='chatlist'>";
			sHtml += "<dd class='send-content f_left'>";
			sHtml += "<div class='t_top'>";
			sHtml += "<em>" + myDate.toLocaleString() + "</em>";
			sHtml += "<span	id='" + GetPhoneNo(phoneEntry) + sTimeStamp + "'>--" + phoneEntry
					+ "<img src='img/sms-loading.gif' /></span>";
			sHtml += "</div>";
			sHtml += '<p class="t_bottom">' + sContent + '</p>';
			sHtml += '</dd></dl>';
			sHtml += '<div class="clear"></div>';
			//sHtml += "</div>";
			
			$("#SMS_CHAT_LIST").RemoveJscroll();
			$("#SMS_CHAT_LIST").append(sHtml);
		
			$("#SMS_CHAT_LIST").InitJscroll();
			$("#SMS_CHAT_LIST").tinyscrollbar_update('bottom');
		}
	}
	else {
		var sHtml = "";//"<div>";
		sHtml += "<dl class='chatlist'>";
		sHtml += "<dd class='send-content f_left'>";
		sHtml += "<div class='t_top'>";
		sHtml += "<em>" + myDate.toLocaleString() + "</em>";
		sHtml += "<span	id='" + sTimeStamp + "'>--" + "<img src='img/sms-loading.gif' /></span>";
		sHtml += "</div>";
		sHtml += '<p class="t_bottom">' + sContent + '</p>';
		sHtml += '</dd></dl>';
		sHtml += '<div class="clear"></div>';
		
		$("#SMS_CHAT_LIST").RemoveJscroll();
		$("#SMS_CHAT_LIST").append(sHtml);
		
		$("#SMS_CHAT_LIST").InitJscroll();
		$("#SMS_CHAT_LIST").tinyscrollbar_update('bottom');
	}
}


function OnDeleteSms(smsid) {
	var sReq = "SmsList.xml?action=deletesms&id=" + smsid;

	StartAjaxRequest(sReq, "html");
}

function GetContactList() {
	var sReq = "ContactList.xml";
	StartAjaxRequest(sReq, "html");
}

function GetSelectedContact() {
	var sContactList = "";
	var objs = $("#SELECTED-CONTACT-AREA").children();
	for ( var j = 0; j < objs.length; ++j) {
		var sPhone = objs.eq(j).find('a').attr('id');
		if (sPhone == null) {
			continue;
		}
		sContactList += ",";
		sContactList += sPhone;
	}
	
	return sContactList;
}

function GetName(c) {
	var n = c.replace('<', '');
	n = n.replace('>', '');
	n = n.replace('+', '');
	n = n.replace(' ', '');
	return n;
}

function IsExist(phoneNo) {
	var objs = $("#SELECTED-CONTACT-AREA").children();
	for ( var j = 0; j < objs.length; ++j) {
		var sPhone = objs.eq(j).find('a').attr('id');
		if (sPhone == phoneNo) {
			alert(sPhone + ":" + phoneNo);
			return true;
		}
	}

	return false;
}


function OnAutoContactSelected(c) {
	
	var sHtml = GenContactItem4List(c);

	$("#SELECTED_CONTACT_LIST").append(sHtml);
}

function PrepareAutoComplete() {
	var dataArray = [];

	var objs = $("#CONTACTS_LIST").children();

	for ( var pos = 0; pos < objs.length; ++pos) {
		var name = "";
		var phone = "";

		// get contact list from dom
		var sContact = objs.eq(pos).find('label').html();
		if (sContact == null) {
			continue;
		}
		
		sContact = sContact.unescapeHtmlChar();

		name = GetName(sContact);
		var pinyinAry = makePinyin(name);
		if (!pinyinAry || pinyinAry.length < 1) {
			continue;
		}

		var sDataSrc = "";
		if (pinyinAry[0] == name) {
			// English
			sDataSrc = sContact;
		} else {
			// Chinese
			for ( var i = 0; i < pinyinAry.length; ++i) {
				sDataSrc = pinyinAry[i] + '---' + sContact;
			}
		}

		dataArray.push(sDataSrc);
	}

	$("#SMS-RECV-LIST").autocomplete({source: dataArray,select: function( event, ui ) {
                OnAutoContactSelected( ui.item.value );
                return false;
            }});
}

function OnClickReadMore(iFrom){
	g_iMsgReceived = 0;
	g_iMsgCurrentStep = 0;
	g_iMsgFrom = iFrom;
	LOG4MB("OnClickReadMore: " + iFrom);
	GetConversation();
	
	$("#SMS_CHAT_LIST").html("");
}

function SmsChatParser(htmlData) {
	var params = HtmlGetParameterList(htmlData);

	var RespType = HtmlGetParamsProperty(params, "RespType");
	// alert("RespType:" + RespType);
	if (RespType == null) {
		var retCode = HtmlGetRetCode(htmlData);
		if (retCode == EA_RET_QUERY_STATE_LATER) {
			ckSmsStatusTask.start(OnEaTimer);
			g_iEAQueryCount = 0
			return;
		}
		ShowAlert(ParseErrNo(retCode));
		return;
	}

	if (RespType == "DeleteSmsState") {
		var DeleteState = HtmlGetParamsProperty(params, "DeleteState");
		DeleteState = parseInt(DeleteState);
		if (DeleteState == 0) {
			// remove sms here
			// var sID = HtmlGetParamsProperty(params, "id");
			return;
		}
		
		ShowAlert(ParseErrNo(DeleteState));
		return;
	}

	if (RespType == "ChatList") {
		var html = HtmlGetHtmlContent(htmlData);
		
		$("#SMS_CHAT_LIST").RemoveJscroll();
		$("#SMS_CHAT_LIST").prepend(html);
		
		$("#SMS_CHAT_LIST").InitJscroll();
		$("#SMS_CHAT_LIST").tinyscrollbar_update('bottom');
		var iMsgCount = HtmlGetParamsProperty(params, "MsgCount");
		g_iMsgReceived += parseInt(iMsgCount);
		g_iMsgFrom += parseInt(iMsgCount);
		var iTotalMsgCount = parseInt(HtmlGetParamsProperty(params,"TotalMsgCount"));
		
		
		if (g_iMsgFrom < iTotalMsgCount && iTotalMsgCount < 500) {
			GetConversation();
			return;
		}

		return;
	}

	if (RespType == "ContactList") {
		
		var html = HtmlGetHtmlContent(htmlData);
		$("#CONTACTS_LIST").html(html);

		PrepareAutoComplete();
		$("#CONTACTS_LIST").InitJscroll();

		return;
	}
}

function GenContactItem4List(cItem){
	/*
		<dl class="contact_item">
				<dt><a href="#" onclick="OnRemoveContactFromSelList(0);"><img src="img/remove-contact.png"/></a>Topsun +8613649999999</dt>
			</dl>
	*/
	var sID = GenID();
	
	var sHtml = '<dl class="contact_item" id="' + sID + '">';
	sHtml += '<dt>';
	sHtml += '<a href="#" onclick="OnRemoveContactFromSelList(\'' + sID + '\');">';
	sHtml += '<img src="img/remove-contact.png"/>';
	sHtml += '</a>' + '<span  class="pesudo-contact_item">' + cItem + '</span>';
	sHtml += '</dt></dl>';
	return sHtml;
}


function OnAddContact2SelList(cItem){
	var sHtml = GenContactItem4List(cItem);
	$("#SELECTED_CONTACT_LIST").append(sHtml);
}

function OnRemoveContactFromSelList(cID){
	$("#SELECTED_CONTACT_LIST").children("#" + cID).remove();

}

function OnRemoveContactCard(cID) {
	$("#SELECTED-CONTACT-AREA").children("#" + cID).remove();
}

function OnInputPhoneNo(){
	var sVal = $("#SMS-RECV-LIST").val();
	var sPhone = GetPhoneNo(sVal);
	if (sPhone == ""){
		return;
	}
	
	var sHtml = GenContactItem4List(sPhone,true);
	$("#SELECTED_CONTACT_LIST").append(sHtml);
	
	$("#SMS-RECV-LIST").val("");
}

function OnSelectContactDone(){
	$.colorbox.close();
	
	var contactList = $("#SELECTED_CONTACT_LIST").find('.pesudo-contact_item');

	for ( var i = 0; i < contactList.length; i++) {
		var span = contactList.eq(i);
		var c = span.html();
		var sID = GenID();
		var sHtml = '<div class="contact_card" id="' + sID + '">';
		sHtml += '<a href="#" class="aHasBk sprite sprite-delete" onclick="OnRemoveContactCard(\'' + sID + '\');" >';
		sHtml += '<img src="img/remove-contact.png"/></a>'
		sHtml += c;
		sHtml += '</div>';
	
		$("#SELECTED-CONTACT-AREA").append(sHtml);
	}
}

function OnSelectContact(){
	GetContactList();
	$.colorbox({
		inline : true,
		href:'#CONTACTS-SEL-DLG'
	});
}

function OnBack2SmsThreadPage(){
	EnterPage(SMS_THREAD_PAGE_INDEX);
}

function OnChatInit() {
	// for conversation list
	g_iMsgFrom = 0;
	// g_iThreadID = 0;
	g_iMsgReceived = 0;
	g_iMsgCurrentStep = 0;

	g_iContactCount = 0;
	
	$("#SmsEditorContent").attr("value","");

	$("#BTN-BACK-TO-SMS").html(sLBack);
	//$("#ContactTitle").html(sLPageNavCONTACT);
	$("#SEND-SMS-BTN").html(sLSend);
	$("#SmsSubPageName").html(sLNewSMS);
	$("#BTN_SELECT_CONTACT").html(sLOK);
	$("#BTN_CONTACT_INPUT_ENTER").html(sLOK);
	$("#SELECTED-CONTACT-AREA").html("");
	$("#SELECTED_CONTACT_LIST").html("");
	if (g_iThreadID == 0) {
		$("#SmsSubPageName").html(sLContent);
		$("#SMS-RECV-SELECTED-LIST").show();
	}	
	else {
		$("#SMS-RECV-SELECTED-LIST").hide();
		GetConversation();
		$("#SmsSubPageName").html(sLContent);
	}

	$("#SMS_CHAT_LIST").html("");
	$("#sms-chat-div").fadeIn(1000);
}

function OnChatClose() {
	$("#SMS_CHAT_LIST").html("");
	$("#sms-chat-div").hide();
}

var chatPageAdapter = new PageAdapter();
chatPageAdapter.iPageIndex = SMS_CHAT_PAGE_INDEX;
chatPageAdapter.OnInit = function() {
	OnChatInit();
};
chatPageAdapter.OnClose = function() {
	OnChatClose();
};
chatPageAdapter.XmlParser = function(xml) {
	SmsChatParser(xml);
};

PageFuncList.push(chatPageAdapter);
