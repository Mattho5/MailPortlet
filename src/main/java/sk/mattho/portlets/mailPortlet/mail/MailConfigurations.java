package sk.mattho.portlets.mailPortlet.mail;

public enum MailConfigurations {
	OTHER("","",993,465,false,false),
	GMAIL("imap.gmail.com","smtp.gmail.com",993,465,true,true),
	YAHOO("imap.mail.yahoo.com","smtp.mail.yahoo.com",993,465,true,true);
	
	private String imapUrl;
	private String smtpUrl;
	private Integer imapPort;
	private Integer smtpPort;
	public static int DEFAULT_IMAP=993;
	public static int DEFAULT_SMTP=465;
	private boolean secured;
	public boolean isSecured() {
		return secured;
	}

	public void setSecured(boolean secured) {
		this.secured = secured;
	}

	private boolean ssl; //true - ssl ; false - tls
	
	
	private MailConfigurations(String imap, String smtp, Integer imapPort,Integer smtpPort, boolean secured,boolean ssl){
		this.imapUrl=imap;
		this.smtpUrl=smtp;
		this.imapPort=imapPort;
		this.smtpPort= smtpPort;
		this.secured=secured;
		this.ssl=ssl;
	}

	public String getImapUrl() {
		return imapUrl;
	}

	public void setImapUrl(String imapPopUrl) {
		this.imapUrl = imapPopUrl;
	}

	public String getSmtpUrl() {
		return smtpUrl;
	}

	public void setSmtpUrl(String smtpUrl) {
		this.smtpUrl = smtpUrl;
	}

	

	
	
	public static int getDEFAULT_IMAP() {
		return DEFAULT_IMAP;
	}

	public static void setDEFAULT_IMAP(int dEFAULT_IMAP) {
		DEFAULT_IMAP = dEFAULT_IMAP;
	}

	public static int getDEFAULT_SMTP() {
		return DEFAULT_SMTP;
	}

	public static void setDEFAULT_SMTP(int dEFAULT_SMTP) {
		DEFAULT_SMTP = dEFAULT_SMTP;
	}

	public boolean isSsl() {
		return ssl;
	}

	public void setSsl(boolean ssl) {
		this.ssl = ssl;
	}

	public Integer getImapPort() {
		return imapPort;
	}

	public void setImapPort(Integer imapPort) {
		this.imapPort = imapPort;
	}

	public Integer getSmtpPort() {
		return smtpPort;
	}

	public void setSmtpPort(Integer smtpPort) {
		this.smtpPort = smtpPort;
	}


}
