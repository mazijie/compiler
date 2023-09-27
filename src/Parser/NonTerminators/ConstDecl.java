package Parser.NonTerminators;

import Lexer.Token;
import utils.IOUtils;

import java.io.IOException;
import java.util.List;

import Parser.Parser;
import Parser.NTTypes;

public class ConstDecl {
    Token consttk;
    BType bType;
    List<ConstDef> constDefs;
    List<Token> commas;
    Token semicn;
    public ConstDecl(Token consttk, BType bType, List<ConstDef> constDefs, List<Token> commas,Token semicn) {
        this.consttk = consttk;
        this.bType = bType;
        this.constDefs = constDefs;
        this.commas = commas;
        this.semicn = semicn;
    }
    public void print() throws IOException {
        IOUtils.write(this.consttk.toString());
        bType.print();
        for(int i = 0; i < constDefs.size();i++)
        {
            if(i>=1) IOUtils.write(commas.get(i-1).toString());
            constDefs.get(i).print();
        }
//        constDefs.get(constDefs.size()-1).print();
        IOUtils.write(semicn.toString());
        IOUtils.write(Parser.NTTypesToString.get(NTTypes.ConstDecl));
    }
}
