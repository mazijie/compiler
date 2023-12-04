package IR.Value.Instructions;

import IR.Value.BasicBlock;
import IR.Value.Value;
import utils.IOUtils;

import java.io.IOException;

public class BR extends Instruction{

    public Value cond;
    public String label1;
    public String label2;
    public String label_dst;//强制跳转使用

    public BR(BasicBlock curBasicBlock, Value cond, String label1, String label2){
        super(InstructionType.BR,curBasicBlock);
        this.cond = cond;
        this.label1 = label1;
        this.label2 = label2;
    }

    public BR(BasicBlock curBasicBlock,String label_dst){
        super(InstructionType.BR,curBasicBlock);
        this.label_dst=label_dst;
    }

    @Override
    public void print() throws IOException {
        if(cond==null){
            IOUtils.write("\tbr label "+label_dst+"\n");
        }else{
            IOUtils.write("\tbr "+cond.type.toString()+" "+cond.name+", label "+label1+", label "+label2+"\n");
        }
    }
}
