package net.sf.iqser.plugin.mail;

import java.io.ByteArrayInputStream;
import java.io.File;
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

import junit.framework.TestCase;
import net.sf.iqser.plugin.mail.content.MailContentCreator;
import net.sf.iqser.plugin.mail.test.MockAnalyzerTaskStarter;
import net.sf.iqser.plugin.mail.test.MockContentProviderFacade;
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

	private String cache = "false";
	private static final int MAIL_NUMBER = 10;

	private static final int ATTACHMENT_FILES = 17;

	private static final int MAIL_ID_NUMBER = 4;

	String testDataDir;

	String PROVIDER_ID = "net.sf.iqser.plugin.mail";
	List<String> mockMailsIds = null;// the ids of 20 mails from mail-server
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
		initParams.setProperty("EMAIL-PORT", "143");
		

		initParams
				.setProperty("ATTRIBUTE.MAPPINGS",
						"{SENDER_MAIL_0:MAILUL_CELUI_CARE_TRIMITE,TITLE:TITLU,title:TITLU}");
		initParams.setProperty("KEY-ATTRIBUTES", "[SENDER_NAME][TITLE]");
		initParams.setProperty("EMAIL-FOLDER", "[INBOX]");
		initParams.setProperty("SSL-PORT", "993");
		initParams.setProperty("EMAIL-CACHE", cache);

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

		MockContentProviderFacade cpFacade = new MockContentProviderFacade();
		cpFacade.setRepo(rep);
		sl.setFacade(cpFacade);
		
		mockMailsIds = testCreateMockMails();

		Folder folder = connectMockupServer("false");
		// Folder inboxFolder = folder.getStore().getFolder("INBOX");
		// inboxFolder.copyMessages(inboxFolder.getMessages(), folder);
		// for (Message inbMess : inboxFolder.getMessages()) {
		// inbMess.setFlag(Flag.DELETED, true);
		// }
		// inboxFolder.close(true);

		//assertEquals(MAIL_NUMBER, folder.getMessages().length);

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

	// test get binary data from the first mail from the server (mail with
	// attachment)
	public void testGetBinaryData() throws MessagingException {

		assertEquals(mockMailsIds.size(), MAIL_ID_NUMBER);
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
		assertTrue(mockMailsIds.size() == MAIL_ID_NUMBER);
		String id = mockMailsIds.get(0);
		Content content = mscp.getContent(id);
		// Attribute attribute2 = content.getAttributeByName("RECEIVED_DATE");
		Attribute attribute3 = content.getAttributeByName("SENDER_NAME_0");
		assertNotNull(attribute3);
		String fulltext = content.getFulltext();
		assertNotNull(fulltext);
		assertEquals("Hi", fulltext);
		// assertNotNull(attribute2);
		assertNotNull(content);
		Attribute attribute = content
				.getAttributeByName("MESSAGE_ATTACHMENTS_NAME_0");
		// attribute
		assertNotNull(attribute);
		String value = attribute.getValue();
		assertNotNull(value);
		content = mscp.getContent(value);
		assertNotNull(content);
		assertEquals("testing send data with attachments",
				content.getFulltext());
		id = mockMailsIds.get(1);
		content = mscp.getContent(id);
		assertNotNull(content);
		id = mockMailsIds.get(2);
		content = mscp.getContent(id);
		assertNotNull(content);
		id = mockMailsIds.get(3);
		content = mscp.getContent(id);
		assertNotNull(content);
		
		//test owner
		assertEquals(userName,content.getAttributeByName("owner").getValue());
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

			assertNotNull(content.getFulltext());
			assertEquals("Hi", content.getFulltext());

			//test owner
			assertEquals(userName,content.getAttributeByName("owner").getValue());
		} catch (NoSuchProviderException e) {
			throw new IQserRuntimeException(e);
		} catch (MessagingException e) {
			throw new IQserRuntimeException(e);
		}

	}

	public void testGetContentUrls() {

		Collection contentUrls = getMailContentCreator().getMailServerURLs(
				new Date(0).getTime());
		assertEquals(10, contentUrls.size());
	}

	public void testInit() {

		try {
			Field field1 = mscp.getClass().getDeclaredField("mailServer");
			Field field2 = mscp.getClass().getDeclaredField("userName");
			Field field3 = mscp.getClass().getDeclaredField("passWord");
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
			String passWord = (String) field3.get(mscp);
			String sslConnection = (String) field4.get(mscp);
			List<String> folders = (List<String>) field5.get(mscp);
			List<String> keyAttributes = (List<String>) field7.get(mscp);
			Map<String, String> mappingAttrs = (Map<String, String>) field8
					.get(mscp);

			assertEquals(sslPort, "993");
			assertTrue(folders.size() == 1);
			assertEquals(mailServer, "localhost");
			assertEquals(userName, "test");
			assertEquals(passWord, "test");
			assertEquals(sslConnection, "false");
			assertEquals(3, mappingAttrs.keySet().size());
			assertEquals("MAILUL_CELUI_CARE_TRIMITE",
					mappingAttrs.get("SENDER_MAIL_0"));
			assertEquals("TITLU", mappingAttrs.get("TITLE"));
			assertEquals("TITLU", mappingAttrs.get("title"));
			assertEquals(2, keyAttributes.size());
			assertEquals("SENDER_NAME", keyAttributes.get(0));
			assertEquals("TITLE", keyAttributes.get(1));

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
		props.setProperty("mail.password", passWord);

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
		props.setProperty("mail.password", passWord);

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
			} else {
				Attribute titleAttr = content.getAttributeByName("TITLE");
				if (titleAttr != null)
					assertTrue(titleAttr.isKey());
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
			int expected = MAIL_NUMBER + ATTACHMENT_FILES
					+ (MAIL_NUMBER / 2 - 1);
			assertEquals(expected, existingContents.size());

			Collection<Content> contents = repository.getContentByProvider(
					PROVIDER_ID, true);

			assertEquals(expected, contents.size());

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
			assertEquals(0, folder.getMessageCount());

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

		Collection contents = repository.getContentByProvider(PROVIDER_ID, true);
		assertTrue(contents.size() == 0);

		// List<String> contentUrls = new ArrayList<String>(
		// getMailContentCreator().getMailServerURLs());

		mscp.doSynchonization();

		contents = repository.getContentByProvider(PROVIDER_ID, true);

		for (Object contentObject : contents) {
			Content content = (Content) contentObject;
			if (!content.getType().equalsIgnoreCase("EMAIL")) {
				String fulltext = content.getFulltext();
				assertNotNull(fulltext);
				assertTrue(fulltext.trim().length() > 0);
			} else {
				Attribute attribute = content
						.getAttributeByName("MESSAGE_FOLDER");
				assertEquals("INBOX", attribute.getValue());
			}

		}

		int expected = MAIL_NUMBER + ATTACHMENT_FILES + (MAIL_NUMBER / 2 - 1);
		assertEquals(expected, contents.size());

	}

	public void testDoHousekeeping() throws IQserTechnicalException,
			MessagingException, IOException {

		Repository repository = Configuration.getConfiguration()
				.getServiceLocator().getRepository();

		assertEquals(mockMailsIds.size(), 4);
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

		assertEquals(folder.getMessageCount(), 11);
		MailServerUtils msu = new MailServerUtils();
		Message message = msu.identifyMessage(folder, mailWithAttachments);
		message.setFlag(Flag.DELETED, true);
		folder.close(true);
		folder = connectMockupServer("true");
		assertEquals(folder.getMessageCount(), 10);

		mscp.doHousekeeping();

		folder = connectMockupServer("true");
		assertEquals(10, folder.getMessageCount());
		existingContents = repository.getAllContentItem(-1);
		assertEquals(2, existingContents.size());
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
		mc.setPort(143);
		mc.setCache(cache);

		return mc;
	}
}
