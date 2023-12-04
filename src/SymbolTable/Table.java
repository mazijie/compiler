package SymbolTable;

import Errors.ErrorRecord;
import Errors.ErrorType;
import Lexer.Token;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Table {
    private HashMap<String,Symbol> symbols=new HashMap<String,Symbol>();
    private Table pre;
    private List<Table> next=new ArrayList<Table>();

    //只用于最外层符号表
    public Table(){
        this.pre=null;
    }
    //用于内层符号表
    public Table(Table pre){
        this.pre=pre;
    }

    public Table getPre(){
        return pre;
    }

    public void addNext(Table table){
        next.add(table);
    }

    //向符号表添加符号
    public void addSymbol(Symbol symbol){
        if(symbols.containsKey(symbol.getToken().getContent())){
            ErrorRecord.addError(symbol.getToken().getLine(),symbol.getToken().getIndex(), ErrorType.b);
        }
        symbols.put(symbol.getToken().getContent(),symbol);
    }

    //查找符号
    public Symbol findSymbol(Token token){
        if(symbols.containsKey(token.getContent()))
        {
            return symbols.get(token.getContent());
        }
        if(pre==null){
//            ErrorRecord.addError(token.getLine(),token.getIndex(), ErrorType.c);
            return null;
        }
        else return pre.findSymbol(token);
    }

    public Symbol findSymbol(String name){
        if(symbols.containsKey(name))
        {
            return symbols.get(name);
        }
        if(pre==null){
//            ErrorRecord.addError(token.getLine(),token.getIndex(), ErrorType.c);
            return null;
        }
        else return pre.findSymbol(name);
    }
}
