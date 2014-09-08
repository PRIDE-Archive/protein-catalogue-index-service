package uk.ac.ebi.pride.proteinindex.search.indexers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import uk.ac.ebi.pride.proteinindex.search.model.ProteinIdentified;
import uk.ac.ebi.pride.proteinindex.search.search.service.ProteinIdentificationIndexService;
import uk.ac.ebi.pride.proteinindex.search.search.service.ProteinIdentificationSearchService;
import uk.ac.ebi.pride.proteinindex.search.synonyms.ProteinAccessionMappingsFinder;
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
    public void addMappingsToProteinsWithNoMappings() {
        throw new UnsupportedOperationException(); // TODO
    }

    /**
     * Adds mappings to those proteins in the list. These include UniProt, Ensemble, and others if available.
     * Previous mappings will be overwritten.
     *
     * @param proteins the list of proteins to be updated with mappings
     */
    public void addMappingsToProteins(List<ProteinIdentified> proteins) {
        if (proteins != null && proteins.size()>0) {
            logger.debug("Processing " + proteins.size() + " proteins");
            // get the accessions
            Set<String> accessions = getAccessionsAsSet(proteins);
            // get the mappings
            Map<String, String> uniprotMappings = getUniprotMappings(accessions);
            Map<String, String> ensemblMappings = getEnsemblMappings(accessions);
            Map<String, TreeSet<String>> otherMappings = getOtherMappings(accessions);
            // add the mappings (and save)
            addUniprotMappingsToProteinList(proteins, uniprotMappings);
            addEnsemblMappingsToProteinList(proteins, ensemblMappings);
            addOtherMappingsToProteinList(proteins, otherMappings);
        }
    }

    /**
     * Adds mappings to all existing proteins in the catalog. Previous mappings are overwritten. This method is
     * potentially very time consuming for a large catalog
     */
    public void addMappingsToAllExistingProteins() {
        int pageNumber = 0;
        Page<ProteinIdentified> proteinPage =
                this.proteinIdentificationSearchService.findAll(new PageRequest(pageNumber, NUM_PROTEINS_PER_PAGE));
        List<ProteinIdentified> proteins = proteinPage.getContent();

        while (proteins != null && proteins.size()>0) {

            addMappingsToProteins(proteins);

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
                if ( isProteinMissingSomeDetail(protein) ) {
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
                if ( isProteinMissingSomeDetail(protein) ) {
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



    private Map<String, String> getUniprotMappings(Set<String> accessions) {
        Map<String, String> mappings = null;
        try {
            // get the mappings
            mappings = ProteinAccessionMappingsFinder.findProteinUniprotMappingsForAccession(accessions);
        } catch (IOException e) {
            logger.error("Cannot get mappings");
            e.printStackTrace();
        }

        return mappings;
    }
    private Map<String, String> getEnsemblMappings(Set<String> accessions) {
        Map<String, String> mappings = null;
        try {
            // get the mappings
            mappings = ProteinAccessionMappingsFinder.findProteinEnsemblMappingsForAccession(accessions);
        } catch (IOException e) {
            logger.error("Cannot get mappings");
            e.printStackTrace();
        }

        return mappings;
    }
    private Map<String, TreeSet<String>> getOtherMappings(Set<String> accessions) {
        Map<String, TreeSet<String>> synonyms = null;
        try {
            // get the synonyms
            synonyms = ProteinAccessionMappingsFinder.findProteinOtherMappingsForAccession(accessions);
        } catch (IOException e) {
            logger.error("Cannot get synonyms");
            e.printStackTrace();
        }

        return synonyms;
    }

    private void addUniprotMappingsToProteinList(List<ProteinIdentified> proteins, Map<String, String> mappings) {
        for (ProteinIdentified protein: proteins) {
            // add mapping
            if (mappings!= null && mappings.containsKey(protein.getAccession())) {
                protein.setUniprotMapping(mappings.get(protein.getAccession()));
            }

            logger.debug("Protein " + protein.getAccession() + " updated with mapping " + protein.getUniprotMapping());
        }

        // save
        this.proteinIdentificationIndexService.save(proteins);

    }

    private void addEnsemblMappingsToProteinList(List<ProteinIdentified> proteins, Map<String, String> mappings) {
        for (ProteinIdentified protein: proteins) {
            // add mapping
            if (mappings!= null && mappings.containsKey(protein.getAccession())) {
                protein.setEnsemblMapping(mappings.get(protein.getAccession()));
            }

            logger.debug("Protein " + protein.getAccession() + " updated with mapping " + protein.getEnsemblMapping());
        }

        // save
        this.proteinIdentificationIndexService.save(proteins);

    }

    private void addOtherMappingsToProteinList(List<ProteinIdentified> proteins, Map<String, TreeSet<String>> mappings) {
        for (ProteinIdentified protein: proteins) {
            // init mappings if needed
            if (protein.getOtherMappings()== null)
                protein.setOtherMappings(new TreeSet<String>());
            // add mappings
            if (mappings!= null && mappings.containsKey(protein.getAccession())) {
                protein.setOtherMappings(mappings.get(protein.getAccession()));
            }
//            // the protein ID is also added as a synonym, facilitating future searches
//            protein.getOtherMappings().add(protein.getAccession());
            logger.debug("Protein " + protein.getAccession() + " updated with " + protein.getOtherMappings().size() + " mappings");
        }

        // save
        this.proteinIdentificationIndexService.save(proteins);

    }

    private boolean isProteinMissingSomeDetail(ProteinIdentified protein) {
        return (protein.getName() == null || protein.getDescription() == null || protein.getDescription().size() == 0 || protein.getInferredSequence() == null);
    }

    /**
     * Get a set of accessions for proteins
     * @param proteins
     * @return
     */
    private Set<String> getAccessionsAsSet(List<ProteinIdentified> proteins) {
        Set<String> accessions = new TreeSet<String>();
        for (ProteinIdentified protein: proteins) {
            accessions.add(protein.getAccession());
        }

        return accessions;
    }

    /**
     * Get a set of accessions for proteins with no mappings
     * @param proteins
     * @return
     */
    private Set<String> getAccessionsWithoutAnyMappingsAsSet(List<ProteinIdentified> proteins) {
        Set<String> accessions = new TreeSet<String>();
        for (ProteinIdentified protein: proteins) {
            if ( isProteinMissingAllMappings(protein) ) {
                accessions.add(protein.getAccession());
            }
        }

        return accessions;
    }

    private boolean isProteinMissingAllMappings(ProteinIdentified protein) {
        return (
                (protein.getUniprotMapping()==null || "".equals(protein.getUniprotMapping())) &&
                        (protein.getEnsemblMapping()==null || "".equals(protein.getEnsemblMapping())) &&
                        (protein.getOtherMappings()==null || protein.getOtherMappings().size() == 0 )
        );
    }


}
