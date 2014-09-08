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
public class ProteinMappingsUpdater {

    private static Logger logger = LoggerFactory.getLogger(ProteinMappingsUpdater.class.getName());

    @Autowired
    private ProteinIdentificationSearchService proteinIdentificationSearchService;

    @Autowired
    private ProteinIdentificationIndexService proteinIdentificationIndexService;

    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("spring/app-context.xml");
        ProteinMappingsUpdater proteinMappingsUpdater = context.getBean(ProteinMappingsUpdater.class);

        if ("all".equals(args[0])) {
            addMappingsToAllExistingProteins(proteinMappingsUpdater);
        } else if ("inc".equals(args[0])) {
            addMappingsToProteinsWithNoMappings(proteinMappingsUpdater);
        }
    }

    private static void addMappingsToAllExistingProteins(ProteinMappingsUpdater proteinMappingsUpdater) {
        logger.info("Starting application...");
        // create the indexer
        logger.info("Creating protein details indexer...");
        ProteinDetailsIndexer proteinDetailsIndexer = new ProteinDetailsIndexer(proteinMappingsUpdater.proteinIdentificationSearchService, proteinMappingsUpdater.proteinIdentificationIndexService);
        logger.info("Protein details indexer created!");
        // update all
        logger.info("Starting update process...");
        proteinDetailsIndexer.addMappingsToAllExistingProteins();
        logger.info("Update process completed!");
    }

    private static void addMappingsToProteinsWithNoMappings(ProteinMappingsUpdater proteinMappingsUpdater) {
        logger.info("Starting application...");
        // create the indexer
        logger.info("Creating protein details indexer...");
        ProteinDetailsIndexer proteinDetailsIndexer = new ProteinDetailsIndexer(proteinMappingsUpdater.proteinIdentificationSearchService, proteinMappingsUpdater.proteinIdentificationIndexService);
        logger.info("Protein details indexer created!");
        // update all
        logger.info("Starting update process...");
        proteinDetailsIndexer.addMappingsToProteinsWithNoMappings();
        logger.info("Update process completed!");
    }



}
