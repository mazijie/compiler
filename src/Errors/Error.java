package Errors;

public class Error {
    private final int line;//行号
    private final int index;//Token序号
    private final ErrorType type;//错误类型
    public Error(int line,int index,ErrorType type){
        this.line = line;
        this.index = index;
        this.type = type;
    }
    public int getIndex(){
        return index;
    }
    public String toString(){
        return line+" "+type.toString()+"\n";
    }
}
