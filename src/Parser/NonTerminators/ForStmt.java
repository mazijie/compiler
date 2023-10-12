package Parser.NonTerminators;

import Lexer.Token;
import utils.IOUtils;

import Parser.*;

import java.io.IOException;

public class ForStmt {
    public LVal lVal;
    public Token equal;
    public Exp exp;
    public ForStmt(LVal lVal, Token equal, Exp exp)
    {
        this.lVal = lVal;
        this.equal = equal;
        this.exp = exp;
    }
    public void print() throws IOException {
        this.lVal.print();
        IOUtils.write(this.equal.toString());
        this.exp.print();
        IOUtils.write(Parser.NTTypesToString.get(NTTypes.ForStmt));
    }
}
