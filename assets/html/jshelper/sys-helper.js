

var EA_ACT_SYS_PRODUCT_MODEL_TAG = "ProductModel";
var EA_ACT_SYS_EXT_AVAILABLE_SPACE = "SDCardAvailableSpace";
var EA_ACT_SYS_EXT_TOTAL_SPACE = "SDCardTotalSpace";
var EA_ACT_SYS_AVAIL_RAM = "AvailRAM";
var EA_ACT_SYS_TOTAL_RAM = "TotalRAM";
var EA_ACT_SYS_BATTERY_TAG = "BatteryLevel";
var EA_ACT_SYS_CPU_FREQ_TAG = "CpuFreq";
var EA_ACT_SYS_CPU_MODEL_TAG = "CpuModel";
var EA_ACT_SYS_LOG_STATE_TAG = "LogState";


function bytesToSize(bytes) {
    var sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB'];
    if (bytes == 0) return 'n/a';
    var i = parseInt(Math.floor(Math.log(bytes) / Math.log(1024)));
    return Math.round(bytes / Math.pow(1024, i), 2) + ' ' + sizes[i];
}

/*sLSysRamInfo = "RAM Info";
sLSysBatteryInfo = "Battery Info";
sLSysSdCardInfo = "SdCard Info";
sLSysCpuInfo = "CPU Info";
*/

/*<div class="sys-graph"><div>Battery</div><div class="sys-graph1" id="BatteryInfo" ></div><div>Battery 50% left</div></div>*/
function GenSDCardPie(totalSpace,availSpace){
	var percent = Math.round(parseInt(availSpace)/parseInt(totalSpace) * 100);
	
	var sPercent = percent + "%";
	var data = [ {	label : sPercent,	data : parseInt(availSpace)	}, { label :"" ,data : parseInt(totalSpace) -parseInt(availSpace)	}	];
	
	var sDes = '<span class="sys-title">' + sLSysExtTotalSpace + "</span>" + bytesToSize(parseInt(totalSpace) ) + " "; 
	sDes += '<span class="sys-title">' + sLSysExtAvailableSpace + "</span>" + bytesToSize(parseInt(availSpace) ) ;
	var sHtml = '<div class="sys-item"><div>' + sLSysSdCardInfo + '</div><div class="sys-graph" id="' + 'SdCardInfo"></div><div>' + sDes + "</div></div>";
	$("#sys-info-div").append(sHtml);
	
	$.plot($("#SdCardInfo"),
			data,
			{
				series : {
					pie : {
						show : true,
						radius : 1,
						tilt : 0.5,
						label : {
							show : true,
							radius : 3 / 4,
							formatter : function(label, series) {
								return '<div style="font-size:8pt;text-align:center;padding:2px;color:black;">'
										+ label + '<br/></div>';
							},
							background : {
								opacity : 0.5
							}
						}

					}

				},
				legend : {
					show : false
				}
			});
}

function GenRAMPie(totalSpace,availSpace){
	var percent = Math.round(parseInt(availSpace)/parseInt(totalSpace) * 100);
	
	var sPercent = percent + "%";
	var data = [ {	label : sPercent,	data : parseInt(availSpace)	}, { label :"" ,data : parseInt(totalSpace) -parseInt(availSpace)	}	];
	
	var sDes = '<span class="sys-title">' + sLSysTotalRAM + "</span>" + bytesToSize(parseInt(totalSpace) ) + " "; 
	sDes += '<span class="sys-title">' + sLSysAvailRAM + "</span>" + bytesToSize(parseInt(availSpace) ) ;
	var sHtml = '<div class="sys-item"><div>' + sLSysRamInfo + '</div><div class="sys-graph" id="' + 'RAMInfo"></div><div>' + sDes + "</div></div>";
	$("#sys-info-div").append(sHtml);
	
	$.plot($("#RAMInfo"),
			data,
			{
				series : {
					pie : {
						show : true,
						radius : 1,
						tilt : 0.5,
						label : {
							show : true,
							radius : 3 / 4,
							formatter : function(label, series) {
								return '<div style="font-size:8pt;text-align:center;padding:2px;color:black;">'
										+ label + '<br/></div>';
							},
							background : {
								opacity : 0.5
							}
						}

					}

				},
				legend : {
					show : false
				}
			});
}

function GenBatteryPie(level){
	var sAvail = level.replace("%","");
	var sTotal = 100;
	
	var sPercent = level;
	var data = [ {	label : sPercent,	data : parseInt(sAvail)	}, { label :"" ,data : parseInt(sTotal) -parseInt(sAvail)	}	];
	
	var sDes = '<span class="sys-title">' + sLSysBatteryLevel + "</span>" + sPercent; 
	var sHtml = '<div class="sys-item"><div>' + sLSysBatteryInfo + '</div><div class="sys-graph" id="' + 'BatteryInfo"></div><div>' + sDes + "</div></div>";
	$("#sys-info-div").append(sHtml);
	
	$.plot($("#BatteryInfo"),
			data,
			{
				series : {
					pie : {
						show : true,
						radius : 1,
						tilt : 0.5,
						label : {
							show : true,
							radius : 3 / 4,
							formatter : function(label, series) {
								return '<div style="font-size:8pt;text-align:center;padding:2px;color:black;">'
										+ label + '<br/></div>';
							},
							background : {
								opacity : 0.5
							}
						}

					}

				},
				legend : {
					show : false
				}
			});
	
}

/*<div class="sys-graph"><div class="sys-text-item"><p>CPU Model:arm strong </p><p>Freq:569mhz</p></div></div>*/
function GenCPUInfo(sModel,sFreq){
	var sHtml = '<div class="sys-item"><div class="sys-text-item"><p>';
	sHtml += '<span class="sys-title">' + sLSysCpuModel + "</span>" + sModel;
	sHtml += '</p><p>' + '<span class="sys-title">' + sLSysCpuFreq + "</span>" + sFreq + "MHZ";
	sHtml += "</p></div></div>";
	
	$("#sys-info-div").append(sHtml);
}

function GenPhoneModelInfo(sModel){
	var sHtml = '<div class="sys-item"><div class="sys-text-item"><p>';
	sHtml += '<span class="sys-title">' + sLSysProductModel + "</span>" + sModel;
	sHtml += "</p></div></div>";
	
	$("#sys-info-div").append(sHtml);
}


function mainXmlParser(xml){
	$("#sys-info-div").html("");
	
	var sProductModel = "";
	var SDCardAvailSpace = "";
	var SDCardTotalSpace = "";
	var AvailRAM = "";
	var TotalRAM = "";
	var BatteryLevel = "";
	var CpuFreq = "";
	var CpuModel = "";
	
	$(xml).find("Entry").each(
		function(i)
		{
	        var sItemName = $(this).find("Name").text();
			var sItemVal = $(this).find("Value").text();
			
			if (sItemName == EA_ACT_SYS_PRODUCT_MODEL_TAG){
				sProductModel = sItemVal;
				return;
			}
			
			if (sItemName == EA_ACT_SYS_EXT_AVAILABLE_SPACE){
				SDCardAvailSpace = sItemVal;
				return;
			}
			
			if (sItemName == EA_ACT_SYS_EXT_TOTAL_SPACE){
				SDCardTotalSpace = sItemVal;
				return;
			}
			
			if (sItemName == EA_ACT_SYS_AVAIL_RAM ){
				AvailRAM = sItemVal;
				return;
			}
			
			if (sItemName == EA_ACT_SYS_TOTAL_RAM ){
				TotalRAM = sItemVal;
				return;
			}
			
			if (sItemName == EA_ACT_SYS_BATTERY_TAG ){
				BatteryLevel = sItemVal;
				return;
			}
			
			if (sItemName == EA_ACT_SYS_CPU_FREQ_TAG ){
				CpuFreq = sItemVal;
				return;
			}
			
			if (sItemName == EA_ACT_SYS_CPU_MODEL_TAG ){
				CpuModel = sItemVal;
				return;
			}
			
			if (sItemName == EA_ACT_SYS_LOG_STATE_TAG ){
				g_iEnableDebug = parseInt(sItemVal);
				return;
			}
		}
	);

	GenBatteryPie(BatteryLevel);
	GenRAMPie(parseInt(TotalRAM)*1024,parseInt(AvailRAM)*1024);
	GenSDCardPie(SDCardTotalSpace,SDCardAvailSpace);
	
	GenCPUInfo(CpuModel,CpuFreq);
	GenPhoneModelInfo(sProductModel);
}

/*
  * <dl class="listsys">
						<dt class="f_right"></dt>
						<dd class="f_left">
							<p class="title">
								<span>ÄÚ´æ</span>
							</p>
							<p class="txt">1024M</p>
						</dd>
						<div class="clear"></div>
					</dl>
 */




function SysInfoGenEntry(sItemName,sItemVal)
{
	var sLink = '<dl class="listsys"><dt class="f_right"></dt><dd class="f_left"><p class="title">';
		
	sLink += "<span>";
	sLink += GetItemDescription(sItemName);
	sLink += "</span>";
	sLink += "</p>";
	
	sLink += '<p class="txt">';
	sLink += sItemVal;
	sLink += "</p>";

	sLink += '</dd><div class="clear"></div></dl>';
	
	return sLink;
}

function OnMainInit()
{
	//$("#sys-info-div").html("");
	$("#sys-info-div").fadeIn(1000);
	StartAjaxRequest("SysInfo.xml?action=getsysinfo","xml");
}

function OnMainClose()
{
	$("#sys-info-div").hide();
}

var mainPageAdapter = new PageAdapter();
mainPageAdapter.iPageIndex = SYS_PAGE_INDEX;
mainPageAdapter.OnInit = function(){OnMainInit();};
mainPageAdapter.OnClose = function(){OnMainClose();};
mainPageAdapter.XmlParser = function(xml){mainXmlParser(xml);};

PageFuncList.push(mainPageAdapter);





