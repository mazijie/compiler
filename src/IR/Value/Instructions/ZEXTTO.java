package IR.Value.Instructions;

import IR.Value.BasicBlock;
import IR.Value.Value;
import utils.IOUtils;

import java.io.IOException;

public class ZEXTTO extends Instruction{
    public Value zextfrom;
    public Value zextto;
    public ZEXTTO(BasicBlock basicBlock,Value zextfrom,Value zextto) {
        super(InstructionType.ZEXTTO, basicBlock);
        this.zextfrom = zextfrom;
        this.zextto = zextto;
        {
            //优化部分
            this.whomIUse.add(zextfrom);
            if(zextfrom!=zextto)
                this.whomIDefine.add(zextto);
        }
    }

    @Override
    public void print() throws IOException {
        IOUtils.write("\t"+zextto.name+" = zext "+zextfrom.type+" "+zextfrom.name+" to "+zextto.type+"\n");
    }
}
