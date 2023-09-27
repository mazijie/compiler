package Parser.NonTerminators;

import Lexer.Token;
import utils.IOUtils;

import java.io.IOException;
import java.util.List;

import Parser.*;

public class FuncFParams {
    private List<FuncFParam> funcFParams;
    private List<Token> commas;
    public FuncFParams(List<FuncFParam> funcFP,List<Token> commas) {
        this.funcFParams=funcFP;
        this.commas=commas;
    }
    public void print() throws IOException {
        for(int i=0;i<this.funcFParams.size();i++)
        {
            if(i>=1)
            {
                IOUtils.write(this.commas.get(i-1).toString());
            }
            this.funcFParams.get(i).print();
        }
        IOUtils.write(Parser.NTTypesToString.get(NTTypes.FuncFParams));
    }
}
