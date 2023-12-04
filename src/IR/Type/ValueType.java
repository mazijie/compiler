package IR.Type;

import IR.Array.ArrayType;
import IR.Value.VarPointer;

import java.util.ArrayList;
import java.util.List;

public class ValueType {

    public String name;
    public ValueType(String name) {
        this.name = name;
    }

    public static ValueType _i32 = new ValueType("_i32");
    public static ValueType _i8 = new ValueType("_i8");
    public static ValueType _i1 = new ValueType("_i1");
    public static ValueType _void = new ValueType("_void");
    public static ValueType _basicblock = new ValueType("_basicblock");

    public String toString(){
        if(this instanceof ArrayType) return ((ArrayType)this).toString();
        return name.substring(1);
    }
}
