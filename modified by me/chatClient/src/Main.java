import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

public class Main implements Runnable{
    // 서버 포트 지정
    private static final int PORT = 8080;
    static Socket csocket ;
    static BufferedReader in;
    static PrintWriter out;
    private static AtomicBoolean ID_reg_Flag = new AtomicBoolean(false);

    private static final AtomicBoolean lock = new AtomicBoolean(true);

    public void reg_stop(){
        ID_reg_Flag.set(true);
    }

    private static void showUsage(){ //프로토콜 사용 설명 안내
        System.out.println("---------------USAGE---------------");
        System.out.println("\"WIKI/CREATE/message\"");
        System.out.println("\"WIKI/READ/unitID\"");
        System.out.println("\"WIKI/UPDATE/unitID/message\"");
        System.out.println("\"WIKI/DELETE/unitID\"");
        System.out.println("\"CHAT/fromID/toID/message\"");
        System.out.println("\"QUIT\"");
        System.out.println("-----------------------------------");
    }

    @Override
    public void run() { //sub thread에서 동작하는 코드
        String msg;

        //서버로부터 메세지를 받아오는 로직
        while (true){
            try {
                 msg = in.readLine();
            } catch (IOException e) {
                break;
            }
            if (msg.equals("Success:Reg_ID")){
                System.out.println("ID 등록이 성공했습니다.");
                reg_stop();
            }
            else if (msg != null) {
                System.out.println(msg);
            }
            lock.set(false);
        }
    }

    public static void main(String[] args) throws IOException{ //main thread에서 동작하는 코드
        Main t = new Main();
        final Scanner sc = new Scanner(System.in);

            csocket = new Socket("localhost", PORT); // 소켓 생성
            in = new BufferedReader (new InputStreamReader(csocket.getInputStream())); // 읽기 스트림
            out = new PrintWriter(csocket.getOutputStream()); // 쓰기 스트림

            Thread rt = new Thread(t); //receive 전용
            rt.start();

            String msg = null;
            String myID = null;

            //fromID를 입력받는 로직
            while(!ID_reg_Flag.get()) {
                System.out.print("Enter your ID:");
                myID = sc.nextLine();
                msg = "ID/" + myID;
                out.println(msg);
                out.flush();
                lock.set(true);
                while(lock.get()); //t(sub) thread에서 ID_reg_Flag에 값을 줄 때까지 대기시킨다.
            }

            showUsage(); //

            //서버로 입력 메세지를 보내는 로직
            while (true){
                msg = sc.nextLine();
                String[] tokens = msg.split("/");
                String code = tokens[0];

                if(code.equalsIgnoreCase("QUIT")) { //code.equalsIgnoreCase("Quit")
                    msg += "/"+myID+"/";
                    out.println(msg);
                    out.flush();
                    System.out.println("서버와의 연결이 종료되었습니다.");
                    break;
                }
                else {
                    out.println(msg);
                    out.flush();
                }
            }
            out.close();
            csocket.close();
    }
}