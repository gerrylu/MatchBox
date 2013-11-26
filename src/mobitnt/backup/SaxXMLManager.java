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
		SAXParserFactory spf = SAXParserFactory.newInstance(); // 初始化sax解析器  
        SAXParser sp = spf.newSAXParser(); // 创建sax解析器  
        //XMLReader xr = sp.getXMLReader();// 创建xml解析器  
        SMSInfoHandler handler = new SMSInfoHandler();  
        sp.parse(inStream, handler);  
        return handler.getSMSInfoList();
	}
	

}
