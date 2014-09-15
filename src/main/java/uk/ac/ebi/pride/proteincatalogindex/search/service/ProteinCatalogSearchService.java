package uk.ac.ebi.pride.proteincatalogindex.search.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.ac.ebi.pride.proteincatalogindex.search.model.ProteinIdentified;
import uk.ac.ebi.pride.proteincatalogindex.search.service.repository.SolrProteinCatalogRepository;

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
public class ProteinCatalogSearchService {

    private SolrProteinCatalogRepository solrProteinCatalogRepository;

    public ProteinCatalogSearchService(SolrProteinCatalogRepository solrProteinIdentificationRepository) {
        this.solrProteinCatalogRepository = solrProteinIdentificationRepository;
    }

    public void setSolrProteinIdentificationRepository(SolrProteinCatalogRepository solrProteinIdentificationRepository) {
        this.solrProteinCatalogRepository = solrProteinIdentificationRepository;
    }


    // find by accession methods
    public List<ProteinIdentified> findByAccession(String accession) {
        // fix the accession of needed
        return solrProteinCatalogRepository.findByAccession(accession.replaceAll("[:\\]\\[]", "_"));
    }
    public List<ProteinIdentified> findByAccession(Collection<String> accessions) {
        Collection<String> fixedAccessions = new LinkedList<String>();
        for (String accession: accessions) {
            fixedAccessions.add(accession.replaceAll("[:\\]\\[]", "_"));
        }
        return solrProteinCatalogRepository.findByAccessionIn(fixedAccessions);
    }

    // find by mapping methods
    public List<ProteinIdentified> findByUniprotMapping(String uniprotMapping) {
        return this.solrProteinCatalogRepository.findByUniprotMapping(uniprotMapping);
    }
    public List<ProteinIdentified> findByUniprotMappingIn(Collection<String> uniprotMappings) {
        return this.solrProteinCatalogRepository.findByUniprotMappingIn(uniprotMappings);
    }
    public List<ProteinIdentified> findByEnsemblMapping(String ensemblMapping) {
        return this.solrProteinCatalogRepository.findByEnsemblMapping(ensemblMapping);
    }
    public List<ProteinIdentified> findByEnsemblMappingIn(Collection<String> ensemblMappings) {
        return this.solrProteinCatalogRepository.findByEnsemblMappingIn(ensemblMappings);
    }
    public List<ProteinIdentified> findByUniprotMappingOrEnsemblMapping(String mapping) {
        return this.solrProteinCatalogRepository.findByUniprotMappingOrEnsemblMapping(mapping);
    }
    public List<ProteinIdentified> findByUniprotMappingOrEnsemblMappingIn(Collection<String> mappings) {
        return this.solrProteinCatalogRepository.findByUniprotMappingOrEnsemblMappingIn(mappings);
    }
    public List<ProteinIdentified> findByOtherMapping(String otherMapping) {
        return this.solrProteinCatalogRepository.findByOtherMapping(otherMapping);
    }
    public List<ProteinIdentified> findByOtherMappingIn(Collection<String> otherMappings) {
        return this.solrProteinCatalogRepository.findByOtherMappingIn(otherMappings);
    }

    // Sequence query methods
    public List<ProteinIdentified> findBySequence(String sequence) {
        return this.solrProteinCatalogRepository.findBySequence(sequence);
    }
    public List<ProteinIdentified> findBySequenceIn(Collection<String> sequences) {
        return this.solrProteinCatalogRepository.findBySequenceIn(sequences);
    }


    // find all
    public List<ProteinIdentified> findAll() {
        List<ProteinIdentified> res = new LinkedList<ProteinIdentified>();
        Iterator<ProteinIdentified> it = solrProteinCatalogRepository.findAll().iterator();
        if (it != null) {
            while (it.hasNext()) {
                res.add(it.next());
            }
        }

        return res;
    }
    public Page<ProteinIdentified> findAll(Pageable pageable) {
        return solrProteinCatalogRepository.findAll(pageable);
    }


}
