package uk.ac.ebi.pride.proteinindex.search.tools;

import org.apache.solr.client.solrj.SolrServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

/**
 * @author Jose A. Dianes
 * @version $Id$
 */
@Component
public class AccessionSynonymsBuilder {

    private static Logger logger = LoggerFactory.getLogger(AccessionSynonymsBuilder.class.getName());

    @Autowired
    private SolrServer solrProteinServer;

    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("spring/app-context.xml");

        AccessionSynonymsBuilder accessionSynonymsBuilder = context.getBean(AccessionSynonymsBuilder.class);

        indexSynonyms(accessionSynonymsBuilder, accessionSynonymsBuilder.solrProteinServer);

    }

    private static void indexSynonyms(AccessionSynonymsBuilder accessionSynonymsBuilder, SolrServer solrProteinServer) {

    }

}
