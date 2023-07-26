package main.java.com.caesar.domain;

import main.java.com.caesar.enums.CodeFormat;

import java.io.Serializable;

public abstract class Storable implements Serializable {
    public Storable(){};
    public Storable(long id){};
    public Storable(byte[] bytes){};
    public Storable(String code, CodeFormat format){};
    public static int MEMORY_SIZE = 0;
    public abstract byte[] toByteArray();
    public abstract long getId();
}
