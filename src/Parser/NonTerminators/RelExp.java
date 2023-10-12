package Parser.NonTerminators;

import Lexer.Token;
import utils.IOUtils;

import Parser.*;

import java.io.IOException;

public class RelExp {
    //RelExp → AddExp | RelExp ('<' | '>' | '<=' | '>=') AddExp
    //去除左递归：RelExp → AddExp | AddExp ('<' | '>' | '<=' | '>=') RelExp
    public AddExp addExp;
    public Token token;
    public RelExp relExp;
    public RelExp(AddExp addExp, Token token, RelExp relExp){
        this.relExp = relExp;
        this.token = token;
        this.addExp = addExp;
    }
    public void print() throws IOException {
        addExp.print();
        IOUtils.write(Parser.NTTypesToString.get(NTTypes.RelExp));
        if(token!=null){
            IOUtils.write(token.toString());
            relExp.print();
        }
    }
}
