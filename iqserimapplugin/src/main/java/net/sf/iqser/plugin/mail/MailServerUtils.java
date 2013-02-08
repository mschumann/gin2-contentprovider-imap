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
 * connection and query of the mail server.
 * 
 * @author alexandru.galos
 * 
 */
public class MailServerUtils {

	/**
	 * the name of the mail server.
	 */
	private String mailServer;

	/**
	 * the name of the user.
	 */
	private String userName;

	/**
	 * the password.
	 */
	private String password;

	/**
	 * the ssl port.
	 */
	private String sslPort;

	/**
	 * the store.
	 */
	private Store store;

	/**
	 * the port.
	 */
	private int port;

	/**
	 * logger.
	 */
	private static Logger logger = Logger.getLogger(MailServerUtils.class);

	/**
	 * identify a mail using the id from a specified folder.
	 * 
	 * @param folder
	 *            - the folder from where the message is queried
	 * @param mailID
	 *            - the mail id
	 * @return - the mail message
	 * @throws MessagingException
	 *             exception that may appear when the message is created
	 */
	public Message identifyMessage(Folder folder, String mailID)
			throws MessagingException {

		if (!folder.exists())
			throw new IQserRuntimeException("Folder does not exist");
		if (!folder.isOpen())
			folder.open(Folder.READ_ONLY);

		Message[] messages = folder
				.search(new HeaderTerm("MESSAGE-ID", mailID));

		if (messages.length == 0) {
			logger.fatal("no messages with specified id identified");
			throw new IQserRuntimeException(
					"no messages with specified id identified");
		} else
			return messages[0];
	}

	/**
	 * attempt to establish a secured or non-secured connection. between a
	 * client and the mail server---the connection is performed 3 times.
	 * 
	 * @param sslConnection
	 *            - flag (string which is true or false) for checking if the
	 *            connection is secured of not
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
	 * re-trial.
	 * 
	 * @param sslConnection
	 *            - the type of the connection (secured or not)
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

			if (port != 0)
				store.connect(mailServer, port, userName, password);
			else
				store.connect(mailServer, userName, password);

		} catch (NoSuchProviderException e) {
			throw new IQserRuntimeException(e);
		} catch (MessagingException e) {
			throw new IQserRuntimeException(e);
		}
		return store;
	}

	/**
	 * gets the name of the server.
	 * 
	 * @return the name of the server
	 */
	public String getMailServer() {
		return mailServer;
	}

	/**
	 * sets the name of the server.
	 * 
	 * @param mailServer
	 *            the name of the server
	 */
	public void setMailServer(String mailServer) {
		this.mailServer = mailServer;
	}

	/**
	 * gets the user name.
	 * 
	 * @return the user name
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * sets the username of the server.
	 * 
	 * @param userName
	 *            the name of the user of the server
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	/**
	 * gets the password.
	 * 
	 * @return the password of the user of the server
	 */
	public String getPassWord() {
		return password;
	}

	/**
	 * sets the password.
	 * 
	 * @param password
	 *            the password that can be used on the server
	 */
	public void setPassWord(String password) {
		this.password = password;
	}

	/**
	 * sets the secured port.
	 * 
	 * @param sslPort
	 *            the secured port (993)
	 */
	public void setSslPort(String sslPort) {
		this.sslPort = sslPort;
	}

	/**
	 * gets the secured port (993).
	 * 
	 * @return the secured port (993)
	 */
	public String getSslPort() {
		return sslPort;
	}

	/**
	 * sets the default port (143 for imap).
	 * 
	 * @param port
	 *            the port for imap (default 143)
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * gets the port (default 143).
	 * 
	 * @return the port (default 143)
	 */
	public int getPort() {
		return port;
	}
}
