package IR.Value.Instructions;

import IR.Array.ArrayType;
import IR.Value.BasicBlock;
import IR.Value.Value;
import IR.Value.VarPointer;
import utils.IOUtils;

import java.io.IOException;

public class ALLOCA extends Instruction{
    public VarPointer pointer;

    public ALLOCA(BasicBlock basicBlock, VarPointer pointer) {
        super(InstructionType.ALLOCA, basicBlock);
        this.pointer = pointer;
        {
            //优化部分
            if(!(pointer.type.pointto instanceof ArrayType))
                this.whomIDefine.add(pointer);
        }
    }


    @Override
    public void print() throws IOException {
        IOUtils.write("\t"+pointer.name+" = alloca "+ pointer.type.pointto+"\n");
    }
}
