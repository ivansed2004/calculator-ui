package ru.sedinkin.calculator_ui.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Persistence {

    public static void persist( File file, String targetPath, String fileName ) {
        try {
            Path directory = Paths.get( targetPath );
            Path filePath = directory.resolve( fileName );
            Files.createDirectories( directory );
            File fileToReturn = Files.createFile( filePath ).toFile();
            try ( FileInputStream fis = new FileInputStream(file);
                  FileOutputStream fos = new FileOutputStream(fileToReturn)) {
                byte[] fileBytes = fis.readAllBytes();
                fos.write(fileBytes);
            }
        } catch (IOException ex) {
            System.out.println("Error occurred while creating the file...: " + ex.getMessage());
        }
    }

}
