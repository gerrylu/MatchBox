// for backup sms list
var g_iBackupFrom = 0;
var g_iTCurrentStep = 0;

var g_iBackupTotalPage = 0;
var THREAD_ITEM_PER_PAGE = 10;

function GetBkOption() {
	var requestURL = "Backup.xml?action=getbackupoption";
	StartAjaxRequest(requestURL, "xml");
}


function GetBkHistory() {
	var requestURL = "Backup.xml?action=getbackuphistory";
	StartAjaxRequest(requestURL, "xml");
}

function SetBkOption() {
	var sUsername = $("#SMS-MAIL-USERNAME-INPUT").attr("value");
	var sPassword = $("#SMS-MAIL-PASSWORD-INPUT").attr("value");
	var sFreq = $("#BK-BK-FREQ-SEL").val();
	// check mail address invalid
	//var reg = /^\w{1,25}(?:@(?!-))(?:(?:[a-z0-9-]*)(?:[a-z0-9](?!-))(?:\.(?!-)))+[a-z]{2,4}$/;

	var requestURL = "Backup.xml?action=setbackupoption&mailaccount="
			+ encodeURIComponent(sUsername) + "&mailpwd=" + encodeURIComponent(sPassword) + "&bkfreq=" + sFreq;
	StartAjaxRequest(requestURL, "xml");
	
	ShowAlert(sLBkPlsWait);
}


function StartBackupNow() {
	var sReq = "Backup.xml?action=startbackup";
	StartAjaxRequest(sReq, "xml");
}


//decodeURIComponent
function OnBackupParser(xml) {
	var section = $(xml).find("BKOPTION").text();
	if (section.length > 1){
		var sAccount = $(xml).find("MailAccount").text();
		var sPassword = $(xml).find("MailPwd").text();
 		var sFreq = $(xml).find("BkFreq").text();

		$('#SMS-MAIL-USERNAME-INPUT').val(decodeURIComponent(sAccount));
		$('#SMS-MAIL-PASSWORD-INPUT').val(decodeURIComponent(sPassword));
		GenFreqSel(parseInt(sFreq));
		
		GetBkHistory();
	}
	
	section = $(xml).find("BkHistory").text();
	if (section.length > 1){
		$("#BK-HISTORY-LIST").html("");
		$(xml).find("History").each(
			function(i) {
				var s = $(this).text();
				var sTime = s.split(",")[0];
				var sResCode = s.split(",")[1];
				var sTxt = sLBkFailed;
				if (parseInt(sResCode) == 0){
					sTxt = sLBkSucc;
				}
				
				$("#BK-HISTORY-LIST").append("<p>" + sTime + " " + sTxt + "</p>");
			}
		)
	}
	
}

//<option value="office" selected="selected">Ã¿ÖÜ(ÍÆ¼ö)</option>
function GenFreqSel(selIndex){
	if (selIndex > 2 ){
		selIndex = 1;
	}

	var sHtml = '<select id="BK-BK-FREQ-SEL">';
	
	sHtml += '<option value="0"';
	if (selIndex == 0 ){
		sHtml += ' selected="selected"';
	}
	sHtml += ">" + sLFreqEveryDay + '</option>';
	
	sHtml += '<option value="1"';
	if (selIndex == 1 ){
		sHtml += ' selected="selected"';
	}
	sHtml += ">" + sLFreqEveryWeek + '</option>';
	
	sHtml += '<option value="2"';
	if (selIndex == 2 ){
		sHtml += ' selected="selected"';
	}
	sHtml += ">" + sLFreqEveryMonth + '</option>';
	
	sHtml += "</select>";
	
	$("#BK-FREQ-SEL-DIV").html(sHtml);
}

function OnBackupInit() {
	$("#BK-HISTORY-LIST").html("");
	$("#BK-BK-OPTION").html(sLBKSet);
	$("#SET-MAIL-USERNAME-H4").html(sLBKMailUsername);
	$("#SET-MAIL-PASSWORD-H4").html(sLBKMailPassword);
	$("#SET-MAIL-FREQ-H4").html(sLBKFreq);
	
	$("#BK-SAVE-OPTION-BTN").html(sLSaveChange);
	
	$("#BK-HISTORY-H2").html(sLBKHistory);
	
	GenFreqSel(10);
	
	GetBkOption();
	
	$("#backup-div").fadeIn(1000);
}

function OnBackupClose() {
	$("#SMSR-SELECT-DATE-LABEL").html("");
	$("#SMSR-SELECT-TO-LABEL").html("");
	$("#backup-div").hide();
}


var backupPageAdapter = new PageAdapter();
backupPageAdapter.iPageIndex = BACKUP_PAGE_INDEX;
backupPageAdapter.OnInit = function() {
	OnBackupInit();
};
backupPageAdapter.OnClose = function() {
	OnBackupClose();
};
backupPageAdapter.XmlParser = function(xml) {
	OnBackupParser(xml);
};

PageFuncList.push(backupPageAdapter);
