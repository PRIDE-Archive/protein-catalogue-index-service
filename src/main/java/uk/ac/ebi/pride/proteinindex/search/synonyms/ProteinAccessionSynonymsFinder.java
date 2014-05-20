package uk.ac.ebi.pride.proteinindex.search.synonyms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.pride.proteinindex.search.model.ProteinIdentified;
import uk.ac.ebi.pride.tools.protein_details_fetcher.util.ProteinAccessionPattern;
import uk.ac.ebi.pride.prider.dataprovider.identification.ProteinReferenceProvider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.SocketException;
import java.util.*;

/**
 * @author Jose A. Dianes
 * @version $Id$
 *
 * Note: synonyms will include the source accession itself
 *
 */
public class ProteinAccessionSynonymsFinder {
    private static final String GI_PREFIX = "gi";
    private static Logger logger = LoggerFactory.getLogger(ProteinAccessionSynonymsFinder.class.getName());

    private static final int MAX_SYNONYMS_REQUEST = 100;

    private static final String FROM_TAG = "From";
    private static final String NULL_ACCESSION_TAG = "null";
    private static final int MAX_REQUEST_TRIES = 5;
    private static final long WAIT_TIME_BEFORE_NEW_TRY_MILLIS = 1000;

    public static final String MAPPING_TOOL_PARAM = "mapping";
    public static final String TAB_MAPPING_TOOL_FORMAT = "tab"; // list format will return one 'accession[tab]mapping' per line, useful for batches
    public static final String UNIPROT_MAPPING_SERVICE_URL = "http://www.uniprot.org";

    public static final String ENSMBL_PROTEIN_TAG = "ENSEMBL_PRO_ID";
    public static final String UNIPROT_KB_ID_TAG = "ID";
    public static final String UNIPROT_KB_ACC_TAG = "ACC";
    public static final String UNIPROT_KB_ACC_ID_TAG = "ACC+ID";
    public static final String GI_NUMBER_TAG = "P_GI";
    public static final String REF_SEQ_PROTEIN_TAG = "P_REFSEQ_AC";
    public static final String UNI_PARC_TAG = "UPARC";
    public static final String IPI_TAG = "IPI";

    private static final String IPI_FILE_PATH = "ipi/last-UniProtKB2IPI.map";

    private static final IpiMapper ipiMappings = new IpiMapper(IPI_FILE_PATH);

    /**
     * Given a collection of protein accessions, returns a map of synonyms for them
     * @param accessions
     * @return a Map from the original protein accessions to their synonyms
     * @throws IOException
     */
    public static Map<String, TreeSet<String>> findProteinSynonymsForAccession(Set<String> accessions) throws IOException {
        Map<String, TreeSet<String>> res = new HashMap<String, TreeSet<String>>();

        Map<String, TreeSet<String>> accessionsByDb = groupAccessionsByDb(accessions);

        // first, we get Uniprot accessions for each of the accessions
        if ( accessionsByDb != null && accessionsByDb.size()>0 ) {

            // TODO: review this, we need to add as destination DB as well
            if (accessionsByDb.containsKey(IPI_TAG)) {
                Map<String, TreeSet<String>> ipiMappingsMap = new HashMap<String, TreeSet<String>>();

                long totalIpiMappings = 0;

                for ( String ipiAccession: accessionsByDb.get(IPI_TAG) ) {
                    TreeSet<String> ipiMappingsForAccession = ipiMappings.getMappingsForIpiAccession(ipiAccession);
                    if (ipiMappingsForAccession == null)
                        ipiMappingsForAccession = new TreeSet<String>();
                    ipiMappingsForAccession.add(ipiAccession); // the accession itself is considered its synonym in our domain
                    ipiMappingsMap.put(ipiAccession, ipiMappingsForAccession);
                    totalIpiMappings = totalIpiMappings + ipiMappingsForAccession.size();
                }
                addAll(res, ipiMappingsMap);
                logger.debug("Found a total of " + totalIpiMappings + " UniProt accession mappings for " + accessionsByDb.get(IPI_TAG).size() + " IPI accessions");
                accessionsByDb.remove( IPI_TAG );
            }

            // now we are going to get mappings to Uniprot accessions for each of the originally submitted accessions, excluding IPI that
            // are taken from the mapping file
            Map<String, TreeSet<String>> uniprotToOriginals = new HashMap<String, TreeSet<String>>();
            Set<String> allUniprot = new TreeSet<String>();
            for (String db: accessionsByDb.keySet()) {
                Map<String, TreeSet<String>> mappings = getMappings(db, UNIPROT_KB_ACC_TAG, accessionsByDb.get(db));
                // update the inverted
                addAllInverted(uniprotToOriginals,mappings);
                // update the all uniprot
                addAllFlat(allUniprot,mappings);
                // update result
                addAll(res, mappings);
            }

            // now we need to get synonyms for each of the other considered DBs
            // for that we will use the already obtained Uniprot accessions
            Map<String, TreeSet<String>> uniProtMappings = new HashMap<String, TreeSet<String>>();
            addAll(uniProtMappings, getMappings(UNIPROT_KB_ACC_ID_TAG, ENSMBL_PROTEIN_TAG, allUniprot));
            addAll(uniProtMappings, getMappings(UNIPROT_KB_ACC_ID_TAG, GI_NUMBER_TAG, allUniprot));
            addAll(uniProtMappings, getMappings(UNIPROT_KB_ACC_ID_TAG, UNIPROT_KB_ACC_TAG, allUniprot));
            addAll(uniProtMappings, getMappings(UNIPROT_KB_ACC_ID_TAG, UNIPROT_KB_ID_TAG, allUniprot));
            addAll(uniProtMappings, getMappings(UNIPROT_KB_ACC_ID_TAG, REF_SEQ_PROTEIN_TAG, allUniprot));
            addAll(uniProtMappings, getMappings(UNIPROT_KB_ACC_ID_TAG, UNI_PARC_TAG, allUniprot));

            // now we need to merge them back to the original accessions, using the inverted map
            for (String uniprotAccession: uniProtMappings.keySet()) {
                for (String originalAccession: uniprotToOriginals.get(uniprotAccession)) {
                    res.get(originalAccession).addAll(uniProtMappings.get(uniprotAccession));
                }
            }

            logger.debug("Got " + getTotalSynonymsCount(res) + " synonyms in total for " + accessions.size() + " accessions");

        }


        return res;
    }

    /**
     * Given a protein identifications map, add the synonyms to the protein identifications in the map
     * @param proteinIdentificationsMap
     */
    public static Map<String, ProteinReferenceProvider> getAllSynonyms(Map<String, ? extends ProteinReferenceProvider> proteinIdentificationsMap) {
        return getAllSynonyms(proteinIdentificationsMap, proteinIdentificationsMap.keySet());
    }

    /**
     * Given a protein identifications map, and a list of accessions to consider, add the synonyms to the protein identifications
     * in the map. This is a convenience method that allows filtering out some elements on the map.
     * @param proteinIdentificationsMap
     * @param accessionSynonymsToFind
     */
    public static Map<String, ProteinReferenceProvider> getAllSynonyms(Map<String, ? extends ProteinReferenceProvider> proteinIdentificationsMap, Set<String> accessionSynonymsToFind) {
        Map<String, ProteinReferenceProvider> res = new HashMap<String, ProteinReferenceProvider>();

        try {
            Map<String, TreeSet<String>> synonyms = ProteinAccessionSynonymsFinder.findProteinSynonymsForAccession(accessionSynonymsToFind);

            if (synonyms != null) {
                for (String accession: synonyms.keySet()) {
                    logger.debug("Adding synonyms to protein " + accession);
                    ProteinReferenceProvider theIdentification = proteinIdentificationsMap.get(accession);
                    if ( theIdentification == null) {
                        String giAccession = "gi|" + accession;
                        logger.debug("Trying again for GI accession " + giAccession + " from trimmed accession " + accession);
                        theIdentification = proteinIdentificationsMap.get(giAccession);
                        if (theIdentification == null) {
                            giAccession = giAccession + "|";
                            logger.debug("Trying again for GI accession " + giAccession + " from trimmed accession " + accession);
                            theIdentification = proteinIdentificationsMap.get(giAccession);
                            if (theIdentification != null) {
                                logger.debug("Got identification " + theIdentification.getAccession());
                            } else logger.debug("Still cannot get identification for accession " + accession);
                        }
                    }

                    if (theIdentification != null) {
                        ProteinIdentified proteinIdentified = new ProteinIdentified();
                        proteinIdentified.setAccession(theIdentification.getAccession());
                        proteinIdentified.setSynonyms(synonyms.get(accession));
                        res.put(proteinIdentified.getAccession(),proteinIdentified);

                        if (theIdentification.getAccession().startsWith("gi")) { // TODO - remove
                            if (proteinIdentified.getSynonyms() != null) {
                                logger.debug("Found " + proteinIdentified.getSynonyms().size() +
                                    " synonyms for accession " + accession);
                            } else {
                                logger.error("CAUTION: there are no synonym list for protein with accession " + accession);
                            }
                        }
                    } else {
                        logger.error("Trying to add synonyms to a not found protein with accession: " + accession);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return res;
    }

    /**
     * Finds the DB for a given protein accession. Currently considered DBs include GI, ENSMBL, UNIPROT, UNIPARC, and IPI
     * @param accession
     * @return
     */
    public static String findDb(String accession) {
        String from = null;
        // trim gi accessions
        if (accession.startsWith(GI_PREFIX)) {
            String giAccession = trimGiAccession(accession);
            from = GI_NUMBER_TAG;
        } else if ( ProteinAccessionPattern.isGIAccession(accession) ) {
            from = GI_NUMBER_TAG;
        } else if ( ProteinAccessionPattern.isEnsemblAccession(accession) ) {
            from = ENSMBL_PROTEIN_TAG;
        } else if ( ProteinAccessionPattern.isRefseqAccession(accession) ) {
            from = REF_SEQ_PROTEIN_TAG;
        } else if ( ProteinAccessionPattern.isSwissprotAccession(accession) ) {
            from = UNIPROT_KB_ACC_TAG;
        } else if ( ProteinAccessionPattern.isSwissprotEntryName(accession) ) {
            from = UNIPROT_KB_ID_TAG;
        } else if ( ProteinAccessionPattern.isUniparcAccession(accession) ) {
            from = UNI_PARC_TAG;
        } else if ( ProteinAccessionPattern.isIPIAccession(accession) ) {
            from = IPI_TAG;
        }

        return from;
    }

    /**
     * Returns the number from a gi accession
     * @param accession
     * @return
     */
    public static String trimGiAccession(String accession) {
        String[] accessionTokens = accession.split("\\|");
        if ("gi".equals(accessionTokens[0]) && accessionTokens.length>1) {
            return accessionTokens[1];
        } else {
            return accession;
        }
    }


    /**
     * Will group accessions by DB, while filtering out those accessions from not considered DBs
     * @param accessions
     * @return
     */
    private static Map<String, TreeSet<String>> groupAccessionsByDb(Set<? extends String> accessions) {
        // first we need to group accessions by DB
        Map<String, TreeSet<String>> accessionsByDb = new HashMap<String, TreeSet<String>>();

        for (String accession: accessions) {
            String accessionDb = findDb(accession);
            if (accessionDb != null) {
                if (accessionsByDb.containsKey(accessionDb)) {
                    accessionsByDb.get(accessionDb).add(accession);
                } else {
                    accessionsByDb.put(accessionDb, new TreeSet<String>(Arrays.asList(accession)));
                }
            }
        }

        return accessionsByDb;
    }

    private static Map<String, TreeSet<String>> getMappings(String from, String to, Set<String> accessions) throws IOException {
        long totalSynonyms = 0;

        if (accessions!=null && accessions.size()>0) {
            int fromIndex = 0;
            int toIndex = Math.min(accessions.size()-1,MAX_SYNONYMS_REQUEST);
            Map<String, TreeSet<String>> res = new HashMap<String, TreeSet<String>>();

            while ( toIndex < accessions.size() && fromIndex<=toIndex ) {
                // lazy instantiation of a RestTemplate to use the UniProt service
                RestTemplate restTemplate = new RestTemplate();
                // set the params
                Map<String, String> parameters = buildQueryParams(from,to,accessions,fromIndex,toIndex);
                logger.debug("Built query for accessions from " + fromIndex + " to index " + toIndex + " (of " + accessions.size() + " accessions)");
                // build the url
                String url = UNIPROT_MAPPING_SERVICE_URL
                        + "/{tool}?from={from}&to={to}&format={format}&query={query}";
                // invoke the service
                invokeUniprotService(restTemplate,url,parameters,res,totalSynonyms);

                fromIndex = toIndex + 1;
                toIndex = Math.min( accessions.size()-1, toIndex + MAX_SYNONYMS_REQUEST );
            }

            logger.debug("Found a total of " + totalSynonyms + " for " + accessions.size() + " accessions from database " + from + " to database " + to);
            return res;
        } else {
            return null;
        }
    }

    private static Map<String, String> buildQueryParams(String from, String to, Set<String> accessions, int fromIndex, int toIndex) {

        Map<String, String> parameters = new HashMap<String, String>();

        parameters.put("tool", MAPPING_TOOL_PARAM);
        parameters.put("from", from);
        parameters.put("to", to);
        parameters.put("format", TAB_MAPPING_TOOL_FORMAT);
        String query = new String();


        for ( String accession: new LinkedList<String>(accessions).subList(fromIndex,toIndex+1) ) {
            query = query + accession + " ";
        }


        if (query.length()>0)
            query.substring(0,query.length()-1);

        parameters.put("query", query);

        return parameters;
    }

    private static void invokeUniprotService(
            RestTemplate restTemplate,
            String url,
            Map<String, String> parameters,
            Map<String, TreeSet<String>> res,
            long totalSynonyms
    ) throws IOException {
        int numTries = 0;
        boolean success = false;
        while (!success && numTries<MAX_REQUEST_TRIES) {
            try {
                String response = restTemplate.getForObject(url, String.class, parameters);
                // process
                if (response != null) {
                    BufferedReader reader = new BufferedReader(new StringReader(response));
                    String line = reader.readLine();
                    if (line!= null && line.startsWith(FROM_TAG)) {
                        line = reader.readLine(); // skip the first line, the header
                    }
                    while ( line != null && !"null".equals(line)) {
                        String[] lineTokens = line.split("\t");
                        if (lineTokens.length>1) {
                            logger.debug(line);
                            String accession = lineTokens[0];
                            String aMapping = lineTokens[1];
                            logger.debug("Read response line for ACCESSION: " + accession + " FROM:" + parameters.get("from") + " TO:" + parameters.get("to") + " (should be an accession): " + aMapping);
                            logger.debug("Original query: " + parameters.get("query") + " format: " + parameters.get("format3"));
                            if (res.containsKey(accession) && !NULL_ACCESSION_TAG.equals(aMapping)) {
                                res.get(accession).add(aMapping);
                            } else if (!NULL_ACCESSION_TAG.equals(aMapping)) {
                                res.put(
                                        accession, new TreeSet(Arrays.asList(aMapping))
                                );
                            }

                            totalSynonyms++;
                        }

                        line = reader.readLine();
                    }
                } else {
                    logger.debug("Response NULL from uniprot mapping service for query " + parameters.get("query") + " from database " + parameters.get("from") + " to database " + parameters.get("to"));
                }
                success = true;
            } catch (HttpClientErrorException clientErrorException) {
                logger.error("TRY " + numTries + ": Could not send request to " + url + " for query " + parameters.get("query") + " from database " + parameters.get("from") + " to database " + parameters.get("to"));
                // wait for a sec before trying again
                try {
                    Thread.sleep(WAIT_TIME_BEFORE_NEW_TRY_MILLIS);
                } catch (InterruptedException e) {
                    logger.error("I cannot wait!!");
                }
                numTries++;
            } catch (SocketException se) {
                logger.error("TRY " + numTries + ": Could not send request to " + url + " for query " + parameters.get("query") + " from database " + parameters.get("from") + " to database " + parameters.get("to"));
                // wait for a sec before trying again
                try {
                    Thread.sleep(WAIT_TIME_BEFORE_NEW_TRY_MILLIS);
                } catch (InterruptedException e) {
                    logger.error("I cannot wait!!");
                }
                numTries++;
            } catch (Exception e) {
                logger.error("TRY " + numTries + ": Could not send request to " + url + " for query " + parameters.get("query") + " from database " + parameters.get("from") + " to database " + parameters.get("to") + "Unknown exception:");
                e.printStackTrace();

                // wait for a sec before trying again
                try {
                    Thread.sleep(WAIT_TIME_BEFORE_NEW_TRY_MILLIS);
                } catch (InterruptedException ie) {
                    logger.error("I cannot wait!!");
                }
                numTries++;
                if (numTries<MAX_REQUEST_TRIES)
                    logger.error("Will try again...");
            }
        }
    }

    private static long getTotalSynonymsCount(Map<String, TreeSet<String>> synonyms) {
        long res = 0;

        for (Map.Entry<String, TreeSet<String>> synonymsEntry: synonyms.entrySet()) {
            res = res + synonymsEntry.getValue().size();
        }

        return res;
    }

    /**
     * This method assumes that mappings can be null
     * @param targetMap
     * @param sourceMap
     */
    private static void addAll(Map<String, TreeSet<String>> targetMap, Map<String, TreeSet<String>> sourceMap) {
        if (sourceMap != null) {
            for (String accession: sourceMap.keySet()) {
                if ( !NULL_ACCESSION_TAG.equals(accession) ) {
                    if (targetMap.containsKey(accession)) {
                        targetMap.get(accession).addAll(sourceMap.get(accession));
                    } else {
                        targetMap.put(accession, new TreeSet<String>(sourceMap.get(accession)));
                    }
                }
            }
        }
    }

    private static void addAllInverted(Map<String, TreeSet<String>> invertedMap, Map<String, TreeSet<String>> originalMap) {
        for (Map.Entry<String, TreeSet<String>> mapEntry: originalMap.entrySet()) {
            for (String setElement: mapEntry.getValue()) {
                if (!invertedMap.containsKey(setElement)) {
                    invertedMap.put(setElement,new TreeSet<String>());
                }
                invertedMap.get(setElement).add(mapEntry.getKey());
            }
        }
    }

    private static void addAllFlat(Set<String> flat, Map<String, TreeSet<String>> originalMap) {
        for (Map.Entry<String, TreeSet<String>> mapEntry: originalMap.entrySet()) {
            flat.addAll(mapEntry.getValue());
        }
    }
}
