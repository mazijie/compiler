package Parser.NonTerminators;

import Lexer.Token;
import utils.IOUtils;

import java.io.IOException;
import java.util.List;

import Parser.*;

public class InitVal {
    private int type;
    public Exp exp;
    private Token left;
    public List<InitVal> initVals;
    private Token right;
    private List<Token> commas;
    public InitVal(Exp exp){
        this.type=1;
        this.exp=exp;
    }
    public InitVal(Token left, List<InitVal> initVals,Token right,List<Token> commas){
        this.type=2;
        this.left=left;
        this.right=right;
        this.commas=commas;
        this.initVals=initVals;
    }
    public void print() throws IOException {
        if(type==2){
            IOUtils.write(left.toString());
            for(int i=0;i<initVals.size();i++){
                if(i>=1)
                {
                    IOUtils.write(commas.get(i-1).toString());
                }
                initVals.get(i).print();
            }
            IOUtils.write(right.toString());
        }else{
            exp.print();
        }
        IOUtils.write(Parser.NTTypesToString.get(NTTypes.InitVal));
    }
}
