package mobitnt.net;

import java.util.Properties;

import mobitnt.android.data.CallLogItem;
import mobitnt.android.wrapper.CallLogApi;
import mobitnt.util.*;

public class CallLogManager extends PageGen {

	public String ProcessRequest(String request, Properties parms) {
		try {
			int iFrom = 0;
			int iTo = 0;

			int iTotalCount = 0;
			String sAction = parms.getProperty(EADefine.EA_ACT_ACTION_TAG, "n");
			
			SetRespMimeType(NanoHTTPD.MIME_HTML);

			if (sAction.equalsIgnoreCase(EADefine.EA_ACT_GET_CALL_COUNT)) {

				iTotalCount = CallLogApi.GetCallLogCount();

				String sItemFmt = "<CallLogCount>%d</CallLogCount>\r\n";
				String sXml = String.format(sItemFmt, iTotalCount);
				return sXml;
			}
			/*
			 * if (sAction.equalsIgnoreCase("checknewcall")) { long lNewCall =
			 * 0; if (PESysBroadcastReceiver.m_bHasNewCall){ lNewCall = 1;
			 * PESysBroadcastReceiver.m_bHasNewCall = false;//reset }
			 * 
			 * String sItemFmt = "<NewCall>%d</NewCall>\r\n"; String sXml =
			 * String.format(sItemFmt,lNewCall); return sXml; }
			 */

			if (sAction.equals(EADefine.EA_ACT_GET_CALL_LIST)) {
				String sFrom = parms.getProperty(EADefine.EA_ACT_FROM_TAG, "0");
				String sTo = parms.getProperty(EADefine.EA_ACT_TO_TAG, "0");
				// String sCallType = parms.getProperty("calltype","0");

				iFrom = Integer.valueOf(sFrom);
				iTo = Integer.valueOf(sTo);
				// iCallType = Integer.valueOf(sCallType);

				if (iTo == 0 || iFrom == iTo) {
					return GenRetCode(EADefine.EA_RET_END_OF_FILE);
				}

				iTotalCount = CallLogApi.GetCallLogCount();
				if (iFrom > iTotalCount || iTotalCount < 1) {
					return GenRetCode(EADefine.EA_RET_END_OF_FILE);
				}

				// 这里需要分页处理
				return GetCallLogList(iFrom, iTo);
			}
			
			if (sAction.equals(EADefine.EA_ACT_DELETE_CALL)) {
				String sID = parms.getProperty(EADefine.EA_ACT_ID_TAG, "0");
				
				int iRet = CallLogApi.DeleteCall(sID);
				
				StringBuilder sHtml = new StringBuilder();
				sHtml.append("DeleteID:" + sID);
				sHtml.append(";DeleteState:");
				sHtml.append(iRet);
				sHtml.append(";RespType:DeleteCallState"
						+ EADefine.EA_ACT_HTML_SEPERATOR_TAG);
						
				return sHtml.toString();
			}

			return GenRetCode(EADefine.EA_RET_UNKONW_REQ);

		} catch (Exception e) {
			return ReturnException(e.toString());
		}
	}

	String GetCallLogList(int iFrom, int iTo) {
		/*
		 * 
		 * sLink += sName; sLink += '<span class="wMargin">' + sNumber +
		 * "</span>"; sLink += '<span class="wMargin">' + sTime + "</span>";
		 * sLink += '<span>' + sDuration + "</span>";
		 * 
		 * sLink += '</dd><div class="clear"></div></dl>';
		 */
		/*
		 * String sCallEntryFmt =
		 * "<Calllog>\r\n<Name>%s</Name>\r\n<Number>%s</Number>\r\n";
		 * sCallEntryFmt +=
		 * "<Duration>%s</Duration>\r\n<Time>%s</Time>\r\n<Type>%s</Type>\r\n";
		 * sCallEntryFmt += "<Timestamp>%s</Timestamp>\r\n</Calllog>\r\n";
		 */

		int iCallCount = CallLogApi.GetCallLogCount();
		if (iCallCount < iFrom) {
			return null;
		}

		StringBuilder sXml = new StringBuilder();
/*		sXml.append("<CallLogList>");
		sXml.append("<TotalCount>");
		sXml.append(iCallCount);
		sXml.append("</TotalCount>");*/

		sXml.append("CallTotalCount:");
		sXml.append(iCallCount);
		sXml.append(";RespType:CallList" + EADefine.EA_ACT_HTML_SEPERATOR_TAG);

		if (iCallCount < iTo) {
			iTo = iCallCount;
		}

		if (iFrom < 0) {
			iFrom = 0;
		}
		
		sXml.append("<table class='call-table'>");

		for (int i = iFrom; i < iTo; ++i) {
			CallLogItem call = CallLogApi.GetCallLogByIndex(i);
				// ERR

			sXml.append("<tr  class='call-row'>");
			
			sXml.append("<td class='call-c1 sprite sprite-");
			if (call.sType.equals(CallLogApi.CALL_TYPE_INCOMING)) {
				sXml.append("InCall'>");
			} else if (call.sType.equals(CallLogApi.CALL_TYPE_MISSED)) {
				sXml.append("MissedCall'>");
			} else if (call.sType.equals(CallLogApi.CALL_TYPE_OUTGOING)) {
				sXml.append("OutCall'>");
			}
			sXml.append("</td>");
			
			sXml.append("<td class='call-c2'>");
			sXml.append(call.sName);
			sXml.append("</td>");
			
			sXml.append("<td class='call-c3'>");
			sXml.append(call.sNumber);
			sXml.append("</td>");

			sXml.append("<td class='call-c4'>");
			sXml.append(call.sDate);
			sXml.append("</td>");

			sXml.append("<td class='call-c5'>");
			sXml.append("<script>FormatDuration(" + call.sDuration + ");</script>");
			sXml.append("</td>");
			
			sXml.append("<td class='call-c6'>");
			sXml.append("<a class='aHasBk  sprite sprite-delete' href=\"#\" onclick=\"OnDeleteCall(");
			sXml.append(call.sID);
			sXml.append(");\"></a>");
			sXml.append("</td>");

			/*
			 * sXml.append("<Type>");
			 * sXml.append(sCallLogFields[CallLogApi.CALL_FIELD_TYPE]);
			 * sXml.append("</Type>");
			 * 
			 * sXml.append("<Timestamp>");
			 * sXml.append(sCallLogFields[CallLogApi.CALL_FIELD_TIMESTAMP]);
			 * sXml.append("</Timestamp>");
			 */

			sXml.append("</tr>");

		}

		sXml.append("</table>");
		return sXml.toString();
	}

}
