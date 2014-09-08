package uk.ac.ebi.pride.proteinindex.search.search.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.solr.repository.Query;
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

    // find by mapping methods
    public List<ProteinIdentified> findByUniprotMapping(String uniprotMapping) {
        return this.solrProteinIdentificationRepository.findByUniprotMapping(uniprotMapping);
    }
    public List<ProteinIdentified> findByUniprotMappingIn(Collection<String> uniprotMappings) {
        return this.solrProteinIdentificationRepository.findByUniprotMappingIn(uniprotMappings);
    }
    public List<ProteinIdentified> findByEnsemblMapping(String ensemblMapping) {
        return this.solrProteinIdentificationRepository.findByEnsemblMapping(ensemblMapping);
    }
    public List<ProteinIdentified> findByEnsemblMappingIn(Collection<String> ensemblMappings) {
        return this.solrProteinIdentificationRepository.findByEnsemblMappingIn(ensemblMappings);
    }
    public List<ProteinIdentified> findByUniprotMappingOrEnsemblMapping(String mapping) {
        return this.solrProteinIdentificationRepository.findByUniprotMappingOrEnsemblMapping(mapping);
    }
    public List<ProteinIdentified> findByUniprotMappingOrEnsemblMappingIn(Collection<String> mappings) {
        return this.solrProteinIdentificationRepository.findByUniprotMappingOrEnsemblMappingIn(mappings);
    }
    public List<ProteinIdentified> findByOtherMapping(String otherMapping) {
        return this.solrProteinIdentificationRepository.findByOtherMapping(otherMapping);
    }
    public List<ProteinIdentified> findByOtherMappingIn(Collection<String> otherMappings) {
        return this.solrProteinIdentificationRepository.findByOtherMappingIn(otherMappings);
    }

    // Sequence query methods
    public List<ProteinIdentified> findBySequence(String sequence) {
        return this.solrProteinIdentificationRepository.findBySequence(sequence);
    }
    public List<ProteinIdentified> findBySequenceIn(Collection<String> sequences) {
        return this.solrProteinIdentificationRepository.findBySequenceIn(sequences);
    }


    // find all
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
    public Page<ProteinIdentified> findAll(Pageable pageable) {
        return solrProteinIdentificationRepository.findAll(pageable);
    }


}
