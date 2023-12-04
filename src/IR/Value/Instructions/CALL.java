package IR.Value.Instructions;

import IR.Type.ValueType;
import IR.Value.BasicBlock;
import IR.Value.Value;
import IR.Value.VarPointer;
import utils.IOUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CALL extends Instruction{

    public String name;
    public List<Value> params=new ArrayList<Value>();
    public ValueType type;
    public Value loadto;
    public CALL(BasicBlock basicBlock, String name, ValueType type, List<Value> params) {
        super(InstructionType.CALL, basicBlock);
        this.name = name;
        this.params = params;
        this.type = type;
    }

    public CALL(BasicBlock basicBlock, String name, ValueType type, List<Value> params, Value loadto) {
        super(InstructionType.CALL, basicBlock);
        this.name = name;
        this.params = params;
        this.type = type;
        this.loadto=loadto;
    }

    @Override
    public void print() throws IOException {
        IOUtils.write(("\t"));
        if(loadto!=null){
            IOUtils.write(loadto.name+" = ");
        }
        IOUtils.write("call "+ type.toString()+" @"+name+"(");
        for(int i=0;i<params.size();i++){
            if(i>=1) IOUtils.write(", ");
            Value v= params.get(i);
            if(v instanceof VarPointer) IOUtils.write(((VarPointer)v).type.toString()+" "+v.name);
            else IOUtils.write(v.type.toString()+" "+v.name);
        }
        IOUtils.write(")\n");
    }
}
