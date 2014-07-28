package uk.ac.ebi.pride.proteinindex.search.search.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.ac.ebi.pride.proteinindex.search.model.ProteinIdentified;
import uk.ac.ebi.pride.proteinindex.search.search.repository.SolrProteinIdentificationRepository;

/**
 * @author Jose A. Dianes
 * @version $Id$
 */
@Service
public class ProteinIdentificationIndexService {

    private static Logger logger = LoggerFactory.getLogger(ProteinIdentificationIndexService.class.getName());

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
        for (ProteinIdentified proteinIdentified : proteinIdentifications) {
            logger.info("Protein to SAVE info:");
            logger.info(proteinIdentified.getAccession());
            logger.info("With " + proteinIdentified.getProjectAccessions().size() + " project accessions");
            logger.info("With " + proteinIdentified.getAssayAccessions().size() + " assay accessions");

            // fix the accession of needed
            proteinIdentified.getAccession().replaceAll("[:\\]\\[]", "_");

        }
        solrProteinIdentificationRepository.save(proteinIdentifications);
    }

    public void deleteAll() {
        solrProteinIdentificationRepository.deleteAll();
    }

    public void delete(String accession) {
        solrProteinIdentificationRepository.delete(accession);
    }

}
