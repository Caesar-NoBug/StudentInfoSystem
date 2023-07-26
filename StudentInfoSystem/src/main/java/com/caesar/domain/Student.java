package main.java.com.caesar.domain;

import main.java.com.caesar.enums.CodeFormat;
import main.java.com.caesar.utils.MyUtil;

import java.lang.reflect.InvocationTargetException;
import java.util.StringTokenizer;

public class Student extends Storable {

    public static final String UNDEFINED = "undefined";
    public static final long UNDEFINED_ID = -99999;
    public static final String UNDEFINED_NAME = "UNDEFINED_NAME";
    public static int MEMORY_SIZE = 0;
    private long studentId;
    private String name;
    private Storable detail;
    private static Class<? extends Storable> detailClass;
    // studentId + name(最多支持8个中文或24个英文)
    private static final int SELF_SIZE = 8 + 8 * 3;

    static {
        MEMORY_SIZE = SELF_SIZE;
    }

    public Student(long studentId){
        this.studentId = studentId;
    }

    public Student(long studentId, String name, Storable detail) {
        this.studentId = studentId;
        this.name = name;
        this.detail = detail;

        //detail 理应只同时使用一种
        if(MEMORY_SIZE != SELF_SIZE || detail == null) return;
        detailClass = detail.getClass();
        MEMORY_SIZE += detail.MEMORY_SIZE;
    }

    public Student(String code, CodeFormat format){
        try {
            switch (format){
                case JSON -> {
                    code = code.replaceAll("[\"{}]+", "");
                    StringTokenizer tokenizer = new StringTokenizer(code, ":,");

                    tokenizer.nextToken();
                    String studentId = tokenizer.nextToken();
                    if(UNDEFINED.equals(studentId)){
                        this.studentId = UNDEFINED_ID;
                    }else {
                        this.studentId = Long.parseLong(studentId);
                    }

                    tokenizer.nextToken();
                    String name = tokenizer.nextToken();
                    if(UNDEFINED.equals(name)){
                        this.name = UNDEFINED_NAME;
                    }else {
                        this.name = name;
                    }

                    if(tokenizer.hasMoreTokens()){
                        tokenizer.nextToken();

                            this.detail = detailClass.getConstructor(String.class, CodeFormat.class).newInstance(code, format);

                    }
                }
                case KEY_VALUE -> {
                    StringTokenizer tokenizer = new StringTokenizer(code, "&=");
                    tokenizer.nextToken();

                    String studentId = tokenizer.nextToken();
                    if(UNDEFINED.equals(studentId)){
                        this.studentId = UNDEFINED_ID;
                    }else {
                        this.studentId = Long.parseLong(studentId);
                    }

                    tokenizer.nextToken();

                    String name = tokenizer.nextToken();
                    if(UNDEFINED.equals(name)){
                        this.name = UNDEFINED_NAME;
                    }else {
                        this.name = name;
                    }

                    if(tokenizer.hasMoreTokens()){
                        tokenizer.nextToken();
                        this.detail = detailClass.getConstructor(String.class, CodeFormat.class).newInstance(code, format);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public long getStudentId() {
        return studentId;
    }

    public void setStudentId(long studentId) {
        this.studentId = studentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Storable getDetail() {
        return detail;
    }

    public void setDetail(Storable detail) {
        this.detail = detail;
    }

    public Student(byte[] bytes) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        int p = 0;
        this.studentId = MyUtil.bytesToLong(bytes, p);
        p += 8;
        this.name = MyUtil.bytesToString(bytes, p);
        p += bytes[p] + 1;
        int length = bytes[p ++];
        if(length != 0) {
            byte[] detailBytes = new byte[length];
            System.arraycopy(bytes, p, detailBytes, 0, length);
            this.detail = detailClass.getConstructor(byte[].class).newInstance(detailBytes);
        }
    }

    //用一个byte位储存数据长度，故name和detail字段长度不应超出127byte
    @Override
    public byte[] toByteArray() {

        byte[] bytes = new byte[MEMORY_SIZE];

        int p = MyUtil.longToBytes(bytes, 0, studentId);
        p = MyUtil.stringToBytes(bytes, p, name);

        if(detail != null) {
            byte[] detailBytes = detail.toByteArray();
            bytes[p++] = (byte) detailBytes.length;
            System.arraycopy(detail.toByteArray(), 0, bytes, p, detailBytes.length);
        }

        return bytes;
    }

    @Override
    public String toString() {
        String str =    "{" +
                        "\"studentId\":\"" + studentId + '\"' +
                        ", \"name\":\"" + name + '\"';
        if(detail != null){
            str += ", \"detail\":\"" + detail.toString() + '\"';
        }
        str += "}";
        return str;
    }

    @Override
    public long getId() {
        return studentId;
    }
}
