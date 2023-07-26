package main.java.com.caesar.dao.engine;

import main.java.com.caesar.dao.Condition;
import main.java.com.caesar.domain.Storable;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

public interface StorageEngine {

    void close()throws IOException;
    //增
    boolean insert(Storable data) throws IOException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, InterruptedException;
    //删
    boolean delete(Storable data) throws IOException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, InterruptedException;
    //改
    boolean update(Storable data) throws IOException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, InterruptedException;
    //查
    Storable select(long id) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, IOException, ClassNotFoundException, InterruptedException;

    /**
     * @param condition 条件
     * @param maxCount 最大数量
     * @return 满足条件的数据集合
     */
    //visitor pattern
    List<Storable> selectByCondition(Condition condition, int maxCount) throws IOException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, InterruptedException;

    /**
     * @param pageSize 每页数据的数量
     * @param pageIndex 页数
     * @return 临近3页的数据
     */
    List<Storable> selectPage(int pageSize, int pageIndex) throws IOException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, InterruptedException;
}
