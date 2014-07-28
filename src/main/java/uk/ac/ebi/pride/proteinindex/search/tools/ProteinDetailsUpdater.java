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
public class ProteinDetailsUpdater {

    private static Logger logger = LoggerFactory.getLogger(ProteinDetailsUpdater.class.getName());

    @Autowired
    private ProteinIdentificationSearchService proteinIdentificationSearchService;

    @Autowired
    private ProteinIdentificationIndexService proteinIdentificationIndexService;

    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("spring/app-context.xml");

        ProteinDetailsUpdater proteinDetailsUpdater = context.getBean(ProteinDetailsUpdater.class);

        if ("all".equals(args[0])) {
            addDetailsToAllExistingProteins(proteinDetailsUpdater);
        } else if ("inc".equals(args[0])) {
            addDetailsToProteinsWithNoDetails(proteinDetailsUpdater);
        }


    }

    private static void addDetailsToAllExistingProteins(ProteinDetailsUpdater proteinDetailsUpdater) {
        System.out.println("Starting application...");
        // create the indexer
        logger.info("Creating protein details indexer...");
        ProteinDetailsIndexer proteinDetailsIndexer = new ProteinDetailsIndexer(proteinDetailsUpdater.proteinIdentificationSearchService, proteinDetailsUpdater.proteinIdentificationIndexService);
        logger.info("Protein details indexer created!");
        // update all
        logger.info("Starting update process...");
        proteinDetailsIndexer.addDetailsToAllExistingProteins();
        logger.info("Update process completed!");
    }

    private static void addDetailsToProteinsWithNoDetails(ProteinDetailsUpdater proteinDetailsUpdater) {
        System.out.println("Starting application...");
        // create the indexer
        logger.info("Creating protein details indexer...");
        ProteinDetailsIndexer proteinDetailsIndexer = new ProteinDetailsIndexer(proteinDetailsUpdater.proteinIdentificationSearchService, proteinDetailsUpdater.proteinIdentificationIndexService);
        logger.info("Protein details indexer created!");
        // update all
        logger.info("Starting update process...");
        proteinDetailsIndexer.addDetailsToProteinsWithNoDetails();
        logger.info("Update process completed!");
    }

}
