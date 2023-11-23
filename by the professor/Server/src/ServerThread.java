import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class ServerThread implements Runnable {
    Socket client;//Socket 클래스 타입의 변수 child 선언
    BufferedReader in; // BufferReader 클래스 타입의 변수 ois 선언
    PrintWriter out; // PrintWriter 클래스 타입의 변수 oos 선언
    public HashMap<String, PrintWriter> hm ; // 접속자 관리

    InetAddress ip; // InetAddress 클래스 타입의 변수 ip 선언

    public ServerThread ( Socket s, HashMap<String, PrintWriter> h ) throws IOException{
       client = s;
       hm = h;

        try	{
            in = new BufferedReader( new InputStreamReader( client.getInputStream(), StandardCharsets.UTF_8 ) );
            out = new PrintWriter( client.getOutputStream(), false, StandardCharsets.UTF_8);
            ip = client.getInetAddress();
        }catch (final IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public void run() {
        String msg; // 문자열 변수 receiveDate 선언
        String code;
        String fromID=null;
        try
        {
            while( (msg = in.readLine()) != null ) {
                System.out.println("received msg: "+msg);
                final String[] tokens = msg.split(":"); // StringTokenizer(msg
                code = tokens[0];
                fromID = tokens[1];
                if( "QUIT".equals(code)) {
                    System.out.println(fromID+"종료합니다.");
                    synchronized(hm) {
                        try {
                            hm.remove(fromID);
                        } catch (Exception ignored) {}
                    }
                    break;
                }
                else if("ID".equals(code)) { //ID 등록
                    if (hm.containsKey(fromID)) {
                        out.println("FAIL:Reg_ID");
                        out.flush();
                    }
                    else {
                        synchronized (hm) {
                            hm.put(fromID, out);
                            out.println("Success:Reg_ID");
                            out.flush();
                        }
                    }
                } else if ("TO".equals(code)) {
                    String toID = tokens[2];
                    sendTo(toID, msg);
                } else {
                    broadcast(msg);
                }
            }
        }
        catch (Exception e ) {
            System.out.println(e.toString());
        }
        finally {
            try{
                    in.close();
                    out.close();
                    client.close();
            }catch (Exception e){
                System.out.println(e.toString());
            }
        }
    }



  private void broadcast( String message) throws IOException {
        synchronized( hm ) {
            for( PrintWriter out : hm.values( )){
                out.println( message );
                out.flush();
            }
        }
    }
  private void sendTo(String toID, String message) throws IOException {
        PrintWriter out = hm.get(toID);

        if(out != null){
            out.println( message );
            out.flush();
        }
    }
}