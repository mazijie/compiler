package IR.Type;

import IR.Value.VarPointer;

public class PointerType extends ValueType {
    public final ValueType pointto;
    public PointerType(ValueType pointto) {
        super(pointto.toString()+"*");
        this.pointto = pointto;
    }
    public String toString() {
        return pointto.toString()+"*";//i32*...
    }
    public static PointerType i32P=new PointerType(ValueType._i32);
}
