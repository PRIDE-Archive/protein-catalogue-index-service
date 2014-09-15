package uk.ac.ebi.pride.proteincatalogindex.search.util;

import java.util.List;

/**
 * User: ntoro
 * Date: 09/07/2014
 * Time: 16:23
 */
public class ProteinDetailUtils {

    public static final String NAME = "NAME####";

    public static String extractInformationByType(List<String> description, String type){

        String info = null;

        if(description != null){
            for (String s : description){
                if(s.startsWith(type)){
                    info = s.split(type)[1];
                }
            }
        }

        return info;
    }

    public static String getNameFromDescription(List<String> description){
        return extractInformationByType(description, NAME);
    }
}
