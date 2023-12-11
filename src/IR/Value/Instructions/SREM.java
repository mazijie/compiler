package IR.Value.Instructions;

import IR.Value.BasicBlock;
import IR.Value.Value;
import utils.IOUtils;

import java.io.IOException;

public class SREM extends Instruction{
    public Value v1;
    public Value v2;
    public Value res;
    public SREM(BasicBlock curBasicBlock, Value v1, Value v2, Value res) {
        super(InstructionType.SREM, curBasicBlock);
        this.v1=v1;this.v2=v2;this.res=res;
        {
            //优化部分
            this.whomIUse.add(v1);
            this.whomIUse.add(v2);
            if(res!=v1&&res!=v2)
                this.whomIDefine.add(res);
        }
    }

    @Override
    public void print() throws IOException {
        IOUtils.write("\t"+res.name+" = srem i32 "+v1.name+", "+v2.name+"\n");
    }
}
