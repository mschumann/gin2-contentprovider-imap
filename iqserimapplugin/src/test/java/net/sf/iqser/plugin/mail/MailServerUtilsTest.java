package net.sf.iqser.plugin.mail;

import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Store;

import com.iqser.core.exception.IQserRuntimeException;

import junit.framework.TestCase;

public class MailServerUtilsTest extends TestCase {

	public void testIdentifyMessageWithException() {
		
		MailServerUtils msu = new MailServerUtils();
		msu.setUserName("test");
		msu.setPassWord("test");
		msu.setMailServer("localhost");
		msu.setSslPort("993");
		msu.setPort(143);
		
		assertEquals("test", msu.getUserName());
		assertEquals("test", msu.getPassWord());
		assertEquals("localhost", msu.getMailServer());
		assertEquals("993", msu.getSslPort());
		assertEquals(143, msu.getPort());

		Store store = msu.attemptConnectionMailServer("false");
		
		try{
			Folder folder = store.getFolder("noFolder");
			String mailID = "mailID";
			
			msu.identifyMessage(folder, mailID);
			
			fail("Should throw IQserRuntimeException");
		}catch(MessagingException e){
			fail("Should throw IQserRuntimeException");
		}catch(IQserRuntimeException iqre){
			
		}
	}	

}
