package net.sf.iqser.plugin.mail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
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
import javax.mail.Message.RecipientType;
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

import junit.framework.Assert;
import net.sf.iqser.plugin.mail.content.MailContentCreator;
import net.sf.iqser.plugin.mail.test.MockContentProviderFacade;
import net.sf.iqser.plugin.mail.test.MockRepository;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.log4j.PropertyConfigurator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.iqser.core.config.ServiceLocatorFactory;
import com.iqser.core.exception.IQserException;
import com.iqser.core.exception.IQserRuntimeException;
import com.iqser.core.model.Attribute;
import com.iqser.core.model.Content;
import com.iqser.core.repository.RepositoryReader;
import com.iqser.core.repository.RepositoryWriter;
import com.iqser.gin.developer.test.TestServiceLocator;
import com.iqser.gin.developer.test.plugin.provider.ContentProviderTestCase;

public class MailServerContentProviderTest extends ContentProviderTestCase {

	private MailServerContentProvider mscp;
	private String mailServer = "localhost";
	private String userName = "test";
	private String password = "test";
	private String sslPort = "993";
	private List<String> folders = new ArrayList<String>();

	private String cache = "false";
	private static final int MAIL_NUMBER = 10;

	private static final int ATTACHMENT_FILES = 17;

	private static final int MAIL_ID_NUMBER = 4;

	String testDataDir;

	String PROVIDER_ID = "net.sf.iqser.plugin.mail";
	List<String> mockMailsIds = null;// the ids of 20 mails from mail-server
	private Map<String, String> attributeMappings = new HashMap<String, String>();

	@Before
	public void setUp() throws Exception {

		folders = new ArrayList<String>();
		attributeMappings = new HashMap<String, String>();
		testDataDir = System.getProperty("testdata.dir", "testdata");

		PropertyConfigurator.configure("src/test/resources/log4j.properties");

		Properties initParams = new Properties();
		initParams.setProperty("userName", "test");
		initParams.setProperty("password", "test");
		initParams.setProperty("sslConnection", "false");
		initParams.setProperty("emailPort", "143");
		initParams
				.setProperty("attributeMappings",
						"{SENDER_MAIL_0:MAILUL_CELUI_CARE_TRIMITE,TITLE:TITLU,title:TITLU}");
		initParams.setProperty("keyAttributes", "[SENDER_NAME][TITLE]");
		initParams.setProperty("emailFolder", "[INBOX]");
		initParams.setProperty("sslPort", "993");
		initParams.setProperty("emailCache", cache);
		initParams.setProperty("mailServer", "localhost");

		attributeMappings.put("SENDER_MAIL_0", "MAILUL_CELUI_CARE_TRIMITE");

		mscp = new MailServerContentProvider();
		mscp.setInitParams(initParams);
		mscp.setName(PROVIDER_ID);
		mscp.setInitParams(initParams);
		mscp.init();

		TestServiceLocator serviceLocator = (TestServiceLocator) ServiceLocatorFactory
				.getServiceLocator();
		MockRepository repositoryMock = new MockRepository();
		serviceLocator.setRepositoryReader(repositoryMock);
		serviceLocator.setRepositoryWriter(repositoryMock);
		serviceLocator
				.setContentProviderFacade(new MockContentProviderFacade());

		mockMailsIds = testCreateMockMails();
		folders.add("INBOX");
	}

	@After
	public void tearDown() throws Exception {
		Folder folder = connectMockupServer("false");
		folder.open(Folder.READ_WRITE);
		Message[] messages = folder.getMessages();
		for (Message message : messages) {
			message.setFlag(Flag.DELETED, true);
		}
		folder.close(true);
	}

	// test get binary data from the first mail from the server (mail with
	// attachment)
	@Test
	public void testGetBinaryData() throws MessagingException {
		Assert.assertEquals(mockMailsIds.size(), MAIL_ID_NUMBER);
		Content content = mscp.createContent(mockMailsIds.get(0));// mscp.getContent("<4A28F5F7.5020000@recognos.ro>");

		MailServerUtils msu = new MailServerUtils();
		msu.setMailServer(mailServer);
		msu.setPassWord(password);
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

		Assert.assertEquals(byteArray.length, binaryData.length);

		for (int i = 0; i < byteArray.length; i++) {
			Assert.assertEquals(byteArray[i], binaryData[i]);
		}

	}

	@Test
	public void testGetActions() {
		Collection<String> actions = mscp.getActions(null);
		Assert.assertEquals(actions.size(), 1);
	}

	@Test
	public void testGetContentString() {
		Assert.assertTrue(mockMailsIds.size() == MAIL_ID_NUMBER);
		String id = mockMailsIds.get(0);
		Content content = mscp.createContent(id);
		// Attribute attribute2 = content.getAttributeByName("RECEIVED_DATE");
		Attribute attribute3 = content.getAttributeByName("SENDER_NAME_0");
		Assert.assertNotNull(attribute3);
		String fulltext = content.getFulltext();
		Assert.assertNotNull(fulltext);
		Assert.assertEquals("Hi", fulltext);
		// Assert.assertNotNull(attribute2);
		Assert.assertNotNull(content);
		Attribute attribute = content
				.getAttributeByName("MESSAGE_ATTACHMENTS_NAME_0");
		// attribute
		Assert.assertNotNull(attribute);
		String value = attribute.getValue();
		Assert.assertNotNull(value);
		content = mscp.createContent(value);
		Assert.assertNotNull(content);
		Assert.assertEquals("testing send data with attachments",
				content.getFulltext());
		id = mockMailsIds.get(1);
		content = mscp.createContent(id);
		Assert.assertNotNull(content);
		id = mockMailsIds.get(2);
		content = mscp.createContent(id);
		Assert.assertNotNull(content);
		id = mockMailsIds.get(3);
		content = mscp.createContent(id);
		Assert.assertNotNull(content);

		// test owner
		Assert.assertEquals(userName, content.getAttributeByName("owner")
				.getValue());
	}

	@Test
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

			store.connect(mailServer, userName, password);

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

			Content content = mscp.createContent(inputStream);
			Assert.assertTrue(content.getFulltext() != null);

			Assert.assertNotNull(content
					.getAttributeByName("MESSAGE_ATTACHMENTS_NAME_0"));

			Assert.assertNotNull(content.getFulltext());
			Assert.assertEquals("Hi", content.getFulltext());

			// test owner
			Assert.assertEquals(userName, content.getAttributeByName("owner")
					.getValue());
		} catch (NoSuchProviderException e) {
			throw new IQserRuntimeException(e);
		} catch (MessagingException e) {
			throw new IQserRuntimeException(e);
		}

	}

	@Test
	public void testGetContentUrls() {

		Collection<String> contentUrls = getMailContentCreator()
				.getMailServerURLs(new Date(0).getTime());
		Assert.assertEquals(10, contentUrls.size());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testInit() {

		try {
			Field field1 = mscp.getClass().getDeclaredField("mailServer");
			Field field2 = mscp.getClass().getDeclaredField("userName");
			Field field3 = mscp.getClass().getDeclaredField("password");
			Field field4 = mscp.getClass().getDeclaredField("sslConnection");
			Field field5 = mscp.getClass().getDeclaredField("folderAttributes");
			Field field6 = mscp.getClass().getDeclaredField("sslPort");
			Field field7 = mscp.getClass()
					.getDeclaredField("keyAttributesList");
			Field field8 = mscp.getClass()
					.getDeclaredField("attributeMappings");
			field1.setAccessible(true);
			field2.setAccessible(true);
			field3.setAccessible(true);
			field4.setAccessible(true);
			field5.setAccessible(true);
			field6.setAccessible(true);
			field7.setAccessible(true);
			field8.setAccessible(true);
			String sslPort = (String) field6.get(mscp);
			String mailServer = (String) field1.get(mscp);
			String userName = (String) field2.get(mscp);
			String password = (String) field3.get(mscp);
			String sslConnection = (String) field4.get(mscp);
			List<String> folders = (List<String>) field5.get(mscp);
			List<String> keyAttributes = (List<String>) field7.get(mscp);
			Map<String, String> mappingAttrs = (Map<String, String>) field8
					.get(mscp);

			Assert.assertEquals(sslPort, "993");
			Assert.assertTrue(folders.size() == 1);
			Assert.assertEquals(mailServer, "localhost");
			Assert.assertEquals(userName, "test");
			Assert.assertEquals(password, "test");
			Assert.assertEquals(sslConnection, "false");
			Assert.assertEquals(3, mappingAttrs.keySet().size());
			Assert.assertEquals("MAILUL_CELUI_CARE_TRIMITE",
					mappingAttrs.get("SENDER_MAIL_0"));
			Assert.assertEquals("TITLU", mappingAttrs.get("TITLE"));
			Assert.assertEquals("TITLU", mappingAttrs.get("title"));
			Assert.assertEquals(2, keyAttributes.size());
			Assert.assertEquals("SENDER_NAME", keyAttributes.get(0));
			Assert.assertEquals("TITLE", keyAttributes.get(1));

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

	private List<String> testCreateMockMails() throws MessagingException,
			IOException {

		// the ids of the mails that are used for other operations
		List<String> ids = new ArrayList<String>();
		String id;
		int count;

		id = addMailWithAttachments("Mail Attachment " + 0);
		ids.add(id);

		// add fifty mails with attachments
		count = 0;
		for (int i = 1; i < 5; i++) {
			id = addMailWith1Attachment("Mail Attachment " + i);
			if (id != null && count < 1) {
				ids.add(id);
				count++;
			}
		}

		// add fifty mails without attachments
		count = 0;
		for (int i = 0; i < 5; i++) {
			id = addMailWithoutAttachments("Mail without attachment " + i);
			if (id != null && count < 2) {
				ids.add(id);
				count++;
			}
		}

		return ids;

	}

	private Folder connectMockupServer(String string) throws MessagingException {

		MailServerUtils msu = new MailServerUtils();
		msu.setUserName(userName);
		msu.setPassWord(password);
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
		props.setProperty("mail.password", password);

		Session mailSession = Session.getInstance(props, null);
		Transport transport = mailSession.getTransport();
		MimeMessage message = new MimeMessage(mailSession);
		message.setFrom(new InternetAddress(userName + "@localhost"));
		message.setSubject(subject);
		message.setContent("This is a test", "text/plain");
		message.addRecipient(Message.RecipientType.TO, new InternetAddress(
				userName + "@localhost"));
		message.setSentDate(new Date());

		transport.connect();
		transport.sendMessage(message,
				message.getRecipients(Message.RecipientType.TO));
		transport.close();
		return message.getHeader("MESSAGE-ID")[0];
	}

	private String addMailWith1Attachment(String subject)
			throws MessagingException {
		String from = userName + "_from@localhost";
		String to = userName + "@localhost";
		String bcc = userName + "_bcc@localhost";
		String cc = userName + "_cc@localhost";
		String fileAttachment1 = testDataDir + "/testSent.txt";

		// Get system properties
		Properties props = new Properties();
		props.setProperty("mail.transport.protocol", "smtp");
		props.setProperty("mail.host", mailServer);
		props.setProperty("mail.user", userName);
		props.setProperty("mail.password", password);

		Session session = Session.getInstance(props, null);

		MimeMessage message = new MimeMessage(session);
		message.setFrom(new InternetAddress(from));
		message.addRecipient(RecipientType.BCC, new InternetAddress(bcc));
		message.addRecipient(RecipientType.CC, new InternetAddress(cc));
		message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
		message.setSubject(subject);
		message.setSentDate(new Date());
		MimeBodyPart messageBodyPart = new MimeBodyPart();

		// fill message
		messageBodyPart.setText("Hi");

		Multipart multipart = new MimeMultipart();
		multipart.addBodyPart(messageBodyPart);

		// Part two is attachment
		messageBodyPart = new MimeBodyPart();

		DataSource source = new FileDataSource(fileAttachment1);
		messageBodyPart.setDataHandler(new DataHandler(source));
		messageBodyPart.setFileName(fileAttachment1);
		multipart.addBodyPart(messageBodyPart);

		// Put parts in message
		message.setContent(multipart);

		// Send the message
		Transport.send(message);

		return message.getHeader("MESSAGE-ID")[0];

	}

	private String addMailWithAttachments(String subject)
			throws MessagingException, IOException {
		String from = userName + "_from@localhost";
		String to = userName + "@localhost";
		String bcc = userName + "_bcc@localhost";
		String cc = userName + "_cc@localhost";
		String fileAttachment1 = testDataDir + "/testSent.txt";
		String fileAttachment2 = testDataDir + "/testSent2.txt";
		String fileAttachment3 = testDataDir + "/testzipfiles.zip";
		String fileAttachment5 = testDataDir + "/ExcelDataTest.xls";
		String fileAttachment6 = testDataDir + "/ODFDataTest.odt";
		String fileAttachment7 = testDataDir + "/PowerPointTestData.ppt";
		String fileAttachment8 = testDataDir + "/TestDocument.rtf";
		String fileAttachment9 = testDataDir + "/TxtDataTest.txt";
		String fileAttachment11 = testDataDir + "/WordDataTest.doc";
		String fileAttachment12 = testDataDir + "/ZimbraCommunity.pdf";

		// Get system properties
		Properties props = new Properties();
		props.setProperty("mail.transport.protocol", "smtp");
		props.setProperty("mail.host", mailServer);
		props.setProperty("mail.user", userName);
		props.setProperty("mail.password", password);

		// Get session
		Session session = Session.getInstance(props, null);

		// Define message
		MimeMessage message = new MimeMessage(session);
		message.setFrom(new InternetAddress(from));
		message.addRecipient(RecipientType.BCC, new InternetAddress(bcc));
		message.addRecipient(RecipientType.CC, new InternetAddress(cc));
		message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
		message.setSubject(subject);
		message.setSentDate(new Date());
		// create the message part
		MimeBodyPart messageBodyPart = new MimeBodyPart();

		// fill message
		messageBodyPart.setText("Hi");

		Multipart multipart = new MimeMultipart();
		multipart.addBodyPart(messageBodyPart);

		// Part two is attachment
		messageBodyPart = new MimeBodyPart();
		// messageBodyPart.attachFile(fileAttachment1);
		// messageBodyPart.attachFile(fileAttachment2);
		DataSource source = new FileDataSource(fileAttachment1);
		messageBodyPart.setDataHandler(new DataHandler(source));
		messageBodyPart.setFileName(fileAttachment1);
		multipart.addBodyPart(messageBodyPart);
		//
		//
		messageBodyPart = new MimeBodyPart();
		source = new FileDataSource(fileAttachment2);
		messageBodyPart.setDataHandler(new DataHandler(source));
		messageBodyPart.setFileName(fileAttachment2);
		multipart.addBodyPart(messageBodyPart);

		messageBodyPart = new MimeBodyPart();
		source = new FileDataSource(fileAttachment3);
		messageBodyPart.setDataHandler(new DataHandler(source));
		messageBodyPart.setFileName(fileAttachment3);
		multipart.addBodyPart(messageBodyPart);

		messageBodyPart = new MimeBodyPart();
		source = new FileDataSource(fileAttachment5);
		messageBodyPart.setDataHandler(new DataHandler(source));
		messageBodyPart.setFileName(fileAttachment5);
		multipart.addBodyPart(messageBodyPart);

		messageBodyPart = new MimeBodyPart();
		source = new FileDataSource(fileAttachment6);
		messageBodyPart.setDataHandler(new DataHandler(source));
		messageBodyPart.setFileName(fileAttachment6);
		multipart.addBodyPart(messageBodyPart);

		messageBodyPart = new MimeBodyPart();
		source = new FileDataSource(fileAttachment7);
		messageBodyPart.setDataHandler(new DataHandler(source));
		messageBodyPart.setFileName(fileAttachment7);
		multipart.addBodyPart(messageBodyPart);

		messageBodyPart = new MimeBodyPart();
		source = new FileDataSource(fileAttachment8);
		messageBodyPart.setDataHandler(new DataHandler(source));
		messageBodyPart.setFileName(fileAttachment8);
		multipart.addBodyPart(messageBodyPart);

		messageBodyPart = new MimeBodyPart();
		source = new FileDataSource(fileAttachment9);
		messageBodyPart.setDataHandler(new DataHandler(source));
		messageBodyPart.setFileName(fileAttachment9);
		multipart.addBodyPart(messageBodyPart);

		messageBodyPart = new MimeBodyPart();
		source = new FileDataSource(fileAttachment11);
		messageBodyPart.setDataHandler(new DataHandler(source));
		messageBodyPart.setFileName(fileAttachment11);
		multipart.addBodyPart(messageBodyPart);

		messageBodyPart = new MimeBodyPart();
		source = new FileDataSource(fileAttachment12);
		messageBodyPart.setDataHandler(new DataHandler(source));
		messageBodyPart.setFileName(fileAttachment12);
		multipart.addBodyPart(messageBodyPart);

		// Put parts in message
		message.setContent(multipart);

		// Send the message
		Transport.send(message);

		return message.getHeader("MESSAGE-ID")[0];
	}

	@Test
	public void testConnection() throws SecurityException,
			NoSuchMethodException, IllegalArgumentException,
			IllegalAccessException, InvocationTargetException {

		MailServerUtils msu = new MailServerUtils();
		msu.setUserName(userName);
		msu.setPassWord(password);
		msu.setMailServer(mailServer);
		msu.setSslPort("993");

		Store store = msu.attemptConnectionMailServer("true");
		boolean connected = store.isConnected();
		Assert.assertTrue(connected);
	}

	public void testOnChangeEvent() {

	}

	@Test
	public void testChangeKeyAttributes() throws IQserException {

		mscp.doSynchronization();

		RepositoryReader repository = ServiceLocatorFactory.getServiceLocator()
				.getRepositoryReader();

		Collection<Content> contents = repository.getContentByProvider(
				PROVIDER_ID, true);

		for (Content content : contents) {
			if (content.getType().equalsIgnoreCase("EMAIL")) {
				for (Attribute attr : content.getAttributes()) {
					if (!"SENDER_NAME_0".equalsIgnoreCase(attr.getName()))
						Assert.assertTrue(!attr.isKey());
					else
						Assert.assertTrue(attr.isKey());
				}
			} else {
				Attribute titleAttr = content.getAttributeByName("TITLE");
				if (titleAttr != null)
					Assert.assertTrue(titleAttr.isKey());
			}

		}

	}

	@Test
	public void testKeyMappingAttributes() throws IQserException {

		mscp.doSynchronization();

		RepositoryReader repository = ServiceLocatorFactory.getServiceLocator()
				.getRepositoryReader();

		Collection<Content> contents = repository.getContentByProvider(
				PROVIDER_ID, true);

		for (Content content : contents) {
			if (content.getType().equalsIgnoreCase("EMAIL")) {
				Attribute attribute = content
						.getAttributeByName("MAILUL_CELUI_CARE_TRIMITE");

				Assert.assertNotNull(attribute);
			}
		}
	}

	@Test
	public void testPerformAction() {

		try {
			RepositoryReader repository = ServiceLocatorFactory
					.getServiceLocator().getRepositoryReader();

			mscp.doSynchronization();

			Collection<Content> existingContents = repository
					.getContentByProvider(PROVIDER_ID, false);

			Assert.assertTrue(existingContents.size() > 0);
			int expected = MAIL_NUMBER + ATTACHMENT_FILES
					+ (MAIL_NUMBER / 2 - 1);
			Assert.assertEquals(expected, existingContents.size());

			Collection<Content> contents = repository.getContentByProvider(
					PROVIDER_ID, true);

			Assert.assertEquals(expected, contents.size());

			for (Content content : contents) {
				Assert.assertEquals(PROVIDER_ID, content.getProvider());
			}

			MailServerUtils msu = new MailServerUtils();
			msu.setUserName(userName);
			msu.setPassWord(password);
			msu.setMailServer(mailServer);

			Store store = msu.attemptConnectionMailServer("false");

			Folder folder = store.getFolder("INBOX");
			folder.open(Folder.READ_ONLY);

			Message[] messages = folder.getMessages();

			for (Message message : messages) {
				Content content = mscp.createContent(message
						.getHeader("MESSAGE-ID")[0]);
				Assert.assertNotNull(content);
				mscp.performAction("delete", null, content);

			}
			store = msu.attemptConnectionMailServer("false");
			folder = store.getFolder("INBOX");
			Assert.assertEquals(0, folder.getMessageCount());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testDoSynchonization() throws IQserException,
			MessagingException {
		RepositoryReader repositoryReader = ServiceLocatorFactory
				.getServiceLocator().getRepositoryReader();

		Collection<Content> contents = repositoryReader.getContentByProvider(
				PROVIDER_ID, true);
		Assert.assertTrue(contents.size() == 0);

		// List<String> contentUrls = new ArrayList<String>(
		// getMailContentCreator().getMailServerURLs());

		mscp.doSynchronization();

		contents = repositoryReader.getContentByProvider(PROVIDER_ID, true);

		for (Object contentObject : contents) {
			Content content = (Content) contentObject;
			if (!content.getType().equalsIgnoreCase("EMAIL")) {
				String fulltext = content.getFulltext();
				Assert.assertNotNull(fulltext);
				Assert.assertTrue(fulltext.trim().length() > 0);
			} else {
				Attribute attribute = content
						.getAttributeByName("MESSAGE_FOLDER");
				Assert.assertEquals("INBOX", attribute.getValue());
			}

		}

		int expected = MAIL_NUMBER + ATTACHMENT_FILES + (MAIL_NUMBER / 2 - 1);
		Assert.assertEquals(expected, contents.size());

	}

	@Test
	public void testDoHousekeeping() throws IQserException, MessagingException,
			IOException {

		RepositoryWriter repositoryWriter = ServiceLocatorFactory
				.getServiceLocator().getRepositoryWriter();
		RepositoryReader repositoryReader = ServiceLocatorFactory
				.getServiceLocator().getRepositoryReader();

		Assert.assertEquals(mockMailsIds.size(), 4);
		String id = mockMailsIds.get(0);
		Content content = mscp.createContent(id);
		repositoryWriter.addOrUpdateContent(content);

		id = mockMailsIds.get(1);
		content = mscp.createContent(id);
		repositoryWriter.addOrUpdateContent(content);

		String mailWithAttachments = addMailWithAttachments("s1");
		content = mscp.createContent(mailWithAttachments);
		repositoryWriter.addOrUpdateContent(content);

		Collection<Content> existingContents = repositoryReader.getContentByProvider(PROVIDER_ID, false);
		Assert.assertTrue(existingContents.size() == 3);
		Folder folder = connectMockupServer("true");

		Assert.assertEquals(folder.getMessageCount(), 11);
		MailServerUtils msu = new MailServerUtils();
		Message message = msu.identifyMessage(folder, mailWithAttachments);
		message.setFlag(Flag.DELETED, true);
		folder.close(true);
		folder = connectMockupServer("true");
		Assert.assertEquals(folder.getMessageCount(), 10);

		mscp.doHousekeeping();

		folder = connectMockupServer("true");
		Assert.assertEquals(10, folder.getMessageCount());
		existingContents = repositoryReader.getContentByProvider(PROVIDER_ID, false);
		Assert.assertEquals(2, existingContents.size());
	}

	private MailContentCreator getMailContentCreator() {

		MailContentCreator mc = new MailContentCreator();

		mc.setMailServer(mailServer);
		mc.setPassWord(password);
		mc.setSslConnection("true");
		mc.setUserName(userName);
		mc.setFolders(folders);
		mc.setSslPort(sslPort);
		mc.setAttributeMap(attributeMappings);
		mc.setPort(143);
		mc.setCache(cache);

		return mc;
	}
}
