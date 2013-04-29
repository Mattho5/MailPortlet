package sk.mattho.portlets.mailPortlet;

import sk.mattho.portlets.mailPortlet.mail.MailConfigurations;

public class AccountInfo {

	private String userName;
	private String password;
	private MailConfigurations protocol;
	private Integer smtpPort;
	private Integer imapPort;
	private String smtpServer;
	private String imapServer;
	private boolean secured;
	private boolean ssl;
	
	
	public AccountInfo(String input){
		String[] temp =input.split("\\|");
		this.userName=temp[0];
		this.password=temp[1];
		this.protocol=MailConfigurations.valueOf(temp[2]);
		if(this.protocol==MailConfigurations.OTHER){
			
		
		this.smtpPort=new Integer(temp[3]);
		this.imapPort= new Integer(temp[4]);
		this.smtpServer=temp[5];
		this.imapServer=temp[6];
		this.secured=Boolean.parseBoolean(temp[7]);
		this.ssl=Boolean.parseBoolean(temp[8]);
		}
		else 
			{this.smtpPort=this.protocol.getSmtpPort();
			this.imapPort=this.protocol.getImapPort();
			this.smtpServer=this.protocol.getSmtpUrl();
			this.imapServer=this.protocol.getImapUrl();
			this.secured=this.protocol.isSecured();
			this.ssl=this.protocol.isSsl();
			}
		
	}
	
	public  AccountInfo(String userName, String password, MailConfigurations c){
		init();
		this.userName=userName;
		this.password=password;
		this.protocol=c;
	}
	public void init(){
		this.userName="";
		this.password="";
	//	this.protocol=;
		
		
		this.smtpPort=0;
		this.imapPort=0;
		this.smtpServer="";
		this.imapServer="";
		this.secured=false;
		this.ssl=false;
	}
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return userName+"|"+password+"|"+protocol.name()+"|"+smtpPort+"|"+imapPort+"|"+smtpServer+"|"+imapServer+"|"+secured+"|"+ssl;
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

	public MailConfigurations getProtocol() {
		return protocol;
	}

	public void setProtocol(MailConfigurations protocol) {
		this.protocol = protocol;
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

	public void setImapPort(Integer imapPort) {
		this.imapPort = imapPort;
	}

	public String getSmtpServer() {
		return smtpServer;
	}

	public void setSmtpServer(String smtpServer) {
		this.smtpServer = smtpServer;
	}

	public String getImapServer() {
		return imapServer;
	}

	public void setImapServer(String imapServer) {
		this.imapServer = imapServer;
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
	
	
}
