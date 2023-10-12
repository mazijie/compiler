package Parser.NonTerminators;

import Lexer.Token;
import utils.IOUtils;

import java.io.IOException;
import java.util.List;

import Parser.*;

public class FuncFParam {
    public BType bType;
    public Token ident;
    public List<Token> left;
    public List<Token> right;
    public List<ConstExp> constExps;
    public FuncFParam(BType bType, Token ident, List<Token> left,List<Token> right,List<ConstExp> constExps)
    {
        this.bType = bType;
        this.ident = ident;
        this.left = left;
        this.right = right;
        this.constExps = constExps;
    }
    public void print() throws IOException {
        this.bType.print();
        IOUtils.write(this.ident.toString());
        if(this.left!=null)
        {
            for(int i=0;i<this.left.size();i++)
            {
                IOUtils.write(this.left.get(i).toString());
                if(i>=1)
                {
                    this.constExps.get(i-1).print();
                }
                IOUtils.write(this.right.get(i).toString());
            }
        }
        IOUtils.write(Parser.NTTypesToString.get(NTTypes.FuncFParam));
    }
}
