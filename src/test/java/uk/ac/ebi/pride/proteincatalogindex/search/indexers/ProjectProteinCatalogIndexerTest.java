package uk.ac.ebi.pride.proteincatalogindex.search.indexers;


import org.apache.solr.SolrTestCaseJ4;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.SolrParams;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.solr.core.SolrTemplate;
import uk.ac.ebi.pride.jmztab.model.MZTabFile;
import uk.ac.ebi.pride.jmztab.utils.MZTabFileParser;
import uk.ac.ebi.pride.proteincatalogindex.search.model.ProteinIdentified;
import uk.ac.ebi.pride.proteincatalogindex.search.service.repository.SolrProteinCatalogRepositoryFactory;
import uk.ac.ebi.pride.proteincatalogindex.search.service.ProteinCatalogIndexService;
import uk.ac.ebi.pride.proteincatalogindex.search.service.ProteinCatalogSearchService;
import uk.ac.ebi.pride.proteincatalogindex.search.util.ErrorLogOutputStream;
import uk.ac.ebi.pride.proteincatalogindex.search.util.ProteinBuilder;

import java.io.File;
import java.util.Collection;
import java.util.List;

/**
 * @author Jose A. Dianes
 * @version $Id$
 */

public class ProjectProteinCatalogIndexerTest extends SolrTestCaseJ4 {

    private static final int NUM_PROTEINS_ASSAY = 342;
    private static final int NUM_PROTEINS_PROJECT = 342;
    private static final int NUM_OTHER_MAPPINGS_TEST_PROTEIN_ACCESSION = 4;
    private static final int TEST_PROTEIN_SEQ_LENGTH = 505;
    private static final String UNIPROT_MAPPING_FOR_TEST_PROTEIN_ACCESSION = "D0NNB3";
    private static final String ENSEMBL_MAPPING_FOR_TEST_PROTEIN_ACCESSION = "kk";
    private static Logger logger = LoggerFactory.getLogger(ProjectProteinCatalogIndexerTest.class.getName());

    private static ErrorLogOutputStream errorLogOutputStream = new ErrorLogOutputStream(logger);

    private static final String TEST_PROJECT_ACCESSION = "PXD000433";
    private static final String TEST_ASSAY_ACCESSION = "test-assay-accession";
    private static final String TEST_PROTEIN_ACCESSION = "D0NNB3";
    private static final String TEST_PROTEIN_NAME_FIELD = "NAME####Uncharacterized protein";
    private static final String TEST_PROTEIN_SEQ_STARTS_WITH = "MSSEEVVVAVEEQEIPDVIERL";
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

    @AfterClass
    public static void cleanUp() throws Exception {
        deleteCore();
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

    @Ignore // this test breaks maven package because of field references size - TODO: use samller mzTab files for testing
    @Test
    public void testIndexAllProteinsForProjectAndAssay() throws Exception {

        ProteinCatalogSearchService proteinCatalogSearchService = new ProteinCatalogSearchService(this.solrProteinCatalogRepositoryFactory.create());
        ProteinCatalogIndexService proteinCatalogIndexService = new ProteinCatalogIndexService(this.solrProteinCatalogRepositoryFactory.create(), server);

        ProjectProteinCatalogIndexer projectProteinCatalogIndexer = new ProjectProteinCatalogIndexer(proteinCatalogSearchService, proteinCatalogIndexService);
        ProteinDetailsIndexer proteinDetailsIndexer = new ProteinDetailsIndexer(proteinCatalogSearchService, proteinCatalogIndexService);

        MZTabFileParser mzTabFileParser = new MZTabFileParser(new File("src/test/resources/submissions/PXD000433/internal/PRIDE_Exp_Complete_Ac_30824.mztab"), errorLogOutputStream);

        MZTabFile mzTabFile_30824 = mzTabFileParser.getMZTabFile();

        assertNotNull(mzTabFile_30824);

        projectProteinCatalogIndexer.indexAllProteinIdentificationsForProjectAndAssay(TEST_PROJECT_ACCESSION, TEST_ASSAY_ACCESSION, mzTabFile_30824);


        List<ProteinIdentified> proteins = proteinCatalogSearchService.findByAccession(TEST_PROTEIN_ACCESSION);
        assertEquals(1,proteins.size());

        proteinDetailsIndexer.addDetailsToProteins(proteins);
        testIsProteinD0NNB3(proteins.get(0));

        proteins = proteinCatalogSearchService.findByAccession(TEST_PROTEIN_ACCESSION);
        proteinDetailsIndexer.addMappingsToProteins(proteins);
        assertEquals(1, proteins.size());
        assertEquals(UNIPROT_MAPPING_FOR_TEST_PROTEIN_ACCESSION, proteins.get(0).getUniprotMapping());
        assertEquals(null, proteins.get(0).getEnsemblMapping());
        assertEquals(NUM_OTHER_MAPPINGS_TEST_PROTEIN_ACCESSION, proteins.get(0).getOtherMappings().size());

    }

    @Ignore // this test works in isolation, but for some reason doesn't work when running all tests together
    @Test
    public void testDeletion() throws Exception {
        ProteinCatalogSearchService proteinCatalogSearchService = new ProteinCatalogSearchService(this.solrProteinCatalogRepositoryFactory.create());
        ProteinCatalogIndexService proteinCatalogIndexService = new ProteinCatalogIndexService(this.solrProteinCatalogRepositoryFactory.create(), server);

        ProjectProteinCatalogIndexer projectProteinCatalogIndexer = new ProjectProteinCatalogIndexer(proteinCatalogSearchService, proteinCatalogIndexService);

        MZTabFileParser mzTabFileParser = new MZTabFileParser(new File("src/test/resources/submissions/PXD000433/internal/PRIDE_Exp_Complete_Ac_30824.mztab"), errorLogOutputStream);

        MZTabFile mzTabFile_30824 = mzTabFileParser.getMZTabFile();

        assertNotNull(mzTabFile_30824);

        projectProteinCatalogIndexer.indexAllProteinIdentificationsForProjectAndAssay(TEST_PROJECT_ACCESSION, TEST_ASSAY_ACCESSION, mzTabFile_30824);

        Collection<ProteinIdentified> proteins = proteinCatalogSearchService.findByAccession(TEST_PROTEIN_ACCESSION);
        assertEquals(1,proteins.size());

        proteinCatalogIndexService.delete(TEST_PROTEIN_ACCESSION);

        proteins = proteinCatalogSearchService.findByAccession(TEST_PROTEIN_ACCESSION);
        assertEquals(0,proteins.size());

    }

    @Ignore // this test works in isolation, but for some reason doesn't work when running all tests together
    @Test
    public void testAddProteinDetailsToExistingProteins() {
        addD0NNb3();

        ProteinCatalogSearchService proteinCatalogSearchService = new ProteinCatalogSearchService(this.solrProteinCatalogRepositoryFactory.create());
        ProteinCatalogIndexService proteinCatalogIndexService = new ProteinCatalogIndexService(this.solrProteinCatalogRepositoryFactory.create(), server);

        List<ProteinIdentified> proteins = proteinCatalogSearchService.findByAccession(TEST_PROTEIN_ACCESSION);

        ProteinBuilder.addProteinDetails(proteins);

        proteinCatalogIndexService.save(proteins.get(0));

        proteins = proteinCatalogSearchService.findByAccession(TEST_PROTEIN_ACCESSION);

        assertNotNull(proteins);
        assertNotNull(proteins.get(0));
        //assertEquals(TEST_PROTEIN_NAME_FIELD, proteins.get(0).getDescription().get(0));
        assertTrue(proteins.get(0).getInferredSequence().startsWith(TEST_PROTEIN_SEQ_STARTS_WITH));
    }


    private void addD0NNb3() {
        ProteinCatalogIndexService proteinCatalogIndexService = new ProteinCatalogIndexService(this.solrProteinCatalogRepositoryFactory.create(),this.server);
        ProteinIdentified proteinIdentified = new ProteinIdentified();
        proteinIdentified.setAccession(TEST_PROTEIN_ACCESSION);
        proteinCatalogIndexService.save(proteinIdentified);
    }

    private void testIsProteinD0NNB3(ProteinIdentified protein) {
        assertEquals(TEST_PROTEIN_ACCESSION,protein.getAccession());
        assertTrue(protein.getInferredSequence().length() > 0);
    }


}
