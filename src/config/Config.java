package config;

import utils.IOUtils;

import java.io.IOException;
import java.io.PrintStream;



public class Config {
    public static String fileInPath = "testfile.txt";
    public static String fileOutPath = "mips.txt";
    public static String stdOutPath = "stdout.txt";
    public static String errorOutPath = "error.txt";

    public static Target target = Target.MIPS;//目标步骤
    public static boolean llvm_optimize = true;//是否中端优化
    public static boolean mips_optimize = false;//是否后端优化
    public static boolean error_detect = true;//是否进行错误处理

    public static String fuzzyName = "hqh";//模糊命名，避免与保留字冲突

    public static void init() throws IOException {
        IOUtils.clear(fileOutPath);
        IOUtils.clear(errorOutPath);
//        System.setOut(new PrintStream(stdOutPath));
    }
}
