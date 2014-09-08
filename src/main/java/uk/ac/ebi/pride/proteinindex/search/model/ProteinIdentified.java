package uk.ac.ebi.pride.proteinindex.search.model;

import org.apache.solr.client.solrj.beans.Field;
import uk.ac.ebi.pride.archive.dataprovider.identification.ProteinDetailProvider;
import uk.ac.ebi.pride.archive.dataprovider.identification.ProteinReferenceProvider;
import uk.ac.ebi.pride.proteinindex.search.util.ProteinDetailUtils;

import java.util.List;
import java.util.Set;

/**
 * @author Jose A. Dianes
 * @version $Id$
 *
 */
public class ProteinIdentified implements ProteinReferenceProvider, ProteinDetailProvider {

    @Field(ProteinIdentifiedFields.ACCESSION)
    private String accession;

    @Field(ProteinIdentifiedFields.UNIPROT_MAPPING)
    private String uniprotMapping;

    @Field(ProteinIdentifiedFields.ENSEMBL_MAPPING)
    private String ensemblMapping;

    @Field(ProteinIdentifiedFields.OTHER_MAPPINGS)
    private Set<String> otherMappings;

    @Field(ProteinIdentifiedFields.INFERRED_SEQUENCE)
    private String inferredSequence;

    @Field(ProteinIdentifiedFields.DESCRIPTION)
    private List<String> description;

    public String getAccession() {
        return accession;
    }

    public void setAccession(String accession) {
        this.accession = accession;
    }

    public String getUniprotMapping() {
        return uniprotMapping;
    }

    public void setUniprotMapping(String uniprotMapping) {
        this.uniprotMapping = uniprotMapping;
    }

    public String getEnsemblMapping() {
        return ensemblMapping;
    }

    public void setEnsemblMapping(String ensemblMapping) {
        this.ensemblMapping = ensemblMapping;
    }

    public Set<String> getOtherMappings() {
        return otherMappings;
    }

    public void setOtherMappings(Set<String> otherMappings) {
        this.otherMappings = otherMappings;
    }

    public String getInferredSequence() {
        return inferredSequence;
    }

    public void setInferredSequence(String inferredSequence) {
        this.inferredSequence = inferredSequence;
    }

    public List<String> getDescription() {
        return description;
    }

    public void setDescription(List<String> description) {
        this.description = description;
    }

    @Override
    public String getName() {
        return ProteinDetailUtils.getNameFromDescription(description);
    }
}
