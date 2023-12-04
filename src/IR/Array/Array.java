package IR.Array;

import IR.IRBuildFactory;
import IR.Type.PointerType;
import IR.Type.ValueType;
import IR.Value.ConstInteger;
import IR.Value.Value;
import IR.Value.VarPointer;
import IR.Visitor;

import java.util.ArrayList;
import java.util.List;


public class Array extends Value {

    public VarPointer convertToPointer;

    //用于全局一维数组定义
    public Array(String name, ArrayType arrayType, List<ConstInteger> vals) {
        super(name, arrayType);
        this.convertToPointer=IRBuildFactory.buildVarPointer(name,new PointerType(arrayType.elementType));
        while(vals.size() < arrayType.length) {vals.add(IRBuildFactory.buildConstInt(0));}
        this.vals = vals;
        this.arrayType=arrayType;
        for(ConstInteger v : vals) {
            if(v.val!=0) {
                all_zero=false;
                break;
            }
        }
    }

    //用于全局二维数组定义
    public Array(String name, ArrayType arrayType, List<Array> arrays, int de) {
        super(name, arrayType);
        this.convertToPointer=IRBuildFactory.buildVarPointer(name,new PointerType(arrayType.elementType));
        while(arrays.size() < arrayType.length) {
            List<ConstInteger> zeros = new ArrayList<>();
            for(int i=0;i<((ArrayType)(arrayType.elementType)).length;i++)
                zeros.add(IRBuildFactory.buildConstInt(0));
            arrays.add(IRBuildFactory.buildArray("",(ArrayType)(arrayType.elementType),zeros));
        }
        this.arrays = arrays;
        this.arrayType=arrayType;
        for(Array array : arrays) {
            if(!array.all_zero) {
                all_zero=false;
                break;
            }
        }
    }

    //用于数组调用
    public Array(String name, ArrayType arrayType) {
        super(name, arrayType);
        this.convertToPointer=IRBuildFactory.buildVarPointer(name,new PointerType(arrayType.elementType));
        this.arrayType = arrayType;
    }
    public List<ConstInteger> vals;//可能存在的常量元素值
    public List<Array> arrays;//可能存在的子数组
    public ArrayType arrayType;
    public boolean all_zero = true;

    public String valToString(){
        StringBuilder s = new StringBuilder();
        s.append(type.toString()).append(" ");
        if(all_zero){
            s.append("zeroinitializer");
            return s.toString();
        }
        s.append("[");
        if(vals!=null){
            for(int i=0;i<vals.size();i++){
                if(i>=1) s.append(", ");
                s.append(vals.get(i).type.toString()).append(" ").append(vals.get(i).name);
            }

        }else{
            for(int i=0;i<arrays.size();i++){
                if(i>=1) s.append(", ");
                s.append(arrays.get(i).valToString());
            }
        }
        s.append("]");
        return s.toString();
    }
}
