package main.java.com.caesar.service;

import main.java.com.caesar.dao.Condition;
import main.java.com.caesar.dao.Database;
import main.java.com.caesar.domain.Storable;
import main.java.com.caesar.domain.Student;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class StudentService {
    private static final StudentService singleton = new StudentService();
    private static Database database;

    private StudentService()  {
        database = Database.getInstance("tbl_student.txt",
                "freed_memory_student.txt", Student.class);
        //数据仅供测试使用
        /*
        Random random = new Random();
        for (int i = 0; i < 60; i++) {
            long studentId = Math.abs(random.nextLong()) % 100000000;
            String name = "student" + i;
            try {
                database.insert(new Student(studentId, name, null));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
         */
    }

    public static StudentService getInstance(){

        return singleton;
    }

    public List<Storable> selectPage(int pageSize, int pageIndex){
        List<Storable> students = null;
        try {
            students = database.selectPage(pageSize, pageIndex);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return students;
    }

    public List<Storable> selectStudent(Student student, int maxCount){
        List<Storable> students = null;

        Condition condition = null;
        try {
            //输入学号则精确匹配
            if(student.getStudentId() != Student.UNDEFINED_ID){
                Storable resultStudent = database.select(student.getStudentId());

                if(resultStudent == null){
                    return null;
                }
                students = new ArrayList<>();
                students.add(resultStudent);
            }else {//否则根据名字模糊匹配

                condition = data -> {
                    Student goalStudent = (Student) data;

                    if (goalStudent.getName().contains(student.getName()))
                        return true;

                    return false;
                };

                students = database.selectByCondition(condition, maxCount);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return students;
    }

    public boolean insertStudent(Student student){
        boolean result = false;
        try {
            result = database.insert(student);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public boolean deleteStudent(Student student){
        boolean result = false;
        try {
            result = database.delete(student);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public boolean updateStudent(Student preStudent, Student currStudent){
        boolean result = false;
        try {
            result = database.update(preStudent, currStudent);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public boolean withdraw(){
        try {
            return database.withdraw();
        } catch (IOException | ClassNotFoundException | InterruptedException | InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return false;
    }
}
