import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;

public class Server {

  private static int port = 8080;

  public static void start() throws IOException {
    ServerSocket socket = new ServerSocket(port);//打开服务套接字，监听1412端口

    WaitList waitList = new WaitList();
    Thread queue_thread = new Thread(waitList);
    queue_thread.start();
    while (true) {
      Socket s = socket.accept();
      waitList.addPlayer(s);
//            Game game = new Game(this);
//            Thread thread = new Thread(game);
//            thread.start();
    }
  }

  static class WaitList implements Runnable {
    private final Queue<Socket> players = new LinkedBlockingDeque<>();

    public void addPlayer(Socket s) throws IOException {
      synchronized (players) {
        players.add(s);
        DataOutputStream outputStream = new DataOutputStream(s.getOutputStream());
        outputStream.writeUTF(s.getPort() + " You are searching for opponent");

      }

    }

    @Override
    public void run() {
      while (true) {
        while (players.size() >= 2) {
          synchronized (players) {
            Socket player1 = players.poll();
            Socket player2 = players.poll();
            Thread game = new Game(player1, player2);
            game.start();
            System.out.println(player1.getPort() + " , " + player2.getPort() + " game start");
          }
        }
      }
    }
  }

  public static void main(String[] args) throws IOException {
    start();
  }

}
