package uk.ac.ebi.pride.proteinindex.search.indexers;

import uk.ac.ebi.pride.archive.dataprovider.identification.ProteinReferenceProvider;
import uk.ac.ebi.pride.proteinindex.search.model.ProteinIdentified;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Jose A. Dianes
 * @version $Id$
 */
public class ProteinAccessionSynonymsIndexer {



    public static void addSynonymsToIdentifiedProteins(Map<String, ProteinIdentified> proteinIdentificationsToIndex, Map<String, ProteinReferenceProvider> proteinReferencesWitSynonyms) {
        for (Map.Entry<String, ProteinIdentified> proteinIdentified: proteinIdentificationsToIndex.entrySet()) {
            ProteinReferenceProvider proteinReferenceWithSynonyms = proteinReferencesWitSynonyms.get(proteinIdentified.getKey());
            if (proteinReferenceWithSynonyms != null) {
                Set<String> synonyms = new TreeSet<String>();
                synonyms.addAll(proteinReferenceWithSynonyms.getSynonyms());
                proteinIdentified.getValue().setSynonyms(synonyms);
            }
        }
    }

}
