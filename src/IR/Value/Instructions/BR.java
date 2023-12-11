package IR.Value.Instructions;

import IR.Value.BasicBlock;
import IR.Value.Value;
import utils.IOUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
        {
            //优化部分
            this.whomIUse.add(cond);
        }
    }

    public BR(BasicBlock curBasicBlock,String label_dst){
        super(InstructionType.BR,curBasicBlock);
        this.label_dst=label_dst;
    }

    public List<BasicBlock> getNextBlock(){
        //返回下一个可能到达的基本块，第一个为true，第二个为false；或者只有一个
        List<BasicBlock> res = new ArrayList<>();
        if(label_dst==null){
            for(BasicBlock block:basicBlock.function.basicBlocks){
                if(block.name.equals(label1)){
                    res.add(block);
                    break;
                }
            }
            for(BasicBlock block:basicBlock.function.basicBlocks){
                if(block.name.equals(label2)){
                    res.add(block);
                    break;
                }
            }
        }else{
            for(BasicBlock block:basicBlock.function.basicBlocks){
                if(block.name.equals(label_dst)){
                    res.add(block);
                    break;
                }
            }
        }
        return res;
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
