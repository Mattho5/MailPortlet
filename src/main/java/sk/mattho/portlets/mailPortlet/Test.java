package sk.mattho.portlets.mailPortlet;


import javax.mail.MessagingException;

import sk.mattho.portlets.mailPortlet.mail.GenericMail;
import sk.mattho.portlets.mailPortlet.mail.MailConfigurations;


public class Test {
		 
		 public static void main(String args[]) {
			 MailConfigurations m= MailConfigurations.YAHOO;
			 GenericMail mail= new GenericMail();
			 mail.setImapPopServerUrl(m.getImapPopUrl());
			 mail.setSmtpServerUrl(m.getSmtpUrl());
			
			 mail.setSSL(m.isSsl());
			 mail.setSmtpPort(m.getSmtpPort());
			 mail.setPomImapPort(m.getImapPort());
			 try {
				mail.connect();
			} catch (MessagingException e) {
				// TODO Auto-generated catch block
				System.out.println("sprava"+ e.getMessage());
			}
			 mail.getFolders();
			 mail.sendMessage("mattho5@yahoo.com", "<h2>TAK TOTO JE TESTOVACIA SPRAVA</h2>" +
			 		"<p>what the <strong>fuck</strong> tertre</p>","test");
			 mail.disconnect();
			 
		/* Properties props = System.getProperties();
		 
		// Uncomment if you are using proxy server to access Internet
		/*   props.setProperty("http.proxyHost", "192.168.0.1");
		props.setProperty("http.proxyPort", "8080"); 
		 
		props.setProperty("mail.store.protocol", "imaps");
		 try {
		 Session session = Session.getDefaultInstance(props, null);
		 session.setDebug(true);
		 Store store = session.getStore("imaps");
	//	 store.g
		 store.connect("imap.gmail.com", "5mattho@gmail.com", "123majuska123***");
		 System.out.println(store);
		 
		 Folder inbox = store.getFolder("Inbox");
		 inbox.open(Folder.READ_ONLY);
		 FlagTerm ft = new FlagTerm(new Flags(Flags.Flag.SEEN), false);
		 Message messages[] = inbox.search(ft);
		// inbox.s
		 int pocitadlo=0;
		 for (Message message : messages) {
			 if(pocitadlo<50)
				 pocitadlo++;
			 else break;
		 // message.setFlag(Flags.Flag.ANSWERED, true);
		 // message.setFlag(Flags.Flag.SEEN, true);
		 String subject = message.getSubject();
		 String content = message.getContentType();
		 MimeMultipart part = (MimeMultipart) message.getContent();
		 
		 BodyPart bodyPart = part.getBodyPart(0);
		 part.getContentType();
		 part.getCount();
		 part.getPreamble();
		 System.out.println("Message subject: "+subject);
		 try {
		// printParts(message);
		 } catch (Exception e) {
		 // TODO Auto-generated catch block
		 e.printStackTrace();
		 }
		 
		 Flags flags = message.getFlags();
		 Address[] form = message.getFrom();
		 
		 }
		 } catch (MessagingException e) {
		 e.printStackTrace();
		 System.exit(2);
		 } catch (IOException e) {
		 // TODO Auto-generated catch block
		 e.printStackTrace();
		 }
		 
		 }
		 
		 public static void printParts(Part p) throws Exception {
		 Object o = p.getContent();
		 if (o instanceof String) {
		 System.out.println("This is a String");
		 System.out.println((String) o);
		 } else if (o instanceof Multipart) {
		 System.out.println("This is a Multipart");
		 Multipart mp = (Multipart) o;
		 int count = mp.getCount();
		 for (int i = 0; i < count; i++) {
		 printParts(mp.getBodyPart(i));
		 }
		 } else if (o instanceof InputStream) {
		 System.out.println("This is just an input stream");
		 InputStream is = (InputStream) o;
		 int c;
//		            while ((c = is.read()) != -1)
//		                System.out.write(c);
		 }*/
		 }
		 
		
}
