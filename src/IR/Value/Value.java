package IR.Value;

import IR.Array.Array;
import IR.Array.ArrayType;
import IR.Type.PointerType;
import IR.Type.ValueType;
import IR.Use;
import MIPS.Register;
import config.Config;
import utils.BRManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Value{
    public String name;
    public ValueType type;
    public ArrayList<Use> useList=new ArrayList<Use>();
    public static int valNumber =-1;
    public Value(String name,ValueType type){
        this.name=name;
        this.type=type;
        this.loopFloor= BRManager.loopFloor;
    }
    public Value(String name,ValueType type,int demension){
        this.name=name;
        this.type=type;
        this.loopFloor= BRManager.loopFloor;
    }
    public Value(String name,ValueType type,int demension1,int demension2){
        this.name=name;
        this.type=type;
        this.loopFloor= BRManager.loopFloor;
    }
    public boolean isArray(){
        return this instanceof Array;
    }
    public boolean isPointer(){
        return this instanceof VarPointer;
    }

    //优化部分-冲突图
    public Set<Value> crash= new HashSet<>();
    public Set<Value> tmpCrash = new HashSet<>();
    //冲突次数
    public int getCrashAmount(){
        return tmpCrash.size();
    }
    public double getNeighborDegree(){
        //平均相邻度数
        int sum = 0;
        for(Value neighbor:tmpCrash){
            sum+=neighbor.getCrashAmount();
        }
        return 1.0*sum/getCrashAmount()-loopFloor*Config.loopWeight;
    }
    public int loopFloor;//循环层级
    public Register registerAssigned;//分配好的寄存器，但是可能为null
}
