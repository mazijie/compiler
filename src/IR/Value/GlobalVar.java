package IR.Value;

import IR.Array.Array;
import IR.Type.PointerType;
import IR.Type.ValueType;
import config.Config;
import utils.IOUtils;

import java.io.IOException;
import java.util.List;

//全局变量
public class GlobalVar extends VarPointer{
    boolean isConst;
    public int val=0;
    public Array array;
    public String string;
    public GlobalVar(String name, boolean isConst, PointerType type){
        super("@"+Config.fuzzyName+name+Config.fuzzyName,type);
        this.isConst = isConst;
    }
    public GlobalVar(String name,boolean isConst,PointerType type,int val){
        super("@"+Config.fuzzyName+name+Config.fuzzyName,type);
        this.isConst = isConst;
        this.val = val;
    }

    //数组指针
    public GlobalVar(String name, boolean isConst, PointerType type, Array array){
        super("@"+Config.fuzzyName+name+Config.fuzzyName,type);
        this.isConst = isConst;
        this.array = array;
    }


    public void print() throws IOException {
        if(array!=null){
            if(isConst)
                IOUtils.write(name+" = dso_local constant "+array.valToString()+"\n");
            else
                IOUtils.write(name+" = dso_local global "+array.valToString()+"\n");
        }
        else{
            if(isConst)
                IOUtils.write(name+" = dso_local constant "+type.pointto.toString()+" "+val+"\n");
            else
                IOUtils.write(name+" = dso_local global "+type.pointto.toString()+" "+val+"\n");
        }
    }

    public boolean isArray() {
        return array!=null;
    }

    public boolean isString() {
        return string!=null;
    }
}
