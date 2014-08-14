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

    /**
     * This method will add synonyms to proteins with no synonyms
     */
    public void addSynonymsToProteinsWithNoSynonyms() {
        throw new UnsupportedOperationException(); // TODO
    }

    /**
     * Adds synonyms to those proteins in the list. Previous synonyms will be overwritten
     *
     * @param proteins the list of proteins to be updated with synonyms
     */
    public void addSynonymsToProteins(List<ProteinIdentified> proteins) {
        if (proteins != null && proteins.size()>0) {
            logger.debug("Processing " + proteins.size() + " proteins");
            // get the accessions
            Set<String> accessions = getAccessionsAsSet(proteins);
            // get the synonyms
            Map<String, TreeSet<String>> synonyms = getSynonyms(accessions);
            // add the synonyms (and save)
            addSynonymsToProteinList(proteins, synonyms);
        }
    }

    /**
     * Adds synonyms to all existing proteins in the catalog. Previous synonyms are overwritten. This method is
     * potentially very time consuming for a large catalog
     */
    public void addSynonymsToAllExistingProteins() {
        int pageNumber = 0;
        Page<ProteinIdentified> proteinPage =
                this.proteinIdentificationSearchService.findAll(new PageRequest(pageNumber, NUM_PROTEINS_PER_PAGE));
        List<ProteinIdentified> proteins = proteinPage.getContent();

        while (proteins != null && proteins.size()>0) {

            addSynonymsToProteins(proteins);

            // Next page...
            pageNumber++;
            proteinPage =
                    this.proteinIdentificationSearchService.findAll(new PageRequest(pageNumber, NUM_PROTEINS_PER_PAGE));
            proteins = proteinPage.getContent();

        }
    }


    /**
     * Add protein details to proteins with no details
     */
    public void addDetailsToProteinsWithNoDetails() {
        int pageNumber = 0;
        Page<ProteinIdentified> proteinPage =
                this.proteinIdentificationSearchService.findAll(new PageRequest(pageNumber, NUM_PROTEINS_PER_PAGE));
        List<ProteinIdentified> proteins = proteinPage.getContent();

        while (proteins != null && proteins.size()>0) {

            // PROCESS PAGE
            logger.debug("Processing " + proteins.size() + " proteins from index page number " + pageNumber);
            // get the accessions
            List<ProteinIdentified> proteinsToAddDetails = new LinkedList<ProteinIdentified>();
            for (ProteinIdentified protein: proteins) {
                if (protein.getName()==null || protein.getDescription()==null || protein.getDescription().size()==0 || protein.getSequence()==null) {
                    proteinsToAddDetails.add(protein);
                }
            }

            // add the details & save
            ProteinBuilder.addProteinDetails(proteinsToAddDetails);
            this.proteinIdentificationIndexService.save(proteinsToAddDetails);

            // GO TO NEXT PAGE
            pageNumber++;
            proteinPage =
                    this.proteinIdentificationSearchService.findAll(new PageRequest(pageNumber, NUM_PROTEINS_PER_PAGE));
            proteins = proteinPage.getContent();

        }
    }

    /**
     * Add details to a given list of proteins
     * @param proteins
     */
    public void addDetailsToProteins(List<ProteinIdentified> proteins) {

        if (proteins != null && proteins.size()>0) {
            logger.debug("Processing " + proteins.size() + " proteins");
            // get the accessions
            List<ProteinIdentified> proteinsToAddDetails = new LinkedList<ProteinIdentified>();
            for (ProteinIdentified protein: proteins) {
                if (protein.getName()==null || protein.getDescription()==null || protein.getDescription().size()==0 || protein.getSequence()==null) {
                    proteinsToAddDetails.add(protein);
                }
            }

            // add the details & save
            ProteinBuilder.addProteinDetails(proteinsToAddDetails);
            this.proteinIdentificationIndexService.save(proteinsToAddDetails);

        }
    }



    public void addDetailsToAllExistingProteins() {

        throw new UnsupportedOperationException();
    }
    private Set<String> getAccessionsAsSet(List<ProteinIdentified> proteins) {
        Set<String> accessions = new TreeSet<String>();
        for (ProteinIdentified protein: proteins) {
            if (protein.getSynonyms()==null || protein.getSynonyms().size()==0) {
                accessions.add(protein.getAccession());
            }
        }

        return accessions;
    }


    private Map<String, TreeSet<String>> getSynonyms(Set<String> accessions) {
        Map<String, TreeSet<String>> synonyms = null;
        try {
            // get the synonyms
            synonyms = ProteinAccessionSynonymsFinder.findProteinSynonymsForAccession(accessions);
        } catch (IOException e) {
            logger.error("Cannot get synonyms");
            e.printStackTrace();
        }

        return synonyms;
    }

    /**
     * Adds synonyms to already existing ones - the protein accession is added to the synonym list as well
     * @param proteins The list of proteins to be updated with synonyms
     * @param synonyms The synonyms
     */
    private void addSynonymsToProteinList(List<ProteinIdentified> proteins, Map<String, TreeSet<String>> synonyms) {
        for (ProteinIdentified protein: proteins) {
            // init synonyms if needed
            if (protein.getSynonyms()== null)
                protein.setSynonyms(new TreeSet<String>());
            // add synonyms
            if (synonyms!= null && synonyms.containsKey(protein.getAccession())) {
                protein.setSynonyms(synonyms.get(protein.getAccession()));
            }
            // the protein ID is also added as a synonym, facilitating future searches
            protein.getSynonyms().add(protein.getAccession());
            logger.debug("Protein " + protein.getAccession() + " updated with " + protein.getSynonyms().size() + " synonyms");
        }

        // save
        this.proteinIdentificationIndexService.save(proteins);


    }

}
