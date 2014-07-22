package uk.ac.ebi.pride.proteinindex.search.search.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.solr.repository.Query;
import org.springframework.data.solr.repository.SolrCrudRepository;
import uk.ac.ebi.pride.proteinindex.search.model.ProteinIdentified;

import java.util.Collection;
import java.util.List;

/**
 * @author Jose A. Dianes
 * @version $Id$
 *
 * Note: using the Query annotation allows wildcards to go straight into the query
 */
public interface SolrProteinIdentificationRepository extends SolrCrudRepository<ProteinIdentified, String> {
    // Accession query methods
    @Query("id:?0")
    List<ProteinIdentified> findByAccession(String accession);
    @Query("id:(?0)")
    List<ProteinIdentified> findByAccessionIn(Collection<String> accessions);

    // Synonym query methods
    @Query("synonyms:?0")
    List<ProteinIdentified> findBySynonyms(String synonym);
    @Query("synonyms:?0 AND project_accessions:?1")
    List<ProteinIdentified> findBySynonymsAndProjectAccessions(String synonym, String projectAccession);
    List<ProteinIdentified> findBySynonymsIn(Collection<String> synonyms);

    // Project accession query methods
    @Query("project_accessions:?0")
    List<ProteinIdentified> findByProjectAccessions(String projectAccession);
    @Query("project_accessions:?0")
    Page<ProteinIdentified> findByProjectAccessions(String projectAccession, Pageable pageable);
    @Query("id:?0 AND project_accessions:?1")
    List<ProteinIdentified> findByAccessionAndProjectAccessions(String accession, String projectAccession);
    @Query("project_accessions:(?0)")
    List<ProteinIdentified> findByProjectAccessionsIn(Collection<String> projectAccessions);


    // Assay accession query methods
    @Query("assay_accessions:?0")
    List<ProteinIdentified> findByAssayAccessions(String assayAccession);
    @Query("assay_accessions:?0")
    Page<ProteinIdentified> findByAssayAccessions(String assayAccession, Pageable pageable);
    @Query("id:?0 AND assay_accessions:?1")
    List<ProteinIdentified> findByAccessionAndAssayAccessions(String accession, String assayAccession);
    @Query("assay_accessions:(?0)")
    List<ProteinIdentified> findByAssayAccessionsIn(Collection<String> assayAccessions);
}
