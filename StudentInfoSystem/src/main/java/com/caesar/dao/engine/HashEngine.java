package main.java.com.caesar.dao.engine;

import main.java.com.caesar.dao.Condition;
import main.java.com.caesar.dao.MemoryController;
import main.java.com.caesar.domain.Storable;
import main.java.com.caesar.utils.IndexedTree;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;


/**
 * 哈希表由HASH_SIZE（int）个头节点、一个tailPointer（int）节点和tailPointer个element元素组成
 * 节点由3部分组成：
 * 1.(byte)锁占用一个字节
 * 2.(int)下一节点编号
 * 3.(byte[])当前节点数据
 */
public class HashEngine implements StorageEngine {
    //数据库文件父路径
    private static final String ROOT = HashEngine.class.getClassLoader().getResource("").getPath() + "main/resources/database/";
    //哈希表表长
    private static final int HASH_SIZE = 9907;
    //指针所占字节数
    private static final int POINTER_SIZE = 4;
    //锁所占字节数
    private static final int LOCK_SIZE = 1;
    //上锁标识
    private static final int LOCKED_FLAG = 255;
    //未上锁标识
    private static final int UNLOCKED_FLAG = 0;
    private static final int DEFAULT_PAGE_COUNT = 3;
    //偏移量，即头指针所占字节数
    private final int OFFSET = HASH_SIZE * POINTER_SIZE;
    //当前值最大的指针
    private static int tailPointer = 1;
    //是否在tailPointer处写入数据（因为新分配内存的数据难以确定，故额外加一个软件层面的锁）
    private static boolean isWriting = false;
    //一个数据节点占的字节数
    private int ELEMENT_SIZE;
    //一个数据占的字节数
    private int DATA_SIZE;
    //与文件交互使用的io流
    private RandomAccessFile raf;
    //储存数据的类型
    private Class<? extends Storable> DATA_CLASS;
    //内存管理器
    private MemoryController memoryController;
    //树状数组，用于获取第k个元素的下标
    private IndexedTree array;

    public HashEngine(String filename, Class<? extends Storable> dataClass, MemoryController memoryController)
            throws NoSuchFieldException, IllegalAccessException, IOException {

        this.DATA_CLASS = dataClass;
        this.memoryController = memoryController;

        Field field = dataClass.getField("MEMORY_SIZE");
        field.setAccessible(true);
        DATA_SIZE = (int) field.get(null);
        ELEMENT_SIZE = LOCK_SIZE + POINTER_SIZE + DATA_SIZE;
        init(filename);
    }

    public void init(String filename) throws IOException {
        File file = new File(ROOT + filename);
        if(!file.exists()) {
            file.createNewFile();
            this.raf = new RandomAccessFile(ROOT + filename, "rw");

            for (int i = 0; i < HASH_SIZE; i++) {
                raf.writeInt(-1);
            }

            raf.write(new byte[ELEMENT_SIZE]);
        }
        else{
            this.raf = new RandomAccessFile(ROOT + filename, "rw");
            raf.seek(OFFSET);
            tailPointer = raf.readInt();
        }

        this.array = new IndexedTree(tailPointer - 1);

        for (Integer freedMemory : memoryController.getFreedMemories()) {
            array.change(freedMemory, false);
        }
    }

    @Override
    public boolean insert(Storable data) throws IOException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, InterruptedException {
        int newPointer = memoryController.malloc();
        boolean flag = newPointer != -1;
        int temp = tailPointer;
        if(flag){
            tailPointer = newPointer;
        }
        int pointer = find(data, false);
        if(pointer == -1) return false;

        //将其父节点与其相连
        if(pointer == -2){//头节点
            raf.seek(POINTER_SIZE * getHash(data));
        }else {//一般节点
            raf.seek(OFFSET + ELEMENT_SIZE * pointer);
        }
        raf.writeInt(tailPointer);
        if(flag) {
            tryLock(OFFSET + ELEMENT_SIZE * tailPointer);
        }else{
            tryLock();
            raf.seek(OFFSET + ELEMENT_SIZE * tailPointer);
        }
        isWriting = true;
        raf.write(LOCKED_FLAG);

        byte[] bytes = data.toByteArray();

        raf.writeInt(-1);
        raf.write(bytes);

        raf.seek(OFFSET + ELEMENT_SIZE * tailPointer);
        raf.write(UNLOCKED_FLAG);

        array.change(tailPointer, true);
        tailPointer ++;

        if(flag){
            tailPointer = temp;
        }else{
            raf.seek(OFFSET);
            raf.writeInt(tailPointer);
        }

        isWriting = false;
        return true;
    }

    @Override
    //删除操作至多影响数据时效性，故不加锁以提升效率
    public boolean delete(Storable data) throws IOException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, InterruptedException {
        int pointer = find(data, true);
        if(pointer == -1) return false;

        int nextPointer = 0;
        if(pointer == -2) {
            raf.seek(POINTER_SIZE * getHash(data));
            nextPointer = getNext(raf.readInt());
        }else {
            nextPointer = getNext(getNext(pointer));
        }

        if(pointer == -2){
            raf.seek(POINTER_SIZE * getHash(data));
        }else {
            raf.seek(OFFSET + ELEMENT_SIZE * pointer);
            raf.skipBytes(LOCK_SIZE);
        }
        long pos = raf.getFilePointer();
        int freedPointer = raf.readInt();
        memoryController.free(freedPointer);
        array.change(freedPointer, false);
        raf.seek(pos);
        raf.writeInt(nextPointer);

        return true;
    }

    @Override
    public boolean update(Storable data) throws IOException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, InterruptedException {
        int pointer = find(data, true);
        //TODO: 更改逻辑
        if(pointer == -1) return false;

        if(pointer == -2) {
            raf.seek(POINTER_SIZE * getHash(data));
            pointer = raf.readInt();
        }else {
            pointer = getNext(pointer);
        }

        tryLock(OFFSET + ELEMENT_SIZE * pointer);
        raf.write(LOCKED_FLAG);
        raf.skipBytes(POINTER_SIZE);

        byte[] bytes = data.toByteArray();
        raf.write(bytes);

        raf.seek(OFFSET + ELEMENT_SIZE * pointer);
        raf.write(UNLOCKED_FLAG);
        return true;
    }

    @Override
    public Storable select(long id) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, IOException, ClassNotFoundException, InterruptedException {

        Storable data = DATA_CLASS.getConstructor(long.class).newInstance(id);

        int p = find(data, true);
        if(p == -1) return null;

        if(p == -2) {
            raf.seek(POINTER_SIZE * getHash(data));
            p = raf.readInt();
        }else {
            p = getNext(p);
        }
        tryLock(OFFSET + ELEMENT_SIZE * p);
        raf.skipBytes(LOCK_SIZE + POINTER_SIZE);

        byte[] bytes = new byte[DATA_SIZE];
        raf.read(bytes);
        return DATA_CLASS.getConstructor(byte[].class).newInstance(bytes);
    }

    @Override
    public List<Storable> selectByCondition(Condition condition, int maxCount) throws IOException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, InterruptedException {
        List<Storable> datas = new LinkedList<>();
        Set<Integer> freedMemories = memoryController.getFreedMemories();

        raf.seek(OFFSET);
        for (int i = 1; i < tailPointer; i++) {

            if (freedMemories.contains(i)){
                raf.skipBytes(ELEMENT_SIZE);
                continue;
            }else{
                tryLock(OFFSET + i * ELEMENT_SIZE);
                raf.skipBytes(LOCK_SIZE + POINTER_SIZE);
            }

            byte[] bytes = new byte[DATA_SIZE];
            raf.read(bytes);
            Storable data = DATA_CLASS.getConstructor(byte[].class).newInstance(bytes);

            if(condition.verify(data)){
                    datas.add(data);
                    if(datas.size() == maxCount){
                        break;
                    }
            }
        }

        return datas;
    }

    @Override
    public List<Storable> selectPage(int pageSize, int pageIndex) throws IOException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, InterruptedException {

        int count = 0;
        List<Storable> datas = new LinkedList<>();
        Set<Integer> freedMemories = memoryController.getFreedMemories();
        int startIndex = pageIndex - 2;
        if(startIndex < 0) startIndex = 0;

        for (int i = array.indexAt(startIndex * pageSize + 1); i < tailPointer && count <= pageSize * DEFAULT_PAGE_COUNT; i++) {

            if (freedMemories.contains(i)){
                raf.skipBytes(ELEMENT_SIZE);
                continue;
            }else{
                tryLock(OFFSET + i * ELEMENT_SIZE);
                raf.skipBytes(LOCK_SIZE + POINTER_SIZE);
            }

            byte[] bytes = new byte[DATA_SIZE];
            raf.read(bytes);
            Storable data = DATA_CLASS.getConstructor(byte[].class).newInstance(bytes);
            datas.add(data);
            count ++;
        }

        return datas;
    }

    /**
     * @param data 待查找的数据
     * @param isExist 假定该数据是否存在
     *
     * @return isExist=true:  返回该元素的父节点的位置编号(不存在则return -1),
     *         isExist=false: 返回其Hash值链上最后一个节点的位置编号(该节点为头节点则返回-2，该数据存在则return -1)
     */
    private int find(Storable data, boolean isExist) throws IOException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, InterruptedException {
        int hashValue = getHash(data);

        raf.seek(hashValue * POINTER_SIZE);
        int nextPointer = raf.readInt(), currPointer = -2, prePointer;

        while(nextPointer != -1){

            prePointer = currPointer;
            currPointer = nextPointer;
            tryLock(OFFSET + ELEMENT_SIZE * nextPointer);
            raf.skipBytes(LOCK_SIZE);
            //raf.seek(OFFSET + ELEMENT_SIZE * nextPointer);
            nextPointer = raf.readInt();

            byte[] bytes = new byte[DATA_SIZE];
            raf.read(bytes);
            Storable dataNode = DATA_CLASS.getConstructor(byte[].class).newInstance(bytes);
            if(data.getId() == dataNode.getId())
                return isExist ? prePointer : -1;

        }

        return isExist ? -1 : currPointer;
    }

    //尝试获取锁，直到成功获取到锁才结束
    private void tryLock() throws InterruptedException {
        while(true){
            if(!isWriting) break;
            Thread.sleep(500);
        }
    }

    //尝试获取锁，直到成功获取到锁才结束,结束后指针将位于pos处
    private void tryLock(long pos) throws IOException, InterruptedException {
        int lock_flag;
        //将节点移至新位置
        while(true){
            raf.seek(pos);
            lock_flag = raf.read();
            if(lock_flag == UNLOCKED_FLAG) break;
            Thread.sleep(500);
        }
        raf.seek(pos);
    }

    private int getHash(Storable data){
        long id = data.getId();
        return (int) (id % HASH_SIZE);
    }

    private int getNext(int pointer) throws IOException {
        raf.seek(OFFSET + ELEMENT_SIZE * pointer + LOCK_SIZE);
        return raf.readInt();
    }

    public void close() throws IOException {
        raf.close();
    }
}
