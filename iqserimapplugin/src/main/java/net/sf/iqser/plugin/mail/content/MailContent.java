package net.sf.iqser.plugin.mail.content;

import java.util.ArrayList;
import java.util.Collection;

import com.iqser.core.model.Content;

public class MailContent {

	
	private Content content;
	
	private Collection<Content> attachmentContents = new ArrayList<Content>();

	public void setContent(Content content) {
		this.content = content;
	}

	public Content getContent() {
		return content;
	}

	public void setAttachmentContents(Collection<Content> attachmentContents) {
		this.attachmentContents = attachmentContents;
	}

	public Collection<Content> getAttachmentContents() {
		return attachmentContents;
	}
	
	public void addAttachmentContent(Content messageContent){
		attachmentContents.add(messageContent);
	}
	
		
}
