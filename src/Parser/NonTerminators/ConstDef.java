package Parser.NonTerminators;

import Lexer.Token;
import utils.IOUtils;

import java.io.IOException;
import java.util.List;

import Parser.Parser;
import Parser.NTTypes;

public class ConstDef {
    Token indent;
    List<Token> left;
    List<ConstExp> constExps;
    List<Token> right;
    Token equal;
    ConstInitVal constInitVal;
    public ConstDef(Token indent, List<Token> left, List<ConstExp> constExps,List<Token>right,Token equal,ConstInitVal constInitVal) {
        this.indent=indent;
        this.left=left;
        this.right=right;
        this.equal=equal;
        this.constInitVal=constInitVal;
        this.constExps=constExps;
    }
    public void print() throws IOException {
        IOUtils.write(this.indent.toString());
        if(this.constExps!=null)
        {
            for(int i=0;i<this.constExps.size();i++)
            {
                IOUtils.write(this.left.get(i).toString());
                constExps.get(i).print();
                IOUtils.write(this.right.get(i).toString());
            }
        }
        IOUtils.write(this.equal.toString());
        this.constInitVal.print();
        IOUtils.write(Parser.NTTypesToString.get(NTTypes.ConstDef));
    }
}
