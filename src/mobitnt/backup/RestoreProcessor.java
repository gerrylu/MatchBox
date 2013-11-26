package mobitnt.backup;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import mobitnt.backup.IContentFilter;
import mobitnt.util.*;
import mobitnt.android.wrapper.SmsApi;
import mobitnt.android.data.*;

public class RestoreProcessor implements IContentFilter {

	//
	public static boolean m_bNeedStop = false;

	//
	static Thread runningT = null;

	// 恢复短信进度 0~100
	static int m_nRestoreProgress = 0;

	// 错误描述
	static String m_sLastError = "";
	static int m_nLastErrorId = 0;

	// mail list
	static Map<String, String> m_mapMail;
	public static int m_nGetSmsBackupListProgress = 0;

	// 当前操作类型
	int m_nOpType = -1;

	// mail key
	String m_sMailSubject;

	//
	public RestoreProcessor() {
	}

	public String GetLastError() {
		return m_sLastError;
	}

	static public void StopThread() {
		if (runningT != null && runningT.isAlive()) {
			m_bNeedStop = true;

			try {
				runningT.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public int RestoreSms(String sSubjectKey) {
		if (runningT != null && runningT.isAlive()) {
			MobiTNTLog.write("AlreadyRunning");
			return EADefine.EA_RET_THREAD_IS_ALIVE;
		}

		// check mail account is empty
		if (BackupOption.GetMailAccount().length() <= 0
				|| BackupOption.GetMailPassword().length() <= 0) {
			m_nGetSmsBackupListProgress = -1;
			m_sLastError = "invalid mail account";
			return EADefine.EA_RET_SMS_INVALID_EMAIL;
		}

		m_sMailSubject = new String();
		m_sMailSubject = sSubjectKey;

		runningT = new Thread(new Runnable() {
			public void run() {
				EAUtil.SetServiceState(EAUtil.SRV_STATE_RESOTORE_SMS);
				DoRestoreSmsFromMailProc();
				EAUtil.SetServiceState(EAUtil.SRV_STATE_WAIT);
			}
		});

		runningT.start();

		return EADefine.EA_RET_OK;
	}

	public int GetBackupSmsList() {
		if (runningT != null && runningT.isAlive()) {
			MobiTNTLog.write("AlreadyRunning");
			return -1;
		}

		// check mail account is empty
		if (BackupOption.GetMailAccount().length() <= 0
				|| BackupOption.GetMailPassword().length() <= 0) {
			MobiTNTLog.write("invalid email account or password");
			m_nGetSmsBackupListProgress = -1;
			m_nLastErrorId = EADefine.EA_RET_SMS_INVALID_EMAIL;
			m_sLastError = "mail account or password is null";

			return EADefine.EA_RET_SMS_INVALID_EMAIL;
		}

		runningT = new Thread(new Runnable() {
			public void run() {
				EAUtil.SetServiceState(EAUtil.SRV_STATE_RESOTORE_SMS);
				DoGetBackupSmsListProc();
				EAUtil.SetServiceState(EAUtil.SRV_STATE_WAIT);
			}
		});

		runningT.start();

		return EADefine.EA_RET_OK;
	}

	// get the result of sms list
	public static Map<String, String> GetBackupSmsListResult() {
		return m_mapMail;
	}

	// get
	synchronized int DoGetBackupSmsListProc() {
		m_nOpType = OP_GET_BACKUP_SMS;
		m_nGetSmsBackupListProgress = 0;
		m_mapMail = new HashMap<String, String>();

		MailReader.receive(BackupOption.GetMailAccount(),
				BackupOption.GetMailPassword(), this);

		m_nGetSmsBackupListProgress = 100;

		return 0;
	}

	synchronized int DoRestoreSmsFromMailProc() {
		if (m_sMailSubject.length() <= 0) {
			m_nRestoreProgress = -1;
			m_sLastError = "m_sSubjectKey is null";
			return -1;
		}

		if (m_mapMail != null && !m_mapMail.isEmpty() /*
													 * && m_mapMail.containsKey(
													 * m_sMailSubject)
													 */) {
			for (@SuppressWarnings("rawtypes") Map.Entry entry : m_mapMail.entrySet()) {
				// System.out.println(entry.getKey()+"="+entry.getValue());

				String sKey = (String) entry.getKey();
				if (sKey.indexOf(m_sMailSubject) >= 0) {
					String sContent = (String) entry.getValue();
					if (sContent.length() > 0) {
						try {
							doInsertSms(sContent);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		}

		return 0;
	}

	public int ParseMailContent(String sSubject, String sContent)
			throws Exception {
		// TODO Auto-generated method stub
		try {
			switch (m_nOpType) {
			case OP_GET_BACKUP_SMS:
				ParseBackupSms(sSubject, sContent);
				break;

			case OP_RESTORE_SMS:
				ParseRestoreSMS(sSubject, sContent);
				break;

			case OP_RESTORE_CALL:
				ParseCall(sSubject, sContent);
				break;

			default:
				MobiTNTLog.write("invalid optype");
				break;
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return 0;
	}

	int ParseBackupSms(String sSubject, String sContent) {

		// if or not, check date between start time and end time.
		// sSubject : backup sms,mobileno:89860082190653572040,date:2012-06-14
		// 11:03:07
		if (!sSubject.startsWith("backup sms")) {
			return -1;
		} else {
			if (m_mapMail != null) {
				m_mapMail.put(sSubject, sContent);
			}
		}

		return 0;

	}

	int ParseRestoreSMS(String sSubject, String sContent) throws Exception {
		int nPosition = 0;
		String sRetVal = "";
		String sMailSubject = sSubject;

		// if or not, check date between start time and end time.
		// sSubject : backup sms,mobileno:89860082190653572040,date:2012-06-14
		// 11:03:07
		if (!sMailSubject.startsWith("backup sms:")) {
			sRetVal = String.format("[error], %s", sMailSubject);
			MobiTNTLog.write(sRetVal);

			return -1;
		} else {
			// eliminate header
			sMailSubject.replace("backup sms,", "");
		}

		// Get mobile serial no
		if (sMailSubject.startsWith("mobileno:")) {
			// eliminate header
			sMailSubject.replace("mobileno:", "");

			nPosition = sMailSubject.indexOf(",");

		/*	if (nPosition > 0) {
				String sMobileSerialNo = sMailSubject.substring(0, nPosition);
			}*/
		}

		// Get date
		nPosition = sMailSubject.indexOf("date:");
		if (nPosition > 0) {
			//String sCurDate = sMailSubject.substring(nPosition + 5);

			// 比较是在这个选择的时间段
			/*
			 * if (!Utility.Between(sCurDate, m_sStartDate, m_sEndDate)) {
			 * MobiTNTLog.write("your choice date is not between %s"); return
			 * -1; }
			 */
		}

		return 0;
	}

	// restore sms
	int doInsertSms(String sContent) throws Exception {
		try {
			InputStream inputStream = new ByteArrayInputStream(
					sContent.getBytes("UTF-8"));

			List<SmsInfo> personsList = SaxXMLManager.ReadSMS(inputStream);

			for (Iterator<SmsInfo> iterator = personsList.iterator(); iterator
					.hasNext();) {
				SmsInfo sms = (SmsInfo) iterator.next();

				// insert sms into inbox
				SmsApi.InsertSms(sms);
			}

		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}

		return 0;
	}

	static public String GetSimpleContent(String sContent) {
		// 由于显示问题，这里只返回一条信息
		// 注：这里使用解析XML类太慢，所以采用直接取字符串方式
		String sRetVal = "";

		int nStartPos = sContent.indexOf("<Body>");
		int nEndPos = sContent.indexOf("</Body>");
		String sBody = sContent.substring(nStartPos + 6, nEndPos);

		nStartPos = sContent.indexOf("<Address>");
		nEndPos = sContent.indexOf("</Address>");
		String sAddress = sContent.substring(nStartPos + 9, nEndPos);

		nStartPos = sContent.indexOf("<Date>");
		nEndPos = sContent.indexOf("</Date>");
		String sDate = sContent.substring(nStartPos + 6, nEndPos);

		if (sAddress != null && sAddress.length() > 0) {
			sRetVal += sAddress;
		}

		if (sDate != null && sDate.length() > 0) {
			sRetVal += ", ";
			sRetVal += sDate;
		}

		if (sBody != null && sBody.length() > 0) {
			sRetVal += ", ";
			sRetVal += sBody;
		}

		if (sRetVal.length() > 50) {
			sRetVal = sRetVal.substring(0, 50) + "...";
		}

		return sRetVal;
	}

	public int ParseCall(String sSubject, String sContent) {
		m_nOpType = OP_RESTORE_CALL;
		// MailReader.receive(m_sMailAccount, m_sMailPwd, this);
		return 0;
	}

}
