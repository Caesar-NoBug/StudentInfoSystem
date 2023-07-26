package main.java.com.caesar.dao;

import main.java.com.caesar.dao.engine.HashEngine;
import main.java.com.caesar.dao.engine.StorageEngine;
import main.java.com.caesar.dao.tasks.SelectTask;
import main.java.com.caesar.dao.tasks.Task;
import main.java.com.caesar.domain.Storable;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;

public class TaskExecutor{
    private static final int MAX_ENGINE_COUNT = 5;
    private static final int MAX_STACK_SIZE = 10;
    private String filename;
    private String memoryFilename;
    private Class<? extends Storable> dataClass;
    //任务栈，用于撤回操作
    private List<Task> tasks = new LinkedList<>();
    //共享引擎资源
    private List<StorageEngine> engines = new LinkedList<>();

    public TaskExecutor(String filename, String memoryFilename, Class<? extends Storable> dataClass) throws FileNotFoundException, NoSuchFieldException, IllegalAccessException {
        this.filename = filename;
        this.memoryFilename = memoryFilename;
        this.dataClass = dataClass;
    }

    //command pattern
    public Object execute(Task task) throws Exception {

        if(!(task instanceof SelectTask)) {
            tasks.add(task);
            if(tasks.size() > MAX_STACK_SIZE){
                tasks.remove(0);
            }
        }

        if(engines.size() == 0){
            engines.add(new HashEngine(filename, dataClass, MemoryController.getInstance(memoryFilename)));
        }

        task.setTaskExecutor(this);
        task.setEngine(engines.remove(engines.size() - 1));

        return task.execute();
    }

    public boolean withdraw() throws IOException, ClassNotFoundException, InterruptedException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {

        if(tasks.size() == 0){
            return false;
        }
        Task task = tasks.remove(tasks.size() - 1);
        System.out.println(task);
        task.withdraw();
        return true;
    }

    //Observer pattern(Observer)
    public void update(StorageEngine engine){
        if(engines.size() < MAX_ENGINE_COUNT) {
            this.engines.add(engine);
        }else {
            try {
                engine.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
