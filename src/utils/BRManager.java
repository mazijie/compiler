package utils;

import IR.Value.Instructions.BR;

import java.util.ArrayList;
import java.util.List;

public class BRManager {
    public static List<BRTable> tables = new ArrayList<BRTable>();
    public static List<BRForTable> fortables = new ArrayList<BRForTable>();
    public static BRTable curTable = null;
    public static BRForTable curForTable = null;

    public static void addForTable(){
        BRForTable newTable = new BRForTable(curForTable);
        curForTable=newTable;
        fortables.add(newTable);
        addTable();
    }
    public static void removeForTable(){
        curTable=curTable.pre;
        curForTable=curForTable.pre;
    }

    public static List<BR> br_breakToEnd(){
        return curForTable.getBreakToEnd();
    }

    public static List<BR> br_continueToNext(){
        return curForTable.getContinueToNext();
    }

    public static void set_label_for_begin(String label){
        curForTable.label=label;
    }

    public static String get_label_for_begin(){
        return curForTable.label;
    }

    public static void addTable(){
        BRTable newTable = new BRTable(curTable);
        tables.add(newTable);
        curTable = newTable;
    }
    public static void removeTable(){
        curTable=curTable.pre;
    }
    public static List<BR> br_true_wait_if(){
        return curTable.br_true_wait_if;
    }
    public static List<BR> br_false_wait_else(){
        return curTable.br_false_wait_else;
    }
    public static List<BR> br_false_wait_LAndExp(){
        return curTable.br_false_wait_LAndExp;
    }
    public static List<BR> br_true_wait_EqExp(){
        return curTable.br_true_wait_EqExp;
    }

    public static List<BR> br_break_wait_end(){return curForTable.getBreakToEnd();}
    public static List<BR> br_continue_wait_next(){return curForTable.getContinueToNext();}

    public static void add_br_true_wait_if(BR b){
        curTable.br_true_wait_if.add(b);
    }
    public static void add_br_false_wait_else(BR b){
        curTable.br_false_wait_else.add(b);
    }
    public static void add_br_false_wait_LAndExp(BR b){
        curTable.br_false_wait_LAndExp.add(b);
    }
    public static void add_br_true_wait_EqExp(BR b){
        curTable.br_true_wait_EqExp.add(b);
    }
    public static void add_br_break_wait_end(BR b){curForTable.breakToEnd.add(b);}
    public static void add_br_continue_wait_next(BR b){curForTable.continueToNext.add(b);}
    public static void refresh_br_true_wait_if(){
        curTable.br_true_wait_if=new ArrayList<>();
    }
    public static void refresh_br_false_wait_else(){
        curTable.br_false_wait_else=new ArrayList<>();
    }
    public static void refresh_br_false_wait_LAndExp(){
        curTable.br_false_wait_LAndExp=new ArrayList<>();
    }
    public static void refresh_br_true_wait_EqExp(){
        curTable.br_true_wait_EqExp=new ArrayList<>();
    }
}
