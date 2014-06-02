package uk.ac.ebi.pride.proteinindex.search.model;

import org.apache.solr.client.solrj.beans.Field;
import uk.ac.ebi.pride.archive.dataprovider.identification.ProteinReferenceProvider;

import java.util.List;
import java.util.Set;

/**
 * @author Jose A. Dianes
 * @version $Id$
 *
 */
public class ProteinIdentified implements ProteinReferenceProvider {

    @Field(ProteinIdentifiedFields.ACCESSION)
    private String accession;

    @Field(ProteinIdentifiedFields.PROJECT_ACCESSIONS)
    private Set<String> projectAccessions;

    @Field(ProteinIdentifiedFields.ASSAY_ACCESSIONS)
    private Set<String> assayAccessions;

    @Field(ProteinIdentifiedFields.SYNONYMS)
    private Set<String> synonyms;

    @Field(ProteinIdentifiedFields.SEQUENCE)
    private String sequence;

    @Field(ProteinIdentifiedFields.DESCRIPTION)
    private List<String> description;

    public String getAccession() {
        return accession;
    }

    public void setAccession(String accession) {
        this.accession = accession;
    }

    public Set<String> getSynonyms() {
        return synonyms;
    }

    public void setSynonyms(Set<String> synonyms) {
        this.synonyms = synonyms;
    }

    public Set<String> getProjectAccessions() {
        return projectAccessions;
    }

    public void setProjectAccessions(Set<String> projectAccessions) {
        this.projectAccessions = projectAccessions;
    }

    public Set<String> getAssayAccessions() {
        return assayAccessions;
    }

    public void setAssayAccessions(Set<String> assayAccessions) {
        this.assayAccessions = assayAccessions;
    }

    public String getSequence() {
        return sequence;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

    public List<String> getDescription() {
        return description;
    }

    public void setDescription(List<String> description) {
        this.description = description;
    }
}
