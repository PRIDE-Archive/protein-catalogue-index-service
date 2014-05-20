package uk.ac.ebi.pride.proteinindex.search.util;

import java.io.File;
import java.io.FilenameFilter;

/**
 * @author Jose A. Dianes
 * @version $Id$
 */
public class MzTabFileNameFilter implements FilenameFilter {
    @Override
    public boolean accept(File file, String s) {
        return s.toLowerCase().endsWith(".mztab");
    }
}
