package uk.ac.ebi.pride.proteinindex.search.synonyms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeSet;
import java.util.regex.Pattern;

/**
 * @author Jose A. Dianes
 * @version $Id$
 *
 * Note: this class is handling its own IOException. As a consequence, it might be initialized with an empty mappings MAP
 */
public class IpiMapper {

    private static Logger logger = LoggerFactory.getLogger(IpiMapper.class.getName());

    private static final String HEADER_LINE_PATTERN = "^UniProtKB\\s+IPI$";

    private Map<String, TreeSet<String>> mappings;

    private InputStream mappingsFile;

    public IpiMapper(String ipiFilePath) {
        try {
            mappingsFile = IpiMapper.class.getClassLoader().getResourceAsStream(ipiFilePath);
            mappings = new HashMap<String, TreeSet<String>>();
            buildMappingsFromFile();
            logger.info("IPI-UniProt mappings file contains " + mappings.size() + " entries");
        } catch (Exception e) {
            logger.error("Cannot create IPI mappings at " + ipiFilePath);
            logger.error("Reason: ");
            e.printStackTrace();
        }
    }

    public TreeSet<String> getMappingsForIpiAccession(String ipiAccession) {
        return mappings.get(ipiAccession);
    }

    private void buildMappingsFromFile() throws IOException {

        Pattern headerPattern = Pattern.compile(HEADER_LINE_PATTERN);

        // skip all the comments, reach the first line after the header
        BufferedReader reader = new BufferedReader( new InputStreamReader(mappingsFile) );

        String line = reader.readLine();

        while ( line!=null && !headerPattern.matcher(line).find() ) {
            line = reader.readLine();
            logger.debug("Skipping: " + line);
        }

        if (line != null) {
            // skip dashed line, keep the next
            reader.readLine();
            line = reader.readLine();

            while (line != null) {
                logger.debug("Read: " + line);
                String[] tokens = line.split("\\s+");
                String ipiAccession = tokens[1];
                String uniprotAccession = tokens[0];
                if ( !mappings.containsKey(ipiAccession) ) {
                    mappings.put(ipiAccession, new TreeSet<String>());
                }
                mappings.get(ipiAccession).add(uniprotAccession);
                logger.debug("Added: IPI " + ipiAccession + " to UniProt " + uniprotAccession);
                // next line
                line = reader.readLine();
            }
        }
    }

}
