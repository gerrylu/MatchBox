package mobitnt.backup;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import mobitnt.android.data.*;


public class SMSInfoHandler extends DefaultHandler {
	private List<SmsInfo> m_lsSMSInfo;
	private SmsInfo m_SmsInfo;
	private String m_sValue;
	
	private static final String SMSTAG = "SMS";
	private static final String SMSTYPE = "Type";
	private static final String SMSSTATUS = "Status";
	private static final String SMSISREAD = "IsRead";
	private static final String SMSADDRESS = "Address";
	private static final String SMSDATE = "Date";
	private static final String SMSBODY = "Body";
	
	
	//private static final String  
	public List<SmsInfo> getSMSInfoList()
	{
		return m_lsSMSInfo;
	}
	
	@Override  
    public void startDocument() throws SAXException {
		m_lsSMSInfo = new ArrayList<SmsInfo>();  
    }  
	
	@Override  
    public void characters(char[] ch, int start, int length) throws SAXException 
    {            
        if (m_lsSMSInfo != null) 
        {  
            String valueString = new String(ch, start, length);  
            if (SMSTYPE.equals(m_sValue))
            {
            	m_SmsInfo.lMsgType = Integer.parseInt(valueString);  
            } 
            else if (SMSSTATUS.equals(m_sValue)) 
            {  
            	m_SmsInfo.lMsgStatus = Integer.parseInt(valueString);             	
            }
            else if (SMSISREAD.equals(m_sValue)) 
            {  
            	m_SmsInfo.lIsRead = Integer.parseInt(valueString);             	
            } 
            else if (SMSADDRESS.equals(m_sValue)) 
            {  
            	m_SmsInfo.sPhone = valueString;             	
            } 
            else if (SMSDATE.equals(m_sValue)) 
            {  
            	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
            	
				try 
				{
					Date dt2 = sdf.parse(valueString);
					m_SmsInfo.lTimeStamp = dt2.getTime();  
					
				} catch (ParseException e) 
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            	           	
            }
            else if (SMSBODY.equals(m_sValue)) 
            {  
            	m_SmsInfo.sBody = valueString;             	
            } 
        }  
    }  
	
	@Override  
    public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException 
    {  
        if (SMSTAG.equals(localName)) 
        {  
        	m_SmsInfo = new SmsInfo();            
        }   
        m_sValue = localName;  
  
    } 
	
	@Override  
    public void endElement(String uri, String localName, String name) throws SAXException 
    {  
        if(SMSTAG.equals(localName) && m_SmsInfo != null)  
        {  
        	m_lsSMSInfo.add(m_SmsInfo);  
            m_SmsInfo = null;  
        }  
        m_sValue = null;          
    }
	
}
