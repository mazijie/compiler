package IR.NewSymbol;

import IR.Array.Array;
//import IR.Array.GlobalArray;
import IR.Type.ValueType;
import IR.Value.Function;
import IR.Value.GlobalVar;
import IR.Value.Value;
import IR.Value.VarPointer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NewSymbolTable {
    HashMap<String,GlobalVar> globalVars=new HashMap<>();//放全局变量
    HashMap<String,VarPointer> values=new HashMap<>();//放变量
    HashMap<String,Function> funcs=new HashMap<>();//放函数
    HashMap<String, ValueType> outFuncs=new HashMap<>();//放外部函数

    HashMap<String, Array> arrays=new HashMap<>();//放局部数组

//    HashMap<String, GlobalArray> globalArrays=new HashMap<>();//放全局数组
    NewSymbolTable pre=null;
    List<NewSymbolTable> next=new ArrayList<NewSymbolTable>();
    public NewSymbolTable(NewSymbolTable pre){
        this.pre=pre;
    }
    //仅用于全局符号表
    public NewSymbolTable(){}

    public void addValue(String name, VarPointer value) {
        values.put(name,value);
    }
    public void addFunc(String name, Function value){
        funcs.put(name,value);
    };

    public void addFunc(String name,ValueType type){
        outFuncs.put(name,type);
    };

    public VarPointer searchByName(String name) {
        if(values.containsKey(name)){
            return values.get(name);
        }else if(globalVars.containsKey(name)){
            return globalVars.get(name);
        }
        if(pre==null) return null;
        return pre.searchByName(name);
    }

    public void addGlobalVar(String name, GlobalVar value) {
        globalVars.put(name,value);
    }

    public ValueType searchFuncForType(String name) {
        if(outFuncs.containsKey(name)){
            return outFuncs.get(name);
        }else if(funcs.containsKey(name)){
            return funcs.get(name).type;
        }
        if(pre==null) return null;
        return pre.searchFuncForType(name);
    }

//    public void addGlobalArray(String name, GlobalArray value) {
//        globalArrays.put(name, value);
//    }

    public void addArray(String name, Array value) {
        arrays.put(name, value);
    }

    public Array searchArray(String name) {
        if(arrays.containsKey(name)){
            return arrays.get(name);
        }
        if(pre==null) return null;
        return pre.searchArray(name);
    }

//    public GlobalArray searchGlobalArray(String name) {
//        if(globalArrays.containsKey(name)){
//            return globalArrays.get(name);
//        }
//        if(pre==null) return null;
//        return pre.searchGlobalArray(name);
//    }
}
