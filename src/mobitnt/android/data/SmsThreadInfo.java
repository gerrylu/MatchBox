package mobitnt.android.data;

import java.util.List;

public class SmsThreadInfo {
	public long threadId;
	public int  iMsgCount;
	public String sContactName;
	public String sPhone;
	public List<SmsInfo> chatList;
	public int iStartPos;//���б�����Ϣ�ܶ��ʱ����Ҫ��ҳ����
	static public final int MAX_LIST_SIZE = 500;//ÿ��thread�����500����Ϣ
	static public final int MAX_LIST_COUNT	= 50;//����thread�б������50�����Ż���
	
	
	public SmsThreadInfo(){
		threadId = 0;
		iMsgCount = 0;
		chatList = null;
		iStartPos = 0;
		sContactName = "";
		sPhone = "";
	}
	
	
}
