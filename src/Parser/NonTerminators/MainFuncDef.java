package Parser.NonTerminators;

import Lexer.Token;
import utils.IOUtils;

import java.io.IOException;

import Parser.*;

public class MainFuncDef {
    private Token inttk;
    private Token maintk;
    private Token left;
    private Token right;
    private Block block;
    public MainFuncDef(Token inttk, Token maintk, Token left, Token right, Block block) {
        this.inttk = inttk;
        this.maintk = maintk;
        this.left = left;
        this.right = right;
        this.block = block;
    }
    public void print() throws IOException {
        IOUtils.write(inttk.toString());
        IOUtils.write(maintk.toString());
        IOUtils.write(left.toString());
        IOUtils.write(right.toString());
        block.print();
        IOUtils.write(Parser.NTTypesToString.get(NTTypes.MainFuncDef));
    }
}
