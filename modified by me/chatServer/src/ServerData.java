import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class ServerData {

        HashMap<String, PrintWriter> hm; //USER_ID 명단
        public List<Integer> list; //unitID 명단
        public Queue<Integer> spare;//삭제된 unitID의 순번을 재사용할 명단
        public HashMap<Integer, String> serverMemory;//unitID를 key로 하여 serverMemory에 body 저장
        public int[] count; //unitID의 순번을 정할 때 사용되는 카운트 변수이다.
                            //기본 자료형으로 선언할 경우 깊은 복사(deep copy)가 일어난다.
                            //얕은 복사(shallow copy)가 되는 배열 형태로 카운트 변수를 선언하여
                            //스레드간 count 변수가 동기화되지 않는 문제를 해결했다.

        public ServerData() {
            hm = new HashMap<>();
            this.list = new ArrayList<>();
            this.spare = new LinkedList<>();
            this.serverMemory = new HashMap<>();
            this.count = new int[1];
            this.count[0] = 0;
        }
}
