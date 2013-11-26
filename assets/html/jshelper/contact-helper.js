var g_iContactFrom = 0;
var g_iContactCount = 0;
var MAX_CONTACT_NAME_LEN = 10;

var CD_OP_TYPE_ADD = 0;
var CD_OP_TYPE_UPDATE = 1;
var g_iCDOpType = CD_OP_TYPE_ADD;

var g_iContactCurrentStep = 0;

var g_ContactID = 0;

var AddrCtrlTAGs = [ "street:", "city:", "region:"/*, "postCode:", "formatAddress:"*/ ];

function GetContactListXML() {
	if (g_iContactCurrentStep < MAX_ITEM_PER_REQUEST) {
		++g_iContactCurrentStep;
	}

	var iTo = g_iContactFrom + g_iContactCurrentStep;

	var sReq = "ContactList.xml?action=GetContactListXml&from="
			+ g_iContactFrom + "&to=" + iTo;
	LOG4MB(sReq);
	StartAjaxRequest(sReq, "xml");

}

function OnShowContactDetail(cID) {
	var sReq = "ContactList.xml?action=GetContactDetail&id=" + cID;
	LOG4MB(sReq);
	StartAjaxRequest(sReq, "xml");

}

function OnDeleteContact() {
	if (!confirm(sLConfirmeDelete)) {
		return;
	}
	
	var check_obj = $("input:checked[name='Contact-Item-CB']");
	var sContactIDList = "";

	for ( var i = 0; i < check_obj.length; i++) {
		sContactIDList += ",";
		sContactIDList += check_obj.get(i).value;
		// check_obj.get(i).checked = false;
	}
	
	if (sContactIDList == ""){
		return;
	}
	
	var sReq = "ContactList.xml?action=DeleteContact&id=" + sContactIDList;
	StartAjaxRequest(sReq, "xml");
}

function OnAddContact() {
	g_iCDOpType = CD_OP_TYPE_ADD;
	ShowContactDetailDlg("");

}

function OnUpdateContact() {
	var sReq = "ContactList.xml?action=UpdateContactDetail&appname="
			+ sPackageName;
	StartAjaxRequest(sReq, "xml");
}

function OnSendSmsFromContact() {

}

function OnRemoveContactItem(id){
	$("#" + id).remove();
}


function OnAddContactItem(type){
	var sHtml = "";
	var id = GenID();
	if (type == 'phone'){
		sHtml += "<p id='" + id + "'>"; 
		sHtml += "<select class='p-CD-PHONE-TYPE'>";
		for (var oppos = 0; oppos < phoneOptionList.length; ++oppos){
			sHtml += '<option value=\"' + phoneOptionList[oppos][0] + '"';
			sHtml = sHtml + '">' + phoneOptionList[oppos][1];
			sHtml += '</option>';
		}
		sHtml += '</select>';
		
		sHtml += '<input  class="p-CD-PHONE-NO"  type="text" />';
		sHtml += '<a href="#"  class="aHasBk sprite sprite-delete-item" onclick="OnRemoveContactItem(\'' + id + '\');"></a> </p>';
		
		$("#CD-PHONE-LIST").append(sHtml);
		return id;
	}
	
	if (type == "email"){
		sHtml += "<p id='" + id + "'>";
						
		sHtml += "<select  class='p-CD-EMAIL-TYPE'>";	
		for (var oppos = 0; oppos < emailOptionList.length; ++oppos){
			sHtml += '<option value=\"' + emailOptionList[oppos][0] + '">';;
			sHtml +=  emailOptionList[oppos][1];
			sHtml += '</option>';
		}
		sHtml += '</select>';
		
		sHtml += '<input  class="p-CD-EMAIL-VAL" type="text" />';
		sHtml += '<a href="#" class="aHasBk sprite sprite-delete-item" onclick="OnRemoveContactItem(\'' + id + '\');"></a> </p>';
		
		$("#CD-EMAIL-LIST").append(sHtml);
		return id;
	}
	
	if (type == "addr"){
		sHtml += "<p id='" + id + "'>";
		for ( var addrPos = 0; addrPos < AddrCtrlTAGs.length; ++addrPos) {
			sHtml += "<h5>" + addrTags[addrPos] + '<input id="CD-' + AddrCtrlTAGs[addrPos].replace(":", "") + '" type="text"/>';
			sHtml += "</h5>";
		}
		sHtml += "</p>";
		
		$("#CD-ADDR-LIST").append(sHtml);
		
		return id;
	}
	
	$.colorbox.resize();
	
	return 0;
}

/*
 * Pls refer to #CONTACT-DETAIL-DIV for html structure
 */
/*
 * <ContactDetail> <ID>0</ID> <name>gerry</name> <phonelist> <phone>186662936</phone>
 * <phone>07552677</phone> </phonelist> <EmailList> <email>186662936</email>
 * <email>07552677</email> </EmailList> <AddrList> <addr>186662936</addr>
 * <addr>07552677</addr> </AddrList> </ContactDetail>
 */
 
function ShowContactDetailDlg(xml) {
	g_ContactID = $(xml).find("ID").text();
	var sName = $(xml).find("name").text();
	
	$("#CD-NAME-LABEL")
			.html(
					sLContactLabelName + '<input id="CD-NAME-VAL" type="text" value="' + sName
							+ '"/>');
	$("#CD-PHONE-LABEL")
			.html(
					sLContactLabelPhone
							+ '<a href="#"  class="aHasBk sprite sprite-add-item" onclick="OnAddContactItem(\'phone\');"></a>');
	$("#CD-EMAIL-LABEL")
			.html(
					sLContactLabelEmail
							+ '<a href="#"   class="aHasBk sprite sprite-add-item" onclick="OnAddContactItem(\'email\');"></a>');
	$("#CD-ADDR-LABEL").html(sLContactLabelAddr);

	$("#CD-PHONE-LIST").html("");
	$("#CD-EMAIL-LIST").html("");
	$("#CD-ADDR-LIST").html("");
	
	if (g_iCDOpType != CD_OP_TYPE_ADD){
		$("#CD-INSERT-CONTACT-BTN").text(sLUpdateContact);
	}
	else {
		$("#CD-INSERT-CONTACT-BTN").text(sLAddContact);
	}
	
	var id = 0;
	
	$(xml).find("phone").each(function(i) {
						var phoneTxt = $(this).text();
						if (phoneTxt == null){
							return;
						}
						var phoneItem = phoneTxt.split(":");
						var phoneType = phoneItem[0];
						var phoneNo = phoneItem[1];
				
						id = OnAddContactItem('phone');
						$('#' + id).find('.p-CD-PHONE-TYPE').val(phoneType);
						$('#' + id).find('.p-CD-PHONE-NO').val(phoneNo);
					});
	if (id == 0){
		OnAddContactItem('phone');
	}	
	
	$(xml).find("email").each(
					function(i) {
						var etxt = $(this).text();
						if (etxt == null){
							return;
						}
						var emailItem = etxt.split(":");
						var emailType = emailItem[0];
						var email = emailItem[1];
					
						id = OnAddContactItem('email');
						$('#' + id).find('.p-CD-EMAIL-TYPE').val(emailType);
						$('#' + id).find('.p-CD-EMAIL-VAL').val(email);
					});

	OnAddContactItem('addr');
	$(xml).find("addr").each(
			function(i) {
				var addrtxt = $(this).text();
				if (addrtxt == null){
					return;
				}
				
				var addrItem = addrtxt.split(":");
				var addrType = addrItem[0];
				var addr = addrItem[1];
				
				$("#CD-" + addrType).val(addr);
			});

	$.colorbox( {
		inline : true,
		href : '#CONTACT-DETAIL-DIV'
	});
}

function OnClickAddContact(){
	var sName = $("#CD-NAME-VAL").val();
	if (sName == ""){
		alert(sLContactNoName);
		return;
	}
	
	//phones
	var phonelist = "";
	var phonetypelist = $("#CONTACT-DETAIL-DIV").find('.p-CD-PHONE-TYPE');
	var phonenolist = $('#CONTACT-DETAIL-DIV').find('.p-CD-PHONE-NO');
	for (var i = 0; i < phonetypelist.length; ++i){
		if (phonenolist.get(i).value == ""){
			continue;
		}
		
		phonelist += phonetypelist.get(i).value + ":";
		phonelist += phonenolist.get(i).value + ValSeparator;
	}
	
	var emaillist = "";
	var emailtypelist = $('#CONTACT-DETAIL-DIV').find('.p-CD-EMAIL-TYPE');
	var emailvallist = $('#CONTACT-DETAIL-DIV').find('.p-CD-EMAIL-VAL');
	for (var i = 0; i < emailtypelist.length; ++i){
		emaillist += emailtypelist.get(i).value + ":";
		emaillist += emailvallist.get(i).value + ValSeparator;
	}
	
	var addrlist = "";
	
	for ( var addrPos = 0; addrPos < AddrCtrlTAGs.length; ++addrPos) {
		var InputName = "#CD-" + AddrCtrlTAGs[addrPos].replace(":", "");
		var val = $(InputName).val();
		if (null != val && "" != val){
			addrlist += AddrCtrlTAGs[addrPos] + val;
			addrlist += ValSeparator;
		}
	}
	
	/*
	cmail
	cname
	cphone;
	cAddr;
	*/
	var action = "InsertContact";
	if (g_iCDOpType != CD_OP_TYPE_ADD){
		action = "UpdateContact";
	}
	var sReq = "ContactList.xml?action=" + action + "&cname="
			+ encodeURIComponent(sName) + "&cmail=" + encodeURIComponent(emaillist);
	sReq += "'&cphone=" + encodeURIComponent(phonelist) + "&cAddr=" + encodeURIComponent(addrlist) + "";
	sReq += "&id=" + g_ContactID;
	LOG4MB(sReq);
	StartAjaxRequest(sReq, "xml");
	
	$.colorbox.close();
}


function GenContactItem(sID, sName, sNumber) {
	var sContactItem = '<dl class="contactlist">';
	sContactItem += '<a href="#"  class="aHasBk sprite sprite-contact" onclick="OnShowContactDetail(\'' + sID + '\');"></a>';
	sContactItem += '<dd>';
	sContactItem += sName + "<" + sNumber + '>' + '</dd>';
	
	sContactItem += '<dd>';
	sContactItem += '<input type="checkbox" name="Contact-Item-CB" value="' + sID
			+ '" />';
	sContactItem += '</dd>';
	sContactItem += '</dl>';

	return sContactItem;
}

function ContactXmlParser(xml) {
	if ($(xml).find("ContactList").text().length > 2) {
		g_iContactCount = parseInt($(xml).find("TotalCount").text());
		
		$(xml).find("Contact").each(
				function(i) {
					var fItem = GenContactItem($(this).find("ID").text(), $(
							this).find("Name").text(), $(this).find("Number").text());
					//LOG4MB(fItem);
					$("#MAIN_CONTACT_LIST_DIV").append(fItem);
					g_iContactFrom++;
				});

		if (g_iContactFrom < g_iContactCount) {
			GetContactListXML();
		}

		$("#MAIN_CONTACT_LIST_DIV").InitJscroll();
		return;
	}
	
	if ($(xml).find("ContactDetail").text().length > 2) {
		g_iCDOpType = CD_OP_TYPE_UPDATE;
		ShowContactDetailDlg(xml);
	}
	
	if ($(xml).find("Refresh").text().length > 1) {
		RefreshContact();
		return;
	} 
}

function RefreshContact(){
	$("#MAIN_CONTACT_LIST_DIV").html("");
	g_iContactCurrentStep = 0;
	g_iContactFrom = 0;
	GetContactListXML();
}

function OnContactInit() {
	g_iContactFrom = 0;
	g_iContactCurrentStep = 0;

	g_ContactID = 0;

	$("#MAIN_CONTACT_LIST_DIV").html("");
	
	$("#BTN_DELETE_CONTACT").html(sLDeleteContact);
	$("#BTN_ADD_CONTACT").html(sLAddContact);

	GetContactListXML();

	$("#contact-div").fadeIn(1000);
}

function OnContactClose() {
	$("#contact-div").hide();
}

var contactPageAdapter = new PageAdapter();
contactPageAdapter.iPageIndex = CONTACT_PAGE_INDEX;
contactPageAdapter.OnInit = function() {
	OnContactInit();
};
contactPageAdapter.OnClose = function() {
	OnContactClose();
};
contactPageAdapter.XmlParser = function(xml) {
	ContactXmlParser(xml);
};

PageFuncList.push(contactPageAdapter);
