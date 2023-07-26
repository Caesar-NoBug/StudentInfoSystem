package main.java.com.caesar.dao.tasks;

import main.java.com.caesar.domain.Storable;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class UpdateTask extends Task{
    private Storable prevData;
    private Storable currData;

    public UpdateTask(Storable prevData, Storable currData){
        this.prevData = prevData;
        this.currData = currData;
    }

    @Override
    protected Boolean run() throws IOException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, InterruptedException {
        if(prevData.getId() == currData.getId()){
            return engine.update(currData);
        }
        else {
            return engine.delete(prevData) && engine.insert(currData);
        }
    }

    @Override
    public void withdraw() throws IOException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, InterruptedException {
        if(prevData.getId() == currData.getId()){
            engine.update(prevData);
        }
        else {
            engine.delete(currData);
            engine.insert(prevData);
        }
    }
}
