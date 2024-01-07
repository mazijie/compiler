import Errors.ErrorDetect;
import Errors.ErrorRecord;
import Exceptions.CompileException;
import IR.IRModule;
import IR.IROptimizer;
import IR.Visitor;
import IR.VisitorOP;
import MIPS.GraphPaint;
//import MIPS.MIPSGenerator;
import MIPS.MIPSGeneratorOP;
import MIPS.RegisterManager;
import Parser.Parser;
import SymbolTable.TableManager;
import config.Config;
import Lexer.Lexer;
import config.Target;
import utils.Boom;
import utils.IOUtils;

import java.io.IOException;

public class Compiler {
    public static void main(String[] args) throws Exception {
//        try {
            Config.init();

            //词法分析
            Lexer.analyze(IOUtils.read(Config.fileInPath));
            if(Config.target== Target.Lexer){
                Lexer.printLexAns();
                return;
            }
//        } catch (Exception e){
//            System.out.println(e.getMessage());
//        }
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
                VisitorOP.visitCompUnit(Parser.compUnit);
                if(Config.target==Target.LLVM){
                    Visitor.printResult();
                    return;
                }
            } else{
                VisitorOP.visitCompUnit(Parser.compUnit);
                IROptimizer.optimize(Visitor.getModule());
                if(Config.target==Target.LLVM){
                    VisitorOP.printResult();
                    return;
                }
            }

            //目标代码生成
//            if(!Config.mips_optimize){
//                RegisterManager.init();
//                MIPSGenerator.genMips(Visitor.getModule());
//            }else{
                if(Config.mips_optimize) GraphPaint.buildGraph(Visitor.getModule());
                MIPSGeneratorOP.genMips(Visitor.getModule());
//            }
    }
}