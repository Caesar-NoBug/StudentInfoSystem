package main.java.com.caesar.dao;

import main.java.com.caesar.dao.tasks.*;
import main.java.com.caesar.domain.Storable;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Database {

    private static Map<String, Database> singletons = new HashMap<>();
    private TaskExecutor taskExecutor;
    private Class dataClass;

    private Database(String filename, String memoryFilename, Class dataClass) {
        try {
            this.dataClass = dataClass;
            this.taskExecutor = new TaskExecutor(filename, memoryFilename, dataClass);
        } catch (FileNotFoundException | NoSuchFieldException e) {
            System.out.println("找不到该数据文件！");
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static Database getInstance(String filename, String memoryFilename, Class<? extends Storable> dataType) {

        Database singleton = singletons.get(filename);

        if(singleton == null){
            synchronized (Database.class){
                singleton = new Database(filename,memoryFilename, dataType);
            }
        }

        return singleton;
    }

    public boolean insert(Storable data) throws Exception {
        Task task = new InsertTask(data);
        return (boolean) taskExecutor.execute(task);
    };

    public boolean delete(Storable data) throws Exception {
        Task task = new DeleteTask(data);
        return (boolean) taskExecutor.execute(task);
    };

    public boolean update(Storable prevData, Storable currData) throws Exception {
        Task task = new UpdateTask(prevData, currData);
        return (boolean) taskExecutor.execute(task);
    };

    public Storable select(long id) throws Exception {
        Task task = new SelectTask(id);
        return (Storable) taskExecutor.execute(task);
    };

    public List<Storable> selectByCondition(Condition condition, int maxCount) throws Exception {
        Task task = new SelectTask(condition, maxCount);
        return (List<Storable>) taskExecutor.execute(task);
    };

    public List<Storable> selectPage(int pageSize, int pageIndex) throws Exception {
        Task task = new SelectTask(pageSize, pageIndex);
        return (List<Storable>) taskExecutor.execute(task);
    };

    public boolean withdraw() throws IOException, ClassNotFoundException, InterruptedException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        return taskExecutor.withdraw();
    }
}
