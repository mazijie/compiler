package Parser.NonTerminators;

import Lexer.Token;
import utils.IOUtils;

import java.io.IOException;
import java.util.List;

import Parser.*;

public class VarDef {
    private Token ident;
    private List<Token> left;
    private List<Token> right;
    private List<ConstExp> constExps;
    private Token equal;
    private InitVal initVal;
    public VarDef(Token ident, List<Token> left, List<ConstExp> constExps,
                  List<Token> right,Token equal, InitVal initVal) {
        this.ident = ident;
        this.left = left;
        this.right = right;
        this.equal = equal;
        this.initVal = initVal;
        this.constExps=constExps;
    }
    public void print() throws IOException {
        IOUtils.write(ident.toString());
        if(constExps!=null){
            for(int i=0;i<constExps.size();i++){
                IOUtils.write(left.get(i).toString());
                constExps.get(i).print();
                IOUtils.write(right.get(i).toString());
            }
        }
        if(equal!=null){
            IOUtils.write(equal.toString());
            initVal.print();
        }
        IOUtils.write(Parser.NTTypesToString.get(NTTypes.VarDef));
    }
}
