package Parser.NonTerminators;

import Lexer.Token;
import utils.IOUtils;

import java.io.IOException;
import java.util.List;

import Parser.*;

public class LVal {
    public Token ident;
    public List<Token> left;
    public List<Token> right;
    public List<Exp> exps;
    public LVal(Token ident, List<Token> left, List<Token> right, List<Exp> exps) {
        this.ident = ident;
        this.left = left;
        this.right = right;
        this.exps = exps;
    }
    public void print() throws IOException {
        IOUtils.write(ident.toString());
        if(left!=null){
            for(int i=0;i<left.size();i++){
                IOUtils.write(left.get(i).toString());
                exps.get(i).print();
                IOUtils.write(right.get(i).toString());
            }
        }
        IOUtils.write(Parser.NTTypesToString.get(NTTypes.LVal));
    }
}
