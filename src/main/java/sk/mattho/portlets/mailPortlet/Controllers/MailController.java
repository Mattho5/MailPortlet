package sk.mattho.portlets.mailPortlet.Controllers;

import java.io.Serializable;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.mail.MessagingException;

import sk.mattho.portlets.mailPortlet.mail.MailConfigurations;
import sk.mattho.portlets.mailPortlet.mail.MailManager;

@Named
@SessionScoped
public class MailController implements Serializable {
	
/**
	 * 
	 */
	private static final long serialVersionUID = -543261858181040481L;

@Inject MailManager manager;

private boolean connected;
private String imapPopUrl;
private String smtpUrl;
private int imapPort;
private int smtpPort;
private String userName;
private String password;
private MailConfigurations selectedConfiguration;

private String messageText;
private String subject;
private String recipients;

private String selectedAccounts;





public String getSelectedAccounts() {
	return selectedAccounts;
}


public void setSelectedAccounts(String selectedAccounts) {
	this.selectedAccounts = selectedAccounts;
}


private int view;
// view 0 - list of messages
//view 1 concrete message
//view 2 new message







@PostConstruct
public void init(){
	this.connected=false;
	this.selectedConfiguration=MailConfigurations.GMAIL;
	this.view=0;
	
}


public void  connect(){
	if(this.selectedConfiguration!=MailConfigurations.OTHER){
	try {
		this.connected= this.manager.addMailAccount(userName, password, selectedConfiguration);
		this.selectedAccounts=this.manager.getMails().get(0);
	} catch (MessagingException e) {
		// TODO Auto-generated catch block
		FacesContext.getCurrentInstance().addMessage(null,
				new FacesMessage(e.getMessage()));
		e.printStackTrace();
	}
	
	//this.connected=
	}
	
	
}
public void send(){
	//todo more recipients
	this.manager.send(messageText, subject, recipients, selectedAccounts);
}
public boolean isConnected() {
	return connected;
}

public void logout(){
	this.manager.disconnect();
	this.connected=false;
}
//------------------------------------------------------------
// 				GETTERS AND SETTERS
//-----------------------------------------------------------



public List<String> folders(){
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

public void setSelectedConfiguration(MailConfigurations selectedConfiguration) {
	this.selectedConfiguration = selectedConfiguration;
}

public MailConfigurations[] getConfigurations(){
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

public List<String> getAccounts(){
	return this.manager.getMails();
}
}
