package mobitnt.backup;

import mobitnt.util.EAUtil;


public class BackupOption {
	static String m_sMailAccount;
	static String m_sMailPassword;
	static String m_sBkFreq;
	static String m_sBkTime;
	
	static final int FREQ_EVERY_DAY = 0;
	static final int FREQ_EVERY_WEEK = 1;
	static final int FREQ_EVERY_MONTH = 2;

	public static String GetMailAccount() {
		return m_sMailAccount;
	}

	public static String GetMailPassword() {
		return m_sMailPassword;
	}

	public static int GetBkFreq() {
		return Integer.parseInt(m_sBkFreq);
	}

	public static String GetBkTime() {
		return m_sBkTime;
	}

	public static void Load() {
		String sOptions = EAUtil.GetBkOption();
		if (sOptions.length() < 4) {
			m_sMailAccount = "";
			m_sMailPassword = "";
			m_sBkFreq = "";
			m_sBkTime = "";
			return;
		}

		String[] sOpList = sOptions.split(",");
		if (sOpList.length < 4){
			SetOption("","","","");
			return;
		}

		m_sMailAccount = sOpList[0];
		m_sMailPassword = sOpList[1];
		m_sBkFreq = sOpList[2];
		
		m_sBkTime = sOpList[3];
	}
	
	public static void SetLastSyncTime(long lTime){
		EAUtil.SetBkSyncTime(lTime);
	}

	public static long GetLastSyncTime(){
		return EAUtil.GetBkSyncTime();
	}

	public static void SetOption(String sAccount, String sPwd, String sBkFreq,String sBkTime) {
		m_sMailAccount = sAccount;
		m_sMailPassword = sPwd;
		m_sBkFreq = sBkFreq;
		m_sBkTime = sBkTime;
		EAUtil.SetBkOption(m_sMailAccount, m_sMailPassword, m_sBkFreq,m_sBkTime);
	}

}
