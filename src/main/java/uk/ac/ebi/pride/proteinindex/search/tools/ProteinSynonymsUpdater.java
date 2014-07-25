package uk.ac.ebi.pride.proteinindex.search.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;
import uk.ac.ebi.pride.proteinindex.search.indexers.ProteinDetailsIndexer;
import uk.ac.ebi.pride.proteinindex.search.search.service.ProteinIdentificationIndexService;
import uk.ac.ebi.pride.proteinindex.search.search.service.ProteinIdentificationSearchService;

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

        if ("all".equals(args[0])) {
            addSynonymsToAllExistingProteins(proteinSynonymsUpdater);
        } else if ("inc".equals(args[0])) {
            addSynonymsToProteinsWithNoSynonyms(proteinSynonymsUpdater);
        }
    }

    private static void addSynonymsToAllExistingProteins(ProteinSynonymsUpdater proteinSynonymsUpdater) {
        System.out.println("Starting application...");
        // create the indexer
        logger.info("Creating protein details indexer...");
        ProteinDetailsIndexer proteinDetailsIndexer = new ProteinDetailsIndexer(proteinSynonymsUpdater.proteinIdentificationSearchService, proteinSynonymsUpdater.proteinIdentificationIndexService);
        logger.info("Protein details indexer created!");
        // update all
        logger.info("Starting update process...");
        proteinDetailsIndexer.addSynonymsToAllExistingProteins();
        logger.info("Update process completed!");
    }

    private static void addSynonymsToProteinsWithNoSynonyms(ProteinSynonymsUpdater proteinSynonymsUpdater) {
        System.out.println("Starting application...");
        // create the indexer
        logger.info("Creating protein details indexer...");
        ProteinDetailsIndexer proteinDetailsIndexer = new ProteinDetailsIndexer(proteinSynonymsUpdater.proteinIdentificationSearchService, proteinSynonymsUpdater.proteinIdentificationIndexService);
        logger.info("Protein details indexer created!");
        // update all
        logger.info("Starting update process...");
        proteinDetailsIndexer.addSynonymsToProteinsWithNoSynonyms();
        logger.info("Update process completed!");
    }



}
