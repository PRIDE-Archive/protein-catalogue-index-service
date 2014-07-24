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
        addSynonymsToExistingProteins(proteinSynonymsUpdater);
    }

    private static void addSynonymsToExistingProteins(ProteinSynonymsUpdater proteinSynonymsUpdater) {
        System.out.println("Starting application...");
        // create the indexer
        System.out.println("Creating protein details indexer...");
        ProteinDetailsIndexer proteinDetailsIndexer = new ProteinDetailsIndexer(proteinSynonymsUpdater.proteinIdentificationSearchService, proteinSynonymsUpdater.proteinIdentificationIndexService);
        System.out.println("Protein details indexer created!");
        // update all
        System.out.println("Starting update process...");
        proteinDetailsIndexer.addSynonymsToAllExistingProteins();
        System.out.println("Update process completed!");
    }


}
