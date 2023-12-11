package IR.Value.Instructions;

import IR.Value.BasicBlock;
import IR.Value.Value;
import utils.IOUtils;

import java.io.IOException;

public class SDIV extends Instruction{
    public Value v1,v2,res;
    public SDIV(BasicBlock basicBlock, Value v1, Value v2, Value res) {
        super(InstructionType.SDIV, basicBlock);
        this.v1 = v1;
        this.v2 = v2;
        this.res = res;
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
        IOUtils.write("\t"+res.name+" = sdiv i32 "+v1.name+", "+v2.name+"\n");
    }
}
