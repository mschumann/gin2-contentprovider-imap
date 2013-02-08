package net.sf.iqser.plugin.mail;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
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

import com.iqser.core.exception.IQserException;
import com.iqser.core.exception.IQserRuntimeException;
import com.iqser.core.model.Content;
import com.iqser.core.model.Parameter;
import com.iqser.core.plugin.provider.AbstractContentProvider;

/**
 * executes operations on an object graph.
 * 
 * @author alexandru.galos
 * 
 */
public class MailServerContentProvider extends AbstractContentProvider {

	/**
	 * the name of the server.
	 */
	private String mailServer;

	/**
	 * user name.
	 */
	private String userName;

	/**
	 * password.
	 */
	private String password;

	/**
	 * connection flag true or false.
	 */
	private String sslConnection;

	/**
	 * the ssl port.
	 */
	private String sslPort;

	/**
	 * the port of the mail server.
	 */
	private String port;

	/**
	 * flag if the synchronization is cached or not (true or false).
	 */
	private String cache;

	/**
	 * the key attributes list.
	 */
	private Collection<String> keyAttributesList;

	/**
	 * the mail server folders.
	 */
	private Collection<String> folderAttributes;

	/**
	 * the new attributes for the imap content provider.
	 */
	private Map<String, String> attributeMappings = new HashMap<String, String>();

	/**
	 * the last time the synchronization was performed.
	 */
	private static long time = new Date(0).getTime();

	/**
	 * Default Logger for this class.
	 */
	private static Logger logger = Logger
			.getLogger(MailServerContentProvider.class);

	/**
	 * get the binary information from a content mail.
	 * 
	 * @param content
	 *            - the content that corresponds to an email from the mail
	 *            server
	 * @return the binary information of the content
	 */
	public byte[] getBinaryData(Content content) {
		/*
		 * identify the message from the content url get the input stream of the
		 * mail transform the input stream in bytes
		 */
		logger.info("getBinaryData for content with url:"
				+ content.getContentUrl());
		MailServerUtils msu = new MailServerUtils();

		msu.setMailServer(mailServer);
		msu.setPassWord(password);
		msu.setUserName(userName);

		String contentUrl = content.getContentUrl();
		Store store = msu.attemptConnectionMailServer(sslConnection);

		byte[] byteArray = null;

		if (folderAttributes.size() == 0)
			try {
				folderAttributes.add(store.getDefaultFolder().getName());
			} catch (MessagingException e1) {
				throw new IQserRuntimeException(e1);
			}
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

	/**
	 * nothing to do.
	 */
	@Override
	public void destroy() {
	}

	/**
	 * corresponding action to {@link MailServerContentProvider} is "delete".
	 * 
	 * @param content
	 *            - the content from object graph
	 * @return a collection of actions for a content
	 */
	@Override
	public Collection<String> getActions(Content content) {

		logger.info("get actions available for content");

		String[] actions = new String[] { "delete" };
		return Arrays.asList(actions);
	}

	private boolean isEmailURL(String contentURL) {
		if (contentURL.endsWith(">"))
			return true;
		else
			return false;
	}

	/**
	 * creates an instance to {@link MailContentCreator}.
	 * 
	 * @return an object representing that can perform operations on the object
	 *         graph and on the mail server
	 */
	private MailContentCreator getMailContentCreator() {

		logger.info("create connection to mail server");
		MailContentCreator mc = new MailContentCreator();

		mc.setMailServer(mailServer);
		mc.setPassWord(password);
		mc.setSslConnection(sslConnection);
		mc.setUserName(userName);
		mc.setFolders(folderAttributes);
		mc.setSslPort(sslPort);
		mc.setAttributeMap(attributeMappings);
		if (port != null)
			mc.setPort(Integer.parseInt(port));
		mc.setCache(cache);
		return mc;
	}

	/**
	 * initialize the content parameters.
	 */
	@Override
	public void init() {
		logger.info("initialize parameters...");
		Properties params = getInitParams();
		mailServer = params.getProperty("mailServer");
		userName = params.getProperty("userName");
		password = params.getProperty("password");
		sslConnection = params.getProperty("sslConnection");
		sslPort = params.getProperty("sslPort");
		port = params.getProperty("emailPort");
		setCache(params.getProperty("emailCache", "true"));

		String keyAttributes = params.getProperty("keyAttributes");
		String mailLocations = params.getProperty("emailFolder");
		String regex = "\\s*\\]\\s*\\[\\s*|\\s*\\[\\s*|\\s*\\]\\s*";

		keyAttributesList = extractParameters(keyAttributes, regex);
		folderAttributes = extractParameters(mailLocations, regex);

		// default folder attributes
		if (folderAttributes.size() == 0) {
			folderAttributes.add("INBOX");
		}

		String mappings = (String) params
				.getProperty("attributeMappings", "{}");
		JSONObject json = null;
		try {
			json = new JSONObject(mappings);
		} catch (JSONException e) {
			throw new IQserRuntimeException(e);
		}

		@SuppressWarnings("rawtypes")
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
	 * extract key attributes from the configuration file plugin.xml.
	 * 
	 * @param keyAttributes
	 *            - the string with the specified attributes
	 * @param regex
	 *            the regular expression that is used for extracting the
	 *            parameters
	 * @return a collection of string representing the extracted parameters
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

	/**
	 * perform delete operation on a certain content.
	 * 
	 * @param content
	 *            the content on which the delete operation is performed
	 * @param mailContentCreator
	 *            the object that performs the operations on the object graph
	 *            and also on the mail server
	 */
	private void performDeleteAction(Content content,
			MailContentCreator mailContentCreator) {
		try {
			// delete mail message
			String contentUrl = content.getContentUrl();
			MailContent mailContent = mailContentCreator.getContent(contentUrl,
					keyAttributesList, getName());
			String mailURL = mailContent.getContent().getContentUrl();
			String folderName = content.getAttributeByName("MESSAGE_FOLDER")
					.getValue();
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
	 * delete the contents from the object graphs if there are not in the email
	 * server.
	 */
	@Override
	/*
	 * extract the mail message-ids from a specific server and the contents from
	 * the object graph traverse the objects from the content graph and remove
	 * the objects that are not on the mail-server
	 */
	public void doHousekeeping() {

		try {

			MailContentCreator mailContentCreator = getMailContentCreator();

			Collection<String> contentURLs = mailContentCreator
					.getMailServerURLs(0);

			Collection<Content> contents = getExistingContents();

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

	/**
	 * set the cache flag for the synchronization.
	 * 
	 * @param cache
	 *            the flag that specified if the synchronization is flaged or
	 *            not
	 */
	public void setCache(String cache) {
		this.cache = cache;
	}

	/**
	 * gets the cache flag.
	 * 
	 * @return the cache
	 */
	public String getCache() {
		return cache;
	}

	public Content createContent(String contentUrl) {
		logger.info("get content info for " + contentUrl);
		MailContentCreator mcc = getMailContentCreator();

		Content content = null;
		MailContent mc = null;
		if (isEmailURL(contentUrl)) {

			mc = mcc.getContent(contentUrl, keyAttributesList, getName());
			content = mc.getContent();
		} else {
			int index = contentUrl.indexOf(">") + 1;
			String mailContentURL = contentUrl.substring(0, index);
			mc = mcc.getContent(mailContentURL, keyAttributesList, getName());
			Collection<Content> attachmentContents = mc.getAttachmentContents();
			for (Content contentAtt : attachmentContents) {
				if (contentAtt.getContentUrl().equalsIgnoreCase(contentUrl)) {
					content = contentAtt;
					break;
				}
			}
		}

		return content;

	}

	/**
	 * create a content from an inputstream.
	 * 
	 * @param inStream
	 *            the input stream from which the content object is created
	 * @return the content that is created from the input stream.
	 */
	public Content createContent(InputStream inStream) {
		logger.info("get content for inputstream");
		MailContentCreator mc = getMailContentCreator();

		Content content = mc.getContent(inStream, keyAttributesList, getName())
				.getContent();

		return content;
	}

	/**
	 * synchronize the object graph with the mail server if there are more mails
	 * on the server we need to add them to the object graph.
	 */
	@Override
	/*
	 * get the mail message-ids from the mail-server if there are messages on
	 * the server which are not in the object graph create content objects for
	 * them and add them to the object graph
	 */
	public void doSynchronization() {
		long nexttime = System.currentTimeMillis();
		try {

			MailContentCreator mailContentCreator = getMailContentCreator();

			Collection<String> contentURLs = mailContentCreator
					.getMailServerURLs(time);

			for (String contentURL : contentURLs) {
				if (!isExistingContent(contentURL)) {
					MailContent mailContent = mailContentCreator.getContent(
							contentURL, keyAttributesList, getName());
					Content content = mailContent.getContent();
					content.setProvider(getName());
					addContent(content);
					Collection<Content> attachmentContents = mailContent
							.getAttachmentContents();
					for (Content attachmentContent : attachmentContents) {
						addContent(attachmentContent);
					}
				}
			}
			time = nexttime;
		} catch (IQserException e) {
			throw new IQserRuntimeException(e);
		}
	}

	@Override
	public void performAction(String action, Collection<Parameter> parameters,
			Content content) {
		logger.info("performing operation " + action + " on content "
				+ content.getContentUrl());
		Collection<String> actions = getActions(content);

		MailContentCreator mailContentCreator = getMailContentCreator();

		if (actions.contains(action.toLowerCase())
				&& action.toLowerCase().equals("delete")) {
			performDeleteAction(content, mailContentCreator);
		}
	}
}
