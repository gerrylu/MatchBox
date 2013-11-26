package mobitnt.net;

import java.util.List;
import java.util.Properties;

import mobitnt.android.wrapper.SysApi;
import mobitnt.util.*;


public class SysManager  extends PageGen {
	public int m_iReqType;
	
	String GetSysInfo() {
		/*
		 * <?xml version="1.0" encoding="UTF-8"?> <SysInfoList> <Entry>
		 * <Name>Free mem</Name> <Value>45 M</Value> </Entry> </SysInfoList>
		 */

		String sEntryFormat = "<Entry><Name>%s</Name><Value>%s</Value></Entry>";

		String sXml = "<SysInfoList>";

		List<String> sSysInfo = SysApi.getSysInfo();
		if (sSysInfo == null || sSysInfo.size() < 1) {
			return null;
		}

		for (int i = 0; i < sSysInfo.size(); ++i) {
			String sItmes[] = sSysInfo.get(i).split(":");
			if (sItmes.length == 2) {
				sXml += String.format(sEntryFormat, sItmes[0], sItmes[1]);
			}
		}
		
		sXml += String.format(sEntryFormat, EADefine.EA_ACT_SYS_LOG_STATE_TAG, GetLogState());

		sXml += "</SysInfoList>";

		return sXml;
	}

/*	static String m_sLangType = "var g_CurrentLangType = 0;function GetLangType(){return g_CurrentLangType;}";
	static boolean m_bNeedInitLangType = true;

	static public String GetLangType() {
		if (m_bNeedInitLangType) {
			SharedPreferences pref = PreferenceManager
					.getDefaultSharedPreferences(EAUtil.GetEAContext());
			int iLangType = 0;
			if (pref != null) {
				iLangType = pref.getInt("LangType", 0);
			}
			String sJsFmt = "var g_CurrentLangType = %d;function GetLangType(){return g_CurrentLangType;}var g_sIMEI=%s;";
			m_sLangType = String.format(sJsFmt, iLangType, SysApi.getImei());
			m_bNeedInitLangType = false;
		}

		return m_sLangType;
	}

	String SetLangType(int iLangType) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(EAUtil.GetEAContext());
		if (pref != null) {
			SharedPreferences.Editor mEditor = pref.edit();
			mEditor.putInt("LangType", iLangType);
			mEditor.commit();
		}
		
		String sJsFmt = "function GetLangType(){return %d;}";
		m_sLangType = String.format(sJsFmt, iLangType);

		return GenRetCode(EADefine.EA_RET_OK);
	}*/
	
	private int GetLogState(){
		boolean bEnable = EAUtil.GetLogState();
		if (bEnable){
			return 1;
		}
		
		return 0;
	}

	
	public String ProcessRequest(String sReq, Properties parms) {
		//String sLangType = parms.getProperty(EADefine.EA_ACT_LANG_TYPE_TAG);
		
		
	/*	String sAction = parms.getProperty(EADefine.EA_ACT_ACTION_TAG,EADefine.EA_ACT_GET_SYS_INFO_TAG);
		if (sAction != null && sAction.length() > 0) {
			return "var g_sIMEI=" + SysApi.getImei();
		}
			
		String sImei = parms.getProperty(EADefine.EA_ACT_CHECK_IMEI_TAG);
		if (sImei != null && sImei.length() > 0) {
			if (sImei.equals(SysApi.getImei())){
				return GenRetCode(EADefine.EA_RET_OK);
			}
			
			return GenRetCode(EADefine.EA_RET_FAILED);
		}*/
		
		
		return GetSysInfo();
	}
}
