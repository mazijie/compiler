package IR.Value.Instructions;

import IR.Array.Array;
import IR.Value.BasicBlock;
import IR.Value.GlobalVar;
import IR.Value.Value;
import IR.Value.VarPointer;
import utils.IOUtils;

import java.io.IOException;

public class GETELEMENTPTR extends Instruction{
    public VarPointer res;
    public VarPointer array;
    public GlobalVar globalArray;

    public Value offset;
    public int type;


    public GETELEMENTPTR(BasicBlock basicBlock, VarPointer res, VarPointer array, Value offset, int type) {
        super(InstructionType.GETELEMENTPTR, basicBlock);
        this.res = res;
        this.array = array;
        this.offset = offset;
        this.type = type;
    }

    public GETELEMENTPTR(BasicBlock basicBlock, VarPointer res, GlobalVar array, Value offset ,int type) {
        super(InstructionType.GETELEMENTPTR, basicBlock);
        this.res = res;
        this.globalArray = array;
        this.offset = offset;
        this.type=type;
    }

    @Override
    public void print() throws IOException {
        if(type==0)
        {
            if(array!=null)
                IOUtils.write("\t"+res.name+" = getelementptr "+array.type.pointto+", "+array.type+array.name+", i32 0, "+offset.type+" "+offset.name+"\n");
            else
                IOUtils.write("\t"+res.name+" = getelementptr "+globalArray.type.pointto+", "+globalArray.type+globalArray.name+", i32 0, "+offset.type+" "+offset.name+"\n");
        }
        else{
            if(array!=null)
                IOUtils.write("\t"+res.name+" = getelementptr "+array.type.pointto+", "+array.type+array.name+", "+offset.type+" "+offset.name+"\n");
            else
                IOUtils.write("\t"+res.name+" = getelementptr "+globalArray.type.pointto+", "+globalArray.type+globalArray.name+", "+offset.type+" "+offset.name+"\n");
        }
    }
}
