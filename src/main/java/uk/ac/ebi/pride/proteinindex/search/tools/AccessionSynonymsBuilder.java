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
public class AccessionSynonymsBuilder {

    private static Logger logger = LoggerFactory.getLogger(AccessionSynonymsBuilder.class.getName());

    @Autowired
    private SolrServer solrProteinServer;

    @Autowired
    private ProteinIdentificationSearchService proteinIdentificationSearchService;

    @Autowired
    private ProteinIdentificationIndexService proteinIdentificationIndexService;

    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("spring/app-context.xml");

        AccessionSynonymsBuilder accessionSynonymsBuilder = context.getBean(AccessionSynonymsBuilder.class);

        indexSynonyms(accessionSynonymsBuilder, accessionSynonymsBuilder.solrProteinServer);

    }

    private static void indexSynonyms(AccessionSynonymsBuilder accessionSynonymsBuilder, SolrServer solrProteinServer) {

    }

    private static void addSynonymsToExistingProteins(AccessionSynonymsBuilder accessionSynonymsBuilder, SolrServer server) {
        List<ProteinIdentified> proteins = accessionSynonymsBuilder.proteinIdentificationSearchService.findAll();

        if (proteins != null) {
            // get the accessions
            Set<String> accessions = new TreeSet<String>();
            for (ProteinIdentified protein: proteins) {
                accessions.add(protein.getAccession());
            }

            try {
                // get the synonyms
                Map<String, TreeSet<String>> synonyms = ProteinAccessionSynonymsFinder.findProteinSynonymsForAccession(accessions);

                // set the synonyms (and save)
                for (ProteinIdentified protein: proteins) {
                    if (synonyms.containsKey(protein.getAccession())) {
                        protein.setSynonyms(synonyms.get(protein.getAccession()));
                        accessionSynonymsBuilder.proteinIdentificationIndexService.save(protein);
                    }
                }
            } catch (IOException e) {
                logger.error("Cannot get synonyms");
                e.printStackTrace();
            }

        }
    }

    private static void addDetailsToExistingProteins(AccessionSynonymsBuilder accessionSynonymsBuilder, SolrServer server) {
        List<ProteinIdentified> proteins = accessionSynonymsBuilder.proteinIdentificationSearchService.findAll();

        if (proteins != null) {
            // add the details
            ProteinBuilder.addProteinDetails(proteins);
        }
    }

}
