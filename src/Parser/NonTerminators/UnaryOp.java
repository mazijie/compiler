package Parser.NonTerminators;

import Lexer.Token;
import Parser.*;
import utils.IOUtils;

import java.io.IOException;

public class UnaryOp {
    private Token token;
    public UnaryOp(Token token)
    {
        this.token=token;
    }
    public void print() throws IOException {
        IOUtils.write(token.toString());
        IOUtils.write(Parser.NTTypesToString.get(NTTypes.UnaryOp));
    }
}
