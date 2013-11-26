/*
EA js 通用函数
lugang at 2010-08-15
*/

var ValSeparator = "&ensp;";


String.prototype.len=function(){                    
    return this.replace(/[^\x00-\xff]/g,"rr").length;             
};

String.prototype.sub = function(n){       
    var r = /[^\x00-\xff]/g;
	if (this.length <= n){
		return this.toString();
	}
	
    if(this.replace(r, "mm").length <= n)    
        return this.toString();      
  
    var m = n;//Math.floor(n/2);       
    for(var i=m; i<this.length; i++) {       
        if(this.substr(0, i).replace(r, "mm").length>=n) {    
            return this.substr(0, i) + "...";    
        }   
    }    
    return this.toString();      
};

String.prototype.unescapeHtmlChar=function(){
	var tmp = this.toString();
	tmp = tmp.replace("&lt;","<");
	tmp = tmp.replace("&gt;",">");
	/*tmp = tmp.replace("&lt;","<");
	tmp = tmp.replace("&lt;","<");
	tmp = tmp.replace("&lt;","<");*/
	return tmp;
}

Array.prototype.del = function(n) {
	if (n < 0) {
		return this;// 如果n<0，则不进行任何操作。
	}
	
	this.splice(n, 1);
};

$.fn.extend({
	RemoveJscroll : function() {
		return this.each(function() {
			var _self = this;
			var content = "";
			var node = $(_self).find(".overview");
			if (node == null || node.length == 0) {
				return;
			}
			
			content = $(_self).find(".overview").html();
			$(_self).html(content);
		});
	}
});



$.fn.extend({
	InitJscroll : function() {
		return this.each(function() {
			var _self = this;
			var content = "";
			var node = $(_self).find(".overview");
			if (node == null || node.length == 0) {
				content = $(_self).html();
			}
			else{
				content = $(_self).find(".overview").html();
			}
			
			var warpper = '<div class="scrollbar"><div class="track"><div class="thumb"><div class="end"></div></div></div></div>'
			warpper += '<div class="viewport"><div class="overview">';
			warpper += '</div></div>';
			$(_self).html(warpper);
			$(_self).find(".overview").html(content);;
						
			$(_self).tinyscrollbar();
		});
	}
});

function GenID(){
	var sID = new Date().getTime();
	return sID;
}



function isStrictMode(){
	return document.compatMode != "BackCompat";
}

function getHeight(){
        return isStrictMode() ? Math.max(document.documentElement.scrollHeight, document.documentElement.clientHeight) : Math.max(document.body.scrollHeight, document.body.clientHeight);
}


function getWidth(){
        return isStrictMode() ? Math.max(document.documentElement.scrollWidth, document.documentElement.clientWidth) : Math.max(document.body.scrollWidth, document.body.clientWidth);
}


function EATask(){
	this.iTimerID = 0;
	this.iTimerInterval = 1000;
	this.start = function(TimerFunc){
		clearTimeout(this.iTimerID);
		iTimerID = setInterval(TimerFunc,this.iTimerInterval);
	};
	this.stop = function(){
		clearTimeout(iTimerID);
	};
}


 var log4MB = null;
 var g_iEnableDebug = 0;
 function LOG4MB(msg){
	if (g_iEnableDebug == 0){
		return;
	}
	
	if (log4MB == null){
		log4MB = new Log(Log.DEBUG, Log.popupLogger);
	}
		
	log4MB.debug(msg);
 }
 

var g_iCurPageAdapter = null;
var MAX_ITEM_PER_REQUEST = 25;


function HtmlGetHtmlContent(data){
	//LOG4MB("HtmlGetHtmlContent:" + data.split(";=")[1]);
	return data.split(";=")[1];
}

function HtmlGetParameterList(data){
	LOG4MB("HtmlGetParameterList:" + data.split(";=")[0].split(";"));
	return data.split(";=")[0].split(";");
}

function HtmlGetParamsProperty(params,propertyName){
	for (var i = 0; i < params.length; ++i){
		var valuePair = params[i];
		var p = valuePair.split(":");
		if (propertyName == p[0]){
			LOG4MB("Property:" + p[1]);
			return p[1];
		}
	}
	
	return null;
}

// parse data: <xxx>value1</xxx><yyy>value2</yyy><zzz>value3</zzz>
function XmlGetParameterList(data)
{
	var reg = /<\w+>[^<>\r\n]+<\/\w+>?/gi;
	return data.match(reg);
}

// parse data: <xxx>value1</xxx>
function XmlGetParameterProperty(params, propertyName)
{
	for (var i=0; i<params.length; ++i)
	{
		var reg = /^<(\w+)>([^<>\r\n]+)<\/(\w+)>/gi;
		var data = params[i];
		var arr = data.split(reg);

		// check integrity : tag1 == tag2
		if (RegExp.$1 == RegExp.$3 && RegExp.$1 == propertyName)
		{
			return RegExp.$2;
		}		
	}

	return null;
}

function HtmlGetException(htmlData){
	if (htmlData.indexOf('<EARetException>') < 0){
		return null;
	}
	
	var retCode = htmlData.replace("<EARetException>","");
	retCode = retCode.replace("</EARetException>","");
	return retCode;
}

function HtmlGetRetCode(htmlData){
	var retCode = htmlData.replace("<EARetCode>","");
	retCode = retCode.replace("</EARetCode>","");
	if (isNaN(retCode)){
		return 0;
	}
	
	return parseInt(retCode);
}

function ShowAlert(info) {
	$("#AlertContent").html(info);
	$.colorbox({
		inline : true,
		href : '#AlertContent'
	});
}

function ParseErrNo(ErrNo) {
	for (var i = 0; i < ErrInfo.length; ++i){
		if (ErrInfo[i][0] == ErrNo){
			return ErrInfo[i][1];
		}
	}
	return "Unkown Error";
}

function GetRetCode(xml){
	var retVal = $(xml).find("EARetCode");
	if (retVal.text().length > 0) {
		return parseInt(retVal.text());
		
	}
	
	return -1;
}

function OnAjaxReqSuccess(xml) {
	var retVal = $(xml).find("EARetException");
	if (retVal.text().length > 1) {
		ShowAlert(retVal.text());
		return;
	}
	
	if (g_iCurPageAdapter != null) {
		g_iCurPageAdapter.XmlParser(xml);
		return;
	}
}

function OnAjaxReqFailed(XMLHttpRequest, textStatus, errorThrown) {
	
	var sErr = "http-status:" + XMLHttpRequest.status;
	sErr += " http-state:" + XMLHttpRequest.readyState;
	sErr += " Errortext:" + textStatus;
	
	LOG4MB(sErr);
}


// 扩展AJAX请求函数, 传递自定义回调函数
function StartAjaxRequest(xmlUrl, acceptDataType, OnReqSuccess, OnReqFailed)
{
	$.ajaxSetup({
		cache : false
	});
	
	if (acceptDataType == null){
		acceptDataType = "xml";
	}
	
	xmlUrl = xmlUrl + "&" + "mobitnttimestamp=" + Math.random();
	
	if (OnReqSuccess == null){
		OnReqSuccess = OnAjaxReqSuccess;
	}
	
	if (OnReqFailed == null){
		OnReqFailed = OnAjaxReqFailed;
	}

	$.ajax({
		url : xmlUrl,
		type : "get",
		dataType:acceptDataType,
		success : function(msg) {
			OnReqSuccess(msg);
		},
		error : function(XMLHttpRequest, textStatus, errorThrown) {
			OnReqFailed(XMLHttpRequest, textStatus, errorThrown);
		}
	});
}
