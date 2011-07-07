package net.sf.iqser.plugin.mail.test;

import com.iqser.core.analyzer.AnalyzerTaskStarter;
import com.iqser.core.analyzer.ContentAnalyzer;
import com.iqser.core.category.CategoryBuilder;
import com.iqser.core.category.CategoryManager;
import com.iqser.core.client.ClientFacade;
import com.iqser.core.event.EventPublisher;
import com.iqser.core.index.Index;
import com.iqser.core.locator.ServiceLocator;
import com.iqser.core.plugin.ContentProvider;
import com.iqser.core.plugin.ContentProviderFacade;
import com.iqser.core.plugin.PluginManager;
import com.iqser.core.repository.Repository;
import com.iqser.core.tracker.Tracker;

/**
 * Supports testing of classes of the iQser Web Content Provider Family.
 * 
 * @author Jšrg Wurzer
 * 
 */
public class TestServiceLocator implements ServiceLocator {

	private static final String FILESYSTEM_CONTENT_PROVIDER = "FILESYSTEM_PROVIDER";

	/** Repository mockup for testing */
	private Repository rep = null;

	/** AnalyzerTaskStarter mpckup for testing */
	private AnalyzerTaskStarter ats = null;	
	
	/** ContentProviderFacade for testing */
	private ContentProviderFacade facade = null;
	
	
	public void setAnalyzerTaskStarter(AnalyzerTaskStarter arg0) {
		ats = arg0;
	}
	
	public AnalyzerTaskStarter getAnalyzerTaskStarter() {
		return ats;
	}
	
	public void setFacade(ContentProviderFacade facade) {
		this.facade = facade;
	}	

	public CategoryBuilder getCategoryBuilder() {
		
		return null;
	}

	public CategoryManager getCategoryManager() {
		
		return null;
	}

	public ClientFacade getClientFacade() {
		
		return null;
	}

	public ContentAnalyzer getContentAnalyzer() {
		
		return null;
	}	

	public ContentProvider getContentProvider(String arg0) {		
		return null;
	}

	public EventPublisher getEventPublisher() {
		
		return null;
	}

	public Index getIndex() {
		
		return null;
	}

	public PluginManager getPluginManager() {
		
		return null;
	}

	public void setRepository(Repository mockup) {
		rep = mockup;
	}

	public Repository getRepository() {
		return rep;
	}

	public SecurityManager getSecurityManager() {		
		return null;
	}

	public Tracker getTracker() {		
		return null;
	}

	public ContentProviderFacade getContentProviderFacade() {
		return facade;
	}

}
