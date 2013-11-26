package mobitnt.net;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Properties;

import mobitnt.android.data.SmsInfo;
import mobitnt.android.data.SmsThreadInfo;
import mobitnt.android.wrapper.SmsApi;
import mobitnt.util.*;

public class SmsManager extends PageGen {
	String SendSms(String sPhones, String sContent, String sThreadID,
			String sTimeStamp) {
		try {
			sContent = java.net.URLDecoder.decode(sContent, "UTF-8");
			sPhones = java.net.URLDecoder.decode(sPhones, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (sContent == null || sContent.length() < 1) {
			// should never happen
			return GenRetCode(EADefine.EA_RET_INVALID_SMS_CONTENT);
		}

		if (sThreadID.length() < 1 || sThreadID.equals("0")) {
			sThreadID = "0";
		} else {
			sPhones = SmsApi.GetThreadPhone(Long.parseLong(sThreadID));
		}

		if (sPhones == null || sPhones.length() < 1) {
			return GenRetCode(EADefine.EA_RET_INVALID_PHONE_NO);
		}

		String[] sPhoneList = sPhones.split(",");
		if (sPhoneList.length < 1) {
			return GenRetCode(EADefine.EA_RET_INVALID_PHONE_NO);
		}

		// new a sms
		for (int i = 0; i < sPhoneList.length; ++i) {
			if (sPhoneList[i].length() < 2) {
				// skip invalid number
				continue;
			}

			final int MAX_SMS_LEN = 80;
			int iContentLen = sContent.length();
			// int iRet = EADefine.EA_RET_OK;
			for (int j = 0; j < iContentLen / MAX_SMS_LEN + 1; ++j) {
				int iStart = j * MAX_SMS_LEN;
				int iEnd = iStart + MAX_SMS_LEN;
				if (iEnd > iContentLen) {
					iEnd = iContentLen;
				}

				String sSubContent = sContent.substring(iStart, iEnd);

				SmsApi.sendSMS(sPhoneList[i], sSubContent, sTimeStamp);
			}
		}

		return GenRetCode(EADefine.EA_RET_QUERY_STATE_LATER);
	}

	public String ProcessRequest(String request, Properties parms) {
		String sThreadId = parms
				.getProperty(EADefine.EA_ACT_THREAD_ID_TAG, "0");
		String sAction = parms.getProperty(EADefine.EA_ACT_ACTION_TAG, "n");
		String sFrom = parms.getProperty(EADefine.EA_ACT_FROM_TAG, "0");
		int iFrom = Integer.parseInt(sFrom);

		SetRespMimeType(NanoHTTPD.MIME_HTML);

		if (sAction.equalsIgnoreCase(EADefine.EA_ACT_SEND_SMS)) {
			/* 发送短信 */
			String sContent = parms.getProperty(
					EADefine.EA_ACT_SMS_CONTENT_TAG, "");
			String sPhones = parms.getProperty(
					EADefine.EA_ACT_SMS_RECEIVER_TAG, "");

			String sTimeStamp = parms.getProperty(
					EADefine.EA_ACT_SMS_TIMESTAMP_TAG, "");

			return SendSms(sPhones, sContent, sThreadId, sTimeStamp);
		}

		if (sAction.equals(EADefine.EA_ACT_GET_SEND_STATE)) {
			String sState = SmsApi.GetSendState();
			StringBuilder sHtml = new StringBuilder();
			sHtml.append(";SendState:");
			if (sState == null) {
				sHtml.append("unkonw");
			} else {
				sHtml.append(sState);
			}

			return ReturnEATaskVal4Html(EADefine.EA_ACT_SMS_SEND_STATUS_TAG,
					sHtml.toString());
		}

		if (sAction.equalsIgnoreCase(EADefine.EA_ACT_DELETE_SMS)) {
			String sID = parms.getProperty(EADefine.EA_ACT_ID_TAG, "");

			int iRetCode = SmsApi.DeleteSms(sThreadId, sID);
			if (iRetCode == EADefine.EA_RET_OK) {
				NanoHTTPD.AddLooperTask(new EALooperTask() {
					public void run() {
						SmsApi.InitCache();
					};
				});
			}

			StringBuilder sHtml = new StringBuilder();
			sHtml.append(EADefine.EA_ACT_THREAD_ID_TAG + ":" + sThreadId + ";");
			// sHtml.append(EADefine.EA_ACT_ID_TAG + ":" + sID + ";");
			sHtml.append(";DeleteState:");
			sHtml.append(iRetCode);
			sHtml.append(";RespType:DeleteSmsState"
					+ EADefine.EA_ACT_HTML_SEPERATOR_TAG);

			return sHtml.toString();
		} else if (sAction.equalsIgnoreCase(EADefine.EA_ACT_GET_CHAT)) {
			Long lThreadId = Long.valueOf(sThreadId);
			int iChatFrom = Integer.valueOf(sFrom);
			if (lThreadId > 0) {
				return GetSingelThread(lThreadId, iChatFrom);
			}
		} else if (sAction.equalsIgnoreCase(EADefine.EA_ACT_GET_THREAD_COUNT)) {
			return GetThreadCount();
		} else if (sAction.equalsIgnoreCase(EADefine.EA_ACT_GET_THREAD_LIST)) {
			return GetSmsThreadList(iFrom);
		}

		return PageGen.GenRetCode(EADefine.EA_RET_UNKONW_REQ);

	}

	String GetThreadCount() {
		String sItemFmt = "<ThreadCount>%d</ThreadCount>\r\n";
		String sXml = String.format(sItemFmt, SmsApi.GetThreadCount());
		return sXml;
	}

	// final int MAX_REQUEST_COUNT = 0;
	String GetSingelThread(long ThreadId, int iFrom) {
		List<SmsInfo> smsList = SmsApi.GetSmsChat(ThreadId, iFrom);
		if (smsList == null || smsList.size() < 1) {
			return GenRetCode(EADefine.EA_RET_END_OF_FILE);
		}

		SmsThreadInfo t = SmsApi.FindThreadInList(ThreadId);
		if (t == null) {
			return GenRetCode(EADefine.EA_RET_END_OF_FILE);
		}

		int iTotalMsgCount = t.iMsgCount;

		int iMsgCount = smsList.size();
		if (iFrom + SmsApi.MAX_REQUEST_COUNT < iMsgCount) {
			iMsgCount = iFrom + SmsApi.MAX_REQUEST_COUNT;
		} else {
			iMsgCount -= iFrom;
		}

		StringBuilder sXml = new StringBuilder();

		sXml.append("MsgCount:");
		sXml.append(iMsgCount);
		sXml.append(";TotalMsgCount:");
		sXml.append(iTotalMsgCount);
		sXml.append(";RespType:ChatList" + EADefine.EA_ACT_HTML_SEPERATOR_TAG);

		/*
		 * <dl class="chatlist" style="_margin-bottom: -15px;"> <dt
		 * class="Sender f_left"> <img src="images/contact.png"/> </dt> <dd
		 * class="content01 f_right"> <div class="t_top"> <span
		 * class="both">张小明</span><em>2012.3.23 17:50</em> </div> <p
		 * class="t_bottom">
		 * 祝福的话语来自电波:生活的美好要努力拼搏,日子的甜美要品尝波折,快意的心境需要精心打磨,乐悠闲适的适中最难得.乐悠闲适的适中最难得.
		 * </p> </dd> </dl>
		 */

		for (int i = smsList.size() - 1; i >= iFrom; --i) {
			sXml.append("<dl class=\"chatlist\">");

			if (smsList.get(i).lMsgType == 1) {
				// InBox
				sXml.append("<dt class=\"Sender sprite sprite-SenderContact f_left\"></dt>");
				sXml.append("<dd class=\"send-content f_right\">");

				sXml.append("<div class=\"t_top\" id=\""
						+ smsList.get(i).sPhone + "\">");
				sXml.append("<span class=\"both\">");
				sXml.append(smsList.get(i).sContactName);
				sXml.append("</span>");
			} else {
				sXml.append("<dd class=\"recv-content f_right\">");

				sXml.append("<div class=\"t_top\" id=\""
						+ smsList.get(i).sPhone + "\">");

			}

			/*
			 * sXml.append("<ThreadID>"); sXml.append(smsList.get(i).lThreadId);
			 * sXml.append("</ThreadID>");
			 * 
			 * sXml.append("<MsgID>"); sXml.append(smsList.get(i).lMsgId);
			 * sXml.append("</MsgID>");
			 */
			sXml.append("<em>");
			sXml.append(smsList.get(i).sDateTime);
			sXml.append("</em>");

			/*
			 * <a href="#" onclick="OnDeleteSms(0);"><img
			 * src="img/delete.png"></a>
			 */
			sXml.append("<a href=\"#\" class=\"Sender sprite sprite-delete\" onclick=\"OnDeleteSms(");
			sXml.append(smsList.get(i).lMsgId);
			sXml.append(");\"></a>");

			sXml.append("</div>");

			/*
			 * sXml.append("<Phone>"); sXml.append(smsList.get(i).sPhone);
			 * sXml.append("</Phone>");
			 */

			sXml.append("<p class=\"t_bottom\">");
			sXml.append(smsList.get(i).sBody);
			sXml.append("</p>");

			/*
			 * sXml.append("<MsgType>"); sXml.append(smsList.get(i).lMsgType);
			 * sXml.append("</MsgType>");
			 */

			/*
			 * sXml.append("<Timestamp>");
			 * sXml.append(smsList.get(i).lTimeStamp);
			 * sXml.append("</Timestamp>");
			 */
			sXml.append("</dd>");
			if (smsList.get(i).lMsgType != 1) {
				// receiver
				sXml.append("<dt class=\"Receiver f_left\"></dt>");
			}

			sXml.append("</dl>");
			sXml.append("<div class=\"clear\"></div>");

		}
		// sXml.append("</Conversation>");
		return sXml.toString();
	}

	// donn't support page for now
	String GetSmsThreadList(int iFrom) {
		/*
		 * <dl class="listThread"> <dt class="f_right"> <a href="#"
		 * onclick="alert('reply')" class="img01">&nbsp;</a> </dt> <dd
		 * class="f_left"> <p class="title"> <span>张小明</span><em>2012.3.23
		 * 17:50</em> </p> <p
		 * class="txt">一生中能够经历多少东西呢?又有多少是能让你一直惦记着从未丢弃海中又有...</p> </dd> <div
		 * class="clear"></div> </dl>
		 */

		List<SmsThreadInfo> list = SmsApi.GetThreadListWithFirstMsg(iFrom);
		if (list == null || list.size() < 1) {
			return GenRetCode(EADefine.EA_RET_END_OF_FILE);
		}

		StringBuilder sXml = new StringBuilder();

		sXml.append("ThreadTotalCount:");
		sXml.append(SmsApi.GetThreadCount());

		sXml.append(";ThreadFrom:");
		sXml.append(iFrom);

		sXml.append(";ThreadCount:");
		sXml.append(list.size());

		sXml.append(";RespType:ThreadList" + EADefine.EA_ACT_HTML_SEPERATOR_TAG);

		int iTo = iFrom + SmsApi.MAX_THREAD_ITEM_PER_PAGE;
		if (iTo > list.size()) {
			iTo = list.size();
		}
		// here may need to page when there are lots of sms in inbox/sentbox
		for (int i = iFrom; i < iTo; ++i) {
			if (list.get(i) == null || list.get(i).chatList == null) {
				break;
			}

			SmsInfo smsItem = list.get(i).chatList.get(0);
			if (smsItem == null) {
				continue;
			}

			sXml.append("<dl class=\"listThread\">");

			// sXml.append("<dt class=\"f_right\">");
			// sXml.append("<a href=\"#\" onclick=\"alert('reply')\" class=\"img01\">&nbsp;</a>");
			// sXml.append("</dt>");

			/*
			 * sXml.append("<ThreadID>"); sXml.append(list.get(i).threadId);
			 * sXml.append("</ThreadID>");
			 * 
			 * sXml.append("<MsgCount>"); sXml.append(list.get(i).iMsgCount);
			 * sXml.append("</MsgCount>");
			 */

			sXml.append("<dd class=\"f_left\">");

			sXml.append("<p class=\"title\">");

			sXml.append("<span>");
			sXml.append(smsItem.sContactName);
			sXml.append("</span>");

			sXml.append("<em>");
			sXml.append(smsItem.sDateTime);
			sXml.append("</em>");

			sXml.append("<a href=\"#\" class=\"aHasBk sprite sprite-delete\" onclick=\"OnDeleteThread(");
			sXml.append(smsItem.lThreadId);
			sXml.append(");\"></a>");

			sXml.append("</p>");

			sXml.append("<p class=\"txt\">");
			sXml.append("<a  class='pseudo-SmsContent' href=\"#\" onclick=\"OnShowChat(");
			sXml.append(list.get(i).threadId);
			sXml.append(")\">");
			sXml.append(smsItem.sBody);
			sXml.append("</a>");
			sXml.append("</p>");

			/*
			 * <p><a href="#" onclick="OnDeleteThread(0);"><img
			 * src="img/delete.png"></a></p>
			 */

			/*
			 * sXml.append("<Phone>"); sXml.append(smsItem.sPhone);
			 * sXml.append("</Phone>");
			 */

			sXml.append("</dd><div class=\"clear\"></div></dl>");
		}

		// sXml.append("</ThreadList>");
		return sXml.toString();
	}
}
