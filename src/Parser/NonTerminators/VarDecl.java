package Parser.NonTerminators;

import Lexer.Token;
import utils.IOUtils;

import java.io.IOException;
import java.util.List;

import Parser.*;

public class VarDecl {
    public BType bType;
    public List<VarDef> varDefs;
    public List<Token> commas;
    public Token semicn;
    public VarDecl(BType bType, List<VarDef> varDef, List<Token> commas, Token semicn) {
        this.bType=bType;
        this.varDefs=varDef;
        this.commas=commas;
        this.semicn=semicn;
    }
    public void print() throws IOException {
        bType.print();
        for(int i=0;i<varDefs.size();i++) {
            if(i>=1) IOUtils.write(commas.get(i-1).toString());
            varDefs.get(i).print();
        }
        IOUtils.write(semicn.toString());
        IOUtils.write(Parser.NTTypesToString.get(NTTypes.VarDecl));
    }
}
