package utils;

import IR.Value.Instructions.BR;

import java.util.List;

public class BRTable {
    public List<BR> br_true_wait_if;
    public List<BR> br_false_wait_else;
    public List<BR> br_false_wait_LAndExp;
    public List<BR> br_true_wait_EqExp;

    public String label;
    BRTable pre;
    public BRTable(BRTable pre){
        this.pre = pre;
    }
}
