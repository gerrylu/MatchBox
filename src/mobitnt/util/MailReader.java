package  mobitnt.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.FlagTerm;

import android.util.Log;

import mobitnt.backup.BackupProcessor;
import mobitnt.net.EAService;
import mobitnt.backup.IContentFilter;


@SuppressWarnings("all")
public class MailReader {
	
	public static void receive(
			String sMailAccount,
			String sMailPwd, 
			IContentFilter conFilter) 
	{
		Properties props = System.getProperties();

		props.setProperty("mail.store.protocol", "imaps");
		props.setProperty("mail.imaps.host", "imaps.gmail.com"); 
        props.put("mail.imaps.auth", "true"); 
        props.put("mail.imaps.port", "993"); 
        props.put("mail.imaps.socketFactory.port", "993"); 
        props.put("mail.imaps.socketFactory.class", 
                  "javax.net.ssl.SSLSocketFactory"); 
        props.put("mail.imaps.socketFactory.fallback", "false"); 
        props.setProperty("mail.imaps.quitwait", "false"); 
		
		try {
			Session session = Session.getDefaultInstance(props, null);
			session.setDebug(true);
			
			Store store = session.getStore("imaps");
			store.connect("imap.gmail.com", sMailAccount, sMailPwd);

			if (BackupProcessor.m_bNeedStop) {
				return;
			}

			Folder inbox = store.getFolder("Inbox");
			inbox.open(Folder.READ_ONLY);	
					
			//////////////////////////////////////////////////////////////
			Message[] messages = inbox.getMessages();			
			
			for (int i = 0; i < messages.length; i++)
			{
				if (BackupProcessor.m_bNeedStop) 
				{
					return;
				}
				
				MobiTNTLog.write("msg  	: " + messages[i].getMessageNumber()); 
				MobiTNTLog.write("Sent 	: " + messages[i].getSentDate()); 
				MobiTNTLog.write("From  : " + messages[i].getFrom()[0]); 
				MobiTNTLog.write("Subject: "+ messages[i].getSubject()); 
                
                int nMsgNumber = messages[i].getMessageNumber();
                Date dateMsgSentDate = messages[i].getSentDate();
                String sFrom = messages[i].getFrom()[0].toString();
                String sSubject = messages[i].getSubject();                
                
				if(messages[i].getContent() instanceof MimeMultipart) 
				{ 
					MimeMultipart mp = (MimeMultipart)messages[i].getContent(); 
					
					for(int j = 0, count = mp.getCount(); j < count; j++) 
					{ 
						Part p = mp.getBodyPart(j); 
						
						String sMailContent = p.getContent().toString();
						
						// parse the mail content
						conFilter.ParseMailContent(sSubject, sMailContent);
					} 
				} 
				else 
				{ 
					String sMailContent = messages[i].getContent().toString();
				} 
			}
			
			///////////////////////////////////////////////////////////////
			/*FlagTerm ft = new FlagTerm(new Flags(Flags.Flag.SEEN), false);
			if (BackupProcessor.m_bNeedStop) {

				return;
			}
			Message messages[] = inbox.search(ft);
			for (Message message : messages) {
				if (BackupProcessor.m_bNeedStop) {

					return;
				}
				// message.setFlag(Flags.Flag.ANSWERED, true);
				// message.setFlag(Flags.Flag.SEEN, true);
				Object o = message.getContent();
				if (!(o instanceof String)) {
					continue;
				}

				String subject = message.getSubject();
				String content = (String) message.getContent();

				conFilter.ParseMailContent(subject, content);
				
				//if (iType == 0) {
				//	SmsFilter(subject, content);
				//} else {
				//	CallLogFilter(subject, content);
				//}

			}*/
			
			inbox.close(false); 
			store.close(); 
			
		} 
		catch (Exception e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static List<String> GetMailSubject(String sMailAccount, String sMailPwd) 
	{
		List<String> lsMailSubject = null;
		
		Properties props = System.getProperties();

		props.setProperty("mail.store.protocol", "imaps");
		props.setProperty("mail.imaps.host", "imaps.gmail.com"); 
		props.setProperty("mail.imaps.quitwait", "false"); 
		
        props.put("mail.imaps.auth", "true"); 
        props.put("mail.imaps.port", "993"); 
        props.put("mail.imaps.socketFactory.port", "993"); 
        props.put("mail.imaps.socketFactory.class", "javax.net.ssl.SSLSocketFactory"); 
        props.put("mail.imaps.socketFactory.fallback", "false"); 
        		
		try {
			Session session = Session.getDefaultInstance(props, null);
			session.setDebug(true);
			
			Store store = session.getStore("imaps");
			store.connect("imap.gmail.com", sMailAccount, sMailPwd);

			Folder inbox = store.getFolder("Inbox");
			inbox.open(Folder.READ_ONLY);	
		
			Message[] messages = inbox.getMessages();	
			for (int i = 0; i < messages.length; i++)
			{
				if (BackupProcessor.m_bNeedStop) 
				{
					inbox.close(false); 
					store.close(); 					
					return lsMailSubject;
				}
				
				MobiTNTLog.write("msg  	: " + messages[i].getMessageNumber()); 
				MobiTNTLog.write("Sent 	: " + messages[i].getSentDate()); 
				MobiTNTLog.write("From  : " + messages[i].getFrom()[0]); 
				MobiTNTLog.write("Subject: "+ messages[i].getSubject()); 
                                                
                lsMailSubject.add(messages[i].getSubject());				
			}
						
			inbox.close(false); 
			store.close(); 			
		} 
		catch (Exception e) 
		{			
			MobiTNTLog.write("exception happen when call getDefaultInstance()");
		}
		
		return lsMailSubject;
	}
	
	public static int m_iRestoreType = 0;
	
	public static void RetriveTime(String sSubject){
		if (sSubject.contains("at ")){
			String sTime = "";
		}
	}
	
	public static synchronized void GetBackupTimeList(String sMailAccount,
			String sMailPwd, int iType) {
		Properties props = System.getProperties();

		props.setProperty("mail.store.protocol", "imaps");
		try {
			Session session = Session.getDefaultInstance(props, null);
			session.setDebug(true);
			Store store = session.getStore("imaps");

			if (BackupProcessor.m_bNeedStop) {
				
				return;
			}

			store.connect("imap.gmail.com", sMailAccount, sMailPwd);

			Folder inbox = store.getFolder("Inbox");
			if (BackupProcessor.m_bNeedStop) {

				return;
			}
			inbox.open(Folder.READ_ONLY);
			if (BackupProcessor.m_bNeedStop) {

				return;
			}
			FlagTerm ft = new FlagTerm(new Flags(Flags.Flag.SEEN), false);
			if (BackupProcessor.m_bNeedStop) {

				return;
			}
			Message messages[] = inbox.search(ft);
			for (Message message : messages) {
				if (BackupProcessor.m_bNeedStop) {

					return;
				}
				// message.setFlag(Flags.Flag.ANSWERED, true);
				// message.setFlag(Flags.Flag.SEEN, true);
				Object o = message.getContent();
				if (!(o instanceof String)) {
					continue;
				}

				String subject = message.getSubject();
				if (subject == null || subject.length() < 1){
					continue;
				}
				RetriveTime(subject);
				

			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


}
