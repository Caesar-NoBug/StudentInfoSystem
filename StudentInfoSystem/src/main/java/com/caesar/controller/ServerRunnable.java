package main.java.com.caesar.controller;

import main.java.com.caesar.domain.Storable;
import main.java.com.caesar.domain.Student;
import main.java.com.caesar.enums.CodeFormat;
import main.java.com.caesar.service.StudentService;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.StringTokenizer;

public class ServerRunnable implements Runnable{

    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int DEFAULT_MAX_COUNT = 30;
    private static final String PATH = ServerRunnable.class.getClassLoader().getResource("").getPath() + "main/resources/static/student.html";
    private StudentService studentService = StudentService.getInstance();
    private Socket socket;
    private PrintStream ps;

    public ServerRunnable(Socket socket){

        this.socket = socket;
    }

    @Override
    public void run() {

        InputStreamReader is = null;

        try {
            ps = new PrintStream(socket.getOutputStream());
            is = new InputStreamReader(socket.getInputStream());

            String method, path, params = null;

            method = nextWord(is, ' ');
            path = nextWord(is, ' ');


            if("/".equals(path)) {
                doStart();
                return;
            }
            if(!path.startsWith("/student")){
                return;
            }

            //TODO: 解析argument
            if("GET".equals(method) || "DELETE".equals(method)){
                StringTokenizer tokenizer = new StringTokenizer(path, "?");
                path = tokenizer.nextToken();
                params = tokenizer.nextToken();
            }
            else{
                String line = null;
                int length = 0;
                while(!(line = nextWord(is, '\n')).equals("\r")){
                    if(line.startsWith("Content-Length")) {
                        line = line.replaceFirst("Content-Length: ", "");
                        line = line.replaceFirst("\r", "");
                        length = Integer.parseInt(line);
                    }
                }
                char str[] = new char[length];
                is.read(str);
                params = String.valueOf(str);
            }

            Response response = null;

            switch (method){
                case "GET":
                    response = doGet(params);
                    break;
                case "POST":
                    response = doPost(params);
                    break;
                case "PUT":
                    response = doPut(params);
                    break;
                case "DELETE":
                    response = doDelete(params);
                    break;
            }

            String responseStr = response.toString();
            ps.println("HTTP/1.1 200 OK");
            ps.println("Content-Type:application/json;charset=UTF-8");
            ps.println("Content-Length:" + responseStr.getBytes(StandardCharsets.UTF_8).length);
            ps.println();
            ps.print(responseStr);
            ps.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //开始
    private Response doStart() throws IOException {

        BufferedReader br = new BufferedReader(new FileReader(PATH));

        ps.println("HTTP/1.1 200 OK");
        ps.println("Content-Type:text/html;charset=UTF-8");

        ps.println();

        String line = null;
        while ((line = br.readLine()) != null){
            ps.println(line);
        }

        ps.close();
        return null;
    }

    //查询
    private Response doGet(String argument) {
        Response response = new Response();
        String data = "{";
        List<Storable> students = null;
        if(argument.startsWith("page")){
            int pageIndex = Integer.parseInt(argument.replaceFirst("page=", ""));
            students = studentService.selectPage(DEFAULT_PAGE_SIZE, pageIndex);

            int startIndex = pageIndex - 1;
            if(startIndex < 1) startIndex = 1;
            data += "\"minPage\":" + startIndex + ",\"maxPage\":" + (startIndex - 1 + students.size() / DEFAULT_PAGE_SIZE) + ",";

        }else {
            Student student = new Student(argument, CodeFormat.KEY_VALUE);
            students = studentService.selectStudent(student, DEFAULT_MAX_COUNT);

            if(students == null){
                response.setState(Response.ERROR);
                response.setMessage("查无此人！");
                return response;
            }

            data += "\"minPage\":" + 1 + ",\"maxPage\":" + (1 + students.size() / DEFAULT_PAGE_SIZE) + ",";
        }

        data += "\"arr\":[";
        if(students.size() > 0) {
            for (int i = 0; i < students.size() - 1; i++) {
                data += students.get(i).toString() + ",";
            }
            data += students.get(students.size() - 1).toString();
        }
        data += "]}";
        response.setData(data);

        return response;
    }

    //新增
    private Response doPost(String argument) {
        Student student = new Student(argument, CodeFormat.JSON);
        Response response = new Response();

        if(!studentService.insertStudent(student)){
            response.setState(Response.ERROR);
            response.setMessage("添加失败！");
        }
        else {
            response.setMessage("添加成功！");
        }

        return response;
    }

    //修改、撤回
    private Response doPut(String argument){

        Response response = new Response();

        if(argument.startsWith("\"withdraw\"")){

            if(studentService.withdraw()) {
                response.setMessage("撤回成功！");
            }else {
                response.setState(Response.ERROR);
                response.setMessage("撤回失败！");
            }

        }else {
            argument = argument.substring(0, argument.length());
            System.out.println(argument);
            argument = argument.replaceAll("[\\[\\]]+", "");
            System.out.println(argument);
            Student preStudent = new Student(argument.replaceFirst(",\\{.*\\}", ""), CodeFormat.JSON);
            System.out.println(argument);
            Student currStudent = new Student(argument.replaceFirst("\\{.*\\},", ""), CodeFormat.JSON);

            if (!studentService.updateStudent(preStudent, currStudent)) {
                response.setState(Response.ERROR);
                response.setMessage("修改失败！");
            }
        }
        return response;
    }

    //删除
    private Response doDelete(String argument) {
        Student student = new Student(argument, CodeFormat.KEY_VALUE);
        Response response = new Response();

        if(!studentService.deleteStudent(student)){
            response.setState(Response.ERROR);
            response.setMessage("删除失败！");
        }

        return response;
    }

    /**
     * @param is 输入流
     * @param ch 终止处字符
     * @return 从当前位置到ch前截止的字符串
     */
    private String nextWord(InputStreamReader is, char ch) throws IOException {
        String res = "";
        int val;
        char character;
        while((val = is.read()) != -1){
            character = (char) val;
            if(character == ch) break;
            res += character;
        }
        return res;
    }

}
