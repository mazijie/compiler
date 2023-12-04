package IR.Value;

import IR.IRModule;
import IR.Type.ValueType;
import utils.IOUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Function extends Value{
    //记录寄存器号
    public Integer count=-1;

    //以函数为单位进行赐名活动
    public String giveName(){
        count++;
        return "%v_"+count;
    }

    //函数下辖若干基本块
    public List<BasicBlock> basicBlocks = new ArrayList<BasicBlock>();
    public List<Argument> arguments=new ArrayList<Argument>();

    public Function(String name, ValueType type){
        super(name, type);
    }
    public void addBlock(BasicBlock block){
        basicBlocks.add(block);
    }
    public void addArgument(Argument argument){
        arguments.add(argument);
    }

//    public String toString(){
//        StringBuilder s= new StringBuilder("\ndefine dso_local" + type.toString() + " @" + name + "(");
//        for(VarPointer a:arguments){
//            s.append(a.toString());
//        }
//        s.append("){\n");
//        for(BasicBlock b:basicBlocks){
//            s.append(b.toString());
//        }
//        s.append("}\n");
//        return s.toString();
//    }

    public void print() throws IOException {
//        IOUtils.write(((FunctionType) this.getType()).getReturnType()).append(" @").append(this.getName()).append("(");
        //define dso_local i32 @main()
        IOUtils.write("\ndefine dso_local ");
        IOUtils.write(type.toString());
        IOUtils.write(" @");
        IOUtils.write(name+"(");
        int i=0;
        for(Argument argument:arguments){
            if(i>=1) IOUtils.write(", ");
            IOUtils.write(argument.type.toString()+" "+argument.name);
            i++;
        }
        IOUtils.write("){\n");
        i=0;
        for(BasicBlock b:basicBlocks){
            b.print(i>=1);
            i++;
        }
//        if(type== ValueType._void){
//            IOUtils.write("\tret void\n");
//        }
        IOUtils.write("}\n");
    }
}
