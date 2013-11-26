/**
 * 
 */
package mobitnt.backup;

import java.io.InputStream;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

//import org.xml.sax.XMLReader;
import mobitnt.android.data.*;
import mobitnt.backup.SMSInfoHandler;
/**
 * @author Administrator
 *
 */
public class SaxXMLManager {
	// get sms 
	public static List<SmsInfo> ReadSMS(InputStream inStream) throws Exception{
		SAXParserFactory spf = SAXParserFactory.newInstance(); // ��ʼ��sax������  
        SAXParser sp = spf.newSAXParser(); // ����sax������  
        //XMLReader xr = sp.getXMLReader();// ����xml������  
        SMSInfoHandler handler = new SMSInfoHandler();  
        sp.parse(inStream, handler);  
        return handler.getSMSInfoList();
	}
	

}
