package SymbolTable;

import Lexer.Token;
import Parser.NonTerminators.FuncFParam;
import Parser.NonTerminators.FuncFParams;
import Parser.NonTerminators.FuncRParams;

import java.util.ArrayList;
import java.util.List;

public class TableManager {
    private static List<Table> tables = new ArrayList<Table>();
    private static Table mainTable = new Table();
    private static Table curTable = new Table();
    public static void init(){
        //将最外层符号表放入数组
        tables.add(mainTable);
        curTable=mainTable;
    }
    public static Table getMainTable(){
        return mainTable;
    }
    public static Table getCurTable(){
        return curTable;
    }
    //回到上一级符号表
    public static void backToPre(){
        curTable=curTable.getPre();
    }
    //开辟下一级符号表
    public static void goToNext(){
        Table table= new Table(curTable);
        curTable.addNext(table);
        tables.add(table);
        curTable=table;
    }
    public static void addArraySymbol(Token token,Boolean isConst,int dimension){
        curTable.addSymbol(new ArraySymbol(token,isConst,dimension));
    }
    public static void addFuncSymbol(Token token, FuncType type, FuncFParams params){
        curTable.addSymbol(new FuncSymbol(token,type,params));
    }
    public static boolean isConst(Token token){
        Symbol symbol = curTable.findSymbol(token);
        if(symbol==null){return false;}
        if(symbol.getType()==SymbolType.Array&&((ArraySymbol)symbol).isConst)
            return true;
        return false;
    }
    public static boolean isExist(Token token){
        Symbol symbol = curTable.findSymbol(token);
        return symbol!=null;
    }

    public static void funcParamsCompare(Token token, FuncRParams params){
        FuncSymbol symbol=(FuncSymbol) (curTable.findSymbol(token));
        symbol.compare(token,params);
    }

    public static Symbol getSymbol(Token token){
        return curTable.findSymbol(token);
    }

    public static Symbol getSymbol(String name){
        return curTable.findSymbol(name);
    }

}
