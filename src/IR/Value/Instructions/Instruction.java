package IR.Value.Instructions;

import IR.Type.Type;
import IR.Value.BasicBlock;
import IR.Value.User;

import java.io.IOException;

public abstract class Instruction{

    public InstructionType type;
    public BasicBlock basicBlock;
    public Instruction(InstructionType type,BasicBlock basicBlock){
        this.type = type;
        this.basicBlock = basicBlock;
    }

    abstract public void print() throws IOException;
}
