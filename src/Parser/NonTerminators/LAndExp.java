package Parser.NonTerminators;

import Lexer.Token;
import utils.IOUtils;

import Parser.*;

import java.io.IOException;

public class LAndExp {
    //LAndExp → EqExp | LAndExp '&&' EqExp
    //去除左递归：LAndExp → EqExp | EqExp '&&' LAndExp
    private EqExp eqExp;
    private Token AndTK;
    private LAndExp landExp;
    public LAndExp(EqExp eqExp, Token AndTK, LAndExp landExp) {
        this.eqExp=eqExp;
        this.landExp=landExp;
        this.AndTK=AndTK;
    }
    public void print() throws IOException {
        eqExp.print();
        IOUtils.write(Parser.NTTypesToString.get(NTTypes.LAndExp));
        if(AndTK!=null){
            IOUtils.write(AndTK.toString());
            landExp.print();
        }
    }
}
