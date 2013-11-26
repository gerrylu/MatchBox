var g_iTotalImages = 0;
var g_iPhotoStartItem = 0;
var PHOTO_ITEM_PER_PAGE = 100;
var MAX_PHOTO_GALLERY_NAME = 10;
var g_iImgPos = 0;
var g_ImageList = new Array();
var g_GllleryControl = null;

function GetPhotoList() {
	var sReq = "Media.xml?action=getimagelist";
	sReq += "&" + "from=" + g_iPhotoStartItem;
	sReq += "&to=" + (g_iPhotoStartItem + PHOTO_ITEM_PER_PAGE);

	g_iPhotoStartItem += PHOTO_ITEM_PER_PAGE;
	StartAjaxRequest(sReq,"xml");
}

function GenImageData(){
	if (g_ImageList.length <= g_iImgPos){
		return null;
	}
		
	var sImagePath = g_ImageList[g_iImgPos][0];
	var	sGallery = g_ImageList[g_iImgPos][1];
	var sTitle = g_ImageList[g_iImgPos][2];
	var sDisplayName = g_ImageList[g_iImgPos][3];
			
	g_iImgPos += 1;
	
	var sImageUrl = "?action=getfile&file=" + sImagePath;
	var sDescription = decodeURIComponent(sGallery) + "--"	+ decodeURIComponent(sDisplayName);

	var data = [
		{
			image: sImageUrl,
			title:decodeURIComponent(sTitle),
			description:sDescription,
			layer: '<a href="#" class="aHasBk  sprite sprite-delete-image" onclick="OnDeletePhoto(\'' + sImageUrl + '\');"></a>'
		}
		
		];
	return data;
}



function ActiveGallery(){
	var data = GenImageData();
	if (data == null){
		return;
	}
	
	if (g_GllleryControl != null){
		$('#galleria').html("");
	}
	
	g_GllleryControl = $('#galleria').galleria({
			transition : 'fade',
			height : 550,
			debug : false,
			dataSource:data,
			maxScaleRatio:1
	});
	
	OnEvent();
}

function ImageXmlParser(xml) {
	var count = $(xml).find("MediaCount");
	if (count.length < 1){
		return;
	}
	
	g_iTotalImages = count.text();
	// notice:if count changes,maybe need to reload images?

	$(xml).find("Media").each(function(i) {
		var sPath = $(this).find("Path").text();
		var sGallery = $(this).find("GalleryName").text();
		var sTitle = $(this).find("Title").text();
		var sDisplayName = $(this).find("DisplayName").text();

		var photoItems = new Array(sPath, sGallery, sTitle, sDisplayName);
		g_ImageList.push(photoItems);
	});
	
	if (g_iPhotoStartItem < g_iTotalImages) {
		GetPhotoList();
	}else{
		ActiveGallery();
	}
}

function OnDeletePhoto(photoUrl){
	var filePath = photoUrl.split('file=');
	
	if(confirm(sLConfirmeDelete)){
		var sReq = "FolderList.xml?action=removefile&filelist=" + filePath[1];
		sReq += "&isRealPath=1";
		StartAjaxRequest(sReq,"xml");
	}
}

function OnEvent(){
	Galleria.ready(function() {
		this.bind("loadfinish", function(e) {
			var data = GenImageData();
			if (data == null){
				return;
			}
		
			this.push(data[0]);
		});
	});
}

function OnPhotoInit() {
	g_iTotalImages = 0;
	g_iPhotoStartItem = 0;
	g_iImgPos = 0;

	GetPhotoList();
	
	// Load the twelve theme
	Galleria.loadTheme('galleria/themes/twelve/galleria.twelve.min.js');
			
	$("#galleria").html("");

	g_ImageList = new Array();
	g_ImageList.length = 0;

	$("#photo-div").fadeIn(1000);

}

function OnPhotoClose() {
	$("#galleria").html("");

	g_ImageList.length = 0;

	$("#photo-div").hide();
}

var photoPageAdapter = new PageAdapter();
photoPageAdapter.iPageIndex = PHOTO_PAGE_INDEX;
photoPageAdapter.OnInit = function() {
	OnPhotoInit();
};
photoPageAdapter.OnClose = function() {
	OnPhotoClose();
};
photoPageAdapter.XmlParser = function(xml) {
	ImageXmlParser(xml);
};

PageFuncList.push(photoPageAdapter);
