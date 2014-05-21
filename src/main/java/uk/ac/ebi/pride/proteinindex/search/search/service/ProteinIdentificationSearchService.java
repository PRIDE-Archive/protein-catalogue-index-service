package uk.ac.ebi.pride.proteinindex.search.search.service;

import org.springframework.stereotype.Service;
import uk.ac.ebi.pride.proteinindex.search.model.ProteinIdentified;
import uk.ac.ebi.pride.proteinindex.search.search.repository.SolrProteinIdentificationRepository;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Jose A. Dianes
 * @version $Id$
 *
 * NOTE: protein accessions can contain chars that produce problems in solr queries ([,],:). They are replaced by _ when
 * using the repository methods
 */
@Service
public class ProteinIdentificationSearchService {

    private SolrProteinIdentificationRepository solrProteinIdentificationRepository;

    public ProteinIdentificationSearchService(SolrProteinIdentificationRepository solrProteinIdentificationRepository) {
        this.solrProteinIdentificationRepository = solrProteinIdentificationRepository;
    }

    public void setSolrProteinIdentificationRepository(SolrProteinIdentificationRepository solrProteinIdentificationRepository) {
        this.solrProteinIdentificationRepository = solrProteinIdentificationRepository;
    }


    // find by accession methods
    public List<ProteinIdentified> findByAccession(String accession) {
        // fix the accession of needed
        return solrProteinIdentificationRepository.findByAccession(accession.replaceAll("[:\\]\\[]", "_"));
    }
    public List<ProteinIdentified> findByAccession(Collection<String> accessions) {
        Collection<String> fixedAccessions = new LinkedList<String>();
        for (String accession: accessions) {
            fixedAccessions.add(accession.replaceAll("[:\\]\\[]", "_"));
        }
        return solrProteinIdentificationRepository.findByAccessionIn(fixedAccessions);
    }


    // find by project accession methods
    public List<ProteinIdentified> findByProjectAccessions(String projectAccession) {
        return solrProteinIdentificationRepository.findByProjectAccessions(projectAccession);
    }
    public List<ProteinIdentified> findByAccessionAndProjectAccessions(String accession, String projectAccession) {
        return solrProteinIdentificationRepository.findByAccessionAndProjectAccessions(accession.replaceAll("[:\\]\\[]", "_"), projectAccession);
    }
    public List<ProteinIdentified> findByProjectAccessions(Collection<String> projectAccessions) {
        return solrProteinIdentificationRepository.findByProjectAccessionsIn(projectAccessions);
    }


    // find by assay accession methods
    public List<ProteinIdentified> findByAssayAccessions(String assayAccession) {
        return solrProteinIdentificationRepository.findByAssayAccessions(assayAccession);
    }
    public List<ProteinIdentified> findByAccessionAndAssayAccessions(String accession, String assayAccession) {
        return solrProteinIdentificationRepository.findByAccessionAndAssayAccessions(accession.replaceAll("[:\\]\\[]", "_"), assayAccession);
    }
    public List<ProteinIdentified> findByAssayAccessions(Collection<String> assayAccessions) {
        return solrProteinIdentificationRepository.findByAssayAccessionsIn(assayAccessions);
    }


    // find by assay synonym methods
    public List<ProteinIdentified> findBySynonyms(String synonym) {
        return solrProteinIdentificationRepository.findBySynonyms(synonym);
    }
    public List<ProteinIdentified> findBySynonyms(Collection<String> synonyms) {
        return solrProteinIdentificationRepository.findBySynonymsIn(synonyms);
    }
    public List<ProteinIdentified> findBySynonymsAndProjectAccessions(String synonym, String projectAccession) {
        return solrProteinIdentificationRepository.findBySynonymsAndProjectAccessions(synonym, projectAccession);
    }


    public List<ProteinIdentified> findAll() {
        List<ProteinIdentified> res = new LinkedList<ProteinIdentified>();
        Iterator<ProteinIdentified> it = solrProteinIdentificationRepository.findAll().iterator();
        if (it != null) {
            while (it.hasNext()) {
                res.add(it.next());
            }
        }

        return res;
    }



}
