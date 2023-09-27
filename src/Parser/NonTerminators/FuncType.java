package Parser.NonTerminators;

import Lexer.Token;
import utils.IOUtils;

import java.io.IOException;

import Parser.*;

public class FuncType {
    private Token tk;
    public FuncType(Token tk)
    {
        this.tk=tk;
    }
    public void print() throws IOException {
        IOUtils.write(tk.toString());
        IOUtils.write(Parser.NTTypesToString.get(NTTypes.FuncType));
    }
}
