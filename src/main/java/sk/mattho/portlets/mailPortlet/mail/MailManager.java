package sk.mattho.portlets.mailPortlet.mail;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.mail.Message;
import javax.mail.MessagingException;

public class MailManager implements Serializable {

	private List<GenericMail> mailBoxList;
	private List<String> folders;
	private Map<String,String> adressbook;
	
	@PostConstruct
	private void init(){
		this.mailBoxList= new ArrayList<GenericMail>();
		this.folders= new ArrayList<String>();
	}
	
	@PreDestroy
	private void preDestroy(){
		for(GenericMail m:this.mailBoxList){
			m.disconnect();
		}
		this.folders.clear();
		this.mailBoxList.clear();
		
	}
	public boolean addMailAccount(String userName,String password,MailConfigurations m) throws MessagingException{
		
		if(m!=MailConfigurations.OTHER){
			 return addMailAccount(userName, password,m.getSmtpUrl(),m.getSmtpPort(), m.getImapPopUrl(), m.getImapPort(),m.isSsl());
		}
		return false;
		
	}
	
	public boolean addMailAccount(String userName,String password,String smtp,int smtpport, String popimap,int popimapport,boolean ssl) throws MessagingException{
		
		if(getMailAccount(userName)!=null)
			return false;
		GenericMail mailbox= new GenericMail();
	
		mailbox.setImapPopServerUrl(popimap);
		mailbox.setSmtpServerUrl(smtp);
		mailbox.setSmtpPort(smtpport);
		mailbox.setPomImapPort(popimapport);
		mailbox.setUserName(userName);
		mailbox.setPassword(password);
		mailbox.setSSL(ssl);
		
	
		if(mailbox.connect()){
			this.mailBoxList.add(mailbox);
			this.folders.addAll(mailbox.getFolders());
			return true;
		}
		else return false;
		
	}
	
	public void send(String message,String subject, String reciptiens, String account){
	System.out.println("SENDING "+this.mailBoxList.size());

	GenericMail m= this.getMailAccount(account);
				m.sendMessage(reciptiens, message, subject);
						
	}

	public GenericMail getMailAccount(String acc){
	
		for(GenericMail m: this.mailBoxList){
			if(m.getUserName().compareTo(acc)==0)
				System.out.print("ffind!");
				return m;
		}
		return null;
	}
	
	public void disconnect(){
		for(GenericMail m:this.mailBoxList)
			m.disconnect();
		
		this.folders.clear();
		this.mailBoxList.clear();
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
	
	public List<Message> getMessages(String folderP){
		String account=folderP.split(GenericMail.SEPARATOR)[0];
		String folder=folderP.split(GenericMail.SEPARATOR)[1];
		GenericMail m= getMailAccount(account);
		if(m!=null){
			System.out.println("returning messges: ");
			return m.getMessages(folder);
		}
		else return null;
	}
	
	public List<String> getMailAccounts(){
		List<String> temp= new ArrayList<String>();
		for(GenericMail m:this.mailBoxList)
			temp.add(m.getUserName());
		return temp;
	}
	
	
	
}
