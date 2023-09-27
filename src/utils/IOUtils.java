package utils;

import Lexer.Token;
import config.Config;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.StringJoiner;

public class IOUtils {
    public static String read(String filename) throws IOException {
        InputStream in = new BufferedInputStream(Files.newInputStream(Paths.get(filename)));
        Scanner scanner = new Scanner(in);
        StringJoiner stringJoiner = new StringJoiner("\n");
        while (scanner.hasNextLine()) {
            stringJoiner.add(scanner.nextLine());
        }
        scanner.close();
        in.close();
        return stringJoiner.toString();
    }

    public static void write(String value) throws IOException {
        FileWriter fileWriter = new FileWriter(Config.fileOutPath,true);
        fileWriter.write(value);
        fileWriter.close();
    }

    public static void clear(String path) throws IOException {
        File fileToDelete = new File(path);
        if(fileToDelete.exists()) {
            if(!fileToDelete.delete())
            {
                throw new IOException("文件未正确删除。");
            }
        }
    }
}
