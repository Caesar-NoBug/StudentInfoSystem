package main.java.com.caesar.controller;

import main.java.com.caesar.dao.Database;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class WebServer extends Thread{

    private static final WebServer singleton = new WebServer();
    private static final int PORT = 14606;

    private static ServerSocket serverSocket;
    private static ExecutorService pool;

    private WebServer(){

        try {

            serverSocket = new ServerSocket(PORT);
            pool = new ThreadPoolExecutor(5, 7, 6, TimeUnit.SECONDS
                    , new ArrayBlockingQueue<>(2), new ThreadPoolExecutor.AbortPolicy());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static WebServer getInstance(){
        return singleton;
    }

    @Override
    public void run() {

        while(true){
            Socket socket = null;
            try {
                socket = serverSocket.accept();
                pool.execute(new ServerRunnable(socket));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
