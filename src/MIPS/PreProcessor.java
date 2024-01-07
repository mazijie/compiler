package MIPS;

import IR.IRModule;
import IR.Value.*;
import IR.Value.Instructions.*;

import java.util.ArrayList;
import java.util.List;

public class PreProcessor {
    public static void preprocess(IRModule module){


        //指令级活跃变量分析
        {
            for(Function f : module.functions){
                //1.构建函数内指令级别的前后顺序关系
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
                    if(lastIR.type== InstructionType.BR){
                        List<BasicBlock> nextBasicBlocks = ((BR)lastIR).getNextBlock();
                        for(BasicBlock nextBasicBlock: nextBasicBlocks){
                            lastIR.nextInstructions.add(nextBasicBlock.instructions.get(0));
                            nextBasicBlock.instructions.get(0).lastInstructions.add(lastIR);
                        }
                    }
                }
                //2.计算各指令的in和out集合
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
                //3.输出各指令的in和out集合
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
    }
}
