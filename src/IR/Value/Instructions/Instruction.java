package IR.Value.Instructions;

import IR.Type.Type;
import IR.Value.BasicBlock;
import IR.Value.User;
import IR.Value.Value;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class Instruction{

    public InstructionType type;
    public BasicBlock basicBlock;

    //优化部分
    public Set<Value> whomIDefine = new HashSet<>();
    public Set<Value> whomIUse = new HashSet<>();
    public Set<Value> whoIn = new HashSet<>();//以指令为单位进行活跃变量分析
    public Set<Value> whoOut = new HashSet<>();
    public List<Instruction> lastInstructions = new ArrayList<>();
    public List<Instruction> nextInstructions = new ArrayList<>();

    public Instruction(InstructionType type,BasicBlock basicBlock){
        this.type = type;
        this.basicBlock = basicBlock;
    }

    abstract public void print() throws IOException;
}
