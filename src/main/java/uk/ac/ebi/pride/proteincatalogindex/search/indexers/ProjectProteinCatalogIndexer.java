package uk.ac.ebi.pride.proteincatalogindex.search.indexers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.pride.jmztab.model.MZTabFile;
import uk.ac.ebi.pride.proteincatalogindex.search.model.ProteinIdentified;
import uk.ac.ebi.pride.proteincatalogindex.search.service.ProteinCatalogIndexService;
import uk.ac.ebi.pride.proteincatalogindex.search.service.ProteinCatalogSearchService;
import uk.ac.ebi.pride.proteincatalogindex.search.util.ProteinBuilder;

import java.util.*;

/**
 * @author Jose A. Dianes
 * @version $Id$
 */
public class ProjectProteinCatalogIndexer {

    private static Logger logger = LoggerFactory.getLogger(ProjectProteinCatalogIndexer.class.getName());

    private ProteinCatalogSearchService proteinCatalogSearchService;

    private ProteinCatalogIndexService proteinCatalogIndexService;

    public ProjectProteinCatalogIndexer(ProteinCatalogSearchService proteinCatalogSearchService, ProteinCatalogIndexService proteinCatalogIndexService) {
        this.proteinCatalogSearchService = proteinCatalogSearchService;
        this.proteinCatalogIndexService = proteinCatalogIndexService;
    }

//    public void deleteProjectAndAssayFromProteins(String projectAccession, Collection<String> assayAccessions) {
//
//        // search by project accession
//        List<ProteinIdentified> proteinsIdentified = this.proteinIdentificationSearchService.findByProjectAccessions(projectAccession);
//        for (ProteinIdentified protein: proteinsIdentified) {
//            protein.getProjectAccessions().remove(projectAccession);
//            protein.getAssayAccessions().removeAll(assayAccessions);
//            if (protein.getProjectAccessions().isEmpty() && protein.getAssayAccessions().isEmpty()) {
//                this.proteinIdentificationIndexService.delete(protein.getAccession());
//            } else {
//                this.proteinIdentificationIndexService.save(protein);
//            }
//        }
//
//    }

    public void indexAllProteinIdentificationsForProjectAndAssay(String projectAccession, String assayAccession, MZTabFile mzTabFile) {

        List<ProteinIdentified> proteinsIdentified = ProteinBuilder.readProteinIdentificationsFromMzTabFile(assayAccession, mzTabFile);

        Map<String, ProteinIdentified> proteinIdentificationsToIndex = new HashMap<String, ProteinIdentified>();

        for (ProteinIdentified proteinIdentified : proteinsIdentified) {
            logger.debug("Trying to index protein " + proteinIdentified.getAccession());
            try {
                List<ProteinIdentified> proteinIdentificationsFromIndex =
                        proteinCatalogSearchService.findByAccession(
                                proteinIdentified.getAccession()
                        );

                // if it is a new protein...
                if ( proteinIdentificationsFromIndex == null || proteinIdentificationsFromIndex.size()==0 ) {
                    // add to save
                    proteinIdentificationsToIndex.put(proteinIdentified.getAccession(), proteinIdentified);

                    logger.debug(
                            "ADDED protein " + proteinIdentified.getAccession() +
                                    " from PROJECT:" + projectAccession +
                                    " ASSAY:" + assayAccession
                    );
                }
            } catch (Exception e) {
                logger.error("Protein identification " + proteinIdentified.getAccession() + " caused an error");
                logger.error("ASSAY " + assayAccession);
                logger.error("PROJECT " + projectAccession);
                e.printStackTrace();
            }
        }

        // save all proteins
        long startTime2 = System.currentTimeMillis();
        proteinCatalogIndexService.save(proteinIdentificationsToIndex.values());
        long endTime2 = System.currentTimeMillis();
        logger.debug("COMMITTED " + proteinIdentificationsToIndex.size() +
                " proteins from PROJECT:" + projectAccession +
                " ASSAY:" + assayAccession +
                " in " + (double)(endTime2-startTime2)/1000.0 + " seconds");

    }


    private static long getTotalProteinCount(Map<? extends String, ? extends Collection<? extends ProteinIdentified>> proteinIdentifications) {
        long res = 0;

        for (Map.Entry<? extends String, ? extends Collection<? extends ProteinIdentified>> proteinIdetificationEntry: proteinIdentifications.entrySet()) {
            res = res + proteinIdetificationEntry.getValue().size();
        }

        return res;
    }


}
