package Parser.NonTerminators;

import Lexer.Token;
import utils.IOUtils;

import Parser.*;

import java.io.IOException;

public class LOrExp {
    //LOrExp → LAndExp | LOrExp '||' LAndExp
    //去除左递归：LOrExp → LAndExp | LAndExp '||' LOrExp
    private LAndExp lAndExp;
    private Token OrTK;
    private LOrExp lOrExp;
    public LOrExp(LAndExp lAndExp,Token OrTK,LOrExp lOrExp){
        this.lAndExp=lAndExp;
        this.lOrExp=lOrExp;
        this.OrTK=OrTK;
    }
    public void print() throws IOException {
        lAndExp.print();
        IOUtils.write(Parser.NTTypesToString.get(NTTypes.LOrExp));
        if(OrTK!=null){
            IOUtils.write(OrTK.toString());
            lOrExp.print();
        }
    }
}
