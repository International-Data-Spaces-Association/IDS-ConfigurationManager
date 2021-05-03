package de.fraunhofer.isst.configmanager.util.camel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class RouteFileHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(RouteFileHelper.class);

    @Value("${camel.xml-routes.directory}")
    private String filePath;

    /**
     * Writes a given string to a file in the directory specified in application.properties. Creates
     * the file if it does not exist.
     *
     * @param fileName the filename
     * @param content the content to write to the file
     * @throws IOException if the file cannot be created or written
     */
    public void writeToFile(String fileName, String content) throws IOException {
        File file = new File(filePath + File.separator +  fileName);

        try (FileWriter fileWriter = new FileWriter(file);
             BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {

            if (!file.exists() && !file.createNewFile()) {
                LOGGER.error("Could not create file '{}{}{}'", filePath, File.separator, fileName);
            }

            bufferedWriter.write(content);
            LOGGER.info("Successfully created file '{}{}{}'.", filePath, File.separator, fileName);

        } catch (IOException e) {
            LOGGER.error("Cannot write to file '{}{}{}' because an IO error occurred: {}",
                    filePath, File.separator, fileName, e.toString());
            throw e;
        }
    }

    /**
     * Deletes a file with a given name in the directory specified in application.properties.
     *
     * @param name the filename
     * @throws IOException if the file cannot be deleted
     */
    public void deleteFile (String name) throws IOException {
        Path file = Paths.get(filePath + name);
        if (Files.exists(file)) {
            try {
                Files.delete(file);
                LOGGER.info("Successfully deleted file '{}{}{}'.", filePath, File.separator, name);
            } catch (NoSuchFileException e) {
                LOGGER.error("Cannot delete file '{}{}{}' because file does not exist.", filePath,
                        File.separator, name, e);
                throw e;
            } catch (IOException e) {
                LOGGER.error("Cannot delete file '{}{}{}' because an IO error occurred.", filePath,
                        File.separator, name, e);
                throw e;
            }
        }
    }

}
