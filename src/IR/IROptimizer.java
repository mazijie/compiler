package IR;

import IR.Value.*;
import IR.Value.Instructions.*;
import utils.IOUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class IROptimizer {
    public static void optimize(IRModule module){
        //1.内存分析：删除同一基本块中多余的store指令
        {
            for(Function f : module.functions){
                for(BasicBlock basicBlock:f.basicBlocks){
//                    List<Integer> toDelete = new ArrayList<Integer>();
                    for(int i=0;i<basicBlock.instructions.size();i++){
                        Instruction ir = basicBlock.instructions.get(i);
                        if(ir.type== InstructionType.STORE){
                            VarPointer pointer = ((STORE)ir).v2;
                            for(int j=i+1;j<basicBlock.instructions.size();j++){
                                Instruction check = basicBlock.instructions.get(j);
                                if(check.type==InstructionType.LOAD&&((LOAD)check).loadfrom==pointer)
                                    break;
                                if(check.type==InstructionType.STORE&&((STORE)check).v2==pointer){
//                                    toDelete.add(i);
                                    basicBlock.instructions.remove(i);
                                    i--;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }

        //2.删除无用函数
        {
            List<Function> validFunctions = new ArrayList<>();
            List<Function> tmpFunctions = new ArrayList<>();
            tmpFunctions.add(module.getMainFunction());
            while(!tmpFunctions.isEmpty()){
                for(Function f : tmpFunctions.get(0).whomICall){
                    if(!validFunctions.contains(f)&&!tmpFunctions.contains(f)){
                        tmpFunctions.add(f);
                    }
                }
                validFunctions.add(tmpFunctions.remove(0));
            }
            module.functions.removeIf(f -> !validFunctions.contains(f));
        }


        //3.指令级活跃变量分析
        {
            for(Function f : module.functions){
                //3-1.构建函数内指令级别的前后顺序关系
                for(BasicBlock basicBlock: f.basicBlocks){
                    for(Instruction ir:basicBlock.instructions){
                        ir.whomIDefine.removeIf(v -> v instanceof ConstInteger);
                        ir.whomIUse.removeIf(v -> v instanceof ConstInteger);
                    }
                    for(int i=0;i<basicBlock.instructions.size()-1;i++){
                        basicBlock.instructions.get(i).nextInstructions.add(basicBlock.instructions.get(i+1));
                        basicBlock.instructions.get(i+1).lastInstructions.add(basicBlock.instructions.get(i));
                    }
                    Instruction lastIR = basicBlock.instructions.get(basicBlock.instructions.size()-1);
                    if(lastIR.type==InstructionType.BR){
                        List<BasicBlock> nextBasicBlocks = ((BR)lastIR).getNextBlock();
                        for(BasicBlock nextBasicBlock: nextBasicBlocks){
                            lastIR.nextInstructions.add(nextBasicBlock.instructions.get(0));
                            nextBasicBlock.instructions.get(0).lastInstructions.add(lastIR);
                        }
                    }
                }
                //3-2.计算各指令的in和out集合
                boolean flag = true;
                while(flag){
                    flag=false;
                    for(BasicBlock basicBlock: f.basicBlocks){
                        for(Instruction instruction: basicBlock.instructions){
                            for(Instruction nextInstruction: instruction.nextInstructions){
                                instruction.whoOut.addAll(nextInstruction.whoIn);
                            }
                            flag = instruction.whoIn.addAll(instruction.whomIUse)||flag;
                            List<Value> tmpValues = new ArrayList<>(instruction.whoOut);
                            tmpValues.removeAll(instruction.whomIDefine);
                            flag = instruction.whoIn.addAll(tmpValues)||flag;
                        }
                    }
                }
                //3-3.输出各指令的in和out集合
                int count=0;
                for(BasicBlock basicBlock: f.basicBlocks){
                    for(Instruction instruction: basicBlock.instructions){
                        count++;
                        System.out.print(count+" "+instruction.type.toString()+":");
                        for(Value value: instruction.whoIn)
                            System.out.print(value.name+", ");
                        System.out.println();
                    }
                }
            }
        }

//        //4.删除无用代码
//        {
//            for(Function f:module.functions){
//                List<Instruction> useful=new ArrayList<>();
//                List<Instruction> temp=new ArrayList<>();
//                for(BasicBlock b: f.basicBlocks){
//                    for(Instruction instruction: b.instructions){
//
//                    }
//                }
//            }
//        }
    }
}
