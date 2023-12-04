import Errors.ErrorDetect;
import Errors.ErrorRecord;
import Exceptions.CompileException;
import IR.IRModule;
import IR.Visitor;
import IR.VisitorOP;
import MIPS.MIPSGenerator;
import MIPS.RegisterManager;
import Parser.Parser;
import SymbolTable.TableManager;
import config.Config;
import Lexer.Lexer;
import config.Target;
import utils.IOUtils;

import java.io.IOException;

public class Compiler {
    public static void main(String[] args) throws IOException {
        try {
            Config.init();

            //词法分析
            Lexer.analyze(IOUtils.read(Config.fileInPath));
            if(Config.target== Target.Lexer){
                Lexer.printLexAns();
                return;
            }

            //语法分析
            Parser.buildTree(Lexer.getTokens());
            if(Config.target== Target.Parser){
                Parser.printResult();
                return;
            }

            if(Config.error_detect){
                //错误处理
                TableManager.init();
                ErrorDetect.checkCompUnit(Parser.compUnit);
                ErrorRecord.printError();
                if(!ErrorRecord.isCorrect) return;
            }

            //中间代码生成
            if(!Config.llvm_optimize){
                Visitor.visitCompUnit(Parser.compUnit);
                if(Config.target==Target.LLVM){
                    Visitor.printResult();
                    return;
                }
            } else{
                VisitorOP.visitCompUnit(Parser.compUnit);
                if(Config.target==Target.LLVM){
                    VisitorOP.printResult();
                    return;
                }
            }
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
            //目标代码生成
            RegisterManager.init();
            if(!Config.mips_optimize){
                MIPSGenerator.genMips(Visitor.getModule());
            }else{
                System.out.println("很抱歉，尚未完成目标代码优化部分");
            }


    }
}