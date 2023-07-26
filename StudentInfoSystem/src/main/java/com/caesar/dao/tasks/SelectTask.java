package main.java.com.caesar.dao.tasks;

import main.java.com.caesar.dao.Condition;
import main.java.com.caesar.enums.SelectMethod;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class SelectTask extends Task{
    private SelectMethod method;
    private long id;
    private Condition condition;
    private int maxCount;
    private int pageSize;
    private int pageIndex;

    public SelectTask(long id){
        this.method = SelectMethod.ID;
        this.id = id;
    }

    public SelectTask(Condition condition, int maxCount){
        this.method = SelectMethod.CONDITION;
        this.condition = condition;
        this.maxCount = maxCount;
    }

    public SelectTask(int pageSize, int pageIndex){
        this.method = SelectMethod.PAGE;
        this.pageSize = pageSize;
        this.pageIndex = pageIndex;
    }

    @Override
    protected Object run() throws IOException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, InterruptedException {

        switch (method){
            case ID -> {
                return engine.select(id);
            }
            case CONDITION -> {
                return engine.selectByCondition(condition, maxCount);
            }
            case PAGE -> {
                return engine.selectPage(pageSize, pageIndex);
            }
        }

        return null;
    }

    @Override
    public void withdraw() throws IOException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {

    }
}
