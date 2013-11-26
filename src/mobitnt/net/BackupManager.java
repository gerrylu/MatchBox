package mobitnt.net;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Properties;
import mobitnt.backup.BackupOption;
import mobitnt.backup.BackupProcessor;
import mobitnt.util.*;


public class BackupManager extends PageGen {
	public String ProcessRequest(String request, Properties parms) {
		String sAction = parms.getProperty(EADefine.EA_ACT_ACTION_TAG, "n");

		if (sAction.equalsIgnoreCase(EADefine.EA_ACT_START_BACKUP)) {
			return BackupSMSNow();
		}
		else if (sAction.equalsIgnoreCase(EADefine.EA_ACT_GET_BACKUP_HISTORY)){
			return GetBkHistory();
		}
		else if (sAction.equalsIgnoreCase(EADefine.EA_ACT_GET_BK_OPTION)) {
			return GetBkOption();
		} else if (sAction.equalsIgnoreCase(EADefine.EA_ACT_SET_BK_OPTION)) {
			String sMailAccount = parms.getProperty(EADefine.EA_ACT_BK_MAIL_ACCOUNT_TAG, "0");
			String sMailPassword = URLDecoder.decode(parms.getProperty(EADefine.EA_ACT_BK_MAIL_PWD_TAG, ""));
			String sBkFreq = parms.getProperty(EADefine.EA_ACT_BK_FREQ_TAG, "");
			String sBkTime = parms.getProperty(EADefine.EA_ACT_BK_TIME_TAG, "12:00");
			
			BackupOption.SetOption(sMailAccount, sMailPassword,sBkFreq,sBkTime);
			
			BackupProcessor.ScheduleBackup();

			return GenRetCode(EADefine.EA_RET_OK);
		}

		return PageGen.GenRetCode(EADefine.EA_RET_UNKONW_REQ);

	}

	//
	String BackupSMSNow() {
		BackupProcessor bk = new BackupProcessor();
		bk.BackupSms();

		return GenRetCode(EADefine.EA_RET_RUNNING_AT_BACKGROUND);
	}
	
	String GetBkHistory(){
		String sBkHistory = EAUtil.GetBkHistory();
		if (sBkHistory.length() < 1){
			return GenRetCode(EADefine.EA_RET_END_OF_FILE);
		}
		
		String[] historyList = sBkHistory.split(";");
		String sXml = "<BkHistory>";
		for (int i =0; i <historyList.length; ++i){
			sXml += "<History>" + historyList[i] + "</History>"; 
		}
		sXml += "</BkHistory>";
		return sXml;
	}

	String GetBkOption() {
		String sOptions = EAUtil.GetBkOption();
		if (sOptions.length() < 4){
			return GenRetCode(EADefine.EA_RET_END_OF_FILE);
		}
		
		String[] sOpList = sOptions.split(",");
		
		String sXml = "<BKOPTION>";
		sXml += "<MailAccount>" + URLEncoder.encode(sOpList[0]) + "</MailAccount>";
		sXml += "<MailPwd>" + URLEncoder.encode(sOpList[1]) + "</MailPwd>";
		sXml += "<BkFreq>" + sOpList[2] + "</BkFreq>";
		sXml += "<BkTime>" + sOpList[3] + "</BkTime>";
		sXml += "</BKOPTION>";
		
		return sXml;
	}
}
