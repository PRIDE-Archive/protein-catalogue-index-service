package uk.ac.ebi.pride.proteinindex.search.indexers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import uk.ac.ebi.pride.proteinindex.search.model.ProteinIdentified;
import uk.ac.ebi.pride.proteinindex.search.search.service.ProteinIdentificationIndexService;
import uk.ac.ebi.pride.proteinindex.search.search.service.ProteinIdentificationSearchService;
import uk.ac.ebi.pride.proteinindex.search.synonyms.ProteinAccessionSynonymsFinder;
import uk.ac.ebi.pride.proteinindex.search.util.ProteinBuilder;

import java.io.IOException;
import java.util.*;

/**
 * @author Jose A. Dianes
 * @version $Id$
 */
public class ProteinDetailsIndexer {

    private static Logger logger = LoggerFactory.getLogger(ProteinDetailsIndexer.class.getName());

    private static final int NUM_PROTEINS_PER_PAGE = 1000;

    private ProteinIdentificationSearchService proteinIdentificationSearchService;

    private ProteinIdentificationIndexService proteinIdentificationIndexService;

    public ProteinDetailsIndexer(ProteinIdentificationSearchService proteinIdentificationSearchService, ProteinIdentificationIndexService proteinIdentificationIndexService) {
        this.proteinIdentificationSearchService = proteinIdentificationSearchService;
        this.proteinIdentificationIndexService = proteinIdentificationIndexService;
    }

    public void addSynonymsToProteinsWithNoSynonyms() {
        int pageNumber = 0;
        Page<ProteinIdentified> proteinPage =
                this.proteinIdentificationSearchService.findAll(new PageRequest(pageNumber, NUM_PROTEINS_PER_PAGE));
        List<ProteinIdentified> proteins = proteinPage.getContent();

        while (proteins != null && proteins.size()>0) {

            // PROCESS PAGE
            logger.debug("Processing " + proteins.size() + " proteins from index page number " + pageNumber);
            // get the accessions
            Set<String> accessions = new TreeSet<String>();
            for (ProteinIdentified protein: proteins) {
                if (protein.getSynonyms()==null || protein.getSynonyms().size()==0) {
                    accessions.add(protein.getAccession());
                }
            }

            try {
                // get the synonyms
                Map<String, TreeSet<String>> synonyms = ProteinAccessionSynonymsFinder.findProteinSynonymsForAccession(accessions);

                // set the synonyms (and save)
                for (ProteinIdentified protein: proteins) {
                    if (synonyms.containsKey(protein.getAccession())) {
                        protein.setSynonyms(synonyms.get(protein.getAccession()));
                        this.proteinIdentificationIndexService.save(protein);
                        logger.info("Protein " + protein.getAccession() + " updated with " + protein.getSynonyms().size() + " synonyms");
                    }
                }
            } catch (IOException e) {
                logger.error("Cannot get synonyms");
                e.printStackTrace();
            }

            // GO TO NEXT PAGE
            pageNumber++;
            proteinPage =
                    this.proteinIdentificationSearchService.findAll(new PageRequest(pageNumber, NUM_PROTEINS_PER_PAGE));
            proteins = proteinPage.getContent();

        }
    }

    public void addSynonymsToProteins(List<ProteinIdentified> proteins) {
        if (proteins != null && proteins.size()>0) {

            logger.debug("Processing " + proteins.size() + " proteins");
            // get the accessions
            Set<String> accessions = new TreeSet<String>();
            for (ProteinIdentified protein: proteins) {
                if (protein.getSynonyms()==null || protein.getSynonyms().size()==0) {
                    accessions.add(protein.getAccession());
                }
            }

            try {
                // get the synonyms
                Map<String, TreeSet<String>> synonyms = ProteinAccessionSynonymsFinder.findProteinSynonymsForAccession(accessions);

                // set the synonyms (and save)
                for (ProteinIdentified protein: proteins) {
                    if (synonyms.containsKey(protein.getAccession())) {
                        protein.setSynonyms(synonyms.get(protein.getAccession()));
                        this.proteinIdentificationIndexService.save(protein);
                        logger.info("Protein " + protein.getAccession() + " updated with " + protein.getSynonyms().size() + " synonyms");
                    }
                }
            } catch (IOException e) {
                logger.error("Cannot get synonyms");
                e.printStackTrace();
            }

        }
    }


    public void addSynonymsToAllExistingProteins() {
        int pageNumber = 0;
        Page<ProteinIdentified> proteinPage =
                this.proteinIdentificationSearchService.findAll(new PageRequest(pageNumber, NUM_PROTEINS_PER_PAGE));
        List<ProteinIdentified> proteins = proteinPage.getContent();

        while (proteins != null && proteins.size()>0) {

            // PROCESS PAGE
            logger.info("Processing " + proteins.size() + " proteins from index page number " + pageNumber);
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
                        logger.info("Protein " + protein.getAccession() + " updated with " + protein.getSynonyms().size() + " synonyms");
                    }
                }
            } catch (IOException e) {
                logger.error("Cannot get synonyms");
                e.printStackTrace();
            }

            // GO TO NEXT PAGE
            pageNumber++;
            proteinPage =
                    this.proteinIdentificationSearchService.findAll(new PageRequest(pageNumber, NUM_PROTEINS_PER_PAGE));
            proteins = proteinPage.getContent();

        }
    }


    public void addDetailsToProteinsWithNoDetails() {
        int pageNumber = 0;
        Page<ProteinIdentified> proteinPage =
                this.proteinIdentificationSearchService.findAll(new PageRequest(pageNumber, NUM_PROTEINS_PER_PAGE));
        List<ProteinIdentified> proteins = proteinPage.getContent();

        while (proteins != null && proteins.size()>0) {

            // PROCESS PAGE
            logger.info("Processing " + proteins.size() + " proteins from index page number " + pageNumber);
            // get the accessions
            List<ProteinIdentified> proteinsToAddDetails = new LinkedList<ProteinIdentified>();
            for (ProteinIdentified protein: proteins) {
                if (protein.getName()==null || protein.getDescription()==null || protein.getDescription().size()==0 || protein.getSequence()==null) {
                    proteinsToAddDetails.add(protein);
                }
            }

            // add the details
            ProteinBuilder.addProteinDetails(proteinsToAddDetails);

            // GO TO NEXT PAGE
            pageNumber++;
            proteinPage =
                    this.proteinIdentificationSearchService.findAll(new PageRequest(pageNumber, NUM_PROTEINS_PER_PAGE));
            proteins = proteinPage.getContent();

        }
    }

    public void addDetailsToProteins(List<ProteinIdentified> proteins) {

        while (proteins != null && proteins.size()>0) {

            // PROCESS PAGE
            logger.info("Processing " + proteins.size() + " proteins");
            // get the accessions
            List<ProteinIdentified> proteinsToAddDetails = new LinkedList<ProteinIdentified>();
            for (ProteinIdentified protein: proteins) {
                if (protein.getName()==null || protein.getDescription()==null || protein.getDescription().size()==0 || protein.getSequence()==null) {
                    proteinsToAddDetails.add(protein);
                }
            }

            // add the details
            ProteinBuilder.addProteinDetails(proteinsToAddDetails);

        }
    }


    public void addDetailsToAllExistingProteins() {

    }

}
