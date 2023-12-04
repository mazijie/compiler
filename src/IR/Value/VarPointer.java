package IR.Value;

import IR.Type.PointerType;

public class VarPointer extends Value{
    public String symbolName;//在符号表里的名字
    public PointerType type;

    public VarPointer(String name,PointerType type){

        super(name,type.pointto);
        this.type=type;
    }
}
