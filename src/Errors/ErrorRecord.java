package Errors;

import utils.IOUtils;

import java.io.IOException;
import java.util.Comparator;
import java.util.PriorityQueue;

public class ErrorRecord {
    public static boolean isCorrect=true;
    //存放各种错误的优先队列
    private static PriorityQueue<Error> errors = new PriorityQueue<Error>(new Comparator<Error>() {
        @Override
        public int compare(Error e1, Error e2) {
            // 在这里定义你的优先级比较规则
            // 例如，按整数值升序排序
            return e1.getIndex() - e2.getIndex();
        }
    });

    public static void printError() throws IOException {
        while(!errors.isEmpty())
        {
            isCorrect=false;
            Error e = errors.poll();
            IOUtils.write_err(e.toString());
        }
    }

    public static void addError(int line,int index,ErrorType type){
        errors.add(new Error(line,index,type));
    }
}
