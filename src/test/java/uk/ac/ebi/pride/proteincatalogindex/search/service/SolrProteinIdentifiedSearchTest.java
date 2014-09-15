package uk.ac.ebi.pride.proteincatalogindex.search.service;

/**
 * @author Jose A. Dianes
 * @version $Id$
 */

import org.apache.solr.SolrTestCaseJ4;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.SolrParams;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.data.solr.core.SolrTemplate;
import uk.ac.ebi.pride.proteincatalogindex.search.model.ProteinIdentified;
import uk.ac.ebi.pride.proteincatalogindex.search.model.ProteinIdentifiedFields;
import uk.ac.ebi.pride.proteincatalogindex.search.service.repository.SolrProteinCatalogRepositoryFactory;
import uk.ac.ebi.pride.proteincatalogindex.search.util.ProteinDetailUtils;

import java.util.*;

public class SolrProteinIdentifiedSearchTest extends SolrTestCaseJ4 {

    private static final String PROTEIN_1_ACCESSION = "PROTEIN-1-ACCESSION";
    private static final String PROTEIN_1_ACCESSION_SYNONYM_1 = "PROTEIN-1-ACCESSION-synonym-1";
    private static final String PROTEIN_1_ACCESSION_SYNONYM_2 = "PROTEIN-1-ACCESSION-synonym-2";
    private static final String PROTEIN_2_ACCESSION = "PROTEIN-2-ACCESSION";
    private static final String PARTIAL_ACCESSION_WILDCARD = "PROTEIN-*";
    private static final String PARTIAL_ACCESSION_WILDCARD_END_1 = "*1-ACCESSION";
    private static final String PARTIAL_ACCESSION_WILDCARD_END_2 = "*2-ACCESSION";
    private static final String PROTEIN_1_NAME = "PROTEIN_1_NAME";
    private static final String PROTEIN_2_NAME = "PROTEIN_2_NAME";
    private static final String PROTEIN_1_UNIPROT_MAPPING = "PROTEIN-1-UNIPROT-MAPPING";
    private static final String PROTEIN_1_ENSEMBL_MAPPING = "PROTEIN-1-ENSEMBL-MAPPING";
    private static final String PROTEIN_2_UNIPROT_MAPPING = "PROTEIN-1-UNIPROT-MAPPING";
    private static final String PROTEIN_2_ENSEMBL_MAPPING = "PROTEIN-1-ENSEMBL-MAPPING";

    private SolrServer server;
    private SolrProteinCatalogRepositoryFactory solrProteinCatalogRepositoryFactory;

    public static final long ZERO_DOCS = 0L;
    public static final long SINGLE_DOC = 1L;

    @BeforeClass
    public static void initialise() throws Exception {
        initCore("src/test/resources/solr/collection1/conf/solrconfig.xml",
                "src/test/resources/solr/collection1/conf/schema.xml",
                "src/test/resources/solr");
    }


    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        server = new EmbeddedSolrServer(h.getCoreContainer(), h.getCore().getName());
        server.deleteByQuery("*:*");

        solrProteinCatalogRepositoryFactory = new SolrProteinCatalogRepositoryFactory(new SolrTemplate(server));
    }

    @Test
    public void testThatNoResultsAreReturned() throws SolrServerException {
        SolrParams params = new SolrQuery("text that is not found");
        QueryResponse response = server.query(params);
        assertEquals(ZERO_DOCS, response.getResults().getNumFound());
    }


    @Test
    public void testSearchByAccessionUsingQuery() throws Exception {

        addProteinIdentification_1();

        SolrParams params = new SolrQuery(ProteinIdentifiedFields.ACCESSION + ":" + PROTEIN_1_ACCESSION);
        QueryResponse response = server.query(params);
        assertEquals(SINGLE_DOC, response.getResults().getNumFound());
        assertEquals(PROTEIN_1_ACCESSION, response.getResults().get(0).get(ProteinIdentifiedFields.ACCESSION));

    }

    @Test
    public void testSearchByAccession() throws Exception {

        addProteinIdentification_1();
        addProteinIdentification_2();

        ProteinCatalogSearchService proteinCatalogSearchService = new ProteinCatalogSearchService(this.solrProteinCatalogRepositoryFactory.create());
        ProteinIdentified proteinIdentified = proteinCatalogSearchService.findByAccession(PROTEIN_1_ACCESSION).get(0);

        assertEquals(PROTEIN_1_ACCESSION, proteinIdentified.getAccession());
        assertEquals(PROTEIN_1_NAME, proteinIdentified.getName());

    }

    @Test
    public void testSearchByOtherMapping() throws Exception {
        addProteinIdentification_1();
        addProteinIdentification_2();

        ProteinCatalogSearchService proteinCatalogSearchService = new ProteinCatalogSearchService(this.solrProteinCatalogRepositoryFactory.create());

        Collection<ProteinIdentified> proteinIdentifieds = proteinCatalogSearchService.findByOtherMapping(PROTEIN_1_ACCESSION_SYNONYM_1.replace(':', '_'));
        assertEquals(2, proteinIdentifieds.size());

        proteinIdentifieds = proteinCatalogSearchService.findByOtherMapping(PROTEIN_1_ACCESSION_SYNONYM_2);
        assertEquals(1, proteinIdentifieds.size());
    }

    @Test
    public void testWildcardMatchSearch() throws Exception {
        addProteinIdentification_1();
        addProteinIdentification_2();

        ProteinCatalogSearchService proteinCatalogSearchService = new ProteinCatalogSearchService(this.solrProteinCatalogRepositoryFactory.create());

        List<ProteinIdentified> proteinIdentifieds = proteinCatalogSearchService.findByAccession(PARTIAL_ACCESSION_WILDCARD);

        assertEquals(2, proteinIdentifieds.size());

    }

    @Test
    public void testWildcardMatchSearchMultiTerm() throws Exception {
        addProteinIdentification_1();
        addProteinIdentification_2();

        ProteinCatalogSearchService proteinCatalogSearchService = new ProteinCatalogSearchService(this.solrProteinCatalogRepositoryFactory.create());

        List<ProteinIdentified> proteinIdentifieds = proteinCatalogSearchService.findByAccession(new LinkedList<String>(Arrays.asList(PARTIAL_ACCESSION_WILDCARD_END_1, PARTIAL_ACCESSION_WILDCARD_END_2)));

        assertEquals(2, proteinIdentifieds.size());

    }

    @Test
    public void testWildcardMatchSearchMultiTermSingle() throws Exception {
        addProteinIdentification_1();
        addProteinIdentification_2();

        ProteinCatalogSearchService proteinCatalogSearchService = new ProteinCatalogSearchService(this.solrProteinCatalogRepositoryFactory.create());

        List<ProteinIdentified> proteinIdentifieds = proteinCatalogSearchService.findByAccession(new LinkedList<String>(Arrays.asList(PARTIAL_ACCESSION_WILDCARD_END_1 + " " + PARTIAL_ACCESSION_WILDCARD_END_2)));

        assertEquals(2, proteinIdentifieds.size());

    }

    @Test
    public void testFindByAccessionWildcard() throws Exception {
        addProteinIdentification_1();
        addProteinIdentification_2();

        ProteinCatalogSearchService proteinCatalogSearchService = new ProteinCatalogSearchService(this.solrProteinCatalogRepositoryFactory.create());

        List<ProteinIdentified> proteinIdentifieds = proteinCatalogSearchService.findByAccession(PARTIAL_ACCESSION_WILDCARD);

        assertEquals(2, proteinIdentifieds.size());
    }

    @Test
    public void testFindByOtherMapping() throws Exception {
        addProteinIdentification_1();
        addProteinIdentification_2();

        ProteinCatalogSearchService proteinCatalogSearchService = new ProteinCatalogSearchService(this.solrProteinCatalogRepositoryFactory.create());

        List<ProteinIdentified> identifiedProteins = proteinCatalogSearchService.findByOtherMapping(PROTEIN_1_ACCESSION_SYNONYM_1);

        assertEquals(2, identifiedProteins.size());

        identifiedProteins = proteinCatalogSearchService.findByOtherMapping(PROTEIN_1_ACCESSION_SYNONYM_2);

        assertEquals(1, identifiedProteins.size());
    }

    private void addProteinIdentification_1() {
        ProteinIdentified proteinIdentified = new ProteinIdentified();
        proteinIdentified.setAccession(PROTEIN_1_ACCESSION);
        proteinIdentified.setUniprotMapping(PROTEIN_1_UNIPROT_MAPPING);
        proteinIdentified.setEnsemblMapping(PROTEIN_1_ENSEMBL_MAPPING);

        Set<String> synonyms = new TreeSet<String>();
        synonyms.add(PROTEIN_1_ACCESSION_SYNONYM_1);
        synonyms.add(PROTEIN_1_ACCESSION_SYNONYM_2);
        proteinIdentified.setOtherMappings(synonyms);
        proteinIdentified.setDescription(Arrays.asList(ProteinDetailUtils.NAME + PROTEIN_1_NAME));

        ProteinCatalogIndexService proteinCatalogIndexService = new ProteinCatalogIndexService(this.solrProteinCatalogRepositoryFactory.create(), server);
        proteinCatalogIndexService.save(proteinIdentified);
    }


    private void addProteinIdentification_2() {
        ProteinIdentified proteinIdentified = new ProteinIdentified();
        proteinIdentified.setAccession(PROTEIN_2_ACCESSION);
        proteinIdentified.setUniprotMapping(PROTEIN_2_UNIPROT_MAPPING);
        proteinIdentified.setEnsemblMapping(PROTEIN_2_ENSEMBL_MAPPING);

        Set<String> synonyms = new TreeSet<String>();
        synonyms.add(PROTEIN_1_ACCESSION_SYNONYM_1);
        proteinIdentified.setOtherMappings(synonyms);
        proteinIdentified.setDescription(Arrays.asList(ProteinDetailUtils.NAME + PROTEIN_2_NAME));

        ProteinCatalogIndexService proteinCatalogIndexService = new ProteinCatalogIndexService(this.solrProteinCatalogRepositoryFactory.create(), server);
        proteinCatalogIndexService.save(proteinIdentified);

    }

}
