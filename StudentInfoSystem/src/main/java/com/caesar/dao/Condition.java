package main.java.com.caesar.dao;

import main.java.com.caesar.domain.Storable;

public interface Condition {
    //验证该数据是否符合条件
    boolean verify(Storable data);
}
