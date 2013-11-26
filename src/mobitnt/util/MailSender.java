package  mobitnt.util;

import java.util.Date;
import java.util.Properties;
import javax.activation.CommandMap;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.activation.MailcapCommandMap;
import javax.mail.BodyPart;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.Authenticator;

import mobitnt.backup.BackupOption;

public class MailSender extends Authenticator {
	private String _user;
	private String _pass;
	private String[] _to;
	private String _from;
	private String _port;
	private String _host;
	private String _subject;
	private String _body;
	private boolean _auth;
	private boolean _debuggable;
	private Multipart _multipart;

	public MailSender() {
		_host = "smtp.gmail.com"; // default smtp server
		_port = "465"; // default smtp port
		_user = ""; // username
		_pass = ""; // password
		_from = ""; // email sent from
		_subject = ""; // email subject
		_body = ""; // email body
		_debuggable = false; // debug mode on or off - default off
		_auth = true; // smtp authentication - default on
		_multipart = new MimeMultipart(); // There is something wrong with
											// MailCap, javamail can not find a
											// handler for the multipart/mixed
											// part, so this bit needs to be
											// added.
		MailcapCommandMap mc = (MailcapCommandMap) CommandMap
				.getDefaultCommandMap();
		mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
		mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
		mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
		mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
		mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822");
		CommandMap.setDefaultCommandMap(mc);
	}

	public MailSender(String user, String pass) {
		this();
		_user = user;
		_pass = pass;
	}

	public boolean send() throws Exception {
		Properties props = _setProperties();

		if (!_user.equals("") && !_pass.equals("") && _to.length > 0
				&& !_from.equals("") && !_subject.equals("")
				&& !_body.equals("")) {
			Session session = Session.getInstance(props, this);
			MimeMessage msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress(_from));
			InternetAddress[] addressTo = new InternetAddress[_to.length];
			for (int i = 0; i < _to.length; i++) {
				addressTo[i] = new InternetAddress(_to[i]);
			}

			msg.setRecipients(MimeMessage.RecipientType.TO, addressTo);
			msg.setSubject(_subject);
			msg.setSentDate(new Date());
			// setup message body
			BodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.setText(_body);
			_multipart.addBodyPart(messageBodyPart);

			// Put parts in message
			msg.setContent(_multipart);
			// send email
			Transport.send(msg);
			return true;
		} else {
			return false;
		}
	}

	public void addAttachment(String filename) throws Exception {
		BodyPart messageBodyPart = new MimeBodyPart();
		DataSource source = new FileDataSource(filename);
		messageBodyPart.setDataHandler(new DataHandler(source));
		messageBodyPart.setFileName(filename);
		_multipart.addBodyPart(messageBodyPart);
	}

	@Override
	public PasswordAuthentication getPasswordAuthentication() {
		return new PasswordAuthentication(_user, _pass);
	}

	private Properties _setProperties() {
		Properties props = new Properties();

		if (_debuggable) {
			props.put("mail.debug", "true");
		}

		if (_auth) {
			props.put("mail.smtp.auth", "true");
		}

		props.put("mail.smtp.host", _host);

		// Èç¹ûÊÇGMAILÓÊÏä
		if (this._host.indexOf("smtp.gmail.com") >= 0) {
			props.put("mail.smtp.port", "465");
			props.put("mail.smtp.socketFactory.port", "465");
			props.put("mail.smtp.socketFactory.class",
					"javax.net.ssl.SSLSocketFactory");
			props.put("mail.smtp.socketFactory.fallback", "false");
		} else {
			props.put("mail.smtp.port", this._port);
		}

		return props;
	}

	public void setPort(String nPort) {
		this._port = nPort;
	}

	public void setServerHost(String serverHost) {
		this._host = serverHost;
	}

	// the getters and setters
	public String getBody() {
		return _body;
	}

	public void setBody(String _body) {
		this._body = _body;
	}

	public void setTo(String[] toArr) {
		this._to = toArr;
	}

	public void setFrom(String string) {
		this._from = string;
	}

	public void setSubject(String string) {
		this._subject = string;
	}

	static MailSender m_sender = null;

	static public int SendMail(String sSubject, String sBody, String sErr) {
		String sMailAccount = BackupOption.GetMailAccount();
		String sMailPwd = BackupOption.GetMailPassword();

		if (m_sender == null) {
			m_sender = new MailSender(sMailAccount, sMailPwd);
			String[] toArr = { sMailAccount };
			m_sender.setTo(toArr);
			m_sender.setFrom(sMailAccount);
		}

		m_sender.setSubject(sSubject);
		m_sender.setBody(sBody);

		try {
			// m.addAttachment("/sdcard/filelocation");
			if (!m_sender.send()) {
				sErr = "Failed when transfer sms";
			}
		} catch (Exception e) {
			sErr = "Failed when transfer sms: " + e.toString();
			return -1;
		}

		return 0;
	}
}
