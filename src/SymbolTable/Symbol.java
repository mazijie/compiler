package SymbolTable;

import IR.Value.Value;
import Lexer.Token;

public class Symbol {

    private final Token token;
    private final SymbolType type;
    public Token getToken() {return token;}
    public SymbolType getType() {return type;}
    public Value value;
    public Symbol(Token token, SymbolType type) {this.token = token;this.type =type;}
}
