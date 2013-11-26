/**
 * 
 */
package mobitnt.net;

import java.net.URLDecoder;
import java.util.List;
import java.util.Properties;

import mobitnt.android.data.FileProperty;
import mobitnt.android.wrapper.FileApi;
import mobitnt.android.wrapper.SysApi;
import mobitnt.util.*;

/**
 * @author hamigua
 * 
 */
public class StorageManager extends PageGen {
	// String m_sCurrentFolder;

	
	String GetVirtualPath(String sRealPath){
		String sExternalRoot = FileApi.getExternalStoragePath();
		sRealPath = sRealPath.replace(sExternalRoot, "/");
		sRealPath = sRealPath.replace("//", "/");
		return sRealPath;
	}

	public String ProcessRequest(String request, Properties parms) {
		try {
			int iFrom = 0;
			String sFilePath = "/";

			String sAction = parms.getProperty(EADefine.EA_ACT_ACTION_TAG, EADefine.EA_ACT_GET_FOLDER_LIST);
			sAction = sAction.toLowerCase();
			sFilePath = parms.getProperty(EADefine.EA_ACT_FILE_TAG, "n");
			sFilePath = FileApi.GetRealPath(sFilePath);
			// sUpFileName = parms.getProperty("fileName","");

			if (sAction.contains(EADefine.EA_ACT_GET_FOLDER_LIST)) {
				String sFrom = parms.getProperty(EADefine.EA_ACT_FROM_TAG, "0");

				iFrom = Integer.valueOf(sFrom);
				return GetFolderList(sFilePath, iFrom);
			}

			if (sAction.contains(EADefine.EA_ACT_REMOVE_FILE)) {
				String sFileList = parms.getProperty(EADefine.EA_ACT_FILE_LIST_TAG, "n");
				sFileList = URLDecoder.decode(sFileList, "UTF-8");
				String sTmp = parms.getProperty(EADefine.EA_ACT_IS_REAL_PATH_TAG, "n");
				boolean bIsRealPath = true;
				if (sTmp.equals("n")){
					bIsRealPath = false;
				}
				if (sFileList.equals("n")){
					return GenRetCode(EADefine.EA_RET_FAILED);
				}
				
				String[] sFiles = sFileList.split(",");
				for (int i = 0; i < sFiles.length; ++i){
					if (sFiles[i].length() <= 1){
						continue;
					}
					
					String sPath = sFiles[i];
					if (!bIsRealPath){
						sPath = FileApi.GetRealPath(sPath);	
					}
					
					if (!FileApi.RemoveFile(sPath)) {
						return GenRetCode(EADefine.EA_RET_FAILED);
					}
				}
				
				return GenRefreshCmd();
			}
			
			if (sAction.contains(EADefine.EA_ACT_CREATE_FILE)) {
				if (FileApi.CreateFolder(sFilePath)) {
					return GenRefreshCmd();
				}
				return GenRetCode(EADefine.EA_RET_FAILED);
			}
			
			if (sAction.contains(EADefine.EA_ACT_RENAME_FILE)) {
				String sNewPath = parms.getProperty(EADefine.EA_ACT_NEW_PATH_TAG, "n");
				sNewPath = FileApi.GetRealPath(sNewPath);
				if (FileApi.Rename(sFilePath, sNewPath)) {
					return GenRefreshCmd();
				}
				return GenRetCode(EADefine.EA_RET_FAILED);
			}
			
			if (sAction.contains(EADefine.EA_ACT_GET_EXTERNAL_ROOT_FOLDER)) {
				String sExtPath = FileApi.getExternalStoragePath();
				if (sExtPath.length() <= 1){
					return GenRetCode(EADefine.EA_RET_NO_EXTERNAL_STOREAGE);
				}

				return "<RootPath>" + sExtPath + "</RootPath>";
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			return ReturnException(e.toString());
		}
		
		return GenRetCode(EADefine.EA_RET_UNKONW_REQ);

	}

	String GetFolderList(String sFolderPath, int iFrom) {
		/*
		 * <?xml version="1.0" encoding="UTF-8"?> <FolderList>
		 * <CurrentFolder>/sdcard</CurrentFolder> <TotalCount>16</TotalCount>
		 * <Folders> <Folder> <Name>aa Free mem</Name> <Size>0</Size>
		 * <Type>0</Type> </Folder> </Folders> </FolderList>
		 */

		List<FileProperty> files = FileApi.GetFolderList(sFolderPath, iFrom);
		if (files == null || files.size() < 1) {
			if (iFrom > 0){
				return GenRetCode(EADefine.EA_RET_END_OF_FILE);	
			}
		}

		int iTotalCount = FileApi.GetFileCountInFolder(sFolderPath);
		/*
		 * if (iTo > iFileCount - 1){ iTo = iFileCount - 1; }
		 */

		StringBuilder sXml = new StringBuilder();
		sXml.append("<FolderList>");
		sXml.append("<CurrentFolder>" + GetVirtualPath(sFolderPath) + "</CurrentFolder>");
		sXml.append("<TotalCount>" + Integer.toString(iTotalCount)
				+ "</TotalCount>");
		sXml.append("<Folders>");
		if (files != null){
			for (int i = 0; i < files.size(); ++i) {
				sXml.append("<Folder>");
				String sFileName = /* m_sCurrentFolder + */files.get(i).m_sFileName;
				sXml.append("<Name>" + sFileName + "</Name>");

				// String sFileSize = Integer.toString((int)
				// files[iPos].m_iFileSize);
				String sFileSize = SysApi
						.formatSize((long) files.get(i).m_iFileSize);
				sXml.append("<Size>" + sFileSize + "</Size>");

				String sType = String.format("<Type>%d</Type>",
						files.get(i).m_iFileType);
				sXml.append(sType);

				sXml.append("</Folder>");
			}		
		}
	
		sXml.append("</Folders>");
		sXml.append("</FolderList>");

		return sXml.toString();
	}
}
