package uk.ac.ebi.pride.proteincatalogindex.search.service;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.SolrPingResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.solr.UncategorizedSolrException;
import org.springframework.stereotype.Service;
import uk.ac.ebi.pride.proteincatalogindex.search.model.ProteinIdentified;
import uk.ac.ebi.pride.proteincatalogindex.search.service.repository.SolrProteinCatalogRepository;

import java.util.Collection;
import java.util.LinkedList;

/**
 * @author Jose A. Dianes
 * @version $Id$
 */
@Service
public class ProteinCatalogIndexService {

    private static Logger logger = LoggerFactory.getLogger(ProteinCatalogIndexService.class.getName());

    private static final int NUM_TRIES = 10;
    private static final int SECONDS_TO_WAIT = 30;
    private static final long MAX_ELAPSED_TIME_PING_QUERY = 10000;

    private SolrServer proteinCatalogServer;

    private SolrProteinCatalogRepository solrProteinCatalogRepository;

    public ProteinCatalogIndexService(SolrProteinCatalogRepository solrProteinIdentificationRepository, SolrServer proteinCatalogServer) {
        this.proteinCatalogServer = proteinCatalogServer;
        this.solrProteinCatalogRepository = solrProteinIdentificationRepository;
    }

    public void setSolrProteinIdentificationRepository(SolrProteinCatalogRepository solrProteinIdentificationRepository) {
        this.solrProteinCatalogRepository = solrProteinIdentificationRepository;
    }

    public boolean save(ProteinIdentified proteinIdentified) {
        Collection<ProteinIdentified> pii = new LinkedList<ProteinIdentified>();
        pii.add(proteinIdentified);
        return save(pii);
    }

    public boolean save(Collection<ProteinIdentified> proteinsIdentified) {
        if (proteinsIdentified!= null && proteinsIdentified.size()>0) {
            int numTries = 0;
            boolean succeed = false;
            while (numTries < NUM_TRIES && !succeed) {
                try {
                    SolrPingResponse pingResponse = this.proteinCatalogServer.ping();
                    if ((pingResponse.getStatus() == 0) && pingResponse.getElapsedTime() < MAX_ELAPSED_TIME_PING_QUERY) {
                        this.proteinCatalogServer.addBeans(proteinsIdentified);
                        this.proteinCatalogServer.commit();
                        succeed = true;
                    } else {
                        logger.error("[TRY " + numTries + " Solr server too busy!");
                        logger.error("PING response status: " + pingResponse.getStatus());
                        logger.error("PING elapsed time: " + pingResponse.getElapsedTime());
                        logger.error("Re-trying in " + SECONDS_TO_WAIT + " seconds...");
                        waitSecs();
                    }
                } catch (SolrServerException e) {
                    logger.error("[TRY " + numTries + "] There are server problems: " + e.getCause());
                    logger.error("Re-trying in " + SECONDS_TO_WAIT + " seconds...");
                    waitSecs();
                } catch (UncategorizedSolrException e) {
                    logger.error("[TRY " + numTries + "] There are server problems: " + e.getCause());
                    logger.error("Re-trying in " + SECONDS_TO_WAIT + " seconds...");
                    waitSecs();
                } catch (Exception e) {
                    logger.error("[TRY " + numTries + "] There are UNKNOWN problems: " + e.getCause());
                    e.printStackTrace();
                    logger.error("Re-trying in " + SECONDS_TO_WAIT + " seconds...");
                    waitSecs();
                }
                numTries++;
            }

            return succeed;
        } else {
            logger.error("Protein Catalog Index Service [reliable-save]: Trying to save an empty protein list!");

            return false;
        }
    }

    public void deleteAll() {
        solrProteinCatalogRepository.deleteAll();
    }

    public void delete(String accession) {
        solrProteinCatalogRepository.delete(accession);
    }


    private void waitSecs() {
        try {
            Thread.sleep(SECONDS_TO_WAIT * 1000);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
    }

}
