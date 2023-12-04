package IR.Value;

import IR.Array.Array;
import IR.Array.ArrayType;
import IR.Type.PointerType;
import IR.Type.ValueType;
import IR.Use;

import java.util.ArrayList;

public class Value{
    public String name;
    public ValueType type;
    public ArrayList<Use> useList=new ArrayList<Use>();
    public static int valNumber =-1;
    public Value(String name,ValueType type){
        this.name=name;
        this.type=type;
    }
    public Value(String name,ValueType type,int demension){
        this.name=name;
        this.type=type;
    }
    public Value(String name,ValueType type,int demension1,int demension2){
        this.name=name;
        this.type=type;
    }
    public boolean isArray(){
        return this instanceof Array;
    }
    public boolean isPointer(){
        return this instanceof VarPointer;
    }
}
