package uk.ac.ebi.pride.proteincatalogindex.search.service.repository;

import org.springframework.data.solr.core.SolrOperations;
import org.springframework.data.solr.repository.support.SolrRepositoryFactory;

/**
 * @author Jose A. Dianes
 * @version $Id$
 */
public class SolrProteinCatalogRepositoryFactory {

    private org.springframework.data.solr.core.SolrOperations solrOperations;

    public SolrProteinCatalogRepositoryFactory(SolrOperations solrOperations) {
        this.solrOperations = solrOperations;
    }

    public SolrProteinCatalogRepository create() {
        return new SolrRepositoryFactory(this.solrOperations).getRepository(SolrProteinCatalogRepository.class);
    }

}
