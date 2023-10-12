package Parser.NonTerminators;

import Lexer.Token;
import Parser.Parser;
import Parser.NTTypes;
import utils.IOUtils;

import java.io.IOException;
import java.util.List;

public class AddExp {
    //AddExp → MulExp | AddExp ('+' | '−') MulExp
    //取消左递归：AddExp → MulExp | MulExp ('+' | '−') AddExp
    //BNF范式：AddExp → MulExp {('+' | '−') AddExp}
    public MulExp mulExp;
    public Token operand;
    public AddExp addExp;
    public AddExp(MulExp mulExp,Token operand,AddExp addExp){
        this.mulExp=mulExp;
        this.operand=operand;
        this.addExp=addExp;
    }
    public void print() throws IOException {
        this.mulExp.print();

        //按照原文法设置，无论是不是最后一个，都应该赋予AddExp身份
        IOUtils.write(Parser.NTTypesToString.get(NTTypes.AddExp));
        if(this.operand==null) return;
        IOUtils.write(this.operand.toString());
        this.addExp.print();
    }
}
