package utils;

import IR.Value.Instructions.BR;

import java.util.ArrayList;
import java.util.List;

public class BRForTable {
    List<BR> breakToEnd = new ArrayList<BR>();
    List<BR> continueToNext = new ArrayList<BR>();
    String label;//forstmt1末尾标签
    BRForTable pre;
    public BRForTable(BRForTable pre){
        this.pre = pre;
    }
    List<BR> getBreakToEnd() {
        return breakToEnd;
    }
    List<BR> getContinueToNext() {
        return continueToNext;
    }
}
