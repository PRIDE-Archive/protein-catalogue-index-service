package uk.ac.ebi.pride.proteincatalogindex.search.service.repository;

import org.springframework.data.solr.repository.Query;
import org.springframework.data.solr.repository.SolrCrudRepository;
import uk.ac.ebi.pride.proteincatalogindex.search.model.ProteinIdentified;

import java.util.Collection;
import java.util.List;

/**
 * @author Jose A. Dianes
 * @version $Id$
 *
 * Note: using the Query annotation allows wildcards to go straight into the query
 */
public interface SolrProteinCatalogRepository extends SolrCrudRepository<ProteinIdentified, String> {

    // Accession query methods
    @Query("id:?0")
    List<ProteinIdentified> findByAccession(String accession);
    @Query("id:(?0)")
    List<ProteinIdentified> findByAccessionIn(Collection<String> accessions);

    // Mapping query methods
    @Query("uniprot_mapping:?0")
    List<ProteinIdentified> findByUniprotMapping(String uniprotMapping);
    @Query("uniprot_mapping:(?0)")
    List<ProteinIdentified> findByUniprotMappingIn(Collection<String> uniprotMappings);
    @Query("ensembl_mapping:?0")
    List<ProteinIdentified> findByEnsemblMapping(String ensemblMapping);
    @Query("ensembl_mapping:(?0)")
    List<ProteinIdentified> findByEnsemblMappingIn(Collection<String> ensemblMappings);
    @Query("uniprot_mapping:?0 OR ensembl_mapping:?0")
    List<ProteinIdentified> findByUniprotMappingOrEnsemblMapping(String mapping);
    @Query("uniprot_mapping:(?0) OR ensembl_mapping:(?0)")
    List<ProteinIdentified> findByUniprotMappingOrEnsemblMappingIn(Collection<String> mappings);
    @Query("other_mappings:?0")
    List<ProteinIdentified> findByOtherMapping(String otherMapping);
    @Query("other_mappings:(?0)")
    List<ProteinIdentified> findByOtherMappingIn(Collection<String> otherMappings);

    // Sequence query methods
    @Query("submitted_sequence:?0 OR inferred_sequence:?0")
    List<ProteinIdentified> findBySequence(String sequence);
    @Query("submitted_sequence:(?0) OR inferred_sequence:(?0)")
    List<ProteinIdentified> findBySequenceIn(Collection<String> sequences);

}
