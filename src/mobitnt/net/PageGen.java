package mobitnt.net;

import java.util.Properties;
import mobitnt.util.*;


public class PageGen {
	public int m_iReqType;
	//public static String m_sXmlHeader = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n";
	
	String m_sRespMimeType = NanoHTTPD.MIME_XML;

	PageGen() {

	}

	public String GetRespMimeType(){
		return m_sRespMimeType;
	}
	
	public void SetRespMimeType(String sRespMimeType){
		m_sRespMimeType = sRespMimeType;
	}
	
	public static String ReturnException(String sVal) {
		String sXml = String.format("<EARetException>%s</EARetException>", sVal);
		return sXml;
	}
	
	
	public static String ReturnEATaskVal4XML(String DataTag,String data){
		StringBuilder sHtml = new StringBuilder();
		sHtml.append("<" + EADefine.EA_ACT_TIMER_DATA_TAG  + ">");
		sHtml.append("<RespType>");	
		sHtml.append(EADefine.EA_ACT_BK_PROGRESS_TAG);	
		sHtml.append("</RespType>");	
		sHtml.append("<EATaskData>");
		sHtml.append(data);
		sHtml.append("</EATaskData>");
		sHtml.append("</" + EADefine.EA_ACT_TIMER_DATA_TAG  + ">;");
	
		return sHtml.toString();
	}
	
	
	public static String ReturnEATaskVal4Html(String DataTag,String data){
		StringBuilder sHtml = new StringBuilder();
		sHtml.append("<" + EADefine.EA_ACT_TIMER_DATA_TAG  + ">;");
		sHtml.append(data);
		sHtml.append(";RespType:" + DataTag + EADefine.EA_ACT_HTML_SEPERATOR_TAG);
	
		return sHtml.toString();
	}	
	
	public static String GenRefreshCmd(){
		return "<Refresh>refresh</Refresh>";
	}
	
	public static String GenRetCode(int iCode) {
		String sXml = String.format("<EARetCode>%s</EARetCode>",iCode);
		return sXml;
	}
	
	static boolean m_bOnlineState = false;
	public String setOnlineState(boolean bOnline,int iPageIndex){
		m_bOnlineState = bOnline;
		return GenRetCode(EADefine.EA_RET_OK);
	}
	
	public String ProcessRequest(String sReq, Properties parms) {
				
		String sPageIndex = parms.getProperty(EADefine.EA_ACT_PAGE_INDEX_TAG);
		if (sPageIndex != null && sPageIndex.length() > 0) {
			int iPageIndex = Integer.parseInt(sPageIndex);
			return setOnlineState(true,iPageIndex);
		}
		
		
		return GenRetCode(EADefine.HTTP_REQ_TYPE_UNKNOW);
	}

}
