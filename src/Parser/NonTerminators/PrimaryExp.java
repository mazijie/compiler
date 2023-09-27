package Parser.NonTerminators;

import Lexer.Token;
import Parser.Parser;
import utils.IOUtils;

import java.io.IOException;
import Parser.*;

public class PrimaryExp {
    private Token left;
    private Token right;
    private Exp exp;
    private LVal lVal;
    private Number number;
    public PrimaryExp(Token left, Token right,Exp exp,LVal lVal,Number number) {
        this.left = left;
        this.right = right;
        this.exp = exp;
        this.lVal = lVal;
        this.number = number;
    }
    public void print() throws IOException {
        if(left!=null){
            IOUtils.write(left.toString());
            exp.print();
            IOUtils.write(right.toString());
        }else if(lVal!=null){
            lVal.print();
        }else{
            number.print();
        }
        IOUtils.write(Parser.NTTypesToString.get(NTTypes.PrimaryExp));
    }
}
