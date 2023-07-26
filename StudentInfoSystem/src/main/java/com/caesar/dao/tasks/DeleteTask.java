package main.java.com.caesar.dao.tasks;

import main.java.com.caesar.domain.Storable;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class DeleteTask extends Task{

    private Storable data;

    public DeleteTask(Storable data){
        this.data = data;
    }

    @Override
    protected Boolean run() throws IOException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, InterruptedException {
        return engine.delete(data);
    }

    @Override
    public void withdraw() throws IOException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, InterruptedException {
        engine.insert(data);
    }

    @Override
    public String toString() {
        return "DeleteTask{" +
                "data=" + data.toString() +
                "} " + super.toString();
    }
}
