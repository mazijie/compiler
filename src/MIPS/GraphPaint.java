package MIPS;

import IR.IRModule;
import IR.Type.ValueType;
import IR.Value.BasicBlock;
import IR.Value.Function;
import IR.Value.GlobalVar;
import IR.Value.Instructions.Instruction;
import IR.Value.Value;

import java.util.*;

public class GraphPaint {
    public static void buildGraph(IRModule module){
        Set<Value> waitReg = new HashSet<>();//放置所有需要寄存器的变量，包括局部变量和临时变量
        //构建冲突图
        for(Function function:module.functions){
            for(BasicBlock basicBlock:function.basicBlocks){
                for(Instruction instruction:basicBlock.instructions){
                    //冲突规则：出现在in和def的全部变量都是冲突的，应该两两相连；全局变量应该被剔除
                    Set<Value> crashes = new HashSet<>(instruction.whoIn);
                    crashes.addAll(instruction.whomIDefine);
                    crashes.removeIf(value -> value instanceof GlobalVar);
                    waitReg.addAll(crashes);
                    for(Value v1:crashes){
                        for(Value v2:crashes){
                            if(v1!=v2){
                                v1.crash.add(v2);
                                v2.crash.add(v1);
                            }
                        }
                    }
                }
            }
        }
        //tmpCrash可以用来删边
        for(Value v:waitReg){
            v.tmpCrash.addAll(v.crash);
        }
        if(waitReg.size()==0) return;
        PriorityQueue<Value> priorityQueue = new PriorityQueue<>(new CustomComparator());
        priorityQueue.addAll(waitReg);
        List<Value> queue = new ArrayList<>();//排队，度数越小的越先存后取
        Value tmpValue = new Value("", ValueType._void);//用来触发重新排序
        while((!priorityQueue.isEmpty())&&priorityQueue.peek().getCrashAmount()<Register.getGlobalAmount()){
            //优先队列还有可以快乐分配的变量
            Value happyValue = priorityQueue.poll();
            for(Value v:happyValue.tmpCrash){
                v.tmpCrash.remove(v);
            }
            happyValue.tmpCrash.clear();
            queue.add(happyValue);
            //触发重新排序
            priorityQueue.add(tmpValue);
            priorityQueue.remove(tmpValue);
        }
        //这时优先队列空或者有难办的变量
        if(!priorityQueue.isEmpty()){
            PriorityQueue<Value> difficultValues = new PriorityQueue<>(new CustomComparator2());
            difficultValues.addAll(priorityQueue);
            while(!priorityQueue.isEmpty()){
                Value sadValue = priorityQueue.poll();
                for(Value v:sadValue.crash){
                    v.crash.remove(v);
                }
                sadValue.tmpCrash.clear();//没有寄存器的可怜变量
                priorityQueue.remove(sadValue);//被清理不管
                //触发重新排序
                priorityQueue.add(tmpValue);
                priorityQueue.remove(tmpValue);
                while(!priorityQueue.isEmpty()&&priorityQueue.peek().getCrashAmount()<Register.getGlobalAmount()){
                    //优先队列还有可以快乐分配的变量
                    Value happyValue = priorityQueue.poll();
                    for(Value v:happyValue.tmpCrash){
                        v.tmpCrash.remove(v);
                    }
                    happyValue.tmpCrash.clear();
                    queue.add(happyValue);
                    //触发重新排序
                    priorityQueue.add(tmpValue);
                    priorityQueue.remove(tmpValue);
                }
            }
        }

        //变量都在queue里了
        for(int i= queue.size()-1;i>=0;i--){
            Register.assign(queue.get(i));
            System.out.println(queue.get(i).name+":"+queue.get(i).registerAssigned.name);
        }
    }

}
// 自定义比较器类
class CustomComparator implements Comparator<Value> {
    @Override
    public int compare(Value v1, Value v2) {
        return v1.getCrashAmount()-v2.getCrashAmount();
    }
}
class CustomComparator2 implements Comparator<Value> {
    @Override
    public int compare(Value v1, Value v2) {
        if(v1.getNeighborDegree()>v2.getNeighborDegree()) return 1;
        else if(v1.getNeighborDegree()<v2.getNeighborDegree()) return -1;
        return 0;
    }
}
