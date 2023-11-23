import java.net.ServerSocket;
import java.net.Socket;

    public class MainServer implements Runnable {
        int port = 8080;
        ServerSocket server = null;
        Socket socket = null;

        public MainServer() {

        }

        @Override
        public void run() {
            ServerThread st;

            Thread t;

            try {
                server = new ServerSocket(port); //소켓 생성부터 listen까지
                System.out.println("접속대기");

                ServerData sd = new ServerData();

                while (true) {
                    System.out.println("Server while");
                    socket = server.accept();
                    System.out.println("Server accept");
                    if (socket.isConnected()) {
                        st = new ServerThread(socket, sd);
                        t = new Thread(st);
                        t.setDaemon(true);
                        t.start();//쓰레드 시작
                    }
                }
            } catch (Exception e) {
                e.printStackTrace(System.out);
            }
        }
    }