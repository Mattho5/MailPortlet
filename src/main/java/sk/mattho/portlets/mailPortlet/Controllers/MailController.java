package sk.mattho.portlets.mailPortlet.Controllers;

import java.io.File;
import java.io.IOException;
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
import javax.mail.Multipart;
import javax.mail.Flags.Flag;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import javax.portlet.ActionRequest;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;
import javax.portlet.ReadOnlyException;
import javax.portlet.ValidatorException;

import org.richfaces.application.push.MessageException;
import org.richfaces.application.push.TopicKey;
import org.richfaces.application.push.TopicsContext;
import org.richfaces.event.FileUploadEvent;
import org.richfaces.model.UploadedFile;

import sk.mattho.portlets.mailPortlet.AccountInfo;
import sk.mattho.portlets.mailPortlet.PreferencesAccounts;
import sk.mattho.portlets.mailPortlet.mail.MailAccount;
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

	// TODO mazanie s preferencii
	// TODO ucet
	// TODO
	// TODO
	@Inject
	MailManager manager;
	// ------- account values-----------------
	private boolean connected;
	private boolean error;
	private String imapUrl;
	private String smtpUrl;
	private int imapPort;
	private int smtpPort;
	private String userName;
	private String password;
	private boolean secured;
	private boolean ssl;
	private MailConfigurations selectedConfiguration;

	// ---------------- new message values -----------
	private String currentFolder;
	private ByteArrayDataSource attachment;

	private String messageText;
	private String subject;
	private String recipients;

	private String selectedAccounts;
	private Message actualMessage;
	// -------------------------------------------
	private PreferencesAccounts storedAccounts;
	private List<Message> folderMessages;
	// needed for PUSH
	private final String userIdentifier = UUID.randomUUID().toString()
			.replace("-", "");
	private TopicsContext topicsContext;
	private static final String PUSH_TOPIC = "mailService";

	private boolean saveAccount;

	private int view;

	// view 0 - list of messages
	// view 1 concrete message
	// view 2 new message

	@PostConstruct
	public void init() {
		this.connected = false;
		this.selectedConfiguration = MailConfigurations.GMAIL;
		this.secured=false;
		this.ssl=false;
		this.imapPort=MailConfigurations.DEFAULT_IMAP;
		this.smtpPort=MailConfigurations.DEFAULT_SMTP;
		this.view = 0;

	}

	public void messagesListRefresh() {
		this.folderMessages = manager.getMessages(currentFolder, true);
		this.view=0;
		this.error=false;
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
		try {
		this.connected = addAccount();
		if(this.connected)
			if( this.saveAccount)
				addToPreferences();
				this.selectedAccounts = this.manager.getMailAccounts().get(
				0);			
			}
		
		catch (MessagingException e) {
				// TODO Auto-generated catch block
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(e.getMessage()));
				e.printStackTrace();			
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
		
	}

	private boolean addAccount() throws MessagingException {
			if (this.selectedConfiguration != MailConfigurations.OTHER) {
			  return this.manager.addMailAccount(userName, password,
						selectedConfiguration, this);
			}
			else{
				return this.manager.addMailAccount(userName, password,
						smtpUrl,smtpPort,imapUrl,imapPort,secured,ssl,this);
					
			}
	}
			
				
		
		// this.connected=


	public void addNewAccount() {
		try {
			if (addAccount()) {
				if(saveAccount)
					addToPreferences();
				System.out.println("Added account"
						+ this.manager.getMailAccounts().size());

			} else
				System.out.println("error account");
		} 
		catch (MessagingException e) {
			// TODO Auto-generated catch block
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(e.getMessage()));
			e.printStackTrace();
			
	} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	Message m=this.actualMessage;
	
	public boolean checkMessage(Message m){
		try {
			if (m.getContent() instanceof String)
				return true;
			else {
				MimeMultipart multipart = (MimeMultipart) m.getContent();
				for (int i = 0; i < multipart.getCount(); i++) {
					MimeBodyPart b = (MimeBodyPart) multipart.getBodyPart(i);

					String disposition = b.getDisposition();

					// if(disposition==null && b.getContentType())
					// return (String) b.getContent();
					if (b.getContent() instanceof Multipart)
						return true;
					
				}
				return true;
			}
		}catch (Exception e){
		
		return false;
		}
		
	}
	public void send() {
		// todo more recipients
		this.manager.send(messageText, subject, recipients, selectedAccounts,
				attachment);
	}

	public boolean isConnected() {
		return connected;
	}

	public void checkNewEmails() {

		// refreshWindowMessageList();
		try{
		this.manager.checkforNewEmails();
		}
		catch (Exception e){
			this.disconnect();
			
		}

	}

	public void logout() {

		this.connected = false;
		this.manager.disconnect();
		this.folderMessages = null;

	}

	public void setActualMessage(Message actualMessage) {
		try {
			this.actualMessage = actualMessage;
			this.error=!checkMessage(actualMessage);
			System.out.println("Setting CM "
					+ actualMessage.getFrom()[0].toString());
			this.view = 1;
			
			actualMessage.setFlag(Flag.SEEN, true);
			this.manager.refreshMessagesCounts();
			refreshFolderWindow();
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

	public void disconnect(String acc) {
		if (this.manager.getMailAccounts().size() - 1 < 1)
			this.connected = false;
		this.manager.disconnect(acc);

	}

	public void listener(FileUploadEvent event) throws Exception {
		UploadedFile item = event.getUploadedFile();

		System.out
				.println(item.getContentType() + " velkost " + item.getSize());
		ByteArrayDataSource bds = new ByteArrayDataSource(item.getData(),
				item.getContentType());
		bds.setName(item.getName());
		this.attachment = bds;

		// File f=(File) item;
		// UploadedImage file = new UploadedImage();
		// file.setLength(item.getData().length);
		// file.setName(item.getName());
		// file.setData(item.getData());
		// item.ge
	}

	// =============================================
	// PORTLET PREFERENCES
	// =============================================

	public void initFromPreferences() {
		PreferencesAccounts pr = this.getFromPreferences();
		if (pr != null) {
			this.storedAccounts = pr;
			for (AccountInfo a : pr.accounts()) {
				try {
					if (this.manager.addMailAccount(a.getUserName(),
							a.getPassword(), a.getSmtpServer(),
							a.getSmtpPort(), a.getImapServer(),
							a.getImapPort(), a.isSecured(), a.isSsl(), this))
						this.connected = true;
					// this.
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} else
			this.storedAccounts = new PreferencesAccounts();

	}

	private boolean saveToPreferences(PreferencesAccounts pref)
			throws ReadOnlyException, ValidatorException, IOException {
		PortletRequest request = (PortletRequest) FacesContext
				.getCurrentInstance().getExternalContext().getRequest();
		if (request instanceof ActionRequest) {
			ActionRequest actionReq = (ActionRequest) request;
			PortletPreferences p = actionReq.getPreferences();
			p.reset("accounts");
			p.setValue("accounts", pref.getAccounts());
			actionReq.getPreferences().store();
			System.out.println("preferences stored");
			return true;

		} else {
			System.out.print("Isn't instance of ActionRequest");
		}
		return false;
	}

	private PreferencesAccounts getFromPreferences() {
		Object request = FacesContext.getCurrentInstance().getExternalContext()
				.getRequest();
		if (request instanceof ActionRequest) {
			ActionRequest actionReq = (ActionRequest) request;
			PortletPreferences p = actionReq.getPreferences();
			String temp = "";
			temp = p.getValue("accounts", temp);

			if (temp != null && temp.compareTo("") != 0) {
				PreferencesAccounts pr = new PreferencesAccounts(temp);
				return pr;

			} else {
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage("You haven't saved any account"));
				;
				this.storedAccounts = new PreferencesAccounts();
			}
			return null;

		}
		return null;
	}

	private void addToPreferences() throws ReadOnlyException,
			ValidatorException, IOException {
		PreferencesAccounts pr = this.getFromPreferences();
		if (pr == null)
			pr = new PreferencesAccounts();
		AccountInfo prefAccount = new AccountInfo(userName, password,
				this.selectedConfiguration);
		prefAccount.setImapPort(this.imapPort);
		prefAccount.setImapServer(this.imapUrl);
		prefAccount.setSmtpPort(this.smtpPort);
		prefAccount.setSmtpServer(this.smtpUrl);
		prefAccount.setSecured(this.secured);
		prefAccount.setSsl(this.ssl);
		pr.addAccount(prefAccount);

		saveToPreferences(pr);
		this.storedAccounts = pr;
	}
	
public void deleteAccount(AccountInfo acc) throws ReadOnlyException,
	ValidatorException, IOException {
	this.storedAccounts.accounts().remove(acc);
	this.saveToPreferences(this.storedAccounts);
}
	// ============================================
	// PUSH
	// ===========================================
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

	private void refreshFolderWindow() {
		try {
			getTopicsContext().publish(
					new TopicKey(PUSH_TOPIC, this.getUserIdentifier()
							+ "folderWindow"), "sprava");

		} catch (MessageException e) {
			e.printStackTrace();
		}
	}

	// ===========================================
	// IMPLEMENTATION OF LISTENER
	// =======================================

	@Override
	public void onIncomingMessage() {
		System.out.println("Incoming new message");
		if (this.currentFolder != null) {
			this.folderMessages = this.manager
					.getMessages(currentFolder, false);
			// if(this.currentFolder.contains("inbox"));
			// this.folderMessages=this.manager.getMessages(currentFolder,true);
			refreshFolderWindow();
			refreshWindowMessageList();

		}

	}

	@Override
	public void onConnect(MailAccount m) {
		refreshFolderWindow();

	}

	@Override
	public void onDisconnect(MailAccount m) {
		if(this.manager.getMailAccounts().size()<1)
			this.connected=false;
		refreshFolderWindow();

	}

	//============================================================
	// GETTERS AND SETTERS
	// ===========================================================

	public String getUserIdentifier() {
		return userIdentifier;
	}

	public List<String> getFolders() {
		return this.manager.getFolders();
	}

	public List<String> getFolders(String account) {
		return this.manager.getFolderList(account);
	}

	public String getImapPopUrl() {
		return imapUrl;
	}

	public void setImapPopUrl(String imapPopUrl) {
		this.imapUrl = imapPopUrl;
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

	public int getUnreadedMessageCount(String acc) {
		return this.manager.getUnreadedMessageCount(acc);
	}

	public String getSelectedAccounts() {
		return selectedAccounts;
	}

	public void setSelectedAccounts(String selectedAccounts) {
		this.selectedAccounts = selectedAccounts;
	}

	public String getImapUrl() {
		return imapUrl;
	}

	public void setImapUrl(String imapUrl) {
		this.imapUrl = imapUrl;
	}

	public boolean isSecured() {
		return secured;
	}

	public void setSecured(boolean secured) {
		this.secured = secured;
	}

	public boolean isSsl() {
		return ssl;
	}

	public void setSsl(boolean ssl) {
		this.ssl = ssl;
	}

	public boolean isSaveAccount() {
		return saveAccount;
	}

	public void setSaveAccount(boolean saveAccount) {
		this.saveAccount = saveAccount;
	}

	public PreferencesAccounts getStoredAccounts() {
		return storedAccounts;
	}

	public void setStoredAccounts(PreferencesAccounts storedAccounts) {
		this.storedAccounts = storedAccounts;
	}
	
	public boolean isError() {
		return error;
	}

	public void setError(boolean error) {
		this.error = error;
		if(error)
			System.out.println("je chyba");
	}


}
