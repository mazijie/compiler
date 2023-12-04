package IR.Value.Instructions;

import IR.ICMPType;
import IR.Value.BasicBlock;
import IR.Value.Value;
import utils.IOUtils;

import java.io.IOException;

public class ICMP extends Instruction{

    public ICMPType icmpType;
    public Value v1,v2;
    public Value res;
    public ICMP(BasicBlock basicBlock, Value v1,Value v2,ICMPType type,Value res) {
        super(InstructionType.ICMP, basicBlock);
        this.icmpType=type;
        this.v1=v1;
        this.v2=v2;
        this.res=res;
    }

    @Override
    public void print() throws IOException {
        IOUtils.write("\t"+res.name+" = icmp "+icmpType.toString()+" "+v1.type+" "+v1.name+", "+v2.name+"\n");
    }
}
