package uk.ac.ebi.pride.proteincatalogindex.search.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;
import uk.ac.ebi.pride.proteincatalogindex.search.indexers.ProteinDetailsIndexer;
import uk.ac.ebi.pride.proteincatalogindex.search.service.ProteinCatalogIndexService;
import uk.ac.ebi.pride.proteincatalogindex.search.service.ProteinCatalogSearchService;


/**
 * @author Jose A. Dianes
 * @version $Id$
 */
@Component
public class ProteinDetailsUpdater {

    private static Logger logger = LoggerFactory.getLogger(ProteinDetailsUpdater.class.getName());

    @Autowired
    private ProteinCatalogSearchService proteinCatalogSearchService;

    @Autowired
    private ProteinCatalogIndexService proteinCatalogIndexService;

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
        ProteinDetailsIndexer proteinDetailsIndexer = new ProteinDetailsIndexer(proteinDetailsUpdater.proteinCatalogSearchService, proteinDetailsUpdater.proteinCatalogIndexService);
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
        ProteinDetailsIndexer proteinDetailsIndexer = new ProteinDetailsIndexer(proteinDetailsUpdater.proteinCatalogSearchService, proteinDetailsUpdater.proteinCatalogIndexService);
        logger.info("Protein details indexer created!");
        // update all
        logger.info("Starting update process...");
        proteinDetailsIndexer.addDetailsToProteinsWithNoDetails();
        logger.info("Update process completed!");
    }

}
