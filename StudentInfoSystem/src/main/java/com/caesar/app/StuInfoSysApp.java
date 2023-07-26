package main.java.com.caesar.app;

import main.java.com.caesar.controller.WebServer;
import main.java.com.caesar.dao.Condition;
import main.java.com.caesar.dao.Database;
import main.java.com.caesar.domain.Storable;
import main.java.com.caesar.domain.Student;

import java.util.Random;

public class StuInfoSysApp {
    public static void main(String[] args) throws Exception {

        WebServer webServer = WebServer.getInstance();

        webServer.start();
    }
}
