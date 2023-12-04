package IR.Value;

import IR.Type.ValueType;

import java.util.ArrayList;

public class User extends Value{
    public ArrayList<Value> operands=new ArrayList<Value>();
    public User(String name, ValueType type){
        super(name,type);
    }
}
