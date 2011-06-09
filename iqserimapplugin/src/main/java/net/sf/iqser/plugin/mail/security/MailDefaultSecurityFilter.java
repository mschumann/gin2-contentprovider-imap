package net.sf.iqser.plugin.mail.security;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.iqser.core.model.Attribute;
import com.iqser.core.model.Content;
import com.iqser.core.plugin.security.IQserSecurityException;
import com.iqser.core.plugin.security.SecurityFilter;

/**
 * Default security filter for MailContentProvider.
 * 
 * Each content should have an attribute that specifies the content owner. The user name is matched against this attribute.
 * The name of the attribute can be configured in "imap.security.properties" file that must be placed in the root of the classpath.
 * 		ownerAttribute=owner
 * Default name for the owner attribute is "owner"
 * 
 * @author robert.baban
 *
 */
public class MailDefaultSecurityFilter implements SecurityFilter {

	private static Logger logger = Logger.getLogger(MailDefaultSecurityFilter.class);
	
	private String ownerAttribute;

	/**
	 * Reads initialization parameters from imap.security.properties file.
	 */
	public MailDefaultSecurityFilter(){
		
		ownerAttribute = "owner";
		
		try {
			InputStream in = getClass().getResourceAsStream("imap.security.properties");
			if (in!=null){
				Properties prop = new Properties();
				prop.load(in);			
				ownerAttribute = prop.getProperty("owner.attribute", "owner");
			}else{
				logger.error("Property file is missing: imap.security.properties");
			}
		} catch (IOException e) {
			logger.error("Could not read property file: imap.security.properties"); 
		}
	}
	
	/**
	 * Method that checks read permission.
	 * 
	 * @param user the user.
	 * @param password the password.
	 * @param content the content.
	 * @return true if the user is allowed to edit the content, false otherwise.
	 * @throws IQserSecurityException @see SecurityFilter
	 */
	public boolean canRead(String user, String password, Content content)
			throws IQserSecurityException {		
		return isContentOwner(user, content);
	}

	/**
	 * Method that checks edit permission.
	 * 
	 * @param user the user.
	 * @param password the password.
	 * @param content the content.
	 * @return true if the user is allowed to edit the content, false otherwise.
	 * @throws IQserSecurityException @see SecurityFilter
	 */
	public boolean canEdit(String user, String password, Content content)
			throws IQserSecurityException {
		return isContentOwner(user, content);
	}
	
	/**
	 * Method that checks execute action permission.
	 * 
	 * @param user the user.
	 * @param password the password.
	 * @param action name of the action.
	 * @param content the content.
	 * @return true if the user is allowed to execute the action on the given content, false otherwise.
	 * @throws IQserSecurityException @see SecurityFilter
	 */
	public boolean canExecuteAction(String user, String password, String action,
			Content content) throws IQserSecurityException {
		return isContentOwner(user, content);
	}

	private boolean isContentOwner(String user, Content content) {
		
		if (user == null) return false;
		
		Attribute a = content.getAttributeByName(ownerAttribute);
		
		if( a != null){
			return user.equalsIgnoreCase(a.getValue());
		}
		
		return false;
	}	

}
