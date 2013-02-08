package net.sf.iqser.plugin.mail.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.iqser.core.exception.IQserException;
import com.iqser.core.model.Concept;
import com.iqser.core.model.Content;
import com.iqser.core.model.ContentConceptTuple;
import com.iqser.core.model.ContentCooccurrenceTuple;
import com.iqser.core.model.Cooccurrence;
import com.iqser.core.model.Relation;
import com.iqser.core.model.RelationEqualizer;
import com.iqser.core.model.Result;
import com.iqser.core.model.Statement;
import com.iqser.core.model.UsageTrackerItem;
import com.iqser.core.repository.RepositoryReader;
import com.iqser.core.repository.RepositoryWriter;

/**
 * Supports testing of classes of the iQser Web Content Provider Family.
 * 
 * @author J�rg Wurzer
 *
 */
public class MockRepository implements RepositoryReader, RepositoryWriter {
	
	private ArrayList<Content> cl = new ArrayList<Content>();
		
	public static final int ADD_CONTENT = 0;
	
	public static final int UPDATE_CONTENT = 1;
	
	public static final int DELETE_CONTENT = 2;
	
	private static Logger logger = Logger.getLogger( MockRepository.class );
		
	@Override
	public boolean addOrUpdateContent(Content c) throws IQserException {
		logger.debug("addOrUpdateContent() called for content " + c.getContentUrl());
		
		boolean isUpdate = false;
		
		Iterator<Content> iter = cl.iterator();
		
		while (iter.hasNext()) {
			Content oldc = (Content)iter.next();
			
			if (oldc.getContentUrl().equals(c.getContentUrl()) && 
					oldc.getProvider().equals(c.getProvider())) {
				iter.remove();
				isUpdate = true;
				break;
			}
		}
			
		cl.add(c);
		
		return isUpdate;
	}
	
	@Override
	public boolean contains(String url, String provider)
			throws IQserException {
		logger.debug("contains() called for " + url + " and " + provider);
		
		Iterator<Content> iter = cl.iterator();
		Content c = null;
		
		while (iter.hasNext()) {
			c = (Content)iter.next();
			if (c.getContentUrl().equals(url) && c.getProvider().equals(provider))
					return true;
		}
		
		return false;
	}
	
	@Override
	public void deleteContent(Content c) throws IQserException {
		logger.debug("deleteContent() called for " + c.getContentUrl());
		
		Iterator<Content> iter = cl.iterator();
		boolean removed = false;
		Content oldc = null;
		
		while (iter.hasNext()) {
			oldc = (Content)iter.next();
			
			if (oldc.getContentUrl().equals(c.getContentUrl()) && 
					oldc.getProvider().equals(c.getProvider())) {
				removed = true;
				break;
			}
		}
		
		if (removed)
			cl.remove(oldc);
		else
			throw new IQserException(
					"Old content object was not found", IQserException.SEVERITY_ERROR);
	}
	
	@Override
	public Content getContent(String url, String provider)
			throws IQserException {
		logger.debug("getContent() called for " + url + " and " + provider);
		
		Iterator<Content> iter = cl.iterator();
		Content c = null;
		
		while (iter.hasNext()) {
			c = (Content)iter.next();
			if (c.getContentUrl().equals(url) && c.getProvider().equals(provider))
					return c;
		}
		
		if (c == null)
			throw new IQserException(
					"Content object was not found", IQserException.SEVERITY_ERROR);
		
		return null;
	}

	@Override
	public Collection<Content> getContentByProvider(String provider, boolean complete)
			throws IQserException {
		logger.debug("getContentByProvider() called for " + provider);
		
		Iterator<Content> iter = cl.iterator();
		ArrayList<Content> out = new ArrayList<Content>();
		
		while (iter.hasNext()) {
			Content c = (Content)iter.next();
			if (c.getProvider().equals(provider))
				out.add(c);
		}
		
		return out;
	}

	@Override
	public void addOrUpdateContentConceptTuple(long contentId,
			String conceptName, double significance, int frequency)
			throws IQserException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addOrUpdateContentConceptTuples(Collection<Concept> concepts,
			long contentId, int pluginInstanceId) throws IQserException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addOrUpdateContentCooccurrenceTuple(long contentId,
			String conceptName, String anotherConceptName, int frequency)
			throws IQserException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addOrUpdateContentCooccurrenceTuples(
			Collection<Cooccurrence> cooccurrences, long contentId,
			int pluginInstanceId) throws IQserException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addOrUpdateStatement(Statement statement) throws IQserException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addOrUpdateStatements(Collection<Statement> statements)
			throws IQserException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addUsageTrackerItem(UsageTrackerItem item)
			throws IQserException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteConcept(Concept concept) throws IQserException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteContentConceptTuple(
			ContentConceptTuple contentConceptTuple) throws IQserException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteContentConceptTuples(long contentId)
			throws IQserException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteContentConceptTuples(long contentId, int pluginInstanceId)
			throws IQserException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteContentConceptTuples(
			Collection<ContentConceptTuple> contentConceptTuples,
			long contentId, int pluginInstanceId) throws IQserException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteContentCooccurrenceTuple(
			ContentCooccurrenceTuple contentCooccurrenceTuple)
			throws IQserException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteContentCooccurrenceTuples(long contentId)
			throws IQserException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteContentCooccurrenceTuples(long contentId,
			int pluginInstanceId) throws IQserException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteContentCooccurrenceTuples(
			Collection<ContentCooccurrenceTuple> contentCooccurrenceTuples,
			long contentId, int pluginInstanceId) throws IQserException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteCooccurrence(Cooccurrence cooc) throws IQserException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteStatement(Statement statement) throws IQserException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteStatements(Collection<Statement> statements)
			throws IQserException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteUsageTrackerItem(UsageTrackerItem item)
			throws IQserException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void normalizeStatementWeights(long subjectId, String predicate,
			double normalizationFactor) throws IQserException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Collection<String> getAllAttributeNames() throws IQserException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Long> getAllContentIds() throws IQserException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<String> getAttributeNames(Collection<Long> contentIds)
			throws IQserException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<String> getAttributesByProvider(String providerM)
			throws IQserException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<String> getAttributesByType(String typeM)
			throws IQserException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Concept getConcept(String concept) throws IQserException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Concept getConcept(long conceptId) throws IQserException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Concept> getConcepts(long contentIDM)
			throws IQserException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Content getContent(long id) throws IQserException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Content getContent(long id, Collection<String> visibleAttributes)
			throws IQserException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Content getContent(String url, String provider, boolean complete)
			throws IQserException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ContentConceptTuple getContentConceptTuple(long contentId,
			long conceptId) throws IQserException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<ContentConceptTuple> getContentConceptTuples(
			long contentId) throws IQserException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<ContentConceptTuple> getContentConceptTuples(
			long contentId, int pluginInstanceId) throws IQserException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<ContentConceptTuple> getContentConceptTuplesByConceptId(
			long conceptId) throws IQserException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ContentCooccurrenceTuple getContentCooccurrenceTuple(long contentId,
			long firstConceptId, long secondConceptId) throws IQserException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<ContentCooccurrenceTuple> getContentCooccurrenceTuples(
			long contentId) throws IQserException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<ContentCooccurrenceTuple> getContentCooccurrenceTuples(
			long contentId, int pluginInstanceId) throws IQserException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Long> getContentIdsOfProvidersWithoutSecurityFilter()
			throws IQserException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Content> getContentOfProvidersWithSecurityFilter(
			boolean complete) throws IQserException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Content> getContentOfProvidersWithoutSecurityFilter(
			boolean complete) throws IQserException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Long> getContentRelatedToContext(
			Collection<Long> contextContentIds,
			Collection<Long> resultContentIds) throws IQserException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<String> getContentTypes() throws IQserException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Long> getContentsContainingAttribute(String attributeName)
			throws IQserException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Cooccurrence getCooccurrence(long conceptId, long anotherConceptId)
			throws IQserException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Cooccurrence getCooccurrence(String conceptName,
			String anotherConceptName) throws IQserException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Cooccurrence> getCooccurrences(String conceptName)
			throws IQserException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getDocumentFrequency(Concept concept) throws IQserException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Collection<String> getKeyAttributeNames(String contentProviderName)
			throws IQserException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Relation> getReasoningLightRelations(
			Set<Long> subjectIds, String predicate, Set<Long> objectIds)
			throws IQserException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Relation> getReasoningLightRelations(String subjectQuery,
			String predicate, String objectQuery) throws IQserException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Statement> getReasoningLightStatements(
			Set<Long> subjectIds, String predicate, Set<Long> objectIds)
			throws IQserException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Statement> getReasoningLightStatements(
			String subjectQuery, String predicate, String objectQuery)
			throws IQserException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Statement> getReasoningLightStatements(
			String subjectQuery, String predicate, String objectQuery,
			double minWeight) throws IQserException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Concept> getRelatedConcepts(long contentIDM)
			throws IQserException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Concept> getRelatedConceptsByConcept(Concept concept)
			throws IQserException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Concept> getRelatedConceptsByConcepts(
			Collection<Concept> concepts) throws IQserException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Result> getRelatedContent(Collection<Long> contentIds)
			throws IQserException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Result> getRelatedContent(long id, double minWeight)
			throws IQserException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Result> getRelatedContent(long id, double minWeight,
			RelationEqualizer relationEqualizer) throws IQserException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Relation getRelation(long contentId, long anotherContentId)
			throws IQserException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getRelationCount(long contentId) throws IQserException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Collection<Result> getResults(Long... contentIds)
			throws IQserException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Statement getStatement(long subjectContentId, String predicate,
			long objectContentId, int pluginInstanceId) throws IQserException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Statement> getStatements(long contentId)
			throws IQserException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Statement> getStatements(long contentId,
			int pluginInstanceId) throws IQserException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Statement> getStatements(long contentId,
			long anotherContentId) throws IQserException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Statement> getStatements(long subjectContentId,
			String predicate, long objectContentId, double minWeight,
			int pluginInstanceId) throws IQserException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Concept> getSubCategories(List<String> concepts)
			throws IQserException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UsageTrackerItem getUsageTrackerItem(String username, long timestamp)
			throws IQserException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isEmpty() throws IQserException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isExistingContent(long contentId) throws IQserException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isRelated(long contentId1, long contentId2)
			throws IQserException {
		// TODO Auto-generated method stub
		return false;
	}
}
