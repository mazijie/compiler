package IR.Value.Instructions;

import IR.Value.BasicBlock;
import IR.Value.GlobalVar;
import IR.Value.Value;
import IR.Value.VarPointer;
import utils.IOUtils;

import java.io.IOException;

public class STORE extends Instruction{
    public Value v1;
    public VarPointer v2;
    public STORE(BasicBlock basicBlock, Value v1, VarPointer v2) {
        super(InstructionType.STORE, basicBlock);
        this.v1 = v1;
        this.v2 = v2;
        {
            //优化部分
            this.whomIUse.add(v1);
            if(v2.isAddressMode)
                this.whomIUse.add(v2);
            else
                this.whomIDefine.add(v2);
//            if(v1!=v2)
//                this.whomIDefine.add(v2);
        }
    }

    @Override
    public void print() throws IOException {
        IOUtils.write("\tstore "+v2.type.pointto.toString()+" "+v1.name+", "+v2.type.toString()+" "+v2.name+"\n");
    }
}
