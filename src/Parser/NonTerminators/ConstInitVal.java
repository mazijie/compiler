package Parser.NonTerminators;

import Lexer.Token;
import utils.IOUtils;

import java.io.IOException;
import java.util.List;

import Parser.*;

public class ConstInitVal {
    private ConstExp constExp;
    private Token left;
    private Token right;
    private List<ConstInitVal> constInitVals;
    private List<Token> commas;

    public ConstInitVal(ConstExp constExp, Token left, Token right, List<ConstInitVal> constInitVals, List<Token> commas) {
        this.constExp = constExp;
        this.left = left;
        this.right = right;
        this.constInitVals = constInitVals;
        this.commas = commas;
    }

    public void print() throws IOException {
        if(this.constExp!=null){
            this.constExp.print();
        }else{
            IOUtils.write(this.left.toString());
            if(this.constInitVals!=null){
                for(int i = 0; i<this.constInitVals.size();i++){
                    if(i>=1) IOUtils.write(this.commas.get(i-1).toString());
                    this.constInitVals.get(i).print();
                }
//                this.constInitVals.get(this.constInitVals.size()-1).print();
            }
            IOUtils.write(this.right.toString());
        }
        IOUtils.write(Parser.NTTypesToString.get(NTTypes.ConstInitVal));
    }
}
