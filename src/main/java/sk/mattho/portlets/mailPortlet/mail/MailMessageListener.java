package sk.mattho.portlets.mailPortlet.mail;

public interface MailMessageListener {
	public void onIncomingMessage();
	public void onConnect(MailAccount m);
	public void onDisconnect(MailAccount m);

	

}
