package main.java.com.caesar.dao.tasks;

import main.java.com.caesar.domain.Storable;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class InsertTask extends Task{
    private Storable data;

    public InsertTask(Storable data){
        this.data = data;
    }

    @Override
    protected Boolean run() throws IOException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, InterruptedException {
        return engine.insert(data);
    }

    @Override
    public void withdraw() throws IOException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, InterruptedException {
        engine.delete(data);
    }

    @Override
    public String toString() {
        return "InsertTask{" +
                "data=" + data.toString() +
                "} " + super.toString();
    }
}
