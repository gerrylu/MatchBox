package mobitnt.backup;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import mobitnt.util.*;
import mobitnt.android.data.*;
import mobitnt.android.wrapper.SmsApi;
import mobitnt.backup.IContentFilter;
import android.text.format.DateFormat;
import android.util.Log;


public class BackupProcessor implements IContentFilter {

	static Thread runningT = null;

	public static boolean m_bDeleteAfterBk = false;
	public static boolean m_bNeedStop = false;

	int m_nOpType = -1;

	// String m_sStartDate;
	// String m_sEndDate;
	//String m_sLastError;

	static int m_nBackupSmsProgress = 0; // 备份短信进度0-100
	static int m_nRestoreSmsProgress = 0; // 恢复短信进度0~100

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
	
	static Timer BkTimer = null;

	static public void ScheduleBackup() {
		if (BkTimer != null){
			BkTimer.cancel();	
		}
		
		long ONE_DAY = 1000 * 60 * 60 * 24;
		
		long period = ONE_DAY;
		
		if (BackupOption.GetBkFreq() == BackupOption.FREQ_EVERY_WEEK){
			period = 7 * ONE_DAY;
		}else if (BackupOption.GetBkFreq() == BackupOption.FREQ_EVERY_MONTH){
			period = 30 * ONE_DAY;
		}
			
		BkTimer = new Timer();
		BkTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				BackupProcessor bk = new BackupProcessor();
				bk.BackupSms();
			}
		}, 0, period);

	}

	// static BackupProcessor m_BK = null;

	public BackupProcessor() {
		// m_BK = this;
	}

	public int BackupCall() {
		Log.e("EA", "Not implemented yet");
		m_nOpType = OP_BACKUP_CALL;
		return 0;
	}

	public int BackupSms() {
		if (!EAUtil.IsInternetReachable()) {
			MobiTNTLog.write("internet is not reachable");
			return EADefine.EA_RET_INTERNET_NON_REACHABLE;
		}

		// check mail account is empty
		if (BackupOption.GetMailAccount() == null
				|| BackupOption.GetMailPassword() == null) {
			MobiTNTLog.write("invalid mail account");
			return EADefine.EA_RET_SMS_INVALID_EMAIL;
		}

		if (runningT != null && runningT.isAlive()) {
			MobiTNTLog.write("AlreadyRunning");
			return EADefine.EA_RET_THREAD_IS_ALIVE;
		}

		runningT = new Thread(new Runnable() {
			public void run() {
				
				int iRet = BackupSms2Mail();
				Date dt = new Date();
				SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// 设置日期格式
				String sBkTime = df.format(dt);
				String sInfo = sBkTime + "," + String.valueOf(iRet);
				EAUtil.SaveBkHistory(sInfo);
			}
		});

		runningT.start();

		return EADefine.EA_RET_OK;
	}

	synchronized int BackupSms2Mail() {
		// 先获取所有thread 列表
		List<SmsThreadInfo> threadList = SmsApi.GetThreadList();
		if (threadList == null || threadList.size() < 1) {
			m_nBackupSmsProgress = 100;
			return -1;
		}
	
		m_nBackupSmsProgress = 5;

		try {
	
			int MAX_COUNT = 500;
			int iFrom = 0;
			long lMaxSmsId = 0;
			String sError = "n";
			
			long lLastBkTime = BackupOption.GetLastSyncTime();
			
			Date today = new Date();
			SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String sDatatime = f.format(today);
			String sSubject = String.format("backup sms by MatchBox,Time:%s",sDatatime);

			while (true) {
				List<SmsInfo> smsList = SmsApi.GetSmsItem(iFrom, MAX_COUNT);
				if (smsList == null || smsList.size() < 1) {
					break;
				}
				
				StringBuilder smsData = new StringBuilder();

				int iSmsNeedUpdateCount = smsList.size();
				for (int m = 0; m < iSmsNeedUpdateCount; ++m) {
					if (lMaxSmsId < smsList.get(m).lMsgId) {
						lMaxSmsId = smsList.get(m).lMsgId;
					}
					
					long lTime = smsList.get(m).lTimeStamp;
					if (lTime <= lLastBkTime){
						lLastBkTime = lTime;
						continue;
					}
					
					smsData.append("\r\n=========msg head==============\r\n");

					// add Address
					smsData.append("Address:");
					smsData.append(smsList.get(m).sPhone);
					smsData.append("\r\n");

					// add Date
					smsData.append("Date:");
					String sDate = DateFormat.format("yyyy-MM-dd kk:mm:ss",lTime).toString();
					smsData.append(sDate);
					smsData.append("\r\n");

					// add IsRead
					smsData.append("Body:");
					
					smsData.append(smsList.get(m).sBody);
					smsData.append("\r\n");

					smsData.append("========msg end===============\r\n");
				}

				iFrom += iSmsNeedUpdateCount;

				m_nBackupSmsProgress++;
				if (m_nBackupSmsProgress > 70) {
					m_nBackupSmsProgress = 70;
				}
					
				String sBody = smsData.toString();

				if (0 != MailSender.SendMail(sSubject, sBody, sError)) {
					MobiTNTLog.write(sError);
					m_nBackupSmsProgress = -1;
					return -1;
				}
				
				BackupOption.SetLastSyncTime(lLastBkTime);
			}

			m_nBackupSmsProgress = 100;
		} catch (Exception e) {
			// MobiTNTLog.write(sError);
			m_nBackupSmsProgress = -1;
			return -1;
		}

		return 0;
	}

	public int ParseMailContent(String sSubject, String sContent)
			throws Exception {
		// TODO Auto-generated method stub

		/*
		 * switch (m_nOpType) { case OP_RESTORE_SMS: try { ParseSMS(sSubject,
		 * sContent); } catch (ParseException e) { // TODO Auto-generated catch
		 * block e.printStackTrace(); } break;
		 * 
		 * case OP_RESTORE_CALL: ParseCall(sSubject, sContent); break;
		 * 
		 * default: MobiTNTLog.write("invalid optype"); break; }
		 */

		return 0;
	}

	public int ParseXMLContent(String sContent) throws Exception {
		// //////////////////////////
		/*
		 * try { OutputStream os = openFileOutput("blog.xml",
		 * MODE_WORLD_READABLE); OutputStreamWriter osw=new
		 * OutputStreamWriter(os); osw.write(sContent); osw.close(); os.close();
		 * } catch(FileNotFoundException e) { return -1; } catch(IOException e)
		 * { return -1; }
		 */

		// ////////////////////////////////
		/*
		 * SAXParserFactory factory = SAXParserFactory.newInstance(); SAXParser
		 * parser = factory.newSAXParser(); XMLReader xmlreader =
		 * parser.getXMLReader(); URL rrl = new URL(urlstring); InputSource is =
		 * new InputSource(url.openStream()); Xmlreader.setContentHanlder(XXXX);
		 * Xmlreader.parse(is);
		 */

		/*
		 * try { InputStream inputStream = new ByteArrayInputStream(
		 * sContent.getBytes("UTF-8")); List<SmsInfo> personsList =
		 * SaxXMLManager.ReadSMS(inputStream);
		 * 
		 * for (Iterator<SmsInfo> iterator = personsList.iterator(); iterator
		 * .hasNext();) { SmsInfo sms = (SmsInfo) iterator.next(); }
		 * 
		 * } catch (UnsupportedEncodingException e) { // TODO Auto-generated
		 * catch block e.printStackTrace(); }
		 */

		return 0;
	}

	//

	//
	int ParseCall(String sSubject, String sContent) {

		return 0;
	}

	public static int GetSmsBackupProgress() {
		return m_nBackupSmsProgress;
	}

	static int GetSmsRestoreProgress() {
		return m_nRestoreSmsProgress;
	}

}
