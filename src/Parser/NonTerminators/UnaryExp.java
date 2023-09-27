package Parser.NonTerminators;

import Lexer.Token;
import utils.IOUtils;

import java.io.IOException;
import Parser.*;

public class UnaryExp {
    private PrimaryExp primaryExp;
    private Token ident;
    private Token left;
    private Token right;
    private FuncRParams funcRParams;

    private UnaryExp unaryExp;
    private UnaryOp unaryOp;
    public UnaryExp(PrimaryExp primaryExp,Token ident,Token left,Token right, FuncRParams funcRParams, UnaryOp unaryOp, UnaryExp unaryExp){
        this.primaryExp = primaryExp;
        this.ident = ident;
        this.left = left;
        this.right = right;
        this.funcRParams = funcRParams;
        this.unaryExp=unaryExp;
        this.unaryOp=unaryOp;
    }
    public void print() throws IOException {
        if(primaryExp!=null){
            primaryExp.print();
        }else if(ident!=null){
            IOUtils.write(ident.toString());
            IOUtils.write(left.toString());
            if(funcRParams!=null) funcRParams.print();
            IOUtils.write(right.toString());
        }else{
            unaryOp.print();
            unaryExp.print();
        }
        IOUtils.write(Parser.NTTypesToString.get(NTTypes.UnaryExp));
    }
}
