package sk.mattho.portlets.mailPortlet.Controllers;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.activation.DataHandler;
import javax.activation.MimeType;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Flags.Flag;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;

/**
 * This bean is used for retrieving data from message object.
 * 
 * @author mattho
 * 
 */

@Named
@RequestScoped
public class MessageDetails {
	@Inject 
	private MailController mc;

	public boolean hasAttachment(Message m) {
		// if(m.getFlags().contains(Flag.))
		return false;
	}

	public boolean isSeen(Message m) {
		try {
			if (m == null)
				System.out.println("is null");

			if (m.getFlags().contains(Flag.SEEN))
				return true;
			else
				return false;
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

	public String getReciever(Message m) {
		try {
			InternetAddress i = (InternetAddress) m.getFrom()[0];
			return i.toUnicodeString();
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}

	
	public String getDate(Message m) {
		SimpleDateFormat df = new SimpleDateFormat("dd/MM/YY");
		try {
			return df.format(m.getReceivedDate());
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}
		return "";
	}

	public String getMessageBody(Message m) {
		String res = "";
		try {
			if (m.getContent() instanceof String)
				return (String) m.getContent();
			else {
				MimeMultipart multipart = (MimeMultipart) m.getContent();
				for (int i = 0; i < multipart.getCount(); i++) {
					MimeBodyPart b = (MimeBodyPart) multipart.getBodyPart(i);

					String disposition = b.getDisposition();

					// if(disposition==null && b.getContentType())
					// return (String) b.getContent();
					if (b.getContent() instanceof Multipart)
						res = proccesMultipart((MimeMultipart) b.getContent());
					else {
						if (b.getDisposition() == null)
							res = b.getContent().toString();
						else if (!disposition.equalsIgnoreCase(b.ATTACHMENT))
							res = (String) b.getContent();
					}
				}
			}

		} catch (IOException e) {
			this.mc.setError(true);
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			
			this.mc.setError(true);
		}

		return res;
	}

	private String proccesMultipart(MimeMultipart multipart) {
		String res = "";
		try {
			for (int i = 0; i < multipart.getCount(); i++) {
				MimeBodyPart b = (MimeBodyPart) multipart.getBodyPart(i);
				// if(b.isMimeType()
				String disposition = b.getDisposition();
				// if(disposition==null && b.getContentType())
				// return (String) b.getContent();
				if (b.getContent() instanceof MimeMultipart)
					res = proccesMultipart((MimeMultipart) b.getContent());
				else if (b.getDisposition() == null)
					res = b.getContent().toString();
				else if (!disposition.equals(BodyPart.ATTACHMENT))
					res = (String) b.getContent();
			}
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return res;
	}

	public List<String> getMessageAttachments(Message m) {
	//	m.getFolder().getS
		try {
			List<String> attachments = new ArrayList<String>();
			if (m.getContent() instanceof Multipart) {
				Multipart multipart = (Multipart) m.getContent();
				for (int i = 0; i < multipart.getCount(); i++) {
					BodyPart b = multipart.getBodyPart(i);
					if (b.getContent() instanceof Multipart)
						findAttachment(multipart, attachments);
					String disposition = b.getDisposition();
					if (disposition != null
							&& (disposition
									.equalsIgnoreCase(BodyPart.ATTACHMENT))) {
						DataHandler handler = b.getDataHandler();
						attachments.add(handler.getName());
					}

				}
			}
			return attachments;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}
	public boolean isStar(Message m) throws MessagingException{
		if(m.getFlags().contains(Flag.FLAGGED))
			return true;
		return false;
	}
	private void findAttachment(Multipart multipart, List<String> list)
			throws MessagingException, IOException {
		for (int i = 0; i < multipart.getCount(); i++) {
			BodyPart b = multipart.getBodyPart(i);
			if (b.getContent() instanceof Multipart)
				findAttachment((Multipart) b.getContent(), list);
			else {
				String disposition = b.getDisposition();
				if (disposition != null
						&& (disposition.equalsIgnoreCase(BodyPart.ATTACHMENT))) {
					DataHandler handler = b.getDataHandler();
					//handler.getO
					list.add(handler.getName());
				}
			}

		}
	}
	

}
