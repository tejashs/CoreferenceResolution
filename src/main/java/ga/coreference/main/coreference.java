package ga.coreference.main;

import org.apache.log4j.BasicConfigurator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

/**
 * Created by tejas on 07/11/16.
 */
public class coreference {

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.out.println("Program Arguments don't match");
            System.out.println("Please Enter in the following format:");
            System.out.println("coreference <listFile> <responseDir>");
            return;
        }
        String pathForListOfFiles = args[0];
        String directoryForResponseFiles = args[1];

        File fileToRead = new File(pathForListOfFiles);
        Scanner s = new Scanner(fileToRead);
        while (s.hasNextLine()) {
            String fileName = s.nextLine();
            CoreferenceResolution resolver = new CoreferenceResolution();
            BasicConfigurator.configure();
            String output = resolver.parseInputFile(fileName);
            String[] filenameArray = fileName.split("\\.");
            String fullFilePath = filenameArray[0];
            String[] temp = fullFilePath.split(File.separator);
            String filenamePrefix = temp[temp.length - 1];
            String fileToPrint = directoryForResponseFiles + File.separator + filenamePrefix + "." + "response";
            PrintWriter out = new PrintWriter(new FileWriter(fileToPrint));
            out.print(output);
            out.flush();
            out.close();
        }
    }
}
