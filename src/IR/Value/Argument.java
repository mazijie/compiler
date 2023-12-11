package IR.Value;

import IR.Type.ValueType;

public class Argument extends Value{
    public Argument(String name, ValueType type){
        super(name,type);
    }
    public Value ValueInFunc;
}
