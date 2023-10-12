import Errors.ErrorDetect;
import Errors.ErrorRecord;
import Exceptions.CompileException;
import Parser.Parser;
import SymbolTable.TableManager;
import config.Config;
import Lexer.Lexer;
import utils.IOUtils;

import java.io.IOException;

public class Compiler {
    public static void main(String[] args) {
        try {
            Config.init();
            TableManager.init();
            Lexer.analyze(IOUtils.read(Config.fileInPath));
            Lexer.printLexAns();
            Parser.buildTree(Lexer.getTokens());
        }catch (IOException e) {
            System.out.println("Error(IO):" + e);
        } catch(Exception e){
            System.out.println(e.getMessage());
        }
        try{
            ErrorDetect.checkCompUnit(Parser.compUnit);
            ErrorRecord.printError();
        } catch (IOException e){
            System.out.println(e.getMessage());
        }

    }
}