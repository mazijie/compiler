package Parser.NonTerminators;

import Lexer.Token;
import utils.IOUtils;

import java.io.IOException;

import Parser.*;

public class FuncDef {
    //FuncDef → FuncType Ident '(' [FuncFParams] ')' Block
    private FuncType funcType;
    private Token ident;
    private Token left;
    private FuncFParams funcFParams;
    private Token right;
    private Block block;
    public FuncDef(FuncType funcType, Token ident, Token left, FuncFParams funcFParams, Token right, Block block){
        this.funcType = funcType;
        this.ident = ident;
        this.left = left;
        this.funcFParams = funcFParams;
        this.right = right;
        this.block = block;
    }
    public void print() throws IOException {
        this.funcType.print();
        IOUtils.write(this.ident.toString());
        IOUtils.write(this.left.toString());
        if(this.funcFParams!=null) this.funcFParams.print();
        IOUtils.write(this.right.toString());
        this.block.print();
        IOUtils.write(Parser.NTTypesToString.get(NTTypes.FuncDef));
    }
}
