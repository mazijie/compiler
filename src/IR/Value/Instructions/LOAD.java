package IR.Value.Instructions;

import IR.Array.Array;
import IR.Array.ArrayType;
import IR.Type.PointerType;
import IR.Type.ValueType;
import IR.Value.BasicBlock;
import IR.Value.Value;
import IR.Value.VarPointer;
import utils.IOUtils;

import java.io.IOException;

public class LOAD extends Instruction{

    public Value loadto;
    public VarPointer loadfrom;
    public LOAD(BasicBlock basicBlock,Value loadto,VarPointer loadfrom) {
        super(InstructionType.LOAD, basicBlock);
        this.loadto = loadto;
        this.loadfrom = loadfrom;
        {
            //优化部分
            this.whomIUse.add(loadfrom);
//            this.whomIUse.add(loadto);
            if(loadto!=loadfrom)
                this.whomIDefine.add(loadto);
        }
    }

    @Override
    public void print() throws IOException {
        IOUtils.write("\t"+loadto.name+" = load ");
//        if(loadto.isArray()){
//            IOUtils.write(((Array)loadto).arrayType.toString());
//        }else if(loadto.isPointer()){
//            IOUtils.write(((VarPointer)(loadto)).type.toString());
//        }else{
//            IOUtils.write(loadto.type.toString());
//        }
        IOUtils.write(loadfrom.type.pointto.toString());
        IOUtils.write(", "+loadfrom.type.toString()+" "+loadfrom.name+"\n");
    }
}
