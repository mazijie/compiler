package Parser.NonTerminators;

import Lexer.Token;
import utils.IOUtils;

import java.io.IOException;

import Parser.*;

public class MainFuncDef {
    public Token inttk;
    public Token maintk;
    public Token left;
    public Token right;
    public Block block;
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
