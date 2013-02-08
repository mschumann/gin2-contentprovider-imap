package net.sf.iqser.plugin.mail.security;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import com.iqser.core.exception.IQserException;
import com.iqser.core.model.Attribute;
import com.iqser.core.model.Content;
import com.iqser.core.plugin.security.SecurityFilter;
import com.iqser.gin.developer.test.plugin.PluginTestCase;

import junit.framework.Assert;

public class MailDefaultSecurityFilterTest extends PluginTestCase {

	private Content c1, c2;
	private SecurityFilter mailSf;	
	
	@Before
	public void setUp(){
		mailSf = new MailDefaultSecurityFilter();
		
		c1 = new Content();
		c1.setContentId(1);
		c1.addAttribute(new Attribute("owner","john",Attribute.ATTRIBUTE_TYPE_TEXT, false));
		
		c2 = new Content();
		c2.setContentId(2);
		c2.addAttribute(new Attribute("owner","mary",Attribute.ATTRIBUTE_TYPE_TEXT, false));

	}
	
	@Test
	public void testCanRead() throws IQserException{		
		EasyMock.expect(repositoryReaderMock.getContent(c1.getContentId())).andReturn(c1).times(2);
		EasyMock.expect(repositoryReaderMock.getContent(c2.getContentId())).andReturn(c2);
		
		prepare();
		
		Assert.assertTrue(mailSf.canRead("john", "secret", c1.getContentId()));
		Assert.assertFalse(mailSf.canRead("john", "secret", c2.getContentId()));
		Assert.assertFalse(mailSf.canRead(null, "secret", c1.getContentId()));
		
		verify();
	}

	@Test
	public void testCanExecute() throws IQserException{		
		EasyMock.expect(repositoryReaderMock.getContent(c1.getContentId())).andReturn(c1).times(2);
		EasyMock.expect(repositoryReaderMock.getContent(c2.getContentId())).andReturn(c2);
		
		prepare();
		
		Assert.assertTrue(mailSf.canExecuteAction("john", "secret", "action", c1.getContentId()));
		Assert.assertFalse(mailSf.canExecuteAction("john", "secret", "action", c2.getContentId()));
		Assert.assertFalse(mailSf.canExecuteAction(null, "secret", "action", c1.getContentId()));
		
		verify();
	}
}
