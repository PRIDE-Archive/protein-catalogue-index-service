package uk.ac.ebi.pride.proteinindex.search.indexers;


import org.apache.solr.SolrTestCaseJ4;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.SolrParams;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.solr.core.SolrTemplate;
import uk.ac.ebi.pride.jmztab.model.MZTabFile;
import uk.ac.ebi.pride.jmztab.utils.MZTabFileParser;
import uk.ac.ebi.pride.proteinindex.search.model.ProteinIdentified;
import uk.ac.ebi.pride.proteinindex.search.search.repository.SolrProteinIdentificationRepositoryFactory;
import uk.ac.ebi.pride.proteinindex.search.search.service.ProteinIdentificationIndexService;
import uk.ac.ebi.pride.proteinindex.search.search.service.ProteinIdentificationSearchService;
import uk.ac.ebi.pride.proteinindex.search.util.ErrorLogOutputStream;
import uk.ac.ebi.pride.proteinindex.search.util.ProteinBuilder;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

/**
 * @author Jose A. Dianes
 * @version $Id$
 */

public class ProjectProteinIdentificationsIndexerTest extends SolrTestCaseJ4 {

    private static final int NUM_PROTEINS_ASSAY = 342;
    private static final int NUM_PROTEINS_PROJECT = 342;
    private static final int NUM_SYNONYMS_TEST_PROTEIN = 6;
    private static final int TEST_PROTEIN_SEQ_LENGTH = 505;
    private static Logger logger = LoggerFactory.getLogger(ProjectProteinIdentificationsIndexerTest.class.getName());

    private static ErrorLogOutputStream errorLogOutputStream = new ErrorLogOutputStream(logger);

    private static final String TEST_PROJECT_ACCESSION = "PXD000433";
    private static final String TEST_ASSAY_ACCESSION = "test-assay-accession";
    private static final String TEST_PROTEIN_ACCESSION = "D0NNB3";
    private static final String TEST_PROTEIN_NAME_FIELD = "NAME####Putative uncharacterized protein";
    private static final String TEST_PROTEIN_SEQ_STARTS_WITH = "MSSEEVVVAVEEQEIPDVIERL";
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

    @Ignore
    @Test
    public void testIndexAllProteinsForProjectAndAssay() throws Exception {

        ProteinIdentificationSearchService proteinIdentificationSearchService = new ProteinIdentificationSearchService(this.solrProteinIdentificationRepositoryFactory.create());
        ProteinIdentificationIndexService proteinIdentificationIndexService = new ProteinIdentificationIndexService(this.solrProteinIdentificationRepositoryFactory.create(), server);

        ProjectProteinIdentificationsIndexer projectProteinIdentificationsIndexer = new ProjectProteinIdentificationsIndexer(proteinIdentificationSearchService,proteinIdentificationIndexService);


        MZTabFileParser mzTabFileParser = new MZTabFileParser(new File("src/test/resources/submissions/PXD000433/internal/PRIDE_Exp_Complete_Ac_30824.mztab"), errorLogOutputStream);

        MZTabFile mzTabFile_30824 = mzTabFileParser.getMZTabFile();

        assertNotNull(mzTabFile_30824);

        projectProteinIdentificationsIndexer.indexAllProteinIdentificationsForProjectAndAssay(TEST_PROJECT_ACCESSION, TEST_ASSAY_ACCESSION, mzTabFile_30824);

        List<ProteinIdentified> proteins = proteinIdentificationSearchService.findByAssayAccessions(TEST_ASSAY_ACCESSION);
        assertEquals(NUM_PROTEINS_ASSAY,proteins.size());

        proteins = proteinIdentificationSearchService.findByProjectAccessions(TEST_PROJECT_ACCESSION);
        assertEquals(NUM_PROTEINS_PROJECT,proteins.size());
        testIsProteinD0NNB3(proteins.get(0));

        proteins = proteinIdentificationSearchService.findByAccession(TEST_PROTEIN_ACCESSION);
        assertEquals(1,proteins.size());
        assertEquals(NUM_SYNONYMS_TEST_PROTEIN, proteins.iterator().next().getSynonyms().size());

    }

    @Ignore
    @Test
    public void testDeletion() throws Exception {
        ProteinIdentificationSearchService proteinIdentificationSearchService = new ProteinIdentificationSearchService(this.solrProteinIdentificationRepositoryFactory.create());
        ProteinIdentificationIndexService proteinIdentificationIndexService = new ProteinIdentificationIndexService(this.solrProteinIdentificationRepositoryFactory.create(), server);

        ProjectProteinIdentificationsIndexer projectProteinIdentificationsIndexer = new ProjectProteinIdentificationsIndexer(proteinIdentificationSearchService,proteinIdentificationIndexService);

        MZTabFileParser mzTabFileParser = new MZTabFileParser(new File("src/test/resources/submissions/PXD000433/internal/PRIDE_Exp_Complete_Ac_30824.mztab"), errorLogOutputStream);

        MZTabFile mzTabFile_30824 = mzTabFileParser.getMZTabFile();

        assertNotNull(mzTabFile_30824);

        projectProteinIdentificationsIndexer.indexAllProteinIdentificationsForProjectAndAssay(TEST_PROJECT_ACCESSION, TEST_ASSAY_ACCESSION, mzTabFile_30824);

        Collection<ProteinIdentified> proteins = proteinIdentificationSearchService.findByAssayAccessions(TEST_ASSAY_ACCESSION);
        assertEquals(NUM_PROTEINS_ASSAY,proteins.size());

        proteins = proteinIdentificationSearchService.findByProjectAccessions(TEST_PROJECT_ACCESSION);
        assertEquals(NUM_PROTEINS_PROJECT,proteins.size());

        proteins = proteinIdentificationSearchService.findByAccession(TEST_PROTEIN_ACCESSION);
        assertEquals(1,proteins.size());
        assertEquals(NUM_SYNONYMS_TEST_PROTEIN, proteins.iterator().next().getSynonyms().size());



        // delete
        projectProteinIdentificationsIndexer.deleteProjectAndAssayFromProteins(
                TEST_PROJECT_ACCESSION,
                new TreeSet<String>( Arrays.asList(TEST_ASSAY_ACCESSION) )
        );

        proteins = proteinIdentificationSearchService.findByAssayAccessions(TEST_ASSAY_ACCESSION);
        assertEquals(0,proteins.size());

        proteins = proteinIdentificationSearchService.findByProjectAccessions(TEST_PROJECT_ACCESSION);
        assertEquals(0,proteins.size());

        proteins = proteinIdentificationSearchService.findByAccession(TEST_PROTEIN_ACCESSION);
        assertEquals(0,proteins.size());


    }


    @Test
    public void testAddProteinDetailsToExistingProteins() {
        addD0NNb3();

        ProteinIdentificationSearchService proteinIdentificationSearchService = new ProteinIdentificationSearchService(this.solrProteinIdentificationRepositoryFactory.create());
        ProteinIdentificationIndexService proteinIdentificationIndexService = new ProteinIdentificationIndexService(this.solrProteinIdentificationRepositoryFactory.create(), server);

        List<ProteinIdentified> proteins = proteinIdentificationSearchService.findByAccession(TEST_PROTEIN_ACCESSION);

        ProteinBuilder.addProteinDetails(proteins);

        proteinIdentificationIndexService.save(proteins.get(0));

        proteins = proteinIdentificationSearchService.findByAccession(TEST_PROTEIN_ACCESSION);

        assertNotNull(proteins);
        assertNotNull(proteins.get(0));
        assertEquals(TEST_PROTEIN_NAME_FIELD,proteins.get(0).getDescription().get(0));
        assertTrue(proteins.get(0).getSequence().startsWith(TEST_PROTEIN_SEQ_STARTS_WITH));
    }


    private void addD0NNb3() {
        ProteinIdentificationIndexService proteinIdentificationIndexService = new ProteinIdentificationIndexService(this.solrProteinIdentificationRepositoryFactory.create(),this.server);
        ProteinIdentified proteinIdentified = new ProteinIdentified();
        proteinIdentified.setAccession(TEST_PROTEIN_ACCESSION);
        proteinIdentified.setProjectAccessions(new TreeSet<String>(Arrays.asList(new String[]{TEST_PROJECT_ACCESSION})));
        proteinIdentificationIndexService.save(proteinIdentified);
    }

    private void testIsProteinD0NNB3(ProteinIdentified protein) {
        assertEquals(TEST_PROTEIN_ACCESSION,protein.getAccession());
        assertEquals(TEST_PROTEIN_SEQ_LENGTH,protein.getSequence().length());
    }


}
