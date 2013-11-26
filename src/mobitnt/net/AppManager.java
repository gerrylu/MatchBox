package mobitnt.net;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Properties;

import mobitnt.android.data.AppPackageInfo;
import mobitnt.android.wrapper.AppApi;
import mobitnt.util.*;


public class AppManager extends PageGen {

	public String ProcessRequest(String request, Properties parms) {

		String sAction = parms.getProperty(EADefine.EA_ACT_ACTION_TAG,
				EADefine.EA_ACT_GET_APP_LIST);
		if (sAction.contains(EADefine.EA_ACT_REMOVE_APP)) {
			String sAppName = parms.getProperty(EADefine.EA_ACT_APP_NAME_TAG,
					"");
			AppApi.RemoveApp(sAppName);
			return GenRetCode(EADefine.EA_RET_NEED_OP_ON_PHONE);
		}
		
		if (sAction.equals(EADefine.EA_ACT_GET_APK_STORE_PATH)){
			String sApkStorePath = AppApi.GetApkStorePath();
			return "<" + EADefine.EA_ACT_APK_PATH_TAG + ">" + sApkStorePath + "<" + EADefine.EA_ACT_APK_PATH_TAG + "/>"; 
		}
		
		if (sAction.equals(EADefine.EA_ACT_INSTALL_APP)){
			String sApkPath = parms.getProperty(EADefine.EA_ACT_APK_PATH_TAG);
			AppApi.InstallApp(sApkPath);
			
			return GenRetCode(EADefine.EA_RET_NEED_OP_ON_PHONE);
		}

		if (sAction.contains(EADefine.EA_ACT_REFRESH_APP_LIST)) {
			AppApi.updateAppList(false);
			sAction = EADefine.EA_ACT_GET_APP_LIST;
		}

		if (sAction.contains(EADefine.EA_ACT_GET_APP_LIST)) {
			String sFrom = parms.getProperty(EADefine.EA_ACT_FROM_TAG, "0");
			int iFrom = Integer.parseInt(sFrom);
			return GetAppList(iFrom);
		}

		return GenRetCode(EADefine.EA_RET_UNKONW_REQ);

	}

	String GetAppList(int iFrom) {

		ArrayList<AppPackageInfo> appList = AppApi.GetAppList(iFrom);
		if (appList == null || appList.size() < 1) {
			return GenRetCode(EADefine.EA_RET_END_OF_FILE);
		}

		/*
		 * /* <AppList> <App> <Name>Free mem</Name> <PackageName>0</PackageName>
		 * <VersionName>59 seconds</VersionName> </App> </AppList>
		 */

		int iAppCount = AppApi.GetAppCount();

		// String sXmlFmt =
		// "<App><Name>%s</Name><PackageName>%s</PackageName><VersionName>%s</VersionName></App>\r\n";

		StringBuilder sXml = new StringBuilder();
		sXml.append("<AppList>");
		sXml.append("<AppCount>");
		sXml.append(iAppCount);
		sXml.append("</AppCount>");

		for (int i = iFrom; i < appList.size(); ++i) {
			AppPackageInfo appItem = appList.get(i);
			// sXml +=
			// String.format(sXmlFmt,appItem.appname,appItem.pname,appItem.versionName);

			sXml.append("<App>");

			sXml.append("<Name>");
			sXml.append(URLEncoder.encode(appItem.appname));
			sXml.append("</Name>");

			sXml.append("<PackageName>");
			sXml.append(URLEncoder.encode(appItem.pname));
			sXml.append("</PackageName>");

			sXml.append("<VersionName>");
			sXml.append(URLEncoder.encode(appItem.versionName));
			sXml.append("</VersionName>");

			sXml.append("</App>");
		}

		sXml.append("</AppList>\r\n");
		return sXml.toString();
	}

}
