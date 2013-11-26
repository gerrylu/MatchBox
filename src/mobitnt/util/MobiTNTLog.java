package  mobitnt.util;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.database.Cursor;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

public class MobiTNTLog {
	static final String m_sDbName = "PocketExportLog.db";
	static public boolean m_bEnableLog = false;

	static public void write(String sInfo) {
		if (!m_bEnableLog) {
			return;
		}

		CheckLogSize();

		Date dt = new Date();

		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String sTimes = df.format(dt);

		DBHelper db = new DBHelper(m_sDbName);

		String[] recInfo = new String[3];
		recInfo[0] = "0";
		recInfo[1] = sTimes + " " + sInfo + "\r\n";
		recInfo[2] = "r";

		// ²åÈëÐÂ¼ÇÂ¼
		db.insert(recInfo);
		db.close();
	}

	static final int MAX_LOG_COUNT = 2000;

	static public void CheckLogSize() {
		DBHelper db = new DBHelper(m_sDbName);
		Cursor cursor = db.query();
		if (cursor == null || !cursor.moveToFirst()) {
			db.close();
			return;
		}

		if (cursor.getCount() > MAX_LOG_COUNT) {
			/* clear all data */
			db.clearTable();
		}

		cursor.close();
		db.close();
	}
	
	static public String GetLog() {
		DBHelper db = new DBHelper(m_sDbName);
		Cursor cursor = db.query();
		if (cursor == null || cursor.getCount() < 1) {
			db.close();
			return null;
		}

		if (!cursor.moveToFirst()) {
			return null;
		}

		StringBuilder sBuf = new StringBuilder();
		do {

			sBuf.append(cursor.getString(1));
		} while (cursor.moveToNext());

		cursor.close();
		db.close();
		return sBuf.toString();
	}

	static public int SendLog() {
		String sLog = GetLog();
		if (sLog == null) {
			return EADefine.EA_RET_END_OF_FILE;
		}

		String sUrl = "http://www.mobitnt.com/detect-phone/GetLog.php";
		String replyMail = EAUtil.GetLogReplyMail();
		if (replyMail.length() < 2){
			replyMail = "nobody@mobitnt.com";
		}
				
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("MBLOG", URLEncoder
				.encode(sLog)));
		nameValuePairs.add(new BasicNameValuePair("REPLYMAIL", URLEncoder
				.encode(replyMail)));
		
		HttpRequestHelper httpAgent = new HttpRequestHelper();
		httpAgent.doPost(sUrl, nameValuePairs);
		
		return EADefine.EA_RET_OK;
	}

}
