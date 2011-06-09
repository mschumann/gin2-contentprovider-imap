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

	public void setAnalyzerTaskStarter(AnalyzerTaskStarter arg0) {
		ats = arg0;
	}

	public AnalyzerTaskStarter getAnalyzerTaskStarter() {
		return ats;
	}

	public CategoryBuilder getCategoryBuilder() {
		// TODO Auto-generated method stub
		return null;
	}

	public CategoryManager getCategoryManager() {
		// TODO Auto-generated method stub
		return null;
	}

	public ClientFacade getClientFacade() {
		// TODO Auto-generated method stub
		return null;
	}

	public ContentAnalyzer getContentAnalyzer() {
		// TODO Auto-generated method stub
		return null;
	}	

	public ContentProvider getContentProvider(String arg0) {		
		return null;
	}

	public EventPublisher getEventPublisher() {
		// TODO Auto-generated method stub
		return null;
	}

	public Index getIndex() {
		// TODO Auto-generated method stub
		return null;
	}

	public PluginManager getPluginManager() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setRepository(Repository mockup) {
		rep = mockup;
	}

	public Repository getRepository() {
		return rep;
	}

	public SecurityManager getSecurityManager() {
		// TODO Auto-generated method stub
		return null;
	}

	public Tracker getTracker() {
		// TODO Auto-generated method stub
		return null;
	}

}
