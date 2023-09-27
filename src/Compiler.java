import Exceptions.CompileException;
import Parser.Parser;
import config.Config;
import Lexer.Lexer;
import utils.IOUtils;

import java.io.IOException;

public class Compiler {
    public static void main(String[] args) {
        try {
            Config.init();
            Lexer.analyze(IOUtils.read(Config.fileInPath));
//            Lexer.printLexAns();

//            System.out.println("build finished");
//            Parser.printResult();
        }catch (IOException e) {
            System.out.println("Error(IO):" + e);
        }catch(CompileException e){
            System.out.println(e.getMessage());
        }
        catch (Exception e) {
            System.out.println(e.toString());
        }
        try{
            Parser.buildTree(Lexer.getTokens());
            Parser.printResult();
        }catch (IOException | CompileException e){
            System.out.println(e.getMessage());
        }

    }
}