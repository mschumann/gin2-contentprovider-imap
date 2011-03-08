package net.sf.iqser.plugin.mail;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import junit.framework.TestCase;
import net.sf.iqser.plugin.mail.content.MailContentCreator;
import net.sf.iqser.plugin.mail.test.MockAnalyzerTaskStarter;
import net.sf.iqser.plugin.mail.test.MockRepository;
import net.sf.iqser.plugin.mail.test.TestServiceLocator;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.log4j.PropertyConfigurator;

import com.iqser.core.config.Configuration;
import com.iqser.core.exception.IQserRuntimeException;
import com.iqser.core.exception.IQserTechnicalException;
import com.iqser.core.model.Attribute;
import com.iqser.core.model.Content;
import com.iqser.core.model.ContentItem;
import com.iqser.core.repository.Repository;

public class MailServerContentProviderTest extends TestCase {

	private MailServerContentProvider mscp;
	private String mailServer = "localhost";
	private String userName = "test";
	private String passWord = "test";
	private String sslPort = "993";
	private List<String> folders = new ArrayList<String>();


	String testDataDir;

	String PROVIDER_ID = "net.sf.iqser.plugin.mail";
	List<String> mockMailsIds = null;//the ids of 20 mails from mail-server
	private Map<String, String> attributeMappings = new HashMap<String, String>();

	@Override
	protected void setUp() throws Exception {

		folders = new ArrayList<String>();
		attributeMappings = new HashMap<String, String>();
		testDataDir = System.getProperty("testdata.dir", "testdata");

		PropertyConfigurator.configure("src/test/resources/log4j.properties");

		super.setUp();

		Properties initParams = new Properties();
		initParams.setProperty("MAIL_SERVER", "localhost");

		initParams.setProperty("USERNAME", "test");
		initParams.setProperty("PASSWORD", "test");
		initParams.setProperty("SSL-CONNECTION", "false");

		initParams.setProperty("ATTRIBUTE.MAPPINGS",
				"{SENDER_MAIL_0:MAILUL_CELUI_CARE_TRIMITE}");
		initParams.setProperty("KEY-ATTRIBUTES", "[SENDER_NAME]");
		initParams.setProperty("EMAIL-FOLDER", "[INBOX]");
		initParams.setProperty("SSL-PORT", "993");

		attributeMappings.put("SENDER_MAIL_0", "MAILUL_CELUI_CARE_TRIMITE");
		Configuration
				.configure(new File("src/test/resources/iqser-config.xml"));

		mscp = new MailServerContentProvider();
		mscp.setInitParams(initParams);
		mscp.setType("MAIL-SERVER");
		mscp.setId(PROVIDER_ID);
		mscp.setInitParams(initParams);
		mscp.init();

		TestServiceLocator sl = (TestServiceLocator) Configuration
				.getConfiguration().getServiceLocator();

		MockRepository rep = new MockRepository();
		rep.init();

		sl.setRepository(rep);
		sl.setAnalyzerTaskStarter(new MockAnalyzerTaskStarter());

		mockMailsIds = testCreateMockMails();
		
		folders.add("INBOX");

	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		Folder folder = connectMockupServer("false");
		folder.open(Folder.READ_WRITE);
		Message[] messages = folder.getMessages();
		for (Message message : messages) {
			message.setFlag(Flag.DELETED, true);
		}
		folder.close(true);

	}

	//test get binary data from the first mail from the server (mail with attachment)
	public void testGetBinaryData() throws MessagingException {

		assertEquals(mockMailsIds.size(), 20);
		Content content = mscp.getContent(mockMailsIds.get(0));// mscp.getContent("<4A28F5F7.5020000@recognos.ro>");

		MailServerUtils msu = new MailServerUtils();
		msu.setMailServer(mailServer);
		msu.setPassWord(passWord);
		msu.setUserName(userName);

		Folder folder = connectMockupServer("true");

		byte[] byteArray = null;

		try {
			Message message = msu.identifyMessage(folder,
					content.getContentUrl());
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			message.writeTo(baos);
			byteArray = baos.toByteArray();

			StringBuffer messageContent = new StringBuffer();
			for (byte b : byteArray) {
				messageContent.append((char) b);
			}

			
		} catch (MessagingException e) {
			throw new IQserRuntimeException(e);
		} catch (IOException e) {
			throw new IQserRuntimeException(e);
		}

		byte[] binaryData = mscp.getBinaryData(content);

		assertEquals(byteArray.length, binaryData.length);

		for (int i = 0; i < byteArray.length; i++)
			assertEquals(byteArray[i], binaryData[i]);

	}

	public void testDestroy() {

	}

	public void testGetActions() {
		Collection actions = mscp.getActions(null);
		assertEquals(actions.size(), 1);
	}

	public void testGetContentString() {
		assertTrue(mockMailsIds.size() == 20);
		String id = mockMailsIds.get(0);
		Content content = mscp.getContent(id);
		assertNotNull(content);
		id = mockMailsIds.get(1);
		content = mscp.getContent(id);
		assertNotNull(content);
		id = mockMailsIds.get(2);
		content = mscp.getContent(id);
		assertNotNull(content);
		id = mockMailsIds.get(3);
		content = mscp.getContent(id);
		assertNotNull(content);
	}

	public void testGetContentInputStream() throws IOException {
		Store store;
		try {
			Properties props = System.getProperties();
			props.setProperty("mail.store.protocol", "imap");
			String sslConnection = "false";
			if (sslConnection.equalsIgnoreCase("true")) {
				props.setProperty("mail.imap.socketFactory.class",
						"javax.net.ssl.SSLSocketFactory");
				props.setProperty("mail.imap.socketFactory.fallback", "false");
				props.setProperty("mail.imap.socketFactory.port", sslPort);
			}
			Session session = Session.getDefaultInstance(props);
			store = session.getStore("imap");

			store.connect(mailServer, userName, passWord);

			Folder folder = store.getFolder("INBOX");
			folder.open(Folder.READ_ONLY);
			Message messageBuf = null;
			Message[] messages = folder.getMessages();
			for (Message message : messages) {
				if (message.getHeader("MESSAGE-ID")[0]
						.equalsIgnoreCase(mockMailsIds.get(0))) {
					messageBuf = message;
					break;
				}
			}

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			messageBuf.writeTo(baos);
			byte[] byteArray = baos.toByteArray();

			InputStream inputStream = new ByteArrayInputStream(byteArray);

			Content content = mscp.getContent(inputStream);
			assertTrue(content.getFulltext() != null);
			
			assertNotNull(content
					.getAttributeByName("MESSAGE_ATTACHMENTS_NAME_0"));
			

		} catch (NoSuchProviderException e) {
			throw new IQserRuntimeException(e);
		} catch (MessagingException e) {
			throw new IQserRuntimeException(e);
		}

	}

	public void testGetContentUrls() {

		
		Collection contentUrls = getMailContentCreator().getMailServerURLs();
		assertTrue(contentUrls.size() == 100);
	}

	public void testInit() {

		try {
			Field field1 = mscp.getClass().getDeclaredField("mailServer");
			Field field2 = mscp.getClass().getDeclaredField("userName");
			Field field3 = mscp.getClass().getDeclaredField("passWord");
			Field field4 = mscp.getClass().getDeclaredField("sslConnection");
			Field field5 = mscp.getClass().getDeclaredField("folderAttributes");
			Field field6 = mscp.getClass().getDeclaredField("sslPort");
			Field field7 = mscp.getClass().getDeclaredField("keyAttributesList");
			field1.setAccessible(true);
			field2.setAccessible(true);
			field3.setAccessible(true);
			field4.setAccessible(true);
			field5.setAccessible(true);
			field6.setAccessible(true);
			field7.setAccessible(true);
			String sslPort = (String) field6.get(mscp);
			String mailServer = (String) field1.get(mscp);
			String userName = (String) field2.get(mscp);
			String passWord = (String) field3.get(mscp);
			String sslConnection = (String) field4.get(mscp);
			List<String> folders = (List<String>) field5.get(mscp);
			List<String> keyAttributes = (List<String>)field7.get(mscp);
			
			assertEquals(keyAttributes.size(), 1);

			assertEquals(sslPort, "993");
			assertTrue(folders.size() == 1);
			assertEquals(mailServer, "localhost");
			assertEquals(userName, "test");
			assertEquals(passWord, "test");
			assertEquals(sslConnection, "false");

		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

	}

	private List<String> testCreateMockMails() throws MessagingException {

		//the ids of the mails that are used for other operations
		List<String> ids = new ArrayList<String>();

		// add fifty mails with attachments
		int count = 0;
		for (int i = 0; i < 50; i++) {
			String id = addMailWithAttachments("Mail Attachment " + i);
			if (id != null && count < 10) {
				ids.add(id);
				count++;
			}
		}

		// add fifty mails without attachments
		count = 0;
		for (int i = 0; i < 50; i++) {
			String id = addMailWithoutAttachments("Mail without attachment "
					+ i);
			if (id != null && count < 10) {
				ids.add(id);
				count++;
			}
		}

		return ids;

	}

	private Folder connectMockupServer(String string) throws MessagingException {

		MailServerUtils msu = new MailServerUtils();
		msu.setUserName(userName);
		msu.setPassWord(passWord);
		msu.setMailServer(mailServer);
		msu.setSslPort("993");

		Store store = msu.attemptConnectionMailServer(string);

		boolean connected = store.isConnected();
	
		if (connected)
			return store.getFolder("INBOX");
		else
			return null;
	}

	private String addMailWithoutAttachments(String subject)
			throws MessagingException {

		Properties props = new Properties();
		props.setProperty("mail.transport.protocol", "smtp");
		props.setProperty("mail.host", mailServer);
		props.setProperty("mail.user", userName);
		props.setProperty("mail.password", passWord);

		Session mailSession = Session.getInstance(props, null);
		Transport transport = mailSession.getTransport();
		MimeMessage message = new MimeMessage(mailSession);
		message.setFrom(new InternetAddress(userName + "@localhost"));
		message.setSubject(subject);
		message.setContent("This is a test", "text/plain");
		message.addRecipient(Message.RecipientType.TO, new InternetAddress(
				userName + "@localhost"));

		transport.connect();
		transport.sendMessage(message,
				message.getRecipients(Message.RecipientType.TO));
		transport.close();
		return message.getHeader("MESSAGE-ID")[0];
	}

	private String addMailWithAttachments(String subject)
			throws MessagingException {
		String from = userName + "@localhost";
		String to = userName + "@localhost";
		String fileAttachment = testDataDir + "/testSent.txt";

		// Get system properties
		Properties props = new Properties();
		props.setProperty("mail.transport.protocol", "smtp");
		props.setProperty("mail.host", mailServer);
		props.setProperty("mail.user", userName);
		props.setProperty("mail.password", passWord);

		// Get session
		Session session = Session.getInstance(props, null);

		// Define message
		MimeMessage message = new MimeMessage(session);
		message.setFrom(new InternetAddress(from));
		message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
		message.setSubject(subject);

		// create the message part
		MimeBodyPart messageBodyPart = new MimeBodyPart();

		// fill message
		messageBodyPart.setText("Hi");

		Multipart multipart = new MimeMultipart();
		multipart.addBodyPart(messageBodyPart);

		// Part two is attachment
		messageBodyPart = new MimeBodyPart();
		DataSource source = new FileDataSource(fileAttachment);
		messageBodyPart.setDataHandler(new DataHandler(source));
		messageBodyPart.setFileName(fileAttachment);
		multipart.addBodyPart(messageBodyPart);

		// Put parts in message
		message.setContent(multipart);

		// Send the message
		Transport.send(message);
		return message.getHeader("MESSAGE-ID")[0];
	}

	public void testConnection() throws SecurityException,
			NoSuchMethodException, IllegalArgumentException,
			IllegalAccessException, InvocationTargetException {

		MailServerUtils msu = new MailServerUtils();
		msu.setUserName(userName);
		msu.setPassWord(passWord);
		msu.setMailServer(mailServer);
		msu.setSslPort("993");

		Store store = msu.attemptConnectionMailServer("true");
		boolean connected = store.isConnected();
		assertTrue(connected);
	}

	public void testOnChangeEvent() {

	}

	public void testChangeKeyAttributes() throws IQserTechnicalException {

		mscp.doSynchonization();

		Repository repository = Configuration.getConfiguration()
				.getServiceLocator().getRepository();

		Collection<Content> contents = repository.getContentByProvider(
				PROVIDER_ID, true);

		for (Content content : contents) {
			if (content.getType().equalsIgnoreCase("EMAIL")) {
				for (Attribute attr : content.getAttributes()) {
					if (!"SENDER_NAME_0".equalsIgnoreCase(attr.getName()))
						assertTrue(!attr.isKey());
					else
						assertTrue(attr.isKey());
				}
			}

		}

	}

	public void testKeyMappingAttributes() throws IQserTechnicalException {

		mscp.doSynchonization();

		Repository repository = Configuration.getConfiguration()
				.getServiceLocator().getRepository();

		Collection<Content> contents = repository.getContentByProvider(
				PROVIDER_ID, true);

		for (Content content : contents) {
			if (content.getType().equalsIgnoreCase("EMAIL")) {
				Attribute attribute = content
						.getAttributeByName("MAILUL_CELUI_CARE_TRIMITE");

				assertNotNull(attribute);
			}
		}
	}

	public void testPerformAction() {

		try {

			Repository repository = Configuration.getConfiguration()
					.getServiceLocator().getRepository();

			mscp.doSynchonization();

			ArrayList<ContentItem> existingContents = repository
					.getAllContentItem(-1);

			assertTrue(existingContents.size() > 0);
			assertEquals(existingContents.size(), 150);

			Collection<Content> contents = repository.getContentByProvider(
					PROVIDER_ID, true);

			assertEquals(contents.size(), 150);

			for (Content content : contents) {
				assertEquals(PROVIDER_ID, content.getProvider());
			}

			MailServerUtils msu = new MailServerUtils();
			msu.setUserName(userName);
			msu.setPassWord(passWord);
			msu.setMailServer(mailServer);

			Store store = msu.attemptConnectionMailServer("false");

			Folder folder = store.getFolder("INBOX");
			folder.open(Folder.READ_ONLY);

			Message[] messages = folder.getMessages();

			for (Message message : messages) {
				Content content = mscp.getContent(message
						.getHeader("MESSAGE-ID")[0]);
				assertNotNull(content);
				mscp.performAction("delete", content);

			}
			store = msu.attemptConnectionMailServer("false");
			folder = store.getFolder("INBOX");
			assertEquals(0,folder.getMessageCount());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void testDoSynchonization() throws IQserTechnicalException,
			MessagingException {

		Repository repository = Configuration.getConfiguration()
				.getServiceLocator().getRepository();

		ArrayList<ContentItem> existingContents = repository
				.getAllContentItem(-1);

		assertTrue(existingContents.size() == 0);

		List<String> contentUrls = new ArrayList<String>(
				getMailContentCreator().getMailServerURLs());

		mscp.doSynchonization();

		existingContents = repository.getAllContentItem(-1);

		assertTrue(contentUrls.size() + 50 == existingContents.size());

	}

	public void testDoHousekeeping() throws IQserTechnicalException,
			MessagingException {
		
		Repository repository = Configuration.getConfiguration()
				.getServiceLocator().getRepository();

		assertEquals(mockMailsIds.size(), 20);
		String id = mockMailsIds.get(0);
		Content content = mscp.getContent(id);
		repository.addContent(content);

		id = mockMailsIds.get(1);
		content = mscp.getContent(id);
		repository.addContent(content);
		
		String mailWithAttachments = addMailWithAttachments("s1");
		content = mscp.getContent(mailWithAttachments);
		repository.addContent(content);
		
	

		ArrayList<ContentItem> existingContents = repository
				.getAllContentItem(-1);
		assertTrue(existingContents.size() == 3);
		Folder folder = connectMockupServer("true");
		
		assertEquals(folder.getMessageCount(), 101);
		MailServerUtils msu = new MailServerUtils();
		Message message = msu.identifyMessage(folder, mailWithAttachments);
		message.setFlag(Flag.DELETED, true);
		folder.close(true);
		folder = connectMockupServer("true");
		assertEquals(folder.getMessageCount(), 100);

		mscp.doHousekeeping();

		folder = connectMockupServer("true");
		assertEquals(folder.getMessageCount(), 100);
		existingContents = repository
		.getAllContentItem(-1);
		assertEquals(existingContents.size(), 2);
	}
	
	private MailContentCreator getMailContentCreator() {

	
		MailContentCreator mc = new MailContentCreator();

		mc.setMailServer(mailServer);
		mc.setPassWord(passWord);
		mc.setSslConnection("true");
		mc.setUserName(userName);
		mc.setFolders(folders);
		mc.setSslPort(sslPort);
		mc.setAttributeMap(attributeMappings);

		return mc;
	}
}
