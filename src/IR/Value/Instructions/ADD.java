package IR.Value.Instructions;

import IR.Value.BasicBlock;
import IR.Value.Value;
import utils.IOUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ADD extends Instruction{

    public Value v1,v2,res;
    public ADD(BasicBlock basicBlock, Value v1, Value v2, Value res) {
        super(InstructionType.ADD, basicBlock);
        {
            //优化部分
            this.whomIUse.add(v1);
            this.whomIUse.add(v2);
            if(res!=v1&&res!=v2)
                this.whomIDefine.add(res);
        }

        this.v1 = v1;
        this.v2 = v2;
        this.res = res;
    }

    @Override
    public void print() throws IOException {
        IOUtils.write("\t"+res.name+" = add i32 "+v1.name+", "+v2.name+"\n");
    }

}
