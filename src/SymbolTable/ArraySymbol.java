package SymbolTable;

import Lexer.Token;

public class ArraySymbol extends Symbol{
    public boolean isConst;
    public int dimension;
    public ArraySymbol(Token token, boolean isConst,int dimension){
        super(token,SymbolType.Array);
        this.isConst=isConst;
        this.dimension=dimension;
    }
}
