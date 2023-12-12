import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ServerThread implements Runnable {
    Socket client;//Socket 클래스 타입의 변수 child 선언
    BufferedReader in; // BufferReader 클래스 타입의 변수 ois 선언
    PrintWriter out; // PrintWriter 클래스 타입의 변수 oos 선언
    InetAddress ip; // InetAddress 클래스 타입의 변수 ip 선언
    public HashMap<String, PrintWriter> hm;
    public List<Integer> list;
    public Queue<Integer> spare;
    public HashMap<Integer, String> serverMemory;
    public int[] count;
    private static Lock lock = new ReentrantLock(); //unitID 조작을 동기적으로 처리하기 위한 lock


    public ServerThread ( Socket s, ServerData d ) throws IOException{
       client = s;
       hm = d.hm;
       list = d.list;
       spare = d.spare;
       serverMemory = d.serverMemory;
       count = d.count;

        try	{
            in = new BufferedReader( new InputStreamReader( client.getInputStream(), StandardCharsets.UTF_8 ) );
            out = new PrintWriter( client.getOutputStream(), false, StandardCharsets.UTF_8);
            ip = client.getInetAddress();
        }catch (final IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public void run() {
        String msg = null; //클라이언트로부터 받은 전체 메세지
        String code = null; //전체 메세지 중 서비스 유형 구분
        String fromID = null; // 클라이언트로부터 입력받은 ID
        String CRUD = null; //WIKI에서 사용할 서비스 유형
        String body = null; //헤더를 제외한 본문
        String toID = null; //CHAT에서 사용하는 수신자 ID
        int unitID = 0; //WIKI를 통해 저장되는 메세지 식별값

        try
        {
            while( (msg = in.readLine()) != null ) {
                try{
                    System.out.println("received msg: "+msg);
                    final String[] tokens = msg.split("/");
                    code = tokens[0];
                    fromID = tokens[1];
                    if(code.equalsIgnoreCase("QUIT")) {
                        System.out.println("fromID: "+fromID+"를 종료합니다.");
                        synchronized(hm) {
                            try {
                                hm.remove(fromID);
                            } catch (Exception ignored) {}
                        }
                        break;
                    }
                    else if(code.equalsIgnoreCase("ID")) {
                        if (hm.containsKey(fromID)){
                            out.println("FAIL:Reg_ID"); //ID 중복일 경우
                            out.flush();
                        }
                        else {
                            synchronized (hm) {
                                hm.put(fromID, out);
                                out.println("Success:Reg_ID");
                                out.flush();
                            }
                        }
                    }
                    else if (code.equalsIgnoreCase("WIKI")) {
                        CRUD = tokens[1];
                        body = tokens[2];
                        /*CREATE 외 READ,UPDATE,DELETE는 tokens[2]가 unitID를 가리킨다.*/
                        lock.lock();
                        if(CRUD.equalsIgnoreCase("CREATE")) {
                            if(!spare.isEmpty()){
                                unitID = spare.peek();
                                list.add(spare.poll());
                            } else {
                                unitID = count[0];
                                list.add(count[0]);
                                count[0]++;
                            }
                            serverMemory.put(unitID,body);
                            msg = "(wikipedia)CREATE "+unitID+": "+body;
                            broadcast(msg);
                        }
                        else if(CRUD.equalsIgnoreCase("READ")) {
                            try {
                                unitID = Integer.valueOf(tokens[2]);
                            } catch (NumberFormatException e) {
                                out.println("잘못된 형식: unitID는 숫자입니다.");
                                out.flush();
                                continue;
                            }
                            body = serverMemory.get(unitID);
                            out.println("READ "+unitID+": "+body);
                            out.flush();
                        }
                        else if(CRUD.equalsIgnoreCase("UPDATE")) {
                            try {
                                unitID = Integer.valueOf(tokens[2]);
                            } catch (NumberFormatException e) {
                                out.println("잘못된 형식: unitID는 숫자입니다.");
                                out.flush();
                                continue;
                            }
                            if(serverMemory.containsKey(unitID)){
                                body = tokens[3];
                                serverMemory.put(unitID,body);
                                msg =  "(wikipedia)UPDATE "+unitID+": "+body;
                                broadcast(msg);
                            }
                            else{
                                out.println("등록되지 않은 unitID입니다.");
                                out.flush();
                                continue;
                            }
                        }
                        else if(CRUD.equalsIgnoreCase("DELETE")) {
                            unitID = Integer.valueOf(tokens[2]);
                            list.remove(unitID);
                            spare.offer(unitID);
                            serverMemory.remove(unitID);
                            msg = "(wikipedia)DELETE unitID:"+unitID;
                            broadcast(msg);
                        }
                        lock.unlock();
                    }
                    else if (code.equalsIgnoreCase("CHAT")) {
                        toID = tokens[2];
                        body = tokens[3];
                        msg =  "("+fromID+"):"+body;
                        sendTo(toID, msg);
                        out.println("메세지를 성공적으로 전송했습니다.");
                        out.flush();
                    }
                    else {
                        out.println("잘못된 형식의 메세지입니다.");
                        out.flush();
                        continue;
                    }
                }
                catch (Exception e ) {
                    out.println("잘못된 형식의 메세지입니다.");
                    out.flush();
                    continue;
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
            out.println(message);
            out.flush();
        }
    }
}
