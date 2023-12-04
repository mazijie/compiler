package IR.Value;

import IR.Type.ValueType;
import IR.Value.Instructions.Instruction;
import IR.Value.Instructions.InstructionType;
import utils.IOUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BasicBlock extends Value{

    //归Function管辖
    public Function function;
    //基本块下辖若干指令
    public List<Instruction> instructions = new ArrayList<Instruction>();

    public BasicBlock(String name,Function function) {
        super(name, ValueType._basicblock);
        this.function=function;
    }

    public void addInstruction(Instruction instruction){
        instructions.add(instruction);
    }
    public String toString(){
        StringBuilder s = new StringBuilder();
        for(Instruction instruction : instructions){
            s.append(instruction.toString());
        }
        return s.toString();
    }

    public void print(Boolean isNotFirst) throws IOException {
        if(isNotFirst) IOUtils.write("\n"+name.substring(1)+":\n");
        for(Instruction instruction : instructions){
            instruction.print();
        }
//        if(instructions.size()==0){
//            InstructionType type = instructions.get(instructions.size()-1).type;
//            if(type!=InstructionType.RET&&type!=InstructionType.BR)
//                IOUtils.write("\tret void\n");
//        }
    }
}
