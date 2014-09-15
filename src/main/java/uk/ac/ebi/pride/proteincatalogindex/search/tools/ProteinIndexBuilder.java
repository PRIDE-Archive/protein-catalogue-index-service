package uk.ac.ebi.pride.proteincatalogindex.search.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;
import uk.ac.ebi.pride.jmztab.model.MZTabFile;
import uk.ac.ebi.pride.jmztab.utils.MZTabFileParser;
import uk.ac.ebi.pride.archive.dataprovider.file.ProjectFileSource;
import uk.ac.ebi.pride.archive.dataprovider.project.ProjectProvider;
import uk.ac.ebi.pride.archive.repo.assay.AssayRepository;
import uk.ac.ebi.pride.archive.repo.file.ProjectFile;
import uk.ac.ebi.pride.archive.repo.file.ProjectFileRepository;
import uk.ac.ebi.pride.archive.repo.project.ProjectRepository;
import uk.ac.ebi.pride.proteincatalogindex.search.indexers.ProjectProteinCatalogIndexer;
import uk.ac.ebi.pride.proteincatalogindex.search.service.ProteinCatalogIndexService;
import uk.ac.ebi.pride.proteincatalogindex.search.service.ProteinCatalogSearchService;
import uk.ac.ebi.pride.proteincatalogindex.search.util.ErrorLogOutputStream;

import java.io.File;
import java.io.IOException;
import java.util.*;


/**
 * @author Jose A. Dianes
 * @version $Id$
 */
@Component
public class ProteinIndexBuilder {

    private static Logger logger = LoggerFactory.getLogger(ProteinIndexBuilder.class.getName());
    private static ErrorLogOutputStream errorLogOutputStream = new ErrorLogOutputStream(logger);

    private static final String PRIDE_MZ_TAB_FILE_EXTENSION = ".pride.mztab";

    private static final String COMPRESS_EXTENSION = "gz";

    private static final String INTERNAL_FOLDER_NAME = ProjectFileSource.INTERNAL.getFolderName();
    private static final String GENERATED_FOLDER_NAME = ProjectFileSource.GENERATED.getFolderName();

    @Autowired
    private File submissionsDirectory;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectFileRepository projectFileRepository;

    @Autowired
    private AssayRepository assayRepository;

    @Autowired
    private ProteinCatalogSearchService proteinCatalogSearchService;

    @Autowired
    private ProteinCatalogIndexService proteinCatalogIndexService;


    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("spring/app-context.xml");

        ProteinIndexBuilder proteinIndexBuilder = context.getBean(ProteinIndexBuilder.class);

        try {
            if ("index".equals(args[0])) {
                indexProteins(proteinIndexBuilder);
            } else if ("delete".equals(args[0])) {
                deleteAllProteins(proteinIndexBuilder);
            }
        } catch (IndexOutOfBoundsException e) {

            logger.info("Args: index OR delete is mandatory ");
        }

    }

    public static void deleteAllProteins(ProteinIndexBuilder proteinIndexBuilder) {
        // reset index
        proteinIndexBuilder.proteinCatalogIndexService.deleteAll();
        logger.info("All proteins are now DELETED (new method)");
    }

    public static void indexProteins(ProteinIndexBuilder proteinIndexBuilder) {

        // get all projects on repository
        Iterable<? extends ProjectProvider> projects = proteinIndexBuilder.projectRepository.findAll();
        logger.info("There are " + proteinIndexBuilder.projectRepository.count() + " projects in repository");

        // create the indexer
        ProjectProteinCatalogIndexer projectProteinCatalogIndexer = new ProjectProteinCatalogIndexer(proteinIndexBuilder.proteinCatalogSearchService, proteinIndexBuilder.proteinCatalogIndexService);

        // iterate through project to index protein identifications
        for (ProjectProvider project : projects) {
            logger.info("Indexing proteins for project " + project.getAccession());
//            String generatedFolderPath = ProteinBuilder.buildGeneratedDirectoryFilePath(
//                    proteinIndexBuilder.submissionsDirectory.getAbsolutePath(),
//                    project
//            );

            List<ProjectFile> projectFiles = proteinIndexBuilder.projectFileRepository.findAllByProjectId(project.getId());

            for (ProjectFile projectFile : projectFiles) {

//                logger.info("Indexing proteins for project file " + projectFile.getFileName());
                //To avoid using submitted mztab we need to filter by generated ones first
                //TODO: This will change when we have the internal file names in the database
                if (ProjectFileSource.GENERATED.equals(projectFile.getFileSource())) {
                    if (projectFile.getFileName().contains(PRIDE_MZ_TAB_FILE_EXTENSION)) {
                        String assayAccession = proteinIndexBuilder.assayRepository.findOne(projectFile.getAssayId()).getAccession();

                        String pathToMzTabFile = buildAbsoluteMzTabFilePath(
                                proteinIndexBuilder.submissionsDirectory.getAbsolutePath(),
                                project,
                                projectFile.getFileName()
                        );

                        logger.debug("Trying to open MzTab file " + pathToMzTabFile);
                        MZTabFileParser mzTabFileParser = null;
                        try {
                            mzTabFileParser = new MZTabFileParser(new File(pathToMzTabFile), errorLogOutputStream);
                            MZTabFile mzTabFile = mzTabFileParser.getMZTabFile();
                            logger.debug("Trying to index from MzTab file " + pathToMzTabFile);
                            projectProteinCatalogIndexer.indexAllProteinIdentificationsForProjectAndAssay(project.getAccession(), assayAccession, mzTabFile);
                        } catch (IOException e) {
                            logger.error("Could not open MzTab file: " + pathToMzTabFile);
                        }
                    }
                }
            }
        }
    }

//    @Deprecated
//    public static void indexProteinsOld(ProteinIndexBuilder proteinIndexBuilder, SolrServer server) {
//
//        logger.info("Server URL is : " + server.toString());
//        // get all projects on repository
//        Iterable<? extends ProjectProvider> projects = proteinIndexBuilder.projectRepository.findAll();
//
//        // reset index
//        proteinIndexBuilder.proteinIdentificationIndexService.deleteAll();
//        logger.info("All proteins are now DELETED in server " + server);
//
//        // create the indexer
//        ProjectProteinIdentificationsIndexer projectProteinIdentificationsIndexer = new ProjectProteinIdentificationsIndexer(proteinIndexBuilder.proteinIdentificationSearchService, proteinIndexBuilder.proteinIdentificationIndexService);
//
//        // iterate through project to index protein identifications
//        for (ProjectProvider project : projects) {
//
//            String generatedFolderPath = buildGeneratedDirectoryFilePath(
//                    proteinIndexBuilder.submissionsDirectory.getAbsolutePath(),
//                    project
//            );
//
//            projectProteinIdentificationsIndexer.indexAllProteinIdentifications(project.getAccession(),generatedFolderPath);
//
//        }
//    }

    //TODO: Move it to a pride-archive-utils
    public static String buildGeneratedDirectoryFilePath(String prefix, ProjectProvider project) {
        if (project.isPublicProject()) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(project.getPublicationDate());
            int month = calendar.get(Calendar.MONTH) + 1;

            return prefix
                    + File.separator + calendar.get(Calendar.YEAR)
                    + File.separator + (month < 10 ? "0" : "") + month
                    + File.separator + project.getAccession()
                    + File.separator + GENERATED_FOLDER_NAME;
        } else {
            return prefix
                    + File.separator + project.getAccession()
                    + File.separator + GENERATED_FOLDER_NAME;
        }

    }

    //TODO: Move it to a pride-archive-utils
    public static String buildAbsoluteMzTabFilePath(String prefix, ProjectProvider project, String fileName) {
        if (project.isPublicProject()) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(project.getPublicationDate());
            int month = calendar.get(Calendar.MONTH) + 1;

            return prefix
                    + File.separator + calendar.get(Calendar.YEAR)
                    + File.separator + (month < 10 ? "0" : "") + month
                    + File.separator + project.getAccession()
                    + File.separator + INTERNAL_FOLDER_NAME
                    + File.separator + translateFromGeneratedToInternalFolderFileName(fileName);
        } else {
            return prefix
                    + File.separator + project.getAccession()
                    + File.separator + INTERNAL_FOLDER_NAME
                    + File.separator + translateFromGeneratedToInternalFolderFileName(fileName);
        }

    }

    //TODO: Move it to a pride-archive-utils
    /**
     * In the generated folder(the which one we are taking the file names) the files are gzip, so we need to remove
     * the extension to have the name in the internal folder (the one that we want)
     *
     * @param fileName mztab file name in generated folder
     * @return mztab file name in internal folder
     */
    private static String translateFromGeneratedToInternalFolderFileName(String fileName) {

        if (fileName != null) {
            if (fileName.endsWith(COMPRESS_EXTENSION)) {
                fileName = fileName.substring(0, fileName.lastIndexOf("."));
            }
        }
        return fileName;
    }

}
