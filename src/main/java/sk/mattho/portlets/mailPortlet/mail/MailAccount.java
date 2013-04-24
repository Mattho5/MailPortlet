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
import javax.mail.Flags;
import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.event.MessageCountEvent;
import javax.mail.event.MessageCountListener;
import javax.mail.event.StoreEvent;
import javax.mail.event.StoreListener;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.FlagTerm;

import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPFolder.FetchProfileItem;

public class MailAccount {
	private String imapServerUrl;
	private String smtpServerUrl;
	private List<MailMessageListener> listeners;
	private String userName;
	private int messageCount;
	private Integer smtpPort;
	private Integer imapPort;
	private boolean ssl;
	private Session session;
	private String password;
	private Store store;
	private IMAPFolder inbox;
	private List<Message> messages;
	private Map<String, List<Message>> folders;
	public static String SEPARATOR = "__";
	private int unreadedMessages;

	public MailAccount(String imap_pop, String smtp, boolean aut) {
		init();
	}

	@PostConstruct
	private void init() {
		// TODO Auto-generated method stub

		this.messages = new ArrayList<Message>();
		this.folders = new HashMap<String, List<Message>>();
		this.listeners = new ArrayList<MailMessageListener>();
	}

	public MailAccount() {
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
		this.store.addStoreListener(new StoreListener() {

			@Override
			public void notification(StoreEvent arg0) {
				// TODO Auto-generated method stub
				System.out.println("Something happend " + arg0.getMessage());
				if (!store.isConnected())
					try {
						store = session.getStore("imaps");
					} catch (NoSuchProviderException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			}
		});

		this.store.connect(imapServerUrl, userName, password);
		if (store.isConnected()) {
			this.inbox = (IMAPFolder) store.getFolder("Inbox");
			this.messageCount=this.inbox.getMessageCount();
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
			for (Folder f : this.store.getDefaultFolder().list()) {
				// System.out.println(f.getFullName());
				if (f.getName().equalsIgnoreCase("inbox"))
					f.addMessageCountListener(new MessageCountListener() {

						@Override
						public void messagesRemoved(MessageCountEvent arg0) {
							// TODO Auto-generated method stub

						}

						@Override
						public void messagesAdded(MessageCountEvent arg0) {
							System.out.print("new message");

						}
					});
				temp.add(this.userName + SEPARATOR + f.getFullName());
			}
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		}

		return temp;
	}
	/**
	 * This method read messages from folder into internal map
	 * @param folder
	 */
	public void initFolder(String folder) {
		Folder f;
		List<Message> temp;
		try {
			f = this.store.getFolder(folder);
			// f.
			if (!f.exists())
				System.out.println("dont exist");
			f.open(Folder.READ_WRITE);

			// do not read delete messages
			FlagTerm ft = new FlagTerm(new Flags(Flag.DELETED), false);
			int mc = f.getMessageCount();
			Message[] tmp;
			// read only last 100 messages
			if (mc > 101)
				tmp = f.getMessages(mc - 100, f.getMessageCount());
			else
				tmp = f.getMessages();

			FetchProfile fp = new FetchProfile();
			fp.add(FetchProfile.Item.ENVELOPE);
			fp.add(FetchProfileItem.FLAGS);
			fp.add(FetchProfileItem.CONTENT_INFO);
			fp.add("X-mailer");

			f.fetch(tmp, fp);
			temp = Arrays.asList(tmp);
			resortFolder(temp);
			String folderName = folder;
			
			if (!this.folders.containsKey(folder))
				this.folders.put(folder, temp);
			else {
				this.folders.remove(folder);
				this.folders.put(folder, temp);
			}

		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
/**
 * Return list of messages
 * @param folder name of folder
 * @param reload if is true method reload messages list and return it  otherwise method will return messages from map
 * @return list of messages
 */
	public List<Message> getMessages(String folder, boolean reload) {
		List<Message> temp = null;
		if (folder.compareToIgnoreCase("inbox") == 0)
			folder = "Inbox";
		if (!this.folders.containsKey(folder) || reload)
			initFolder(folder);

		temp = this.folders.get(folder);

		resortFolder(temp);
		return temp;
	}

	/**
	 * This method resort list of message according to seen and date flags
	 * @param messages
	 */
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
	 * Disconect from mail server.
	 */

	public void disconnect() {
		if (this.store.isConnected())
			try {
				this.store.close();
				
				this.folders.clear();
				this.listeners.clear();
			} catch (MessagingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		// this.session.
	}
	/**
	 * This method is chcecking for new messages
	 */
	public void checkNewMessages() {
		try {
			if (inbox.getMessageCount() > this.messageCount){
				System.out.println("New message");
				this.initFolder("Inbox");
				this.notifyAboutMessage();
				this.messageCount =inbox.getMessageCount();
				
				
				
			}
			else {
				System.out.println("no new messages");
			}
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	public void initMessagesCount(){
		try {
			this.messageCount=this.inbox.getMessageCount();
			this.unreadedMessages=this.inbox.getUnreadMessageCount();
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public void addMessageListener(MailMessageListener m) {
		this.listeners.add(m);
	}

	public void removeMessageListener(MailMessageListener m) {
		if (this.listeners.contains(m))
			this.listeners.remove(m);
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

	public String getImapServerUrl() {
		return imapServerUrl;
	}

	public void setImapServerUrl(String imapPopServerUrl) {
		this.imapServerUrl = imapPopServerUrl;
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

	public Integer getImapPort() {
		return imapPort;
	}

	public void setImapPort(Integer pomImapPort) {
		this.imapPort = pomImapPort;
	}

	public int getUnreadedMessages() {
		return unreadedMessages;
	}

	public void setUnreadedMessages(int unreadedMessages) {
		this.unreadedMessages = unreadedMessages;
	}
	
	

}
