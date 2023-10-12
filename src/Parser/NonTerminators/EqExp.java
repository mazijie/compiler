package Parser.NonTerminators;

import Lexer.Token;
import utils.IOUtils;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.util.List;

import Parser.*;

public class EqExp {
    //EqExp → RelExp | EqExp ('==' | '!=') RelExp
    //去除左递归文法：EqExp → RelExp | RelExp ('==' | '!=') EqExp
    //BNF范式：EqExp → RelExp {('==' | '!=') EqExp}
    public RelExp relExp;
    public Token operand;
    public EqExp eqExp;
    public EqExp(RelExp relExp,Token operand,EqExp eqExp) {
        this.relExp = relExp;
        this.operand = operand;
        this.eqExp = eqExp;
    }
    public void print() throws IOException {
        this.relExp.print();
        IOUtils.write(Parser.NTTypesToString.get(NTTypes.EqExp));
        if(this.operand!=null){
            IOUtils.write(this.operand.toString());
            this.eqExp.print();
        }
    }

}
