package utils;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class FileUtils {

    public static void writeText(String content, String filepath) {
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(filepath);
            fileWriter.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fileWriter != null) try {
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String readText(String filepath) throws IOException {
        StringBuilder sb = new StringBuilder();
        FileReader fileReader = null;
        try {
            fileReader = new FileReader(filepath);
            int ch = fileReader.read();
            while (ch != -1) {
                sb.append((char) ch);
                ch = fileReader.read();
            }
        } finally {
            if (fileReader != null)
                fileReader.close();
        }
        return sb.toString();
    }
}
