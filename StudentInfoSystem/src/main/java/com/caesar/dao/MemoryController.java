package main.java.com.caesar.dao;

import main.java.com.caesar.utils.MyUtil;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

public class MemoryController {
    private String filename;
    private static final Map<String, MemoryController> singletons = new HashMap<>();

    private MemoryController(String filename){
        this.filename = filename;
        try {
            init();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static MemoryController getInstance(String filename){
        if(singletons.get(filename) == null){
            synchronized (MemoryController.class) {
                singletons.put(filename, new MemoryController(filename));
            }
        }
        return singletons.get(filename);
    }

    private void init() throws IOException {
        RandomAccessFile raf = new RandomAccessFile(filename, "rw");
        raf.writeInt(0);
        raf.close();
    }

    public Set<Integer> getFreedMemories() throws IOException {
        Set<Integer> memories = new HashSet<>();
        RandomAccessFile raf = new RandomAccessFile(filename, "rw");
        int count = raf.readInt();
        while(count -- > 0){
            memories.add(raf.readInt());
        }
        return memories;
    }

    public synchronized int malloc() throws IOException {

        RandomAccessFile raf = null;

        raf = new RandomAccessFile(filename, "rw");

        int count = raf.readInt();

        if(count == 0){
            raf.close();
            return -1;
        }
        raf.seek(0);
        raf.writeInt(-- count);
        //读取新的空间
        raf.seek(4 + count * 4);
        int pointer = raf.readInt();

        raf.close();

        return pointer;
    }

    public void free(int pointer) throws IOException{
        RandomAccessFile raf = new RandomAccessFile(filename, "rw");

        int count = raf.readInt();
        raf.seek(0);
        raf.writeInt(count + 1);
        raf.seek(4 + count * 4);
        raf.writeInt(pointer);
        raf.close();
    }
}
