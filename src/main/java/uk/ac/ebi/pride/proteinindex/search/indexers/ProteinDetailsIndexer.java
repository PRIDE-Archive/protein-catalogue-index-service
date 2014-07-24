package uk.ac.ebi.pride.proteinindex.search.indexers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.pride.archive.dataprovider.identification.ProteinReferenceProvider;
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
public class ProteinDetailsIndexer {

    private static Logger logger = LoggerFactory.getLogger(ProteinDetailsIndexer.class.getName());

    private ProteinIdentificationSearchService proteinIdentificationSearchService;

    private ProteinIdentificationIndexService proteinIdentificationIndexService;

    public ProteinDetailsIndexer(ProteinIdentificationSearchService proteinIdentificationSearchService, ProteinIdentificationIndexService proteinIdentificationIndexService) {
        this.proteinIdentificationSearchService = proteinIdentificationSearchService;
        this.proteinIdentificationIndexService = proteinIdentificationIndexService;
    }


    public void addSynonymsToAllExistingProteins() {
        List<ProteinIdentified> proteins = this.proteinIdentificationSearchService.findAll();

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
                        this.proteinIdentificationIndexService.save(protein);
                    }
                }
            } catch (IOException e) {
                logger.error("Cannot get synonyms");
                e.printStackTrace();
            }

        }
    }

//    public static void addSynonymsToIdentifiedProteins(Map<String, ProteinIdentified> proteinIdentificationsToIndex, Map<String, ProteinReferenceProvider> proteinReferencesWitSynonyms) {
//        for (Map.Entry<String, ProteinIdentified> proteinIdentified: proteinIdentificationsToIndex.entrySet()) {
//            ProteinReferenceProvider proteinReferenceWithSynonyms = proteinReferencesWitSynonyms.get(proteinIdentified.getKey());
//            if (proteinReferenceWithSynonyms != null) {
//                Set<String> synonyms = new TreeSet<String>();
//                synonyms.addAll(proteinReferenceWithSynonyms.getSynonyms());
//                proteinIdentified.getValue().setSynonyms(synonyms);
//            }
//        }
//    }

    public void addDetailsToIdentifiedProteins(List<ProteinIdentified> proteins) {
        ProteinBuilder.addProteinDetails(proteins);

        // Index them
        // TODO
    }

}
