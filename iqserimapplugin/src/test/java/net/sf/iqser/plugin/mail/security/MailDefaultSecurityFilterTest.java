package net.sf.iqser.plugin.mail.security;

import com.iqser.core.model.Attribute;
import com.iqser.core.model.Content;
import com.iqser.core.plugin.security.IQserSecurityException;
import com.iqser.core.plugin.security.SecurityFilter;

import junit.framework.TestCase;

public class MailDefaultSecurityFilterTest extends TestCase{

	private Content c1, c2;
	private SecurityFilter mailSf;	
	
	protected void setUp(){
		mailSf = new MailDefaultSecurityFilter();
		
		c1 = new Content();
		c1.addAttribute(new Attribute("owner","john",Attribute.ATTRIBUTE_TYPE_TEXT, false));
		
		c2 = new Content();
		c2.addAttribute(new Attribute("owner","mary",Attribute.ATTRIBUTE_TYPE_TEXT, false));

	}
	
	public void testCanRead() throws IQserSecurityException{						
		assertTrue(mailSf.canRead("john", "secret", c1));
		assertFalse(mailSf.canRead("john", "secret", c2));
		assertFalse(mailSf.canRead(null, "secret", c1));
	}
	
	public void testCanEdit()throws IQserSecurityException{
		assertTrue(mailSf.canEdit("john", "secret", c1));
		assertFalse(mailSf.canEdit("john", "secret", c2));
		assertFalse(mailSf.canEdit(null, "secret", c1));
	}
	
	public void testCanExecute() throws IQserSecurityException{
		assertTrue(mailSf.canExecuteAction("john", "secret", "action", c1));
		assertFalse(mailSf.canExecuteAction("john", "secret", "action", c2));
		assertFalse(mailSf.canExecuteAction(null, "secret", "action", c1));
	}
}
