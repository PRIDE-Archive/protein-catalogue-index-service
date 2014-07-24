package uk.ac.ebi.pride.proteinindex.search.tools;

import org.apache.solr.client.solrj.SolrServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;
import uk.ac.ebi.pride.proteinindex.search.indexers.ProteinDetailsIndexer;
import uk.ac.ebi.pride.proteinindex.search.model.ProteinIdentified;
import uk.ac.ebi.pride.proteinindex.search.search.service.ProteinIdentificationIndexService;
import uk.ac.ebi.pride.proteinindex.search.search.service.ProteinIdentificationSearchService;
import uk.ac.ebi.pride.proteinindex.search.synonyms.ProteinAccessionSynonymsFinder;
import uk.ac.ebi.pride.proteinindex.search.util.ProteinBuilder;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Jose A. Dianes
 * @version $Id$
 */
@Component
public class ProteinSynonymsUpdater {

    private static Logger logger = LoggerFactory.getLogger(ProteinSynonymsUpdater.class.getName());

    @Autowired
    private ProteinIdentificationSearchService proteinIdentificationSearchService;

    @Autowired
    private ProteinIdentificationIndexService proteinIdentificationIndexService;

    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("spring/app-context.xml");

        ProteinSynonymsUpdater proteinSynonymsUpdater = context.getBean(ProteinSynonymsUpdater.class);

        addSynonymsToExistingProteins(proteinSynonymsUpdater);

    }

    private static void addSynonymsToExistingProteins(ProteinSynonymsUpdater proteinSynonymsUpdater) {

        // create the indexer
        ProteinDetailsIndexer proteinDetailsIndexer = new ProteinDetailsIndexer(proteinSynonymsUpdater.proteinIdentificationSearchService, proteinSynonymsUpdater.proteinIdentificationIndexService);
        // update all
        proteinDetailsIndexer.addSynonymsToAllExistingProteins();


    }


}
