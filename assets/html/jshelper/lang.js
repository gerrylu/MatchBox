var LANG_INDEX_ENGLISH = 0;
var LANG_INDEX_CHINESE = 1;
var g_CurrentLangType = 0;
var g_FavLangType = -1;
function GetLangType() {
	if (g_FavLangType >= 0){
		return g_FavLangType;
	}
	
	var langType = LANG_INDEX_ENGLISH;
	var lang = window.navigator.systemLanguage;
	if (lang == null){
		lang = window.navigator.language;
	}
	
	lang = lang.toLowerCase();
	if (lang == 'zh-cn'){
		langType = LANG_INDEX_CHINESE;
	}
	
	g_CurrentLangType = langType;
	return g_CurrentLangType;
}

function SetFavLangType(langType){
	g_FavLangType = langType;
}


var LangInfo = new Array(['English', 'us', 'sprite-us'], ['中   文', 'cn','sprite-cn']);

var sLLangInfo, sLAppName, sLAddFav, sLAbout, sLSendLog;

var sLErrInfoOK, sLErrInfoFailed, sLErrInfoFileExist, sLErrInfoFileNotExist, sLErrInfoAuthFailed, sLErrInfoInvalidSms;
var sLErrInfoInvalidPhone, sLErrInfoNoData, sLErrInfoUnKnowReq, sLErrInfoInvaliEmail, sLErrInfoBusyBackup;
var sLErrInfoAccessDenined, sLErrInfoQueryLater, sLErrInfoSaveFileFailed, sLErrInfoNeedOpOnPhone, sLErrInfoNoExternalStorage;

var sLPageNavSYS, sLPageNavSMS, sLPageNavCONTACT, sLPageNavCALL, sLPageNavPHOTO, sLPageNavFILE, sLPageNavAPP;
var sLPageNavBackup,slPageNavOption;

var sLHomePage, sLTechSupport;

var sLLoadingNow;

var sLBack;

var sLNewSMS, sLSmsReply, sLSmsQueryFailed, sLInputContact,sLOK, sLSend, sLContent, sLRecvEmpty, sLTo;

var sLUpload,sLRefresh,sLDeleteFile,sLNewFolder,sLReNameFile,sLFileName,sLNewFileName,sLSelectFile,sLNotRunOnIE,sLConfirmeDelete,sLRootFolder;

var sLPhoneTypeHome,sLPhoneTypeMobile,sLPhoneTypeWork,sLPhoneTypeOther,sLAddrTypeStreet,sLAddrTypeCity,sLAddrTypeRegion;

var sLContactLabelName,sLContactLabelPhone,sLContactLabelEmail,sLContactLabelAddr,sLContactNoName;

var sLUpdateContact,sLDeleteContact,sLAddContact;

var sLDragAndDrop;

var sLSysProductModel,sLSysExtAvailableSpace,sLSysExtTotalSpace,sLSysAvailRAM,sLSysTotalRAM,sLSysBatteryLevel,sLSysCpuFreq,sLSysCpuModel;

var sLSysRamInfo,sLSysBatteryInfo,sLSysSdCardInfo,sLSysCpuInfo;

var sLBKComments,sLBKSet,sLBKMailUsername,sLBKMailPassword,sLBKHistory,sLBKFreq,sLSaveChange,sLFreqEveryDay,sLFreqEveryWeek,sLFreqEveryMonth,sLBkSucc,sLBkFailed;
var sLBkPlsWait;

var sLTimeDay,sLTimeDays,sLTimeMinute,sLTimeMinutes,sLTimeSecond,sLTimeSeconds;

function InitLangString() {
	var ilangType = GetLangType();
	ilangType = LANG_INDEX_ENGLISH;
	if (ilangType == LANG_INDEX_ENGLISH) {
		sLLangInfo = "English";
		sLAppName = "MatchBox (1.2 Beta)";
		sLAddFav = "Add to favorite";
		sLAbout = "About MatchBox";
		sLSendLog = "Send Log";

		sLErrInfoOK = "success";
		sLErrInfoFailed = "Failed";
		sLErrInfoFileExist = "File already exist";
		sLErrInfoFileNotExist = "File not exist";
		sLErrInfoAuthFailed = "Auth failed";
		sLErrInfoInvalidSms = "Invalid sms content";
		sLErrInfoInvalidPhone = "Invalid phone number";
		sLErrInfoNoData = "No more data";
		sLErrInfoUnKnowReq = "Unkonw request";
		sLErrInfoInvaliEmail = "Invalid email";
		sLErrInfoBusyBackup = "Backup now..";
		sLErrInfoAccessDenined = "Access denined";
		sLErrInfoQueryLater = "Please query the state later";
		sLErrInfoSaveFileFailed = "Failed to save the file";
		sLErrInfoNeedOpOnPhone = "Please take a look at your device's screen:)";
		sLErrInfoNoExternalStorage = "No external storage found!";

		sLPageNavSYS = "Device";
		sLPageNavSMS = "SMS";
		sLPageNavCONTACT = "Contact";
		sLPageNavCALL = "Call Log";
		sLPageNavPHOTO = "Photo";
		sLPageNavFILE = "SD Card";
		sLPageNavAPP = "APP";
		sLPageNavBackup = "Backup"; 
		slPageNavOption = "Options";

		sLHomePage = "Home";
		sLTechSupport = "Support";

		sLLoadingNow = "Loading,please wait...";

		sLBack = "Back";

		sLNewSMS = "New SMS";
		sLSmsReply = "Reply";
		sLSmsQueryFailed = "Failed to get sms send state";
		sLEmail = "EMail";
		sLPassword = "Password";
		sLSend = "Send";
		sLContent = "Content";
		sLRecvEmpty = "Receiver is empty";
		sLTo = "To";
		sLInputContact = "Enter phone number:";
		sLOK = "OK";

		sLUpload = "Upload";
		sLRefresh = "Refresh";
		sLDeleteFile = "Delete file";
		sLNewFolder = "Create folder";
		sLReNameFile = "ReName";
		sLFileName = "Name";
		sLNewFileName = "New Name";
		sLSelectFile = "Please select one file";
		sLNotRunOnIE = "Sorry,upload function doesn't work with IE";
		sLConfirmeDelete = "Are you sure you want to delete:";
		sLRootFolder = "/";

		sLPhoneTypeHome = "Home";
		sLPhoneTypeMobile = "Mobile";
		sLPhoneTypeWork = "Work";
		sLPhoneTypeOther = "Other";
		sLAddrTypeStreet = "Street";
		sLAddrTypeCity = "City";
		sLAddrTypeRegion = "Region";

		sLContactLabelName = "Name:";
		sLContactLabelPhone = "Phone:";
		sLContactLabelEmail = "Email:";
		sLContactLabelAddr = "Address:";
		sLContactNoName = "Please Input Contact's Name";

		sLUpdateContact = "Update";
		sLDeleteContact = "Delete";
		sLAddContact = "Add";

		sLDragAndDrop = "Drag and drop file here";

		sLSysProductModel = "Phone Model:";
		sLSysExtAvailableSpace = "Available Space:";
		sLSysExtTotalSpace = "Total Space:";
		sLSysAvailRAM = "Available RAM:";
		sLSysTotalRAM = "Total RAM:";
		sLSysBatteryLevel = "Battery Level:";
		sLSysCpuFreq = "CPU Frequence:";
		sLSysCpuModel = "CPU Model:";

		sLSysRamInfo = "RAM Info";
		sLSysBatteryInfo = "Battery Info";
		sLSysSdCardInfo = "SdCard Info";
		sLSysCpuInfo = "CPU Info";
		
		sLBKComments = "This will backup your sms messages to your GMail.";
		sLBKSet = "Bakcup Setting";
		sLBKMailUsername = "EMail";
		sLBKMailPassword = "Mail password";
		sLBKHistory = "Backup history";
		sLBKFreq = "Backup by:";
		sLFreqEveryDay = "every day";
		sLFreqEveryWeek = "every week(recommend)";
		sLFreqEveryMonth = "every month";
		sLBkSucc = "backup success";
		sLBkFailed = "backup failed";
		sLBkPlsWait = "If will take a few minutes,please wait and do not do other operation,progress will show on your device's screen";
		
		sLSaveChange = "Save";

		sLTimeDay = "day";
		sLTimeDays = "days";
		sLTimeMinute = "minute";
		sLTimeMinutes = "minutes";
		sLTimeSecond = "second";
		sLTimeSeconds = "seconds";
	} else if (ilangType == LANG_INDEX_CHINESE) {
		sLLangInfo = "中文";
		sLAppName = "火柴盒 (1.2 Beta)";
		sLAddFav = "添加到收藏夹";
		sLAbout = "关于火柴盒";
		sLSendLog = "打开日志";

		sLErrInfoOK = "成功";
		sLErrInfoFailed = "失败";
		sLErrInfoFileExist = "文件已经存在";
		sLErrInfoFileNotExist = "文件不存在";
		sLErrInfoAuthFailed = "认证失败";
		sLErrInfoInvalidSms = "短信息内容无效";
		sLErrInfoInvalidPhone = "电话号码无效";
		sLErrInfoNoData = "没有数据了";
		sLErrInfoUnKnowReq = "未知请求";
		sLErrInfoInvaliEmail = "无效的email";
		sLErrInfoBusyBackup = "正在备份中...";
		sLErrInfoAccessDenined = "访问被拒绝";
		sLErrInfoQueryLater = "请稍后查询状态";
		sLErrInfoSaveFileFailed = "保存文件失败";
		sLErrInfoNeedOpOnPhone = "亲，去瞅瞅你的手机屏幕:)";

		sLPageNavSYS = "系统信息";
		sLPageNavSMS = "短信";
		sLPageNavCONTACT = "联系人";
		sLPageNavCALL = "通话记录";
		sLPageNavPHOTO = "照片";
		sLPageNavFILE = "文件管理";
		sLPageNavAPP = "应用";
		sLPageNavBackup = "备份"; 
		slPageNavOption = "选项";

		sLHomePage = "主页";
		sLTechSupport = "技术支持";

		sLLoadingNow = "正在拼命加载...";

		sLBack = "返回";

		sLNewSMS = "新短信";
		sLSmsReply = "回复";
		sLSmsQueryFailed = "获取发送状态失败";
		sLEmail = "电子邮件";
		sLPassword = "密码";
		sLSend = "发送";
		sLContent = "短信内容";
		sLSelectDate = "选择日期";
		sLRecvEmpty = "接收者为空";
		sLTo = "到";
		sLInputContact = "请输入联系人号码：";
		sLOK = "确定";

		sLUpload = "上传";
		sLRefresh = "刷新";
		sLDeleteFile = "删除文件";
		sLNewFolder = "新建文件夹";
		sLReNameFile = "重命名";
		sLFileName = "名称";
		sLNewFileName = "新名称";
		sLSelectFile = "请选择一个文件";
		sLNotRunOnIE = "抱歉亲，此功能不支持IE内核浏览器";
		sLConfirmeDelete = "请确认是否删除:";
		sLRootFolder = "/";

		sLPhoneTypeHome = "住宅";
		sLPhoneTypeMobile = "手机";
		sLPhoneTypeWork = "单位";
		sLPhoneTypeOther = "其它";
		sLAddrTypeStreet = "街道";
		sLAddrTypeCity = "市";
		sLAddrTypeRegion = "省";

		sLContactLabelName = "姓名:";
		sLContactLabelPhone = "电话:";
		sLContactLabelEmail = "电子邮件:";
		sLContactLabelAddr = "地址:";
		sLContactNoName = "请输入联系人姓名";

		sLUpdateContact = "更新";
		sLDeleteContact = "删除";
		sLAddContact = "添加";

		sLDragAndDrop = "将文件拖动到此处";

		sLSysProductModel = "手机型号:";
		sLSysExtAvailableSpace = "可用空间:";
		sLSysExtTotalSpace = "总空间:";
		sLSysAvailRAM = "可用内存:";
		sLSysTotalRAM = "总内存:";
		sLSysBatteryLevel = "电池电量:";
		sLSysCpuFreq = "处理器频率:";
		sLSysCpuModel = "处理器型号:";

		sLSysRamInfo = "内存信息";
		sLSysBatteryInfo = "电池信息";
		sLSysSdCardInfo = "SdCard信息";
		sLSysCpuInfo = "处理器信息";
		
		sLBKComments = "将你的短信备份到GMAIL里";
		sLBKSet = "备份设置";
		sLBKMailUsername = "邮件";
		sLBKMailPassword = "邮件密码";
		sLBKHistory = "备份历史";
		sLBKFreq = "备份频率 ：";
		sLSaveChange = " 保存"
		sLFreqEveryDay = "每天";
		sLFreqEveryWeek = "每周(推荐)";
		sLFreqEveryMonth = "每月";
		sLBkSucc = "备份成功";
		sLBkFailed = "备份失败";
		sLBkPlsWait = "备份正在进行中，请不要进行其他操作，进度会在手机屏幕上显示";

		sLTimeDay = "天";
		sLTimeDays = "天";
		sLTimeMinute = "分钟";
		sLTimeMinutes = "分钟";
		sLTimeSecond = "秒";
		sLTimeSeconds = "秒";
	}

	ReInitStringAry();
}

var phoneOptionList = null;
var emailOptionList = null;
var addrTags = null;
var PageNav = null;
var ErrInfo = null;

function ReInitStringAry() {
	ErrInfo = [[EA_RET_OK, sLErrInfoOK], [EA_RET_FAILED, sLErrInfoFailed],
			[EA_RET_FILE_EXIST, sLErrInfoFileExist],
			[EA_RET_FILE_NOT_EXIST, sLErrInfoFileNotExist],
			[EA_RET_AUTH_FAILED, sLErrInfoAuthFailed],
			[EA_RET_INVALID_SMS_CONTENT, sLErrInfoInvalidSms],
			[EA_RET_INVALID_PHONE_NO, sLErrInfoInvalidPhone],
			[EA_RET_END_OF_FILE, sLErrInfoNoData],
			[EA_RET_UNKONW_REQ, sLErrInfoUnKnowReq],
			[EA_RET_INVALID_EMAIL, sLErrInfoInvaliEmail],
			[EA_RET_BUSY_BACKUP, sLErrInfoBusyBackup],
			[EA_RET_ACCESS_DENINED, sLErrInfoAccessDenined],
			[EA_RET_QUERY_STATE_LATER, sLErrInfoQueryLater],
			[EA_RET_SAVE_FILE_FAILED, sLErrInfoSaveFileFailed],
			[EA_RET_NEED_OP_ON_PHONE, sLErrInfoNeedOpOnPhone]];

	// div=id,description,page index
	PageNav = new Array(
			new Array("sys-info-div", sLPageNavSYS, SYS_PAGE_INDEX), 
			new Array("sms-thread-div", sLPageNavSMS, SMS_THREAD_PAGE_INDEX),
			new Array("sms-chat-div", "", SMS_CHAT_PAGE_INDEX), 
			new Array("contact-div", sLPageNavCONTACT, CONTACT_PAGE_INDEX),
			new Array("call-div", sLPageNavCALL, CALL_PAGE_INDEX),
			new Array("photo-div", sLPageNavPHOTO, PHOTO_PAGE_INDEX),
			new Array("file-div", sLPageNavFILE, FILE_PAGE_INDEX),
			new Array("app-div", sLPageNavAPP, APP_PAGE_INDEX)
			//,new Array("backup-div", sLPageNavBackup, BACKUP_PAGE_INDEX)
			//,new Array("option-div", slPageNavOption, OPTION_PAGE_INDEX)
			);

	phoneOptionList = new Array([1, sLPhoneTypeHome], [2, sLPhoneTypeMobile], [
			3, sLPhoneTypeWork]);
	emailOptionList = new Array([1, sLPhoneTypeHome], [2, sLPhoneTypeWork], [4,
			sLPhoneTypeMobile], [3, sLPhoneTypeOther]);
	addrTags = [sLAddrTypeStreet, sLAddrTypeCity, sLAddrTypeRegion];
};
