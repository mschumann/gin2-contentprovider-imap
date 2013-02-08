package net.sf.iqser.plugin.mail.security;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.iqser.core.config.ServiceLocatorFactory;
import com.iqser.core.exception.IQserException;
import com.iqser.core.model.Attribute;
import com.iqser.core.model.Content;
import com.iqser.core.model.Result;
import com.iqser.core.plugin.AbstractPlugin;
import com.iqser.core.plugin.security.IQserSecurityException;
import com.iqser.core.plugin.security.SecurityFilter;

/**
 * Default security filter for MailContentProvider.
 * 
 * Each content should have an attribute that specifies the content owner. The
 * user name is matched against this attribute. The name of the attribute can be
 * configured in "imap.security.properties" file that must be placed in the root
 * of the classpath. ownerAttribute=owner Default name for the owner attribute
 * is "owner"
 * 
 * @author robert.baban
 * 
 */
public class MailDefaultSecurityFilter extends AbstractPlugin implements
		SecurityFilter {

	private static Logger logger = Logger
			.getLogger(MailDefaultSecurityFilter.class);

	private String ownerAttribute;

	/**
	 * Reads initialization parameters from imap.security.properties file.
	 */
	public MailDefaultSecurityFilter() {

		ownerAttribute = "owner";

		try {
			InputStream in = getClass().getResourceAsStream(
					"imap.security.properties");
			if (in != null) {
				Properties prop = new Properties();
				prop.load(in);
				ownerAttribute = prop.getProperty("owner.attribute", "owner");
			} else {
				logger.error("Property file is missing: imap.security.properties");
			}
		} catch (IOException e) {
			logger.error("Could not read property file: imap.security.properties");
		}
	}

	private boolean isContentOwner(String user, Content content) {
		if (user == null || content == null) {
			return false;
		}

		Attribute a = content.getAttributeByName(ownerAttribute);

		if (a != null) {
			return user.equalsIgnoreCase(a.getValue());
		}
		return false;
	}

	private boolean isContentOwner(String userId, long contentId) {
		Content content = null;
		try {
			content = ServiceLocatorFactory.getServiceLocator()
					.getRepositoryReader().getContent(contentId);
		} catch (IQserException e) {
			logger.error(String
					.format("Error while checking if the user %s is the owner of the content object (contentId: %s).",
							userId, contentId));
		}
		return isContentOwner(userId, content);
	}

	@Override
	public boolean canExecuteAction(String userId, String password,
			String action, long contentId) throws IQserSecurityException {
		return isContentOwner(userId, contentId);
	}

	@Override
	public boolean canRead(String userId, String password, long contentId)
			throws IQserSecurityException {
		return isContentOwner(userId, contentId);
	}

	@Override
	public Collection<Content> filterReadableContent(String userId,
			String password, Collection<Content> contentObjects)
			throws IQserSecurityException {
		Iterator<Content> contentIterator = contentObjects.iterator();
		while(contentIterator.hasNext()) {
			Content content = contentIterator.next();
			if(!isContentOwner(userId, content)) {
				contentIterator.remove();
			}
		}
		return contentObjects;
	}

	@Override
	public Collection<Result> filterReadableResult(String userId,
			String password, Collection<Result> resultObjects)
			throws IQserSecurityException {
		Iterator<Result> resultIterator = resultObjects.iterator();
		while(resultIterator.hasNext()) {
			Result result = resultIterator.next();
			if(!isContentOwner(userId, result.getContentId())) {
				resultIterator.remove();
			}
		}
		return resultObjects;
	}
}
