package uk.ac.ebi.pride.proteinindex.search.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.pride.jmztab.model.MZTabFile;
import uk.ac.ebi.pride.jmztab.model.Protein;
import uk.ac.ebi.pride.jmztab.utils.MZTabFileParser;
import uk.ac.ebi.pride.jmztab.utils.errors.MZTabException;
import uk.ac.ebi.pride.proteinindex.search.model.ProteinIdentified;
import uk.ac.ebi.pride.tools.protein_details_fetcher.ProteinDetailFetcher;
import uk.ac.ebi.pride.tools.utils.AccessionResolver;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @author Jose A. Dianes
 * @version $Id$
 */
public class ProteinBuilder {

    private static final int PROCESSING_ACCESSIONS_STEP = 50;
    private static Logger logger = LoggerFactory.getLogger(ProteinBuilder.class.getName());

    private static ErrorLogOutputStream errorLogOutputStream = new ErrorLogOutputStream(logger);

    /**
     * mzTab files in the directory will have names such as PRIDE_Exp_Complete_Ac_28654.submissions. We are interested in the
     * assay accession, the last bit if we split by '_'.
     * @return A map of assay accessions to peptide identifications
     * @throws java.io.IOException
     */
    @Deprecated
    public static Map<String, LinkedList<ProteinIdentified>> readProteinIdentificationsFromMzTabFilesDirectory(File mzTabFilesDirectory) throws IOException, MZTabException {

        Map<String, LinkedList<ProteinIdentified>> res =
                new HashMap<String, LinkedList<ProteinIdentified>>();

        File[] mzTabFilesInDirectory = mzTabFilesDirectory.listFiles(new MzTabFileNameFilter());
        if (mzTabFilesInDirectory != null) {
            for (File tabFile: mzTabFilesInDirectory) {
                MZTabFileParser mzTabFileParser = new MZTabFileParser(tabFile, errorLogOutputStream);
                if (mzTabFileParser != null) {
                    // get all the peptide identifications from the file
                    MZTabFile mzTabFile = mzTabFileParser.getMZTabFile();
                    if (mzTabFile != null) {
                        // get assay accession
                        String assayAccession = tabFile.getName().split("[_\\.]")[4];
                        // get proteins
                        LinkedList<ProteinIdentified> assayProteinIdentifieds = new LinkedList<ProteinIdentified>();
                        Collection<Protein> mzTabProteins = mzTabFile.getProteins();
                        for (Protein mzTabProtein: mzTabProteins) {
                            ProteinIdentified proteinIdentified = new ProteinIdentified();
                            proteinIdentified.setSynonyms(new TreeSet<String>());
                            proteinIdentified.setProjectAccessions(new TreeSet<String>());
                            proteinIdentified.setAssayAccessions(new TreeSet<String>());
                            String correctedAccession = getCorrectedAccession(mzTabProtein.getAccession(), mzTabProtein.getDatabase());
                            proteinIdentified.setAccession(correctedAccession);
                            assayProteinIdentifieds.add(proteinIdentified);

                        }
                        // add protein details
                        addProteinDetails(assayProteinIdentifieds);

                        // add assay proteins to the result
                        res.put(assayAccession, assayProteinIdentifieds);
                        logger.debug("Found " + assayProteinIdentifieds.size() + " protein identifications for Assay " + assayAccession + " in file " + tabFile.getAbsolutePath());
                    } else {
                        mzTabFileParser.getErrorList().print(errorLogOutputStream);
                    }
                }
            }
        }

        return res;
    }

    public static List<ProteinIdentified> readProteinIdentificationsFromMzTabFile(String assayAccession, MZTabFile tabFile) {

        List<ProteinIdentified> res = new LinkedList<ProteinIdentified>();

        if (tabFile != null) {
            // get proteins
            Collection<Protein> mzTabProteins = tabFile.getProteins();
            for (Protein mzTabProtein: mzTabProteins) {
                ProteinIdentified proteinIdentified = new ProteinIdentified();
                proteinIdentified.setSynonyms(new TreeSet<String>());
                proteinIdentified.setProjectAccessions(new TreeSet<String>());
                proteinIdentified.setAssayAccessions(new TreeSet<String>());
                String correctedAccession = getCorrectedAccession(mzTabProtein.getAccession(), mzTabProtein.getDatabase());
                proteinIdentified.setAccession(correctedAccession);
                res.add(proteinIdentified);
            }

            // add protein details

            addProteinDetails(res);


            logger.debug("Found " + res.size() + " protein identifications for Assay " + assayAccession + " in file " + tabFile);
        } else {
            logger.error("Passed null mzTab file to protein identifications reader");
        }

        return res;
    }

    public static void addProteinDetails(List<ProteinIdentified> proteins) {
        // build accession list to reduce the number of fetching requests
        List<String> accessions = new LinkedList<String>();
        for (ProteinIdentified protein: proteins) {
            accessions.add(protein.getAccession());
        }
        try {
            // get protein details (e.g. sequence, name)
            ProteinDetailFetcher proteinDetailFetcher = new ProteinDetailFetcher();

            HashMap<String, uk.ac.ebi.pride.tools.protein_details_fetcher.model.Protein> details = new HashMap<String, uk.ac.ebi.pride.tools.protein_details_fetcher.model.Protein>();

            int processedAccessions = 0;
            while (processedAccessions<accessions.size()) {
                // logging accessions
                String accessionListLog = new String();
                for (String accession: accessions.subList(processedAccessions, Math.min(accessions.size(),processedAccessions+PROCESSING_ACCESSIONS_STEP))) {
                    accessionListLog = accessionListLog + " <" + accession +">";
                }
                logger.info("accession list is: " + accessionListLog);

                details.putAll(
                        proteinDetailFetcher.getProteinDetails(
                                accessions.subList(processedAccessions, Math.min(accessions.size(),processedAccessions+PROCESSING_ACCESSIONS_STEP))
                        )
                );

                logger.info("Processed accessions: " + processedAccessions + " of " + accessions.size());
                logger.info("Next step: " + (processedAccessions+PROCESSING_ACCESSIONS_STEP));
                logger.info("Got details for up to " + details.size() + " accessions (cumulative)");

                processedAccessions = processedAccessions+PROCESSING_ACCESSIONS_STEP;
            }


            // add details to proteins
            for (ProteinIdentified protein: proteins) {
                if (details.containsKey(protein.getAccession())) {
                    if (details.get(protein.getAccession()).getSequenceString() != null) {
                        protein.setSequence(details.get(protein.getAccession()).getSequenceString());
                    }
                    if (details.get(protein.getAccession()).getName() != null) {
                        protein.setDescription(Arrays.asList("NAME####" + details.get(protein.getAccession()).getName()));
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Cannot retrieve protein details for " + accessions.size() + " accessions.");
            e.printStackTrace();
        }
    }

    private static String getCorrectedAccession(String accession, String database) {

        AccessionResolver accessionResolver = new AccessionResolver(accession, null, database); // we don't have versions
        String fixedAccession = accessionResolver.getAccession();

        logger.debug("Original accession " + accession + " fixed to " + fixedAccession);
        return (fixedAccession==null)?accession:fixedAccession;
    }

}
