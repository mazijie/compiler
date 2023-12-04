package IR.Array;

import IR.Type.ValueType;

public class ArrayType extends ValueType{

    public ArrayType(int length,ValueType elementType) {
        super("");
        this.length = length;
        this.elementType = elementType;
    }
    public int length;
    public ValueType elementType;

    public String toString() {
        if(length==-100) {
            return elementType +"*";
        }
        return "["+length+" x "+ elementType +"]";//[3 x i32]
    }
}
