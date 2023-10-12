package SymbolTable;

import Lexer.Token;

public class FuncParam {
    public FuncType type;
    public Token ident;
    public FuncParam(Token ident, FuncType type){
        this.type = type;
        this.ident = ident;
    }
}
