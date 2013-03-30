package sk.mattho.portlets.mailPortlet.mail;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class GenericMail {
	private String imapPopServerUrl;
	private String smtpServerUrl;
	private List<MailMessageListener> listeners;
	private String userName;
	private Integer smtpPort;
	private Integer pomImapPort;
	private boolean ssl;
	private Session session;
	private String password;
	private Store store;
	private Folder inbox;
	private List<Message> messages;
	private Map<String,List<Message>> folders;
	public static String SEPARATOR="__";
	
	

	public GenericMail(String imap_pop, String smtp, boolean aut) {
		this.ssl = aut;
		this.imapPopServerUrl = imap_pop;
		this.smtpServerUrl = smtp;
		this.messages = new ArrayList<Message>();
		this.folders= new Map<String,List<Message>>();

	}

	public GenericMail(){
		super();
	}
	public boolean connect() throws MessagingException {
		
			Properties props = System.getProperties();
			if(this.ssl)
				props.put("mail.smtp.ssl.enable", "true");
			else
				props.put("mail.smtp.starttls.enable", "true");
			props.put("mail.smtp.host", this.smtpServerUrl);
			props.put("mail.smtp.port", this.smtpPort.toString());
			props.put("mail.smtp.auth", "true");
		
			
		
			props.setProperty("mail.store.protocol", "imaps");
			this.session = Session.getInstance(props,new Authenticator() {
			
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(userName, password);
				}});
			
			this.session.setDebug(true);
			this.store = session.getStore("imaps");
			
			this.store.connect(imapPopServerUrl, userName,
					password);
			
		//	for(Folder f:this.store.getDefaultFolder().list("*"))
				//	System.out.println(f.getFullName());
			//		this.folders.add(f);
			//store.co
			//this.inbox=store.getFolder("Inbox");
			//System.out.println(inbox.getMessageCount() + "Messages");
			//store.g
			if(store.isConnected())
			return true;
			else	
		return false;
	}
	
	public void sendMessage(String to, String m, String subject){
		
		try {
			Message message= new MimeMessage(this.session);
		message.setFrom(new InternetAddress(this.userName));
		message.setRecipients(Message.RecipientType.TO,InternetAddress.parse(to));
		message.setSubject(subject);
		message.setText(m);
		Multipart mp = new MimeMultipart();

		MimeBodyPart mbp1 = new MimeBodyPart();
		mbp1.setContent(m, "text/html");
		mp.addBodyPart(mbp1);
		message.setContent(mp);
	//	Transport t= this.session.getTransport("smtp");
	//	Transport.send(msg, user, subject)
	//	if(this.userName.contains("gmail.com"))
			Transport.send(message);
	//	else
		//	Transport.send(message,userName,password);
		} catch (MessagingException e) {
			System.out.println("Sending ERROD"+ e.getMessage());
		//	e.printStackTrace();
		}
	}
	
	public ArrayList<String> getFolders(){
		//this.store.
		ArrayList<String> temp = new ArrayList<String>();
	
		
			try {
				for(Folder f:this.store.getDefaultFolder().list("*"))
				//	System.out.println(f.getFullName());
					temp.add(this.userName+SEPARATOR+f.getFullName());
			} catch (MessagingException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
		
		return temp;
	}
	
	public Message[] getMessages(String folder){
		return null;
	}
	public void disconnect(){
		if(this.store.isConnected())
			try {
				//this.inbox.c
				this.store.close();
			} catch (MessagingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		//this.session.
	}
	// ===============================================================
		// Notify about 
		// ===============================================================
	
	public void notifyAboutMessage(){
		for(MailMessageListener m:this.listeners){
			m.onIncomingMessage();
		}
	}
	// ===============================================================
	// GETTERS AND SETTERS
	// ===============================================================
	
	public String getImapPopServerUrl() {
		return imapPopServerUrl;
	}

	public void setImapPopServerUrl(String imapPopServerUrl) {
		this.imapPopServerUrl = imapPopServerUrl;
	}

	public String getSmtpServerUrl() {
		return smtpServerUrl;
	}

	public void setSmtpServerUrl(String smtpServerUrl) {
		this.smtpServerUrl = smtpServerUrl;
	}

	public boolean isSSL() {
		return ssl;
	}

	public void setSSL(boolean auth) {
		this.ssl = auth;
	}

	public List<Message> getMessages() {
		return messages;
	}

	public void setMessages(List<Message> messages) {
		this.messages = messages;
	}
	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}


	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}


	public Integer getSmtpPort() {
		return smtpPort;
	}


	public void setSmtpPort(Integer smtpPort) {
		this.smtpPort = smtpPort;
	}


	public Integer getPomImapPort() {
		return pomImapPort;
	}


	public void setPomImapPort(Integer pomImapPort) {
		this.pomImapPort = pomImapPort;
	}

}
