package uk.ac.ebi.pride.proteinindex.search.service;

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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.solr.core.SolrTemplate;
import uk.ac.ebi.pride.proteinindex.search.model.ProteinIdentified;
import uk.ac.ebi.pride.proteinindex.search.model.ProteinIdentifiedFields;
import uk.ac.ebi.pride.proteinindex.search.search.repository.SolrProteinIdentificationRepositoryFactory;
import uk.ac.ebi.pride.proteinindex.search.search.service.ProteinIdentificationIndexService;
import uk.ac.ebi.pride.proteinindex.search.search.service.ProteinIdentificationSearchService;

import java.util.*;

public class SolrProteinIdentifiedSearchTest extends SolrTestCaseJ4 {

    private static final String PROTEIN_1_ACCESSION = "PROTEIN-1-ACCESSION";
    private static final String PROTEIN_1_ACCESSION_SYNONYM_1 = "PROTEIN-1-ACCESSION-synonym-1";
    private static final String PROTEIN_1_ACCESSION_SYNONYM_2 = "PROTEIN-1-ACCESSION-synonym-2";
    private static final String PROTEIN_2_ACCESSION = "PROTEIN-2-ACCESSION";
    private static final String PARTIAL_ACCESSION_WILDCARD = "PROTEIN-*";
    private static final String PARTIAL_ACCESSION_WILDCARD_END_1 = "*1-ACCESSION";
    private static final String PARTIAL_ACCESSION_WILDCARD_END_2 = "*2-ACCESSION";
    private static final String PROJECT_1_ACCESSION = "PROJECT-1-ACCESSION";
    private static final String PROJECT_2_ACCESSION = "PROJECT-2-ACCESSION";
    private static final String ASSAY_1_ACCESSION = "ASSAY-1-ACCESSION";
    private static final String ASSAY_2_ACCESSION = "ASSAY-2-ACCESSION";

    private SolrServer server;
    private SolrProteinIdentificationRepositoryFactory solrProteinIdentificationRepositoryFactory;

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

        solrProteinIdentificationRepositoryFactory = new SolrProteinIdentificationRepositoryFactory(new SolrTemplate(server));
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

        SolrParams params = new SolrQuery(ProteinIdentifiedFields.ACCESSION+":"+ PROTEIN_1_ACCESSION);
        QueryResponse response = server.query(params);
        assertEquals(SINGLE_DOC, response.getResults().getNumFound());
        assertEquals(PROTEIN_1_ACCESSION, response.getResults().get(0).get(ProteinIdentifiedFields.ACCESSION));

    }

    @Test
    public void testSearchByAccession() throws Exception {

        addProteinIdentification_1();
        addProteinIdentification_2();

        ProteinIdentificationSearchService proteinIdentificationSearchService = new ProteinIdentificationSearchService(this.solrProteinIdentificationRepositoryFactory.create());
        ProteinIdentified proteinIdentified2 = proteinIdentificationSearchService.findByAccession(PROTEIN_1_ACCESSION).get(0);

        assertEquals(PROTEIN_1_ACCESSION, proteinIdentified2.getAccession());

    }

    @Test
    public void testSearchBySynonym() throws Exception {
        addProteinIdentification_1();
        addProteinIdentification_2();

        ProteinIdentificationSearchService proteinIdentificationSearchService = new ProteinIdentificationSearchService(this.solrProteinIdentificationRepositoryFactory.create());

        Collection<ProteinIdentified> proteinIdentifieds = proteinIdentificationSearchService.findBySynonyms(PROTEIN_1_ACCESSION_SYNONYM_1.replace(':','_'));
        assertEquals(2, proteinIdentifieds.size());

        proteinIdentifieds = proteinIdentificationSearchService.findBySynonyms(PROTEIN_1_ACCESSION_SYNONYM_2);
        assertEquals(1, proteinIdentifieds.size());
    }

    @Test
    public void testWildcardMatchSearch() throws Exception {
        addProteinIdentification_1();
        addProteinIdentification_2();

        ProteinIdentificationSearchService proteinIdentificationSearchService = new ProteinIdentificationSearchService(this.solrProteinIdentificationRepositoryFactory.create());

        List<ProteinIdentified> proteinIdentifieds = proteinIdentificationSearchService.findByAccession(PARTIAL_ACCESSION_WILDCARD);

        assertEquals( 2, proteinIdentifieds.size() );

    }

    @Test
    public void testWildcardMatchSearchMultiTerm() throws Exception {
        addProteinIdentification_1();
        addProteinIdentification_2();

        ProteinIdentificationSearchService proteinIdentificationSearchService = new ProteinIdentificationSearchService(this.solrProteinIdentificationRepositoryFactory.create());

        List<ProteinIdentified> proteinIdentifieds = proteinIdentificationSearchService.findByAccession(new LinkedList<String>(Arrays.asList(PARTIAL_ACCESSION_WILDCARD_END_1, PARTIAL_ACCESSION_WILDCARD_END_2)));

        assertEquals( 2, proteinIdentifieds.size() );

    }

    @Test
    public void testWildcardMatchSearchMultiTermSingle() throws Exception {
        addProteinIdentification_1();
        addProteinIdentification_2();

        ProteinIdentificationSearchService proteinIdentificationSearchService = new ProteinIdentificationSearchService(this.solrProteinIdentificationRepositoryFactory.create());

        List<ProteinIdentified> proteinIdentifieds = proteinIdentificationSearchService.findByAccession(new LinkedList<String>(Arrays.asList(PARTIAL_ACCESSION_WILDCARD_END_1 + " " + PARTIAL_ACCESSION_WILDCARD_END_2)));

        assertEquals( 2, proteinIdentifieds.size() );

    }

    @Test
    public void testFindByProjectAccession() throws Exception {
        addProteinIdentification_1_2(); // adds a protein to two projects in order to test paging results
        addProteinIdentification_2();

        ProteinIdentificationSearchService proteinIdentificationSearchService = new ProteinIdentificationSearchService(this.solrProteinIdentificationRepositoryFactory.create());

        // find all results for that project
        List<ProteinIdentified> proteinIdentifieds = proteinIdentificationSearchService.findByProjectAccessions(PROJECT_2_ACCESSION);
        assertEquals( 2, proteinIdentifieds.size() );

        // same query, but with paged result
        proteinIdentifieds = proteinIdentificationSearchService.findByProjectAccessions(PROJECT_2_ACCESSION, new PageRequest(1,1));
        assertEquals( 1, proteinIdentifieds.size() );
    }

    @Test
    public void testFindByAccessionWildcardAndProjectAccession() throws Exception {
        addProteinIdentification_1();
        addProteinIdentification_2();

        ProteinIdentificationSearchService proteinIdentificationSearchService = new ProteinIdentificationSearchService(this.solrProteinIdentificationRepositoryFactory.create());

        List<ProteinIdentified> proteinIdentifieds = proteinIdentificationSearchService.findByAccessionAndProjectAccessions(PARTIAL_ACCESSION_WILDCARD,PROJECT_1_ACCESSION);

        assertEquals( 1, proteinIdentifieds.size() );
    }

    @Test
    public void testFindByAssayAccession() throws Exception {
        addProteinIdentification_1_2();  // adds a protein to two assays in order to test paging results
        addProteinIdentification_2();

        ProteinIdentificationSearchService proteinIdentificationSearchService = new ProteinIdentificationSearchService(this.solrProteinIdentificationRepositoryFactory.create());

        List<ProteinIdentified> proteinIdentifieds = proteinIdentificationSearchService.findByAssayAccessions(ASSAY_2_ACCESSION);
        assertEquals( 2, proteinIdentifieds.size() );

        proteinIdentifieds = proteinIdentificationSearchService.findByAssayAccessions(ASSAY_2_ACCESSION, new PageRequest(1,1));
        assertEquals( 1, proteinIdentifieds.size() );
    }

    @Test
    public void testFindByAccessionWildcardAndAssayAccession() throws Exception {
        addProteinIdentification_1();
        addProteinIdentification_2();

        ProteinIdentificationSearchService proteinIdentificationSearchService = new ProteinIdentificationSearchService(this.solrProteinIdentificationRepositoryFactory.create());

        List<ProteinIdentified> proteinIdentifieds = proteinIdentificationSearchService.findByAccessionAndAssayAccessions(PARTIAL_ACCESSION_WILDCARD,ASSAY_1_ACCESSION);

        assertEquals( 1, proteinIdentifieds.size() );
    }

    @Test
    public void testFindBySynonym() throws Exception {
        addProteinIdentification_1();
        addProteinIdentification_2();

        ProteinIdentificationSearchService proteinIdentificationSearchService = new ProteinIdentificationSearchService(this.solrProteinIdentificationRepositoryFactory.create());

        List<ProteinIdentified> identifiedProteins = proteinIdentificationSearchService.findBySynonymsAndProjectAccessions(PROTEIN_1_ACCESSION_SYNONYM_1, PROJECT_1_ACCESSION);

        assertEquals( 1, identifiedProteins.size() );
    }

    private void addProteinIdentification_1() {
        ProteinIdentified proteinIdentified = new ProteinIdentified();
        proteinIdentified.setAccession(PROTEIN_1_ACCESSION);
        proteinIdentified.setSynonyms(new TreeSet<String>(Arrays.asList(PROTEIN_1_ACCESSION_SYNONYM_1)));
        proteinIdentified.setProjectAccessions(new TreeSet<String>(Arrays.asList(PROJECT_1_ACCESSION)));
        proteinIdentified.setAssayAccessions(new TreeSet<String>(Arrays.asList(ASSAY_1_ACCESSION)));

        Set<String> synonyms = new TreeSet<String>();
        synonyms.add(PROTEIN_1_ACCESSION_SYNONYM_1);
        synonyms.add(PROTEIN_1_ACCESSION_SYNONYM_2);
        proteinIdentified.setSynonyms(synonyms);

        ProteinIdentificationIndexService proteinIdentificationIndexService = new ProteinIdentificationIndexService(this.solrProteinIdentificationRepositoryFactory.create());
        proteinIdentificationIndexService.save(proteinIdentified);
    }
    private void addProteinIdentification_1_2() {
        // modified method to associate the protein to two projects and two assays, in order to test paging
        ProteinIdentified proteinIdentified = new ProteinIdentified();
        proteinIdentified.setAccession(PROTEIN_1_ACCESSION);

        Set<String> projects = new TreeSet<String>();
        projects.add(PROJECT_1_ACCESSION);
        projects.add(PROJECT_2_ACCESSION);
        proteinIdentified.setProjectAccessions(projects);

        Set<String> assays = new TreeSet<String>();
        assays.add(ASSAY_1_ACCESSION);
        assays.add(ASSAY_2_ACCESSION);
        proteinIdentified.setAssayAccessions(assays);

        Set<String> synonyms = new TreeSet<String>();
        synonyms.add(PROTEIN_1_ACCESSION_SYNONYM_1);
        synonyms.add(PROTEIN_1_ACCESSION_SYNONYM_2);
        proteinIdentified.setSynonyms(synonyms);

        ProteinIdentificationIndexService proteinIdentificationIndexService = new ProteinIdentificationIndexService(this.solrProteinIdentificationRepositoryFactory.create());
        proteinIdentificationIndexService.save(proteinIdentified);
    }

    private void addProteinIdentification_2() {
        ProteinIdentified proteinIdentified = new ProteinIdentified();
        proteinIdentified.setAccession(PROTEIN_2_ACCESSION);
        proteinIdentified.setSynonyms(new TreeSet<String>(Arrays.asList(PROTEIN_1_ACCESSION_SYNONYM_1)));
        proteinIdentified.setProjectAccessions(new TreeSet<String>(Arrays.asList(PROJECT_2_ACCESSION)));
        proteinIdentified.setAssayAccessions(new TreeSet<String>(Arrays.asList(ASSAY_2_ACCESSION)));

        Set<String> synonyms = new TreeSet<String>();
        synonyms.add(PROTEIN_1_ACCESSION_SYNONYM_1);
        proteinIdentified.setSynonyms(synonyms);

        ProteinIdentificationIndexService proteinIdentificationIndexService = new ProteinIdentificationIndexService(this.solrProteinIdentificationRepositoryFactory.create());
        proteinIdentificationIndexService.save(proteinIdentified);

    }

}
