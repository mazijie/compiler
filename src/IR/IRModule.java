package IR;

import IR.Value.Function;
import IR.Value.GlobalVar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class IRModule {
    public static IRModule module = new IRModule();
    private IRModule(){}
    //Module下辖若干全局变量和函数
    public List<GlobalVar> globalVars = new ArrayList<>();
    public List<Function> functions=new ArrayList<>();


    public void print() throws IOException {
        for(GlobalVar var : globalVars){
            var.print();
        }
        for(Function f : functions){
            f.print();
        }
    }

    public void addFunction(Function func) {
        functions.add(func);
    }
    public void addGlobalVar(GlobalVar var) { globalVars.add(var);}
}
