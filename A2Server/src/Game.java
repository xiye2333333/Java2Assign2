import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;

public class Game extends Thread {

    private Socket player1;
    private Socket player2;

    public Game(Socket player1, Socket player2) {
        this.player1 = player1;
        this.player2 = player2;
    }

    public void setPlayer1(Socket player1) {
        this.player1 = player1;
    }

    public void setPlayer2(Socket player2) {
        this.player2 = player2;
    }

    public void run() {
        try {
            DataOutputStream outputStream1 = new DataOutputStream(player1.getOutputStream());
            DataOutputStream outputStream2 = new DataOutputStream(player2.getOutputStream());
            DataInputStream player1_in = null;
            DataInputStream player2_in = null;
            player1_in = new DataInputStream(player1.getInputStream());
            player2_in = new DataInputStream(player2.getInputStream());
            outputStream1.writeUTF("1");
            outputStream2.writeUTF("2");

            //communicate
            while (true) {
                try {
                    String[] player1_data = player1_in.readUTF().split(" ");
                    System.out.println(player1.getPort() + " " + player1_data[0] + " " + player1_data[1]);
                    outputStream2.writeUTF(player1_data[0] + " " + player1_data[1]);
                } catch (EOFException e) {
                    System.out.println("Game over");
                    break;
                } catch (IOException e) {
                    System.out.println(player1.getPort() + " " + "has disconnected");
                    outputStream2.writeUTF("Your opponent has disconnected");
                    break;
                }

                try {
                    String[] player2_data = player2_in.readUTF().split(" ");
                    System.out.println(player2.getPort() + " " + player2_data[0] + " " + player2_data[1]);
                    outputStream1.writeUTF(player2_data[0] + " " + player2_data[1]);
                } catch (EOFException e) {
                    System.out.println("Game over");
                    break;
                } catch (IOException e) {
                    System.out.println(player2.getPort() + " " + "has disconnected");
                    outputStream1.writeUTF("Your opponent has disconnected");
                    break;
                }
            }
        } catch (IOException e) {
            System.out.println("Game over!");
        }

    }
}
