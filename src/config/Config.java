package config;

import utils.IOUtils;

import java.io.IOException;
import java.io.PrintStream;



public class Config {
    public static String fileInPath = "testfile.txt";
    public static String fileOutPath = "output.txt";
    public static String stdOutPath = "stdout.txt";
    public static String errorOutPath = "error.txt";

    public static void init() throws IOException {
        IOUtils.clear(fileOutPath);
        IOUtils.clear(errorOutPath);
        System.setOut(new PrintStream(stdOutPath));
    }
}
