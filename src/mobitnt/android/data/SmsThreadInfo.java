package mobitnt.android.data;

import java.util.List;

public class SmsThreadInfo {
	public long threadId;
	public int  iMsgCount;
	public String sContactName;
	public String sPhone;
	public List<SmsInfo> chatList;
	public int iStartPos;//当列表中信息很多的时候，需要分页处理
	static public final int MAX_LIST_SIZE = 500;//每个thread中最多500条信息
	static public final int MAX_LIST_COUNT	= 50;//整个thread列表中最多50条短信缓存
	
	
	public SmsThreadInfo(){
		threadId = 0;
		iMsgCount = 0;
		chatList = null;
		iStartPos = 0;
		sContactName = "";
		sPhone = "";
	}
	
	
}
