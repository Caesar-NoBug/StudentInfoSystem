package main.java.com.caesar.dao.tasks;

import main.java.com.caesar.dao.engine.StorageEngine;
import main.java.com.caesar.dao.TaskExecutor;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public abstract class Task{
    protected TaskExecutor taskExecutor;
    protected StorageEngine engine;

    protected abstract Object run() throws Exception;

    public abstract void withdraw() throws IOException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, InterruptedException;

    public void setEngine(StorageEngine engine) {
        this.engine = engine;
    }

    public void setTaskExecutor(TaskExecutor taskExecutor){
        this.taskExecutor = taskExecutor;
    }

    //template method
    public Object execute() throws Exception {
        Object result = this.run();
        notifyExecutor();
        return result;
    }

    //Observer pattern(Subject)
    protected void notifyExecutor(){
        taskExecutor.update(this.engine);
    }
}
