package net.sf.iqser.plugin.mail.content;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Store;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import net.sf.iqser.plugin.file.parser.FileParser;
import net.sf.iqser.plugin.file.parser.FileParserException;
import net.sf.iqser.plugin.file.parser.FileParserFactory;
import net.sf.iqser.plugin.mail.MailServerUtils;

import org.apache.log4j.Logger;

import com.iqser.core.exception.IQserRuntimeException;
import com.iqser.core.model.Attribute;
import com.iqser.core.model.Content;

/**
 * creates mail content from mails
 * @author alexandru.galos
 *
 */
public class MailContentCreator {

	private String mailServer;
	private String userName;
	private String passWord;
	private String sslConnection;
	private Collection<String> folders;
	private MailServerUtils msu = new MailServerUtils();;
	private String sslPort;
	private Map<String, String> attributeMap;
	/**
	 * Default Logger for this class.
	 */
	private static Logger logger = Logger
			.getLogger(MailContentCreator.class);
	

	/**
	 * create content from email (email-content and attachment content)
	 * @param contentURL - the message-id
	 * @param keyAttributesList - the key attributes from plugin.xml
	 * @param providerID - the id of the mail provider
	 * @return a mail content that contains the mail and its attachments
	 */
	public MailContent getContent(String contentURL,
			Collection<String> keyAttributesList, String providerID) {

		msu.setMailServer(mailServer);
		msu.setUserName(userName);
		msu.setPassWord(passWord);
		msu.setSslPort(sslPort);

		MailContent mailContent = null;

		Store store = msu.attemptConnectionMailServer(sslConnection);

		Message messageBuf = null;
		String folderN = null;
		Content content = null;
		try {
			//search each folder for a certain mail message
			for (String folderName : folders) {
				Folder folder = store.getFolder(folderName);
				Message message = msu.identifyMessage(folder, contentURL);
				if (message != null) {
					messageBuf = message;
					folderN = folder.getName();
					break;
				}
				folder.close(false);
			}

			if (messageBuf == null) {
				throw new IQserRuntimeException(
						"Message with specified id does not exist "
								+ contentURL);
			} else {
				mailContent = createMailContent(messageBuf);
				content = mailContent.getContent();

				if (content.getAttributeByName("MESSAGE_FOLDER") == null) {
					Attribute attributeM = new Attribute();
					attributeM.setName("MESSAGE_FOLDER");
					attributeM.setValue(folderN);
					attributeM.setKey(false);
					content.addAttribute(attributeM);
				}

			
			
			}

		} catch (Exception e) {
			throw new IQserRuntimeException(e);
		} finally {
			try {
				store.close();
			} catch (MessagingException e) {
				throw new IQserRuntimeException(e);
			}
		}
		assertProviderIDToContent(providerID, mailContent);
		updateKeyAttributes(content, keyAttributesList);
		return mailContent;
	}

	/**
	 * create content from email (email-content and attachment content) from
	 * input stream
	 * @param inputStream - the input stream of the mail that contains
	 * also the header and the attachments
	 * @param keyAttributesList - the key attributes from the plugin.xml
	 * @param providerID - the mail provider id
	 * @return - a mail content that has also attachment contents
	 */
	public MailContent getContent(InputStream inputStream,
			Collection<String> keyAttributesList,String providerID) {

		Message message = null;
		try {

			message = new MimeMessage(null, inputStream);
		} catch (MessagingException e) {
			throw new IQserRuntimeException(e);
		}

		MailContent mailContent = createMailContent(message);
		assertProviderIDToContent(providerID, mailContent);
		updateKeyAttributes(mailContent.getContent(), keyAttributesList);
		
		
		return mailContent;
	}

	/**
	 * assert provider to mail content and attachment content
	 * updates attributes to the content
	 * @param providerID
	 * @param mailContent
	 */
	private void assertProviderIDToContent(String providerID,
			MailContent mailContent) {
		Content content1 = mailContent.getContent();
		content1.setProvider(providerID);
		content1.setType("EMAIL");
	
		updateAttributes(content1, attributeMap);
		Collection<Content> attachmentContents = mailContent
				.getAttachmentContents();
		for (Content content : attachmentContents) {
			content.setProvider(providerID);
		}
	}

	public String getMailServer() {
		return mailServer;
	}

	public void setMailServer(String mailServer) {
		this.mailServer = mailServer;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassWord() {
		return passWord;
	}

	public void setPassWord(String passWord) {
		this.passWord = passWord;
	}

	public String getSslConnection() {
		return sslConnection;
	}

	public void setSslConnection(String sslConnection) {
		this.sslConnection = sslConnection;
	}

	/**
	 * create a mail content from a message 
	 * @param message - the mail message
	 * @return a content that contains also the attachments
	 */
	public MailContent createMailContent(Message message) {

		assert message != null;

		MailContent mc = new MailContent();
		Content content;

		try {

			content = new Content();
			String contentURL = message.getHeader("MESSAGE-ID")[0];
			content.setContentUrl(contentURL );
			String subject = message.getSubject();
			if (message.getFolder() != null) {
				Attribute attribute = createAttribute("MESSAGE_FOLDER", message
						.getFolder().getName(), false);
				content.addAttribute(attribute);
			}

			Attribute attributeM = createAttribute("SUBJECT", subject, true);
			content.addAttribute(attributeM);

			Address[] senders = message.getFrom();
			if (senders != null)
				extractHeaderAddresses(content, senders, "SENDER");

			Address[] recipients = message.getAllRecipients();
			if (recipients != null)
				extractHeaderAddresses(content, recipients, "RECIPIENT");

			Address[] bccRecipients = message.getRecipients(RecipientType.BCC);
			if (bccRecipients != null)
				extractHeaderAddresses(content, bccRecipients, "BCC_RECIPIENT");

			Address[] ccRecipients = message.getRecipients(RecipientType.CC);
			if (ccRecipients != null)
				extractHeaderAddresses(content, ccRecipients, "CC_RECIPIENT");

			Date receivedDate = message.getReceivedDate();

			if (receivedDate != null) {
				attributeM = createAttribute("RECEIVED_DATE",
						receivedDate.toString(), true);
				content.addAttribute(attributeM);
			}
			Object messageContent = message.getContent();

			assert messageContent != null;

			if (messageContent instanceof Part) {
				extractPartMessage(content, messageContent, 0);
			} else if (messageContent instanceof Multipart) {
				int count = ((Multipart) messageContent).getCount();
				int index = 0;
				for (int i = 0; i < count; i++) {
					BodyPart bodyPart = ((Multipart) messageContent)
							.getBodyPart(i);
					Content attachmentContent = extractPartMessage(content,
							bodyPart, index);
					if (attachmentContent != null) {
						mc.addAttachmentContent(attachmentContent);
						index++;
					}
				}

			} else {
				if (messageContent != null) {
					attributeM = createAttribute("MESSAGE_CONTENT_0",
							messageContent.toString(), false);
					content.setFulltext(messageContent.toString());
					content.addAttribute(attributeM);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new IQserRuntimeException(e);
		}

		mc.setContent(content);
		return mc;
	}

	/**
	 * used for extracting multi part messages
	 * @param content - the content that is created in the object graph
	 * @param messageContent - the message content of the mail
	 * @param index - the index of the part in a multi-part message
	 * @return - return the mail content without its attachments
	 * @throws MessagingException
	 * @throws IOException
	 * @throws FileParserException
	 */
	private Content extractPartMessage(Content content, Object messageContent,
			int index) throws MessagingException, IOException, FileParserException {
		Attribute attributeM;
		String fileName = ((Part) messageContent).getFileName();

		if (fileName == null) {

			attributeM = createAttribute("MESSAGE_CONTENT_" + index,
					((Part) messageContent).getContent().toString(), false);
			content.setFulltext(((Part) messageContent).getContent().toString());

			// does not have an attachment content
			return null;
		} else {

			InputStream inputStream = ((Part) messageContent).getInputStream();

			Content attachmentContent = createAttachmentContent(inputStream);//fscp.getContent(inputStream);
			if (attachmentContent != null) {
				attachmentContent.setContentUrl(content.getContentUrl()
						+ fileName);

				Attribute attribute = new Attribute();
				attribute.setKey(false);
				attribute.setName("ATTACHED_TO");
				attribute.setValue(content.getContentUrl());
				attachmentContent.addAttribute(attribute);

				attributeM = createAttribute("MESSAGE_ATTACHMENTS_NAME_"
						+ index, fileName, true);
				content.addAttribute(attributeM);
			}
			return attachmentContent;

		}
	}

	/**
	 * create an attachment content from an input stream
	 * @param inputStream - the input stream of the message body
	 * @return an attachment content
	 * @throws FileParserException
	 */
	private Content createAttachmentContent(InputStream inputStream) throws FileParserException {
		
		
		FileParserFactory parserFactory = FileParserFactory.getInstance();
		FileParser parser = parserFactory.getFileParser(inputStream);

		
			if (parser != null){ 
				Content content = parser.getContent(null,inputStream);
				return content;
			}
		
			return null;
	}

	
	
	/**
	 * create address attributes for content
	 * @param content
	 * @param addresses
	 * @param type
	 */
	private void extractHeaderAddresses(Content content, Address[] addresses,
			String type) {

		if (addresses != null) {
			
			int index = 0;
			Attribute attributeM;
			for (Address address : addresses) {
				if (address instanceof InternetAddress) {
					String addressValue = ((InternetAddress) address)
							.getAddress();
					attributeM = createAttribute(type + "_MAIL_"+index, addressValue,
							false);
					content.addAttribute(attributeM);

					String name = getName(addressValue);
					attributeM = createAttribute(type + "_NAME_"+index, name, false);
					content.addAttribute(attributeM);
					index++;
				}
			}
		}
	}

	/**
	 * get the name of the sender from the email address
	 * @param addressValue
	 * @return
	 */
	private String getName(String addressValue) {

		int index = addressValue.indexOf("@");

		if (index == -1) {
			throw new IQserRuntimeException("Email address not correct");
		}
		//return the first part of the email address
		return addressValue.substring(0, index);
	}

	/**
	 * creates an attribute for a content
	 * @param name 
	 * @param value
	 * @param key
	 * @return the attribute
	 */
	private Attribute createAttribute(String name, String value, boolean key) {
		Attribute attribute = new Attribute();
		attribute.setKey(key);
		attribute.setName(name);
		attribute.setValue(value);
		return attribute;
	}
	
	/**
	 * get the message ids from the mail server 
	 * from each folder that is specified in the configuration file
	 * @return the collection of message-ids (collection of string)
	 */
	public Collection getMailServerURLs() {

		logger.info("extracting messages urls");
		MailServerUtils msu = new MailServerUtils();

		msu.setMailServer(mailServer);
		msu.setPassWord(passWord);
		msu.setUserName(userName);
		msu.setSslPort(sslPort);

		Collection<String> contentURLs = new ArrayList<String>();
		Store store = msu.attemptConnectionMailServer(sslConnection);

		try {
			//for each message from each folder get the message-id
			for (String folderName : folders) {
				logger.info("extracting message urls from folder:" + folderName);
				Folder folder = store.getFolder(folderName);
				folder.open(Folder.READ_ONLY);
				Message[] messages = folder.getMessages();
				for (Message message : messages) {
					String[] header = message.getHeader("MESSAGE-ID");
					contentURLs.add(header[0]);
				}

				folder.close(true);
				store.close();
			}
		} catch (MessagingException e) {
			throw new IQserRuntimeException(e);
		} finally {
			try {
				store.close();
			} catch (MessagingException e) {
				throw new IQserRuntimeException(e);
			}
		}

		return contentURLs;
	}
	
	/**
	 * deletes a mail from the server
	 * @param mailURL - the message-id
	 * @param folderName - the folder from the server where the message is found
	 * @param sslConnection - (true or false according to if the connection is secured
	 * or not)
	 */
	public void deleteMessageFromServer(String mailURL, String folderName,
			String sslConnection) {

		
		//connect to the server
		Store store = msu.attemptConnectionMailServer(sslConnection);
		try {
			Folder folder = store.getFolder(folderName);
			folder.open(Folder.READ_WRITE);

			Message message = msu.identifyMessage(folder, mailURL);

			message.setFlag(Flag.DELETED, true);
			folder.close(true);
			store.close();
			
		} catch (MessagingException e) {
			throw new IQserRuntimeException(e);
		}

	}

	public void setFolders(Collection<String> folders) {
		this.folders = folders;
	}

	public Collection<String> getFolders() {
		return folders;
	}

	public void setSslPort(String sslPort) {
		this.sslPort = sslPort;
	}

	public String getSslPort() {
		return sslPort;
	}

	public void setAttributeMap(Map<String, String> attributeMap) {
		this.attributeMap = attributeMap;
	}

	public Map<String, String> getAttributeMap() {
		return attributeMap;
	}
	
	/**
	 * updates the key attributes of a content graph
	 * the attributes that are not in {@link keyAttributeList} are
	 * set as non-key attributes 
	 * @param content - the mail content 
	 * @param keyAttributesList - the key attributes for the content
	 */
	public void updateKeyAttributes(Content content, Collection keyAttributesList) {

		Collection attributes = content.getAttributes();

		//check if attributes must be changed to key attribute
		for (Object attribute : attributes) {

			String name = ((Attribute)attribute).getName();

			for (Object keyAttrObject : keyAttributesList) {
				String keyAttr = (String)keyAttrObject;
				boolean contains = name.contains(keyAttr);
				if (contains) {
					((Attribute)attribute).setKey(true);
				} else
					((Attribute)attribute).setKey(false);
			}

		}

	}
	
	/**
	 * updates the attributes according to the configuration plugin.xml
	 * @param content a mail or attachment content
	 * @param attributeMappings the new attributes
	 */
	public  void updateAttributes(Content content, Map attributeMappings) {

		Collection attributes = content.getAttributes();
		
		//each attribute is checked in order to see if it must be changed
		for (Object attribute : attributes) {
			String name = ((Attribute)attribute).getName();
			if (attributeMappings.containsKey(name)) {
				name = (String) attributeMappings.get(name);
				((Attribute) attribute).setName(name);
			}

		}

	}
}
