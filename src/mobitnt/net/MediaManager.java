package mobitnt.net;

import java.util.List;
import java.util.Properties;

import mobitnt.util.*;
import mobitnt.media.EAImage;
import mobitnt.media.EAImageProvider;

/**
 * @author hamigua
 * 
 */
public class MediaManager extends PageGen {
	
	String m_sXmlHdr = "<MediaList> <MediaCount>%d</MediaCount>";

	String GetImageList(int iFrom, int iTo) {
		/*
		 * <ImageFolderList> <ImageCount>2</ImageCount>
		 * <Path>/sdcard/DICM</Path> <Path>/sdcard/DICM/ph</Path>
		 * </ImageFolderList>
		 */

		
		//String PathFmt = "<Media><Path>%s</Path><Title>%s</Title><DisplayName>%s</DisplayName><GalleryName>%s</GalleryName></Media>";
		int iCount = EAImageProvider.getCount();
		if (iFrom >= iCount || iFrom > iTo || iCount == 0) {
			return GenRetCode(EADefine.EA_RET_END_OF_FILE);
		}

		if (iTo > iCount) {
			iTo = iCount;
		}

		List<EAImage> PhotoList = (List<EAImage>) EAImageProvider.getList(
				iFrom, iTo);
		if (PhotoList == null) {
			return String.format(m_sXmlHdr, iCount) + "</MediaList>";
		}

		int iMediaCount = PhotoList.size();
		StringBuilder sList = new StringBuilder("<MediaList><MediaCount>");
		sList.append(iCount);
		sList.append("</MediaCount>");
		
		for (int i = 0; i < iMediaCount; ++i) {
			EAImage v = PhotoList.get(i);
			
			sList.append("<Media>");
			
			sList.append("<Path>");
			sList.append(v.getPath());
			sList.append("</Path>");
			
			sList.append("<Title>");
			sList.append(v.getTitle());
			sList.append("</Title>");
			
			sList.append("<DisplayName>");
			sList.append(v.getDisplayName());
			sList.append("</DisplayName>");
			
			sList.append("<GalleryName>");
			sList.append(v.getGalleryName());
			sList.append("</GalleryName>");
			
			sList.append("</Media>");
			
		}

		sList.append("</MediaList>");

		return sList.toString();

	}


	/*
	 * URL format :action=getfile&&file=sdcard/aa
	 */
	public String ProcessRequest(String request, Properties parms) {

		int iFrom = 0;
		int iTo = 0;

		String sAction = parms.getProperty(EADefine.EA_ACT_ACTION_TAG, EADefine.EA_ACT_GET_IMAGE_LIST);
		sAction = sAction.toLowerCase();
		String sFrom = parms.getProperty(EADefine.EA_ACT_FROM_TAG, "0");
		String sTo = parms.getProperty(EADefine.EA_ACT_TO_TAG, "0");

		iFrom = Integer.valueOf(sFrom);
		iTo = Integer.valueOf(sTo);

		if (sAction.equals(EADefine.EA_ACT_GET_IMAGE_LIST)) {
			return GetImageList(iFrom, iTo);
		} 
		
		return GenRetCode(EADefine.EA_RET_FAILED);
	}

}
