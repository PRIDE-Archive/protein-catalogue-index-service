package uk.ac.ebi.pride.proteinindex.search.tools;

import org.apache.solr.client.solrj.SolrServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;
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
    private SolrServer solrProteinServer;

    @Autowired
    private ProteinIdentificationSearchService proteinIdentificationSearchService;

    @Autowired
    private ProteinIdentificationIndexService proteinIdentificationIndexService;

    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("spring/app-context.xml");

        ProteinDetailsUpdater proteinDetailsUpdater = context.getBean(ProteinDetailsUpdater.class);

        addDetailsToExistingProteins(proteinDetailsUpdater, proteinDetailsUpdater.solrProteinServer);

    }


    private static void addDetailsToExistingProteins(ProteinDetailsUpdater proteinDetailsUpdater, SolrServer server) {
        List<ProteinIdentified> proteins = proteinDetailsUpdater.proteinIdentificationSearchService.findAll();

        if (proteins != null) {
            // add the details
            ProteinBuilder.addProteinDetails(proteins);
        }
    }

}
