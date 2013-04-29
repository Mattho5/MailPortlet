package sk.mattho.portlets.mailPortlet.mail;

import java.io.File;
import java.io.FileInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.activation.FileDataSource;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Flags.Flag;
import javax.mail.event.MailEvent;
import javax.mail.util.ByteArrayDataSource;

import org.richfaces.model.UploadedFile;

public class MailManager implements Serializable,MailMessageListener {

	/**
	 * List of connected mailboxes
	 */
	private List<MailAccount> mailBoxList;
	/**
	 * List of all folders of mailboxes
	 */
	private List<String> folders;
	/**
	 * this is adress book, it is not used yet
	 */
	private Map<String,String> adressbook;
	
	@PostConstruct
	private void init(){
		this.mailBoxList= new ArrayList<MailAccount>();
		this.folders= new ArrayList<String>();
	}
	
	@PreDestroy
	private void preDestroy(){
		for(MailAccount m:this.mailBoxList){
			m.disconnect();
		}
		this.folders.clear();
		this.mailBoxList.clear();
		
	}
	/**
	 * This method allows connect to mail account and if account is valid , account is added into the mailbox list.
	 * @param userName - user name of account
	 * @param password  - password
	 * @param m - value from enumeration MailConfigurations. This can't be OTHER value
	 * @return true if is connected or false if is not.
	 * @throws MessagingException
	 */
	public boolean addMailAccount(String userName,String password,MailConfigurations m,MailMessageListener listener) throws MessagingException{
		
		if(m!=MailConfigurations.OTHER){
			 return addMailAccount(userName, password,m.getSmtpUrl(),m.getSmtpPort(), m.getImapUrl(), m.getImapPort(),m.isSecured(),m.isSsl(),listener);
		}
		return false;
		
	}
	/**
	 * This method allows connect to mail account and if account is valid , acc is added into the mailbox list.
	 * @param userName - user name of account
	 * @param password - password
	 * @param smtp - url of smtp server
	 * @param smtpport - smtp port
	 * @param imap - url of imap server
	 * @param popimapport url of imap port
	 * @param ssl if true ssl is used otherwise tls
	 * @return true if is connected otherwise false
	 * @throws MessagingException
	 */
	public boolean addMailAccount(String userName,String password,String smtp,int smtpport, String imap,int imapport,boolean secured,boolean ssl,MailMessageListener listener) throws MessagingException{
		
		if(getMailAccount(userName)!=null)
			return false;
		MailAccount mailbox= new MailAccount();
	
		mailbox.setImapServerUrl(imap);
		mailbox.setSmtpServerUrl(smtp);
		mailbox.setSmtpPort(smtpport);
		mailbox.setImapPort(imapport);
		mailbox.setUserName(userName);
		mailbox.setPassword(password);
		mailbox.setSecured(secured);
		mailbox.setSSL(ssl);
		
	
		if(mailbox.connect()){
			System.out.println("connecting");
			this.mailBoxList.add(mailbox);
			this.folders.addAll(mailbox.getFolders());
			mailbox.addMessageListener(this);
			mailbox.addMessageListener(listener);
			return true;
		}
		else return false;
		
	}
	
	/**
	 * This method send a email
	 * @param message - text of message
	 * @param subject - subject of message
	 * @param reciptiens - reciptient
	 * @param account - name of account  for example user@account.com
	 */
	
	public void send(String message,String subject, String reciptiens, String account,ByteArrayDataSource bds){
		System.out.println("Sending from "+account);
		
	//	FileDataSource
		//FileInputStream fs= new 
		//ByteArrayDataSource bds=null;
		
		
		MailAccount m= this.getMailAccount(account);
					m.sendMessage(reciptiens, message, subject,bds);
							
		}
	/**
	 * This method search account in list of accounts
	 * @param acc account name -- f.e user@example.com
	 * @return MailAccount account if is  founded otherwise  null
	 */
	public MailAccount getMailAccount(String acc){
	
		for(MailAccount m: this.mailBoxList){
			if(m.getUserName().compareTo(acc)==0){
				System.out.print("ffind!");
				return m;
			}
		}
		return null;
	}
	/**
	 * This method is chcecking for new emails
	 */
	public void checkforNewEmails(){
		for(MailAccount m:this.mailBoxList)
			m.checkNewMessages();
	}
	
	/**
	 * Disconnect from all email accounts
	 */
	public void disconnect(){
		for(MailAccount m:this.mailBoxList)
			m.disconnect();
		
		this.folders.clear();
		this.mailBoxList.clear();
	}
	
	public void disconnect(String mailAccount){
		MailAccount m=getMailAccount(mailAccount);
		if(m!=null){
			m.disconnect();
			this.folders.clear();
			this.mailBoxList.remove(m);
		}
		for(MailAccount ma:this.mailBoxList)
			this.folders.addAll(ma.getFolders());
	}
	//------------------------------------------------------------------------
	//					GETTERS AND SETTERS
	//------------------------------------------------------------------------
	
	
	public List<String> getFolders() {
		return folders;
	}

	public void setFolders(List<String> folders) {
		this.folders = folders;
	}
	
	public List<Message> getMessages(String folderP,boolean read){
		String account=folderP.split(MailAccount.SEPARATOR)[0];
		String folder=folderP.split(MailAccount.SEPARATOR)[1];
		MailAccount m= getMailAccount(account);
		if(m!=null){
			System.out.println("returning messges: ");
			return m.getMessages(folder,read);
		}
		else return null;
	}
	
	public List<String> getMailAccounts(){
		List<String> temp= new ArrayList<String>();
		for(MailAccount m:this.mailBoxList)
			temp.add(m.getUserName());
		return temp;
	}
	

		
	
	public void addMessageListener(MailMessageListener m){
		for(MailAccount ml:this.mailBoxList)
			ml.addMessageListener(m);
	}
	public void removeMessageListener(MailMessageListener m){
		for(MailAccount ml:this.mailBoxList)
			ml.removeMessageListener(m);
	}

	
	public void deleteMessage(Message m) throws MessagingException {
		m.setFlag(Flag.DELETED, true);
		refreshMessagesCounts();
		
	}
	
	public void refreshMessagesCounts(){
		for(MailAccount ma:mailBoxList)
			ma.initMessagesCount();
	}
	public List<String> getFolderList(String accountName){
		List<String> folders= new ArrayList<String>();
		for(String f:this.folders){
			if(f.contains(accountName))
				folders.add(f);
		}
		return folders;
	}
	
	public int getUnreadedMessageCount(String accountName){
		MailAccount m=getMailAccount(accountName);
		if(m!=null)
			return m.getUnreadedMessages();
		
		return 0;
	}

	@Override
	public void onIncomingMessage() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onConnect(MailAccount m) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDisconnect(MailAccount m) {
			this.mailBoxList.remove(m);
			this.folders.clear();
			for(MailAccount ma:this.mailBoxList)
				this.folders.addAll(ma.getFolders());
			
		}
		
	
	
	
}
