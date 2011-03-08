package net.sf.iqser.plugin.mail;

import java.util.Properties;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.search.HeaderTerm;

import org.apache.log4j.Logger;

import com.iqser.core.exception.IQserRuntimeException;

/**
 * connection and query of the mail server
 * @author alexandru.galos
 *
 */
public class MailServerUtils {

	private String mailServer;
	private String userName;
	private String passWord;
	private String sslPort;

	private Store store;
	private static Logger logger = Logger.getLogger(MailServerUtils.class);

	/**
	 * identify a mail using the id from a specified folder
	 * @param folder - the folder from where the message is queried
	 * @param mailID - the mail id
	 * @return - the mail message
	 * @throws MessagingException
	 */
	public Message identifyMessage(Folder folder, String mailID)
			throws MessagingException {

		if (!folder.exists())
			throw new IQserRuntimeException("Folder does not exist");
		if (!folder.isOpen())
			folder.open(Folder.READ_ONLY);

		Message[] messages = folder
				.search(new HeaderTerm("MESSAGE-ID", mailID));

		if (messages.length != 1) {
			logger.fatal("several messages have the same id");
			throw new IQserRuntimeException("Several mails identified");
		} else
			return messages[0];
	}

	/**
	 * attempt to establish a secured or non-secured connection
	 * between a client and the mail server---the connection is performed 3 times
	 * @param sslConnection - flag (string) for checking if the connection
	 * is secured of not
	 * @return - return a store of the mail server
	 */
	public Store attemptConnectionMailServer(String sslConnection) {

		// if connection failure try to connect 3 times
		int count = 0;
		boolean isConnected = false;

		if (store == null || !store.isConnected()) {
			Throwable throwable = null;
			while (!isConnected && count < 3) {
				try {
					store = connectMailServer(sslConnection);
					isConnected = store.isConnected();
				} catch (Exception e) {
					throwable = e;
					try {
						// wait 1 sec
						Thread.sleep(1000);
					} catch (InterruptedException e1) {
						throw new IQserRuntimeException(e1);
					}
				} finally {
					count++;
				}
			}
			if (count == 3 && !isConnected) {
				throwable.printStackTrace();
				throw new IQserRuntimeException(throwable);
			}
		}
		return store;
	}

	/**
	 * establish 1 connection the mail server without error detection and 
	 * retrial
	 * @param sslConnection - the type of the connection (secured or not)
	 * @return - return a store of the mail server
	 */
	private Store connectMailServer(String sslConnection) {

		// connect to mail server
		Store store;
		try {
			Properties props = System.getProperties();
			props.setProperty("mail.store.protocol", "imap");
			if (sslConnection.equalsIgnoreCase("true")) {
				props.setProperty("mail.imap.socketFactory.class",
						"javax.net.ssl.SSLSocketFactory");
				props.setProperty("mail.imap.socketFactory.fallback", "false");
				props.setProperty("mail.imap.socketFactory.port", sslPort);
			}
			Session session = Session.getDefaultInstance(props);
			store = session.getStore("imap");

			store.connect(mailServer, userName, passWord);

		} catch (NoSuchProviderException e) {
			throw new IQserRuntimeException(e);
		} catch (MessagingException e) {
			throw new IQserRuntimeException(e);
		}
		return store;
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

	public void setSslPort(String sslPort) {
		this.sslPort = sslPort;
	}

	public String getSslPort() {
		return sslPort;
	}
}
