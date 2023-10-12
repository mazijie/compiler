package Parser.NonTerminators;

import Lexer.Token;
import utils.IOUtils;

import java.io.IOException;

public class BType {
    public Token token;
    //"int"
    public BType(Token token){
        this.token = token;
    }
    public void print() throws IOException {
        IOUtils.write(this.token.toString());
    }
}
