package net.sf.iqser.plugin.mail.test;

import java.util.Collection;

import com.iqser.core.config.ServiceLocatorFactory;
import com.iqser.core.exception.IQserException;
import com.iqser.core.model.Content;
import com.iqser.core.plugin.provider.ContentProviderFacade;
import com.iqser.core.repository.RepositoryReader;
import com.iqser.core.repository.RepositoryWriter;

public class MockContentProviderFacade implements ContentProviderFacade {
	RepositoryWriter repo = ServiceLocatorFactory.getServiceLocator()
			.getRepositoryWriter();
	RepositoryReader repoReader = ServiceLocatorFactory.getServiceLocator()
			.getRepositoryReader();

	public void addContent(Content content) throws IQserException {
		repo.addOrUpdateContent(content);
	}

	public Collection<Content> getExistingContents(String providerId)
			throws IQserException {
		return (Collection<Content>) repoReader.getContentByProvider(
				providerId, false);
	}

	public boolean isExistingContent(String providerId, String url)
			throws IQserException {
		Collection<Content> existingContents = getExistingContents(providerId);
		for (Content content : existingContents) {
			if (url.equalsIgnoreCase(content.getContentUrl())) {
				return true;
			}
		}
		return false;
	}

	public void removeContent(String providerId, String url)
			throws IQserException {
		Collection<Content> existingContents = getExistingContents(providerId);
		for (Content content : existingContents) {
			if (url.equalsIgnoreCase(content.getContentUrl())) {
				repo.deleteContent(content);
			}
		}
	}

	public void updateContent(Content content) throws IQserException {
		repo.addOrUpdateContent(content);
	}

	@Override
	public Content getExistingContent(String contentProviderId,
			String contentUrl) throws IQserException {
		return repoReader.getContent(contentProviderId, contentUrl);
	}
}
