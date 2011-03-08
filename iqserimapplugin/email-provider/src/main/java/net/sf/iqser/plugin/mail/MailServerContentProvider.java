package net.sf.iqser.plugin.mail;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Store;

import net.sf.iqser.plugin.mail.content.MailContent;
import net.sf.iqser.plugin.mail.content.MailContentCreator;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.iqser.core.event.Event;
import com.iqser.core.exception.IQserException;
import com.iqser.core.exception.IQserRuntimeException;
import com.iqser.core.model.Content;
import com.iqser.core.plugin.AbstractContentProvider;

/**
 * executes operations on an object graph
 * @author alexandru.galos
 *
 */
public class MailServerContentProvider extends AbstractContentProvider {

	private String mailServer;

	private String userName;

	private String passWord;

	private String sslConnection;

	private String sslPort;

	private Collection<String> keyAttributesList;

	private Collection<String> folderAttributes;

	private Map<String, String> attributeMappings = new HashMap<String, String>();

	/**
	 * Default Logger for this class.
	 */
	private static Logger logger = Logger
			.getLogger(MailServerContentProvider.class);

	/**
	 * get the binary information from a content mail
	 * @param content - the content that corresponds to an email from the
	 * mail server
	 */
	
	/*identify the message from the content url
	get the input stream of the mail
	transform the input stream in bytes*/
	public byte[] getBinaryData(Content content) {

		logger.info("getBinaryData for content with url:"
				+ content.getContentUrl());
		MailServerUtils msu = new MailServerUtils();

		msu.setMailServer(mailServer);
		msu.setPassWord(passWord);
		msu.setUserName(userName);

		String contentUrl = content.getContentUrl();
		Store store = msu.attemptConnectionMailServer(sslConnection);

		byte[] byteArray = null;

		try {
			for (String folderName : folderAttributes) {
		
				Folder folder = store.getFolder(folderName);
				Message message = msu.identifyMessage(folder, contentUrl);
			
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				message.writeTo(baos);
				byteArray = baos.toByteArray();

			}
		} catch (MessagingException e) {
			throw new IQserRuntimeException(e);

		} catch (IOException e) {
			throw new IQserRuntimeException(e);
		}

		return byteArray;
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	/**
	 * corresponding action to {@link MailServerContentProvider}
	 * is "delete"
	 * @param content - the content from object graph
	 * @return a collection of actions for a content
	 */
	@Override
	public Collection getActions(Content content) {

		logger.info("get actions available for content");

		String[] actions = new String[] { "delete" };
		return Arrays.asList(actions);
	}

	@Override
	public Content getContent(String contentURL) {

		logger.info("get content info for " + contentURL);
		MailContentCreator mc = getMailContentCreator();
		Content content = mc.getContent(contentURL, keyAttributesList, this.getId())
				.getContent();
		return content;

	}

	/**
	 * creates an instance to {@link MailContentCreator}
	 * @return
	 */
	private MailContentCreator getMailContentCreator() {

		logger.info("create connection to mail server");
		MailContentCreator mc = new MailContentCreator();

		mc.setMailServer(mailServer);
		mc.setPassWord(passWord);
		mc.setSslConnection(sslConnection);
		mc.setUserName(userName);
		mc.setFolders(folderAttributes);
		mc.setSslPort(sslPort);
		mc.setAttributeMap(attributeMappings);

		return mc;
	}

	@Override
	public Content getContent(InputStream inputstream) {

		logger.info("get content for inputstream");
		MailContentCreator mc = getMailContentCreator();

		Content content = mc.getContent(inputstream, keyAttributesList,
				this.getId()).getContent();

		return content;
	}

	@Override
	public Collection getContentUrls() {
		return null;
	}

	@Override
	public void init() {
		logger.info("initialize parameters...");
		Properties params = getInitParams();
		mailServer = (String) params.get("MAIL_SERVER");
		userName = (String) params.get("USERNAME");
		passWord = (String) params.get("PASSWORD");
		sslConnection = (String) params.getProperty("SSL-CONNECTION");
		sslPort = (String) params.getProperty("SSL-PORT");

		String keyAttributes = (String) params.get("KEY-ATTRIBUTES");
		String mailLocations = (String) params.get("EMAIL-FOLDER");
		String regex = "\\s*\\]\\s*\\[\\s*|\\s*\\[\\s*|\\s*\\]\\s*";

		keyAttributesList = extractParameters(keyAttributes, regex);
		folderAttributes = extractParameters(mailLocations, regex);

		// default folder attributes
		if (folderAttributes.size() == 0)
			folderAttributes.add("INBOX");

		String mappings = (String) params.get("ATTRIBUTE.MAPPINGS");
		JSONObject json = null;
		try {
			json = new JSONObject(mappings);
		} catch (JSONException e) {
			throw new IQserRuntimeException(e);
		}

		Iterator keys = json.keys();
		while (keys.hasNext()) {
			String key = (String) keys.next();
			try {
				String value = (String) json.get(key);
				attributeMappings.put(key, value);
			} catch (JSONException e) {
				throw new IQserRuntimeException(e);
			}

		}
	}

	/**
	 * extract key attributes from the configuration file plugin.xml
	 * @param keyAttributes - the string with the specified attributes
	 * @param regex 
	 * @return
	 */
	private Collection<String> extractParameters(String keyAttributes,
			String regex) {
		
		if (keyAttributes != null) {
			String[] keyAttrs = keyAttributes.trim().split(regex);
			Collection<String> keyAttributesList = new ArrayList<String>();

			for (String key : keyAttrs) {
				if (key.trim().length() > 0)
					keyAttributesList.add(key);
			}

			return keyAttributesList;
		} else
			return new ArrayList<String>();
	}

	@Override
	public void onChangeEvent(Event event) {
		// TODO Auto-generated method stub

	}

	/**
	 * perform the operations returned by {@value getActions(content)} on a content
	 * @param action - the action to be performed
	 */
	@Override
	public void performAction(String action, Content content) {

		logger.info("performing operation " + action + " on content "
				+ content.getContentUrl());
		Collection<String> actions = getActions(content);

		MailContentCreator mailContentCreator = getMailContentCreator();

		if (actions.contains(action.toLowerCase()) &&
			action.toLowerCase().equals("delete")){
				performDeleteAction(content,  mailContentCreator);
		}

	}

	/**
	 * perform delete operation on a certain content
	 * @param content
	 * @param mailContentCreator
	 */
	private void performDeleteAction(Content content, 
			MailContentCreator mailContentCreator) {
		try {
			// delete mail message
			String contentUrl = content.getContentUrl();
			MailContent mailContent = mailContentCreator.getContent(
					contentUrl, keyAttributesList, this.getId());
			String mailURL = mailContent.getContent().getContentUrl();
			String folderName = content
					.getAttributeByName("MESSAGE_FOLDER").getValue();
			mailContentCreator.deleteMessageFromServer(mailURL, folderName,
					sslConnection);

			// remove content from graph together with attachment contents

			Collection<Content> attachmentContents = mailContent
					.getAttachmentContents();
			removeContent(contentUrl);
			for (Content contentAttachment : attachmentContents) {
				removeContent(contentAttachment.getContentUrl());
			}

		} catch (IQserException e) {
			throw new IQserRuntimeException(e);
		}
	}

	/**
	 * synchronize the object graph with the mail server
	 * if there are more mails on the server we need to add them
	 * to the object graph
	 */
	@Override
	/*
	 get the mail message-ids from the mail-server
	 if there are messages on the server which are not
	 in the object graph create content objects for them 
	 and add them to the object graph
	 */
	public void doSynchonization() {

		try {

			MailContentCreator mailContentCreator = getMailContentCreator();

			Collection<String> contentURLs = mailContentCreator
					.getMailServerURLs();

			for (String contentURL : contentURLs) {
				if (!isExistingContent(contentURL)) {
					MailContent mailContent = mailContentCreator.getContent(
							contentURL, keyAttributesList, this.getId());
					Content content = mailContent.getContent();
					content.setProvider(this.getId());
					addContent(content);
					Collection<Content> attachmentContents = mailContent
							.getAttachmentContents();
					for (Content attachmentContent : attachmentContents) {
						addContent(attachmentContent);
					}
				}
			}
		} catch (IQserException e) {
			throw new IQserRuntimeException(e);
		}

	}

	/**
	 * delete the contents from the object graphs if 
	 * there are not in the email server
	 */
	@Override
	/*
	  extract the mail message-ids from a specific server
	  and the contents from the object graph
	  traverse the objects from the content graph
	  and remove the objects that are not on the mail-server
	 */
	public void doHousekeeping() {

		try {

			MailContentCreator mailContentCreator = getMailContentCreator();

			Collection<String> contentURLs = mailContentCreator
					.getMailServerURLs();

			Collection contents = getExistingContents();

			for (Object object : contents) {
				Content content = (Content) object;
				String contentUrl = content.getContentUrl();
				if (!contentURLs.contains(contentUrl)) {
					removeContent(contentUrl);
				}
			}

		} catch (Exception e) {
			throw new IQserRuntimeException(e);
		}
	}



}
