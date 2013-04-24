package sk.mattho.portlets.mailPortlet.Controllers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Flags.Flag;

import org.richfaces.application.push.MessageException;
import org.richfaces.application.push.TopicKey;
import org.richfaces.application.push.TopicsContext;

import sk.mattho.portlets.mailPortlet.mail.MailConfigurations;
import sk.mattho.portlets.mailPortlet.mail.MailManager;
import sk.mattho.portlets.mailPortlet.mail.MailMessageListener;

@Named
@SessionScoped
public class MailController implements Serializable, MailMessageListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -543261858181040481L;

	@Inject
	MailManager manager;

	private boolean connected;
	private String imapPopUrl;
	private String smtpUrl;
	private int imapPort;
	private int smtpPort;
	private String userName;
	private String password;
	private MailConfigurations selectedConfiguration;
	private String currentFolder;

	private String messageText;
	private String subject;
	private String recipients;

	private String selectedAccounts;
	private Message actualMessage;
	private List<Message> folderMessages;
	// needed for PUSH
	private final String userIdentifier = UUID.randomUUID().toString()
			.replace("-", "");
	private TopicsContext topicsContext;
	private static final String PUSH_TOPIC = "mailService";

	public String getSelectedAccounts() {
		return selectedAccounts;
	}

	public void setSelectedAccounts(String selectedAccounts) {
		this.selectedAccounts = selectedAccounts;
	}

	private int view;

	// view 0 - list of messages
	// view 1 concrete message
	// view 2 new message

	@PostConstruct
	public void init() {
		this.connected = false;
		this.selectedConfiguration = MailConfigurations.GMAIL;
		this.view = 0;

	}

	public void messagesListRefresh() {
		this.folderMessages = manager.getMessages(currentFolder, true);
	}

	public void showList() {
		this.view = 0;
		// this.messagesListRefresh();
	}

	public void deleteMessage(Message m) throws MessagingException {
		// m.getFlags().add(Flag.DELETED);
		this.manager.deleteMessage(m);
		this.messagesListRefresh();
		this.refreshWindowMessageList();
	}

	public void starMessage(Message m) throws MessagingException {

		if (m.getFlags().contains(Flag.FLAGGED)) {
			m.setFlag(Flag.FLAGGED, false);
			System.out.print("unflaging");
		} else
			m.setFlag(Flag.FLAGGED, true);

	}

	public void connect() {
		if (this.selectedConfiguration != MailConfigurations.OTHER) {
			try {
				this.connected = this.manager.addMailAccount(userName,
						password, selectedConfiguration,this);
				this.selectedAccounts = this.manager.getMailAccounts().get(0);
			} catch (MessagingException e) {
				// TODO Auto-generated catch block
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(e.getMessage()));
				e.printStackTrace();
			}

			// this.connected=
		}

	}

	public void initFromPreferences() {
		// TODO complete preferences
	}

	public void saveToPreferences() {
		// TODO complete preferences
	}

	public void send() {
		// todo more recipients
		this.manager.send(messageText, subject, recipients, selectedAccounts);
	}

	public boolean isConnected() {
		return connected;
	}

	public void checkNewEmails() {

		// refreshWindowMessageList();

		this.manager.checkforNewEmails();

	}

	public void logout() {

		this.connected = false;
		this.manager.disconnect();
		this.folderMessages = null;

	}

	// ----------------------------------------------
	// PUSH
	// ---------------------------------------------
	private TopicsContext getTopicsContext() {

		if (topicsContext == null) {
			topicsContext = TopicsContext.lookup();
		}
		return topicsContext;
	}

	private void refreshWindowMessageList() {
		try {
			getTopicsContext().publish(
					new TopicKey(PUSH_TOPIC, this.getUserIdentifier()
							+ "newmessage"), "sprava");

		} catch (MessageException e) {
			e.printStackTrace();
		}
	}

	// ------------------------------------------------------------
	// GETTERS AND SETTERS
	// -----------------------------------------------------------

	public String getUserIdentifier() {
		return userIdentifier;
	}

	public List<String> folders() {
		return this.manager.getFolders();
	}

	public String getImapPopUrl() {
		return imapPopUrl;
	}

	public void setImapPopUrl(String imapPopUrl) {
		this.imapPopUrl = imapPopUrl;
	}

	public String getSmtpUrl() {
		return smtpUrl;
	}

	public void setSmtpUrl(String smtpUrl) {
		this.smtpUrl = smtpUrl;
	}

	public int getImapPort() {
		return imapPort;
	}

	public void setImapPort(int imapPort) {
		this.imapPort = imapPort;
	}

	public int getSmtpPort() {
		return smtpPort;
	}

	public void setSmtpPort(int smtpPort) {
		this.smtpPort = smtpPort;
	}

	public MailConfigurations getSelectedConfiguration() {
		return selectedConfiguration;
	}

	public void setSelectedConfiguration(
			MailConfigurations selectedConfiguration) {
		this.selectedConfiguration = selectedConfiguration;
	}

	public MailConfigurations[] getConfigurations() {
		return MailConfigurations.values();
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

	public int getView() {
		return view;
	}

	public void setView(Long view) {
		this.view = view.intValue();
	}

	public String getMessageText() {
		return messageText;
	}

	public void setMessageText(String messageText) {
		this.messageText = messageText;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getRecipients() {
		return recipients;
	}

	public void setRecipients(String recipients) {
		this.recipients = recipients;
	}

	public List<String> getAccounts() {
		return this.manager.getMailAccounts();
	}

	public Message getActualMessage() {
		return actualMessage;
	}

	public void setActualMessage(Message actualMessage) {
		try {
			System.out.println("Setting CM "
					+ actualMessage.getFrom()[0].toString());
			this.view = 1;
			actualMessage.setFlag(Flag.SEEN, true);
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.actualMessage = actualMessage;
	}

	public List<Message> getFolderMessages() {
		return folderMessages;
	}

	public void readMessagesFromFolder(String folder) {
		this.folderMessages = this.manager.getMessages(folder, false);
		// g
		this.currentFolder = folder;
		this.actualMessage = null;
		this.view = 0;
	}

	@PreDestroy
	public void disconnect() {
		this.manager.disconnect();
	}

	public Date getDate() {
		return new Date();
	}

	// ===========================================

	@Override
	public void onIncomingMessage() {
		System.out.println("Incoming new message");
		if (this.currentFolder != null) {
			this.folderMessages = this.manager
					.getMessages(currentFolder, false);
			// if(this.currentFolder.contains("inbox"));
			// this.folderMessages=this.manager.getMessages(currentFolder,true);
			this.refreshWindowMessageList();

		}

	}
}
