package Parser.NonTerminators;

import Lexer.Token;
import utils.IOUtils;

import Parser.*;

import java.io.IOException;

public class MulExp {
    //MulExp → UnaryExp | MulExp ('*' | '/' | '%') UnaryExp
    //去除左递归：MulExp → UnaryExp | UnaryExp ('*' | '/' | '%') MulExp
    public UnaryExp unaryExp;
    public Token op;
    public MulExp mulExp;
    public MulExp(UnaryExp unaryExp, Token op,MulExp mulExp){
        this.unaryExp = unaryExp;
        this.op = op;
        this.mulExp = mulExp;
    }
    public void print() throws IOException {
        unaryExp.print();
        IOUtils.write(Parser.NTTypesToString.get(NTTypes.MulExp));
        if(op!=null){
            IOUtils.write(op.toString());
            mulExp.print();
        }
    }
}
