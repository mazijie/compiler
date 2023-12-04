package IR.Value.Instructions;

import IR.Value.BasicBlock;
import IR.Value.Value;
import utils.IOUtils;

import java.io.IOException;

public class RET extends Instruction{
    public Value value;
    public RET(BasicBlock curBasicBlock,Value value){
        super(InstructionType.RET,curBasicBlock);
        this.value = value;
    }
    public RET(BasicBlock curBasicBlock){
        super(InstructionType.RET,curBasicBlock);
    }
    @Override
    public void print() throws IOException {
        if(value!=null)
            IOUtils.write("\tret i32 "+value.name+"\n");
        else
            IOUtils.write("\tret void\n");
    }
}
