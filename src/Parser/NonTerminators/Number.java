package Parser.NonTerminators;

import Lexer.Token;
import utils.IOUtils;

import Parser.*;

import java.io.IOException;

public class Number {
    private Token intconst;
    public Number(Token intconst) {
        this.intconst = intconst;
    }
    public void print() throws IOException {
        IOUtils.write(intconst.toString());
        IOUtils.write(Parser.NTTypesToString.get(NTTypes.Number));
    }
}
