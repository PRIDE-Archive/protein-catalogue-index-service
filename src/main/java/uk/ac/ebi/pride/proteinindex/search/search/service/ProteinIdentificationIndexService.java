package uk.ac.ebi.pride.proteinindex.search.search.service;

import org.springframework.stereotype.Service;
import uk.ac.ebi.pride.proteinindex.search.model.ProteinIdentified;
import uk.ac.ebi.pride.proteinindex.search.search.repository.SolrProteinIdentificationRepository;

/**
 * @author Jose A. Dianes
 * @version $Id$
 */
@Service
public class ProteinIdentificationIndexService {

    private SolrProteinIdentificationRepository solrProteinIdentificationRepository;

    public ProteinIdentificationIndexService(SolrProteinIdentificationRepository solrProteinIdentificationRepository) {
        this.solrProteinIdentificationRepository = solrProteinIdentificationRepository;
    }

    public void setSolrProteinIdentificationRepository(SolrProteinIdentificationRepository solrProteinIdentificationRepository) {
        this.solrProteinIdentificationRepository = solrProteinIdentificationRepository;
    }

    public void save(ProteinIdentified proteinIdentified) {
        // fix the accession of needed
        proteinIdentified.getAccession().replaceAll("[:\\]\\[]", "_");
        solrProteinIdentificationRepository.save(proteinIdentified);
    }

    public void save(Iterable<ProteinIdentified> proteinIdentifications) {
        for (ProteinIdentified proteinIdentified : proteinIdentifications)
            // fix the accession of needed
            proteinIdentified.getAccession().replaceAll("[:\\]\\[]", "_");
        solrProteinIdentificationRepository.save(proteinIdentifications);
    }

    public void deleteAll() {
        solrProteinIdentificationRepository.deleteAll();
    }

    public void delete(String accession) {
        solrProteinIdentificationRepository.delete(accession);
    }

}
