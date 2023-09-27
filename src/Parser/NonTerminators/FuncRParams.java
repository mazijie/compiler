package Parser.NonTerminators;

import Lexer.Token;
import utils.IOUtils;

import java.io.IOException;
import java.util.List;

import Parser.*;

public class FuncRParams {
    private List<Exp> exps;
    private List<Token> commas;
    public FuncRParams(List<Exp> exps,List<Token> commas) {
        this.exps=exps;
        this.commas=commas;
    }
    public void print() throws IOException {
        for(int i=0;i<exps.size();i++)
        {
            if(i>=1)
            {
                IOUtils.write(commas.get(i-1).toString());
            }
            exps.get(i).print();
        }
        IOUtils.write(Parser.NTTypesToString.get(NTTypes.FuncRParams));
    }

}
