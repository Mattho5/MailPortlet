package sk.mattho.portlets.mailPortlet.mail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.mail.Authenticator;
import javax.mail.FetchProfile;
import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.event.StoreEvent;
import javax.mail.event.StoreListener;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import com.sun.mail.imap.IMAPFolder.FetchProfileItem;

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
	private Map<String, List<Message>> folders;
	public static String SEPARATOR = "__";

	public GenericMail(String imap_pop, String smtp, boolean aut) {
		init();
	}

	@PostConstruct
	private void init() {
		// TODO Auto-generated method stub

		this.messages = new ArrayList<Message>();
		this.folders = new HashMap<String, List<Message>>();
		this.listeners = new ArrayList<MailMessageListener>();

	}

	public GenericMail() {
		super();
		init();
	}

	public boolean connect() throws MessagingException {

		Properties props = System.getProperties();
		if (this.ssl)
			props.put("mail.smtp.ssl.enable", "true");
		else
			props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", this.smtpServerUrl);
		props.put("mail.smtp.port", this.smtpPort.toString());
		props.put("mail.smtp.auth", "true");

		props.setProperty("mail.store.protocol", "imaps");
		this.session = Session.getInstance(props, new Authenticator() {

			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(userName, password);
			}
		});

		// this.session.setDebug(true);
		this.store = session.getStore("imaps");
		// session.
		this.store.addStoreListener(new StoreListener() {

			@Override
			public void notification(StoreEvent arg0) {
				// TODO Auto-generated method stub
				System.out.println("Something happend " + arg0.getMessage());

			}
		});

		this.store.connect(imapPopServerUrl, userName, password);
		if (store.isConnected()) {
			// this.

			return true;
		} else
			return false;
	}

	public void sendMessage(String to, String m, String subject) {

		try {
			Message message = new MimeMessage(this.session);
			message.setFrom(new InternetAddress(this.userName));
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(to));
			message.setSubject(subject);
			message.setText(m);
			Multipart mp = new MimeMultipart();

			MimeBodyPart mbp1 = new MimeBodyPart();
			mbp1.setContent(m, "text/html");
			mp.addBodyPart(mbp1);
		} catch (MessagingException e) {
			System.out.println("Sending ERROD" + e.getMessage());
			// e.printStackTrace();
		}
	}

	public ArrayList<String> getFolders() {
		// this.store.
		ArrayList<String> temp = new ArrayList<String>();

		try {
			for (Folder f : this.store.getDefaultFolder().list())
				// System.out.println(f.getFullName());
				temp.add(this.userName + SEPARATOR + f.getFullName());
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		}

		return temp;
	}

	public void initFolder(String folder) {
		Folder f;
		List<Message> temp;
		try {
			f = this.store.getFolder(folder);
			// f.
			if (!f.exists())
				System.out.println("dont exist");
			f.open(Folder.READ_WRITE);
			// temp=Arrays.asList(f.getMessages());
			Message[] tmp = f.getMessages();
			FetchProfile fp = new FetchProfile();
			fp.add(FetchProfile.Item.ENVELOPE);
			fp.add(FetchProfileItem.FLAGS);
			fp.add(FetchProfileItem.CONTENT_INFO);

			fp.add("X-mailer");
			f.fetch(tmp, fp);
			temp = Arrays.asList(tmp);
			resortFolder(temp);
			this.folders.put(folder, temp);

		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public List<Message> getMessages(String folder) {
		List<Message> temp = null;
		if (this.folders.containsKey(folder)) {
			temp = this.folders.get(folder);
			resortFolder(temp);
		} else {
			initFolder(folder);
			temp = this.folders.get(folder);
			resortFolder(temp);
			// else return temp;
		}
		return temp;
	}

	public void resortFolder(List<Message> messages) {
		Collections.sort(messages, new Comparator<Message>() {

			@Override
			public int compare(Message o1, Message o2) {
				try {
					if (o1.getFlags().contains(Flag.SEEN)
							&& (!o2.getFlags().contains(Flag.SEEN)))
						return 1;
					else if ((!o1.getFlags().contains(Flag.SEEN))
							&& (o2.getFlags().contains(Flag.SEEN)))
						return -1;
					else
						return o2.getReceivedDate().compareTo(
								o1.getReceivedDate());
				} catch (MessagingException e) {
					// TODO Auto-generated catch block
					try {
						return o2.getReceivedDate().compareTo(
								o1.getReceivedDate());
					} catch (MessagingException e1) {
						// TODO Auto-generated catch block
						return 0;
					}
					// e.printStackTrace();
				}
			}
		});
	}

	/**
	 * 
	 */

	public void disconnect() {
		if (this.store.isConnected())
			try {
				// this.inbox.c
				this.store.close();

				this.folders.clear();
				this.listeners.clear();
			} catch (MessagingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		// this.session.
	}

	// ===============================================================
	// Notify about
	// ===============================================================

	public void notifyAboutMessage() {
		for (MailMessageListener m : this.listeners) {
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
