import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainServer implements  Runnable{
        int port = 8000;
        ServerSocket server = null;
        Socket socket = null;

        HashMap<String, PrintWriter> hm;

        public MainServer() {

        }

    @Override
    public void run() {
        ServerThread st;

        Thread t;

        try {
            server = new ServerSocket( port ); //소켓 생성부터 listen까지
            System.out.println( "접속대기" );//출력

            hm = new HashMap<>(); //hashMap 객체를 생성

            while( true ) {
                System.out.println("Server while");
                socket = server.accept();
                System.out.println("Server accept");
                if (socket.isConnected()) {
                        st = new ServerThread(socket, hm);
                        t = new Thread(st);
                        t.setDaemon(true);
                        t.start();//쓰레드 시작
                }
            }
            // server.close();
            //System.out.println("Server terminated");
        }
        catch ( Exception e )	{
            e.printStackTrace(System.out);
        }
    }
}
