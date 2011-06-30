package net.sf.iqser.plugin.mail.test;

import java.util.Collection;

import com.iqser.core.exception.IQserException;
import com.iqser.core.model.Content;
import com.iqser.core.plugin.ContentProviderFacade;
import com.iqser.core.repository.Repository;

public class MockContentProviderFacade implements ContentProviderFacade{

	private Repository repo;		
		
	public void setRepo(Repository repo) {
		this.repo = repo;
	}	
	
	public void addContent(Content content) throws IQserException {
		repo.addContent(content);		
	}

	public Collection<Content> getExistingContents(String providerId)
			throws IQserException {
			return (Collection<Content>)repo.getContentByProvider(providerId, false);		
	}

	public boolean isExistingContent(String providerId, String url)
			throws IQserException {
		Collection<Content> existingContents = getExistingContents(providerId);
		for (Content content : existingContents) {
			if (url.equalsIgnoreCase(content.getContentUrl())){
				return true;
			}
		}		
		return false;
	}

	public void removeContent(String providerId, String url) throws IQserException {
		Collection<Content> existingContents = getExistingContents(providerId);
		for (Content content : existingContents) {
			if (url.equalsIgnoreCase(content.getContentUrl())){
				repo.deleteContent(content);
			}
		}		
	}

	public void updateContent(Content content) throws IQserException {
		repo.updateContent(content);		
	}

	

}
