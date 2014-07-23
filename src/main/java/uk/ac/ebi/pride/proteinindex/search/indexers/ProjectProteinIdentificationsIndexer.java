package uk.ac.ebi.pride.proteinindex.search.indexers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.pride.jmztab.model.MZTabFile;
import uk.ac.ebi.pride.archive.dataprovider.identification.ProteinReferenceProvider;
import uk.ac.ebi.pride.proteinindex.search.model.ProteinIdentified;
import uk.ac.ebi.pride.proteinindex.search.search.service.ProteinIdentificationIndexService;
import uk.ac.ebi.pride.proteinindex.search.search.service.ProteinIdentificationSearchService;
import uk.ac.ebi.pride.proteinindex.search.synonyms.ProteinAccessionSynonymsFinder;
import uk.ac.ebi.pride.proteinindex.search.util.ProteinBuilder;

import java.io.File;
import java.util.*;

/**
 * @author Jose A. Dianes
 * @version $Id$
 */
public class ProjectProteinIdentificationsIndexer {

    private static final int MAX_SAVE_TRIES = 5;
    private static final long TIME_TO_WAIT_BEFORE_NEW_SAVE_TRY = 5000;
    private static Logger logger = LoggerFactory.getLogger(ProjectProteinIdentificationsIndexer.class.getName());

    private ProteinIdentificationSearchService proteinIdentificationSearchService;

    private ProteinIdentificationIndexService proteinIdentificationIndexService;

    public ProjectProteinIdentificationsIndexer(ProteinIdentificationSearchService proteinIdentificationSearchService, ProteinIdentificationIndexService proteinIdentificationIndexService) {
        this.proteinIdentificationSearchService = proteinIdentificationSearchService;
        this.proteinIdentificationIndexService = proteinIdentificationIndexService;
    }

    public void deleteProjectAndAssayFromProteins(String projectAccession, Collection<String> assayAccessions) {

        // search by project accession
        List<ProteinIdentified> proteinsIdentified = this.proteinIdentificationSearchService.findByProjectAccessions(projectAccession);
        for (ProteinIdentified protein: proteinsIdentified) {
            protein.getProjectAccessions().remove(projectAccession);
            protein.getAssayAccessions().removeAll(assayAccessions);
            if (protein.getProjectAccessions().isEmpty() && protein.getAssayAccessions().isEmpty()) {
                this.proteinIdentificationIndexService.delete(protein.getAccession());
            } else {
                this.proteinIdentificationIndexService.save(protein);
            }
        }

    }

    public void indexAllProteinIdentificationsForProjectAndAssay(String projectAccession, String assayAccession, MZTabFile mzTabFile) {

        List<ProteinIdentified> proteinsIdentified = ProteinBuilder.readProteinIdentificationsFromMzTabFile(assayAccession, mzTabFile);


        Map<String, ProteinIdentified> proteinIdentificationsToIndex = new HashMap<String, ProteinIdentified>();
//        Set<String> synonymsToFind = new TreeSet<String>();

        for (ProteinIdentified proteinIdentified : proteinsIdentified) {
            logger.debug("Trying to index protein " + proteinIdentified.getAccession());
            try {
                // check for existing protein - WE NEED TO REPLACE ':' characters IF ANY
                List<ProteinIdentified> proteinIdentificationsFromIndex =
                        proteinIdentificationSearchService.findByAccession(
                                proteinIdentified.getAccession().replace(":","_")
                        );

                // if the protein is already in the index... update
                if ( proteinIdentificationsFromIndex != null && proteinIdentificationsFromIndex.size()>0 ) {
                    // get the existing protein
                    ProteinIdentified proteinIdentifiedFromIndex = proteinIdentificationsFromIndex.get(0);
                    // add new project accession
                    if (proteinIdentifiedFromIndex.getProjectAccessions() == null) {
                        proteinIdentifiedFromIndex.setProjectAccessions(new TreeSet<String>());
                    }
                    proteinIdentifiedFromIndex.getProjectAccessions().add(projectAccession);
                    // add new assay accession
                    if (proteinIdentifiedFromIndex.getAssayAccessions() == null) {
                        proteinIdentifiedFromIndex.setAssayAccessions(new TreeSet<String>());
                    }
                    proteinIdentifiedFromIndex.getAssayAccessions().add(assayAccession);
                    // add to save
                    proteinIdentificationsToIndex.put(proteinIdentifiedFromIndex.getAccession(), proteinIdentifiedFromIndex);

                    logger.debug(
                            "UPDATED protein " + proteinIdentified.getAccession() +
                                    " from PROJECT:" + projectAccession +
                                    " ASSAY:" + assayAccession
                    );
                }
                // if it is a new protein...
                else {
                    // set the project accessions
                    proteinIdentified.setProjectAccessions(
                            new TreeSet<String>(Arrays.asList(projectAccession))
                    );
                    // set assay accessions
                    proteinIdentified.setAssayAccessions(
                            new TreeSet<String>(Arrays.asList(assayAccession))
                    );
//                    // add the protein to the synonyms-to-get list
//                    synonymsToFind.add(proteinIdentified.getAccession());
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


//        // get all the synonyms for the proteins to index
//        long startTime2 = System.currentTimeMillis();
//        Map<String, ProteinReferenceProvider> proteinReferencesWitSynonyms = ProteinAccessionSynonymsFinder.getAllSynonyms(proteinIdentificationsToIndex, synonymsToFind);
//        ProteinAccessionSynonymsIndexer.addSynonymsToIdentifiedProteins(proteinIdentificationsToIndex,proteinReferencesWitSynonyms);
//
//        long endTime2 = System.currentTimeMillis();
//        logger.debug("DONE getting all synonyms for assay " + assayAccession + " in project " + projectAccession + " in " + (double)(endTime2-startTime2)/1000.0 + " seconds");

        // save all assay identifications
        long startTime2 = System.currentTimeMillis();
        proteinIdentificationIndexService.save(proteinIdentificationsToIndex.values());
        long endTime2 = System.currentTimeMillis();
        logger.debug("COMMITTED " + proteinIdentificationsToIndex.size() +
                " proteins from PROJECT:" + projectAccession +
                " ASSAY:" + assayAccession +
                " in " + (double)(endTime2-startTime2)/1000.0 + " seconds");

    }


//    @Deprecated
//    public void indexAllProteinIdentifications(String projectAccession, String pathToMzTabFiles) {
//
//        Map<String, LinkedList<ProteinIdentified>> proteinIdentifications = new HashMap<String, LinkedList<ProteinIdentified>>();
//
//        long startTime;
//        long endTime;
//
//        startTime = System.currentTimeMillis();
//
//        // build protein identifications from mzTabFiles
//        try {
//            if (pathToMzTabFiles != null) {
//                File generatedDirectory = new File(pathToMzTabFiles);
//                proteinIdentifications = ProteinBuilder.readProteinIdentificationsFromMzTabFilesDirectory(generatedDirectory);
//                logger.debug("Found " + getTotalProteinCount(proteinIdentifications) + " protein identifications in directory " + pathToMzTabFiles);
//            }
//        } catch (Exception e) { // we need to recover from any exception when reading the mzTab file so the whole process can continue
//            logger.error("Cannot get identifications from project " + projectAccession + " in folder" + pathToMzTabFiles);
//            logger.error("Reason: ");
//            e.printStackTrace();
//        }
//
//        endTime = System.currentTimeMillis();
//        logger.info("DONE getting protein identifications from file for project " + projectAccession + " in " + (double)(endTime-startTime)/1000.0 + " seconds");
//
//        //add all proteins
//        logger.info("Adding proteins to index for project " + projectAccession);
//        startTime = System.currentTimeMillis();
//
//        for (Map.Entry<? extends String, ? extends Collection<? extends ProteinIdentified>> assayProteinIdentifications: proteinIdentifications.entrySet()) {
//            Map<String, ProteinIdentified> proteinIdentificationsToIndex = new HashMap<String, ProteinIdentified>();
//            Set<String> synonymsToFind = new TreeSet<String>();
//            for (ProteinIdentified proteinIdentified : assayProteinIdentifications.getValue()) {
//                try {
//                    // check for existing protein - WE NEED TO REPLACE ':' characters IF ANY
//                    List<ProteinIdentified> proteinIdentificationsFromIndex =
//                            proteinIdentificationSearchService.findByAccession(
//                                    proteinIdentified.getAccession().replace(":","_")
//                            );
//                    if ( proteinIdentificationsFromIndex != null && proteinIdentificationsFromIndex.size()>0 ) {
//                        // get the existing protein
//                        ProteinIdentified proteinIdentifiedFromIndex = proteinIdentificationsFromIndex.get(0);
//                        // add new project accession
//                        if (proteinIdentifiedFromIndex.getProjectAccessions() == null) {
//                            proteinIdentifiedFromIndex.setProjectAccessions(new TreeSet<String>());
//                        }
//                        proteinIdentifiedFromIndex.getProjectAccessions().add(projectAccession);
//                        // add new assay accession
//                        if (proteinIdentifiedFromIndex.getAssayAccessions() == null) {
//                            proteinIdentifiedFromIndex.setAssayAccessions(new TreeSet<String>());
//                        }
//                        proteinIdentifiedFromIndex.getAssayAccessions().add(assayProteinIdentifications.getKey());
//                        // add to save
//                        proteinIdentificationsToIndex.put(proteinIdentifiedFromIndex.getAccession(), proteinIdentifiedFromIndex);
//
//                        logger.debug(
//                                "UPDATED protein " + proteinIdentified.getAccession() +
//                                        " from PROJECT:" + projectAccession +
//                                        " ASSAY:" + assayProteinIdentifications.getKey()
//                        );
//                    } else {
//                        // set the project accessions
//                        proteinIdentified.setProjectAccessions(
//                                new TreeSet<String>(Arrays.asList(projectAccession))
//                        );
//                        // set assay accessions
//                        proteinIdentified.setAssayAccessions(
//                                new TreeSet<String>(Arrays.asList(assayProteinIdentifications.getKey()))
//                        );
//                        // add the protein to the synonyms-to-get list
//                        synonymsToFind.add(proteinIdentified.getAccession());
//                        // add to save
//                        proteinIdentificationsToIndex.put(proteinIdentified.getAccession(), proteinIdentified);
//
//                        logger.debug(
//                                "ADDED protein " + proteinIdentified.getAccession() +
//                                        " from PROJECT:" + projectAccession +
//                                        " ASSAY:" + assayProteinIdentifications.getKey()
//                        );
//                    }
//                } catch (Exception e) {
//                    logger.error("Protein identification " + proteinIdentified.getAccession() + " caused an error");
//                    logger.error("ASSAY " + assayProteinIdentifications.getKey());
//                    logger.error("PROJECT " + projectAccession);
//                    e.printStackTrace();
//                }
//
//
//
//            }
//
//
//            // get all the synonyms for the proteins to index
//            long startTime2 = System.currentTimeMillis();
//            Map<String, ProteinReferenceProvider> proteinReferencesWitSynonyms = ProteinAccessionSynonymsFinder.getAllSynonyms(proteinIdentificationsToIndex, synonymsToFind);
//            ProteinAccessionSynonymsIndexer.addSynonymsToIdentifiedProteins(proteinIdentificationsToIndex,proteinReferencesWitSynonyms);
//
//            long endTime2 = System.currentTimeMillis();
//            logger.info("DONE getting all synonyms for assay " + assayProteinIdentifications.getKey() + " in project " + projectAccession + " in " + (double)(endTime2-startTime2)/1000.0 + " seconds");
//
//            // save all assay identifications
//            startTime2 = System.currentTimeMillis();
//            int numSaveTries = 0;
//            if (numSaveTries<=MAX_SAVE_TRIES) {
//                try {
//                    proteinIdentificationIndexService.save(proteinIdentificationsToIndex.values());
//                } catch (Exception e) {
//                    numSaveTries++;
//                    logger.error("Got exception while saving.TRY " + numSaveTries);
//                    try {
//                        e.printStackTrace();
//                        Thread.sleep(TIME_TO_WAIT_BEFORE_NEW_SAVE_TRY);
//                    } catch (InterruptedException e1) {
//                        e1.printStackTrace();
//                    }
//                }
//            }
//            endTime2 = System.currentTimeMillis();
//            logger.debug("COMMITTED " + proteinIdentificationsToIndex.size() +
//                    " proteins from PROJECT:" + projectAccession +
//                    " ASSAY:" + assayProteinIdentifications.getKey() +
//                    " in " + (double) (endTime2 - startTime2) / 1000.0 + " seconds");
//        }
//
//        endTime = System.currentTimeMillis();
//        logger.info("DONE indexing all protein identifications for project " + projectAccession + " in " + (double)(endTime-startTime)/1000.0 + " seconds");
//
//    }


    private static long getTotalProteinCount(Map<? extends String, ? extends Collection<? extends ProteinIdentified>> proteinIdentifications) {
        long res = 0;

        for (Map.Entry<? extends String, ? extends Collection<? extends ProteinIdentified>> proteinIdetificationEntry: proteinIdentifications.entrySet()) {
            res = res + proteinIdetificationEntry.getValue().size();
        }

        return res;
    }


}
