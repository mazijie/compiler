package IR.NewSymbol;

import IR.Array.Array;
import IR.Type.ValueType;
import IR.Value.*;

import java.time.temporal.ValueRange;
import java.util.ArrayList;
import java.util.List;

public class NewSymbolManager {
    static List<NewSymbolTable> tables=new ArrayList<NewSymbolTable>();
    static final NewSymbolTable globalTable=new NewSymbolTable();//全局符号表
    static NewSymbolTable curTable=globalTable;//当前符号表
    public static void backToPre(){
        curTable=curTable.pre;
    }
    public static void goToNext(){
        NewSymbolTable table=new NewSymbolTable(curTable);
        curTable=table;
    }
    public static void addVarPointer(String name, VarPointer value){
        curTable.addValue(name,value);
    }
    public static void addGlobalVar(String name, GlobalVar value){curTable.addGlobalVar(name,value);}

//    public static void addGlobalArray(String name, GlobalArray value){curTable.addGlobalArray(name,value);}

//    public static void addArray(String name, Array value){curTable.addArray(name,value);}

    public static void addFunction(String name, ValueType type){
        curTable.addFunc(name,type);
    }
    public static void addFunction(String name, Function value){
        curTable.addFunc(name,value);
    }
    public static VarPointer searchByName(String name){
        return curTable.searchByName(name);
    }

    public static ValueType searchFuncForType(String name){
        return curTable.searchFuncForType(name);
    }

    //优化部分
    public static Function searchFunc(String name){
        return curTable.searchFunc(name);
    }

    public static void addConstVarPointer(String content, int val) {
        curTable.addConstVarPointer(content,val);
    }

    public static Boolean isConstVarPointer(String content){
        return curTable.isConstVarPointer(content);
    }

    public static int getConstVarPointer(String content){
        return curTable.findConstVarPointer(content);
    }

//    public static Array searchArray(String name){return curTable.searchArray(name);}
//    public static GlobalArray searchGlobalArray(String name){return curTable.searchGlobalArray(name);}

}
