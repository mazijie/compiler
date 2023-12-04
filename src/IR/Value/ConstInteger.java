package IR.Value;

import IR.Type.ValueType;

import static java.lang.Integer.parseInt;

public class ConstInteger extends Value{
    public int val;
    public ConstInteger(String name) {
        super(name, ValueType._i32);
        this.val = parseInt(name);
    }

    public ConstInteger(Integer val) {
        super(val.toString(), ValueType._i32);
        this.val = val;
    }
}
