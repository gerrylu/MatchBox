package mobitnt.android.wrapper;


import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mobitnt.android.data.*;
import mobitnt.net.NanoHTTPD;
import mobitnt.util.EADefine;
import mobitnt.util.EALooperTask;
import mobitnt.util.EAUtil;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.telephony.SmsManager;
import android.text.format.DateUtils;
import android.util.Log;

public class SmsApi {
	
	static public final String SMS_URI_ALL	  = "content://sms/"; 		//0  
	//public final static String SMS_URI_INBOX  = "content://sms/inbox";	//1  
	static public final String SMS_URI_SEND   = "content://sms/sent";	//2  
	static public final String SMS_URI_DRAFT  = "content://sms/draft";	//3  
	static public final String SMS_URI_OUTBOX = "content://sms/outbox";	//4  
	static public final String SMS_URI_FAILED = "content://sms/failed";	//5  
	static public final String SMS_URI_QUEUED = "content://sms/queued";	//6  
	static public final int MAX_THREAD_IN_CACHE = 15000;
	static public final int MAX_THREAD_ITEM_PER_PAGE = 10;
	static public final int MAX_CHAT_LIST_IN_THREAD_LIST = 10;//thread列表中最多保存的完整对话数目
	static public final int MAX_REQUEST_COUNT = 10;
	
	
	static int m_iThreadCount = 0;
	static List<SmsThreadInfo> m_ThreadList = null;
	

	static public int GetThreadCount() {
		if (m_iThreadCount > 0){
			return m_iThreadCount;
		}
		GetThreadList();
		return m_iThreadCount;
	}
	
	
	/* only reserve 6 pages cache */
	static public void FreeThreadChatList(int iReserveFrom) {
		if (m_ThreadList == null || m_ThreadList.size() < 1
				|| iReserveFrom >= m_ThreadList.size()) {
			return;
		}

		int iStart = (iReserveFrom / MAX_THREAD_ITEM_PER_PAGE - MAX_CHAT_LIST_IN_THREAD_LIST/2)
				* MAX_THREAD_ITEM_PER_PAGE;
		if (iStart < 1) {
			iStart = 1;
		}
		int iTo = (iStart + MAX_CHAT_LIST_IN_THREAD_LIST/2) * MAX_THREAD_ITEM_PER_PAGE;
		if (iTo > m_ThreadList.size()) {
			iTo = m_ThreadList.size();
		}

		// always reserve first page
		for (int i = MAX_THREAD_ITEM_PER_PAGE; i < iStart; ++i) {
			SmsThreadInfo thread = m_ThreadList.get(i);
			if (thread == null) {
				continue;
			}
			thread.chatList = null;
		}

		for (int i = iTo; i < m_ThreadList.size(); ++i) {
			SmsThreadInfo thread = m_ThreadList.get(i);
			if (thread == null) {
				continue;
			}
			thread.chatList = null;
		}
	}

	static public int GetFirstMsg4Thread(int iFrom) {
		if (m_ThreadList == null || m_ThreadList.size() < 1
				|| iFrom >= m_ThreadList.size()) {
			return -1;
		}

		int iTo = iFrom + MAX_THREAD_ITEM_PER_PAGE;
		if (iTo >=  m_ThreadList.size()){
			iTo = m_ThreadList.size();
		}

		for (int i = iFrom; i < iTo; ++i) {
			SmsThreadInfo thread = m_ThreadList.get(i);
			if (thread == null || thread.chatList != null) {
				continue;
			}

			SmsApi.GetSmsConversation(thread,0,1);
			SmsApi.GetThreadContactName(thread);
		}

		return 0;
	}
	
	static public int UpdateThreadContact(){
		return -1;
	}

	static public SmsThreadInfo FindThreadInList(long lThreadID) {
		if (m_ThreadList == null) {
			return null;
		}

		SmsThreadInfo thread = null;

		for (int i = 0; i < m_ThreadList.size(); ++i) {
			thread = m_ThreadList.get(i);
			if (thread.threadId == lThreadID) {
				return thread;
			}
		}

		return null;
	}

	//获取指定thread的短信列表
	static public List<SmsInfo> GetSmsChat(long threadId,int iFrom) {
		SmsThreadInfo thread = FindThreadInList(threadId);
		if (thread == null){
			return null;
		}

		if (thread.chatList == null || iFrom >= thread.chatList.size()) {
			// if there is no required items in cache,load 1 item first and then
			// load max size in next loop
			// this will make user feels faster:)
		
			SmsApi.GetSmsConversation(thread,iFrom, 1);
			
			//final SmsThreadInfo t = thread;
			NanoHTTPD.AddLooperTask(new EALooperTask(iFrom + 1,thread) {
				public void run() {
					SmsApi.GetSmsConversation((SmsThreadInfo)m_object,m_iData, MAX_REQUEST_COUNT);
				};
			});
		}

		return thread.chatList;
	}

	static public List<SmsThreadInfo> GetThreadList() {
		if (m_ThreadList != null) {
			return m_ThreadList;
		}
		
		String selection = "1 = 1) group by (thread_id";
		//String[] selectionArgs = new String[] { "1" };

		Cursor cursor = EAUtil.GetContentResolver().query(
				Uri.parse(SMS_URI_ALL),
				new String[] { "thread_id, address, count(*) as count" },
				selection, null, null);
				
		if (cursor == null || cursor.getCount() < 1 || !cursor.moveToPosition(0)) {
			m_iThreadCount = 0;
			return null;
		}

		m_iThreadCount = cursor.getCount();
		if (m_iThreadCount > MAX_THREAD_IN_CACHE) {
			// MAX thread size reached!!!!
			m_iThreadCount = MAX_THREAD_IN_CACHE;
		}

		m_ThreadList = new ArrayList<SmsThreadInfo>();
		int iFrom = 0;

		while (iFrom <= m_iThreadCount) {
			SmsThreadInfo thread = new SmsThreadInfo();

			thread.threadId = cursor.getLong(0);
			thread.sPhone = cursor.getString(1);
			//int iType = (int) cursor.getInt(2);
			
			
			thread.iMsgCount = cursor.getInt(2);

			m_ThreadList.add(thread);

			if (!cursor.moveToNext()) {
				break;
			}

			++iFrom;
		}

		cursor.close();

		return m_ThreadList;
	}

	static public List<SmsThreadInfo> GetThreadListWithFirstMsg(int iFrom) {
		if (m_ThreadList == null || m_ThreadList.size() < 1) {
			InitCache();
		}

		if (m_ThreadList == null || m_ThreadList.size() < 1
				|| iFrom >= m_ThreadList.size()) {
			return null;
		}

		int iTo = iFrom + MAX_THREAD_ITEM_PER_PAGE;
		if (iTo >= m_ThreadList.size()) {
			iTo = m_ThreadList.size();
		}
		
		for (int i = iFrom; i < iTo; ++i) {
			if (m_ThreadList.get(i).chatList == null){
				GetFirstMsg4Thread(i);
			}
		}
		
		// need to update cache in next timer
		NanoHTTPD.AddLooperTask(new EALooperTask(iTo - 1) {
					public void run() {
						SmsApi.FreeThreadChatList(m_iData);
						SmsApi.GetFirstMsg4Thread(m_iData);
					};
				});

		return m_ThreadList;
	}

	static public void InitCache() {
		m_iThreadCount = 0;
			m_ThreadList = null;

		GetThreadList();
		GetFirstMsg4Thread(0);
	}
	
	public static String GetThreadPhone(long lThreadId){
		SmsThreadInfo t = FindThreadInList(lThreadId);
		if (t == null){
			return "";
		}

		return t.sPhone;
	}
	

	public static String GetThreadContactName(long lThreadID) {
		SmsThreadInfo t = SmsApi.FindThreadInList(lThreadID);
		if (t == null){
			return "";
		}

		if (t.sContactName == null || t.sContactName.length() < 1) {
			t.sContactName = ContactApi.getContactNameFromPhoneNum(t.sPhone);
		}	
		
		return t.sContactName;
	}

	public static String GetThreadContactName(SmsThreadInfo t) {
		if (t == null){
			return "";
		}

		if (t.sContactName == null || t.sContactName.length() < 1) {
			t.sContactName = ContactApi.getContactNameFromPhoneNum(t.sPhone);
		}	
		
		return t.sContactName;
	}
	
	public static List<SmsInfo> GetSmsItem(int iFrom,int iCount) {
		
		final String[] projection = new String[] { "_id", "thread_id",
				"address", "date", "body", "read", "status", "type", "person" };

		String sortOrder = "_id DESC";


		Cursor cursor = EAUtil.GetContentResolver().query(
				Uri.parse(SMS_URI_ALL), projection, null,
				null, sortOrder);

		if (cursor == null || cursor.getCount() < 1) {
			if (cursor != null) {
				cursor.close();
			}
			return null;
		}
		
		if (!cursor.moveToPosition(iFrom)) {
			cursor.close();
			return null;
		}
		
		List<SmsInfo> chatList = new ArrayList<SmsInfo>();
		
		for (int j = 0; j < iCount; ++j) {
			SmsInfo sms = new SmsInfo();
			
			sms.lMsgId = cursor.getLong(0);
			sms.lThreadId = cursor.getLong(1);
			
			// smsConversation[i].sPhone = cursor.getString(2);
			String sPhone = cursor.getString(2);
			sPhone = sPhone.trim();
			String[] sPhoneList = sPhone.split(" ");
			sms.sPhone = sPhoneList[0];
			
			// assign time 
			sms.lTimeStamp = cursor.getLong(3);
			
			/* 优化：对已经获取到时间戳，就没有必要在这里格式化对应的时间格式，这个放在外面应用做比较好，想要什么格式就什么格式*/
			sms.sDateTime = DateUtils.formatDateTime(EAUtil.GetEAContext(),
					sms.lTimeStamp, DateUtils.FORMAT_SHOW_TIME);
			sms.sDateTime += " "
					+ DateUtils.formatDateTime(EAUtil.GetEAContext(),
							sms.lTimeStamp, DateUtils.FORMAT_SHOW_YEAR);

			sms.sBody = cursor.getString(4);
			sms.lIsRead = cursor.getLong(5);
			sms.lMsgStatus = cursor.getLong(6);
			sms.lMsgType = cursor.getLong(7);
			
			sms = EncodeSmsItem(sms);

			chatList.add(sms);

			if (!cursor.moveToNext()) {
				break;
			}
		}

		cursor.close();
		
		return chatList;
	}


	static List<SmsInfo> GetSmsConversation(SmsThreadInfo thread,int iFrom,int iCount) {
		
		final String[] projection = new String[] { "_id", "thread_id",
				"address", "date", "body", "read", "status", "type", "person" };
		String selection = "";
		String[] selectionArgs = null;
		String sortOrder = "_id DESC";

		selection += "thread_id = ?";
		selectionArgs = new String[] { String.valueOf(thread.threadId) };

		Cursor cursor = EAUtil.GetContentResolver().query(
				Uri.parse(SMS_URI_ALL), projection, selection,
				selectionArgs, sortOrder);

		if (cursor == null || cursor.getCount() < 1) {
			if (cursor != null) {
				cursor.close();
			}
			return null;
		}
		
		thread.iMsgCount = cursor.getCount();
		
		if (!cursor.moveToPosition(iFrom)) {
			cursor.close();
			return null;
		}
		
		if (thread.chatList == null){
			thread.chatList = new ArrayList<SmsInfo>();
		}
		
		
		if (iFrom != thread.chatList.size()){
			//fatel error
			Log.e("SmsApi","iFrom != thread.chatList.size()");
			return null;
		}
		
		// 移到此处执行，当上述返回错误，就没有必要去花时间做这个动作
		String sContactName = GetThreadContactName(thread);

		for (int j = 0; j < iCount; ++j) {
			SmsInfo sms = new SmsInfo();
			
			sms.lMsgId = cursor.getLong(0);
			sms.lThreadId = cursor.getLong(1);
			
			// smsConversation[i].sPhone = cursor.getString(2);
			String sPhone = cursor.getString(2);
			sPhone = sPhone.trim();
			String[] sPhoneList = sPhone.split(" ");
			sms.sPhone = sPhoneList[0];
			
			// assign time 
			sms.lTimeStamp = cursor.getLong(3);
			
			/* 优化：对已经获取到时间戳，就没有必要在这里格式化对应的时间格式，这个放在外面应用做比较好，想要什么格式就什么格式*/
			sms.sDateTime = DateUtils.formatDateTime(EAUtil.GetEAContext(),
					sms.lTimeStamp, DateUtils.FORMAT_SHOW_TIME);
			sms.sDateTime += " "
					+ DateUtils.formatDateTime(EAUtil.GetEAContext(),
							sms.lTimeStamp, DateUtils.FORMAT_SHOW_YEAR);

			sms.sBody = cursor.getString(4);
			sms.lIsRead = cursor.getLong(5);
			sms.lMsgStatus = cursor.getLong(6);
			sms.lMsgType = cursor.getLong(7);
			sms.sContactName = sContactName;

			sms = EncodeSmsItem(sms);

			thread.chatList.add(sms);

			if (!cursor.moveToNext()) {
				break;
			}
		}

		cursor.close();
		
		return thread.chatList;
	}

	static SmsInfo EncodeSmsItem(SmsInfo sms) {
		sms.sPhone = EAUtil.CHECK_STRING(sms.sPhone, " ");
		sms.sDateTime = EAUtil.CHECK_STRING(sms.sDateTime, " ");
		sms.sContactName = EAUtil.CHECK_STRING(sms.sContactName, " ");
		sms.sBody = EAUtil.CHECK_STRING(sms.sBody, " ");

		// sms.sBody = URLEncoder.encode(sms.sBody);

		return sms;
	}

	public static Uri mSmsUri = Uri.parse("content://sms/inbox");
	public static void insertsms(String sPhoneNo, String sBody, String sRead, String sType) {
		long lDate = new java.util.Date().getTime();
		ContentValues values = new ContentValues();
		values.put("address", sPhoneNo);
		values.put("body", sBody);
		values.put("date", lDate);
		values.put("read", 1);
		values.put("type", sType);
		// values.put("service_center", "+8613010776500");

		EAUtil.GetContentResolver().insert(mSmsUri, values);
	}
	
	public static void InsertSms(SmsInfo sInfo)
	{
		ContentValues values = new ContentValues();
		values.put("address", sInfo.sPhone);
		values.put("body", sInfo.sBody);
		values.put("date", sInfo.lTimeStamp);
		values.put("read", sInfo.lIsRead);
		values.put("type", sInfo.lMsgType);
		values.put("status", sInfo.lMsgStatus);

		EAUtil.GetContentResolver().insert(mSmsUri, values);
	}
	
	static public int DeleteSms(String threadID, String id) {
		int iCount = 0;
		if (id == null || id.length() < 1) {
			iCount = EAUtil.GetContentResolver().delete(Uri.parse("content://sms/conversations/" + threadID),null,null);
		}
		else {
			iCount = EAUtil.GetContentResolver().delete(
					Uri.parse("content://sms/conversations/" + threadID),
					"_id = ?", new String[] { id });	
		}

		if (iCount < 1){
			return EADefine.EA_RET_FAILED;
		}
		
		return EADefine.EA_RET_OK;
	}

	static List<String> m_SendStatelist = new ArrayList<String>();

	static public String GetSendState() {
		if (m_SendStatelist.size() < 1) {
			return null;
		}

		String sState = "";
		for (int i = 0; i < m_SendStatelist.size(); ++i) {
			sState += m_SendStatelist.get(i) + "#";
		}

		m_SendStatelist.clear();

		return sState;
	}
	
	static public String GetPhoneNo(String sPhoneEntry){
		if (sPhoneEntry == null || sPhoneEntry.length() < 1){
			return "";
		}
		
		String sNo = "";
		for (int i = 0; i < sPhoneEntry.length(); ++i){
			char c = sPhoneEntry.charAt(i);
			if (Character.isDigit(c)){
				sNo += c;
			}
		}
		
		return sNo;
	}

	static public int sendSMS(String phoneNumber, String message,String sTimeStamp) {
		phoneNumber = GetPhoneNo(phoneNumber);
		
		String sPhone = phoneNumber;
		sPhone = sPhone.replaceAll("=", "");
		String sVal = phoneNumber+sTimeStamp;
		Pattern pattern = Pattern.compile("[0-9]*");
		Matcher isNum = pattern.matcher(sPhone);
		if (!isNum.matches()) {
			String phoneNo = sVal;
			phoneNo += "=" + String.valueOf(EADefine.EA_RET_INVALID_PHONE_NO);
			m_SendStatelist.add(phoneNo);
			return EADefine.EA_RET_INVALID_PHONE_NO;
		}
		
		String SENT = "SMS_SENT" + System.currentTimeMillis();
		String DELIVERED = "SMS_DELIVERED" + System.currentTimeMillis();
		
		Intent sentIntent = new Intent(SENT);
		sentIntent.putExtra("timestamp", sVal);
		sentIntent.putExtra("smscontent", message);
		sentIntent.putExtra("phoneNo", sPhone);
		PendingIntent sentPI = PendingIntent.getBroadcast(
				EAUtil.GetEAContext(), (int) System.currentTimeMillis(), sentIntent, 0);

		Intent deliveredIntent = new Intent(DELIVERED);
		deliveredIntent.putExtra("timestamp", sVal);
		deliveredIntent.putExtra("smscontent", message);
		deliveredIntent.putExtra("phoneNo", sPhone);
		
		PendingIntent deliveredPI = PendingIntent.getBroadcast(
				EAUtil.GetEAContext(), (int) System.currentTimeMillis(), deliveredIntent, 0);

		// ---when the SMS has been sent---
		EAUtil.GetEAContext().registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context arg0, Intent arg1) {
				String timestamp = arg1.getExtras().getString("timestamp");
				String phoneNo = arg1.getExtras().getString("phoneNo");
				String sContent = arg1.getExtras().getString("smscontent");
				switch (getResultCode()) {
				case Activity.RESULT_OK:
					timestamp += "=" + String.valueOf(EADefine.EA_RET_OK);
					insertsms(phoneNo, sContent, "0", "1");
					InitCache();
					break;
				case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
					timestamp += "=" + String.valueOf(EADefine.EA_RET_FAILED);
					break;
				case SmsManager.RESULT_ERROR_NO_SERVICE:
					timestamp += "=" + String.valueOf(EADefine.EA_RET_FAILED);
					break;
				case SmsManager.RESULT_ERROR_NULL_PDU:
					timestamp += "=" + String.valueOf(EADefine.EA_RET_FAILED);
					break;
				case SmsManager.RESULT_ERROR_RADIO_OFF:
					timestamp += "=" + String.valueOf(EADefine.EA_RET_FAILED);
					break;
				default:
					timestamp += "=" + String.valueOf(EADefine.EA_RET_FAILED);
					break;
				}
				m_SendStatelist.add(timestamp);
			}
		}, new IntentFilter(SENT));

/*		// ---when the SMS has been delivered---
		EAUtil.GetEAContext().registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context arg0, Intent arg1) {
				String phoneNo = arg1.getExtras().getString("timestamp");
				switch (getResultCode()) {
				case Activity.RESULT_OK:
					phoneNo += "=" + String.valueOf(EADefine.EA_RET_OK);
					break;
				case Activity.RESULT_CANCELED:
					phoneNo += "=" + String.valueOf(EADefine.EA_RET_FAILED);
					break;
				default:
					phoneNo += "=" + String.valueOf(EADefine.EA_RET_FAILED);
					break;
				}
				m_SendStatelist.add(phoneNo);
			}
		}, new IntentFilter(DELIVERED));*/

		SmsManager sms = SmsManager.getDefault();
		sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);

		return EADefine.EA_RET_OK;
	}
}
