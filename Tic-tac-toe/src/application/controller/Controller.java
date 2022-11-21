package application.controller;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

public class Controller implements Initializable {
  private static final int PLAY_1 = 1;
  private static final int PLAY_2 = 2;
  private static final int EMPTY = 0;
  private static final int BOUND = 90;
  private static final int OFFSET = 15;

  @FXML
  private Pane base_square;

  @FXML
  private Rectangle game_panel;

  private static boolean TURN = false;

  private Socket socket;
  private DataOutputStream out;
  private DataInputStream in;

  private ClientThread clientThread;

  private static final int[][] chessBoard = new int[3][3];
  private static final boolean[][] flag = new boolean[3][3];

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    game_panel.setOnMouseClicked(event -> {
      int x = (int) (event.getX() / BOUND);
      int y = (int) (event.getY() / BOUND);
      if (TURN && refreshBoard(x, y)) {
        System.out.println("x: " + x + " y: " + y);
        try {
          out.writeUTF(x + " " + y);
          out.flush();

        } catch (IOException e) {
          e.printStackTrace();
        }
        TURN = false;
      }
      if (checkWin()) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Game Over");
        alert.setHeaderText(null);
        alert.setContentText("You win!");
        alert.showAndWait();
        exit();
      }
      if (checkDraw()) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Game Over");
        alert.setHeaderText(null);
        alert.setContentText("Draw!");
        alert.showAndWait();
        exit();

      }
    });


  }

  private boolean checkDraw() {
    for (int i = 0; i < 3; i++) {
      for (int j = 0; j < 3; j++) {
        if (chessBoard[i][j] == EMPTY) {
          return false;
        }
      }
    }
    return true;
  }

  private boolean checkWin() {
    for (int i = 0; i < 3; i++) {
      if (chessBoard[i][0] == chessBoard[i][1] && chessBoard[i][1] == chessBoard[i][2] &&
          chessBoard[i][0] != EMPTY) {
        return true;
      }
      if (chessBoard[0][i] == chessBoard[1][i] && chessBoard[1][i] == chessBoard[2][i] &&
          chessBoard[0][i] != EMPTY) {
        return true;
      }
    }
    if (chessBoard[0][0] == chessBoard[1][1] && chessBoard[1][1] == chessBoard[2][2] &&
        chessBoard[0][0] != EMPTY) {
      return true;
    }
    return chessBoard[0][2] == chessBoard[1][1] && chessBoard[1][1] == chessBoard[2][0] &&
        chessBoard[0][2] != EMPTY;
  }

  private void exit() {
    clientThread.exit();
//            out.writeUTF("exit");
//            out.flush();
//            socket.shutdownOutput();
//            socket.shutdownInput();
//            socket.close();
//            System.exit(0);
  }

  private boolean refreshBoard(int x, int y) {
    if (chessBoard[x][y] == EMPTY) {
      chessBoard[x][y] = TURN ? PLAY_1 : PLAY_2;
      drawChess();
      return true;
    }
    return false;
  }

  private void drawChess() {
    for (int i = 0; i < chessBoard.length; i++) {
      for (int j = 0; j < chessBoard[0].length; j++) {
        if (flag[i][j]) {
          // This square has been drawing, ignore.
          continue;
        }
        switch (chessBoard[i][j]) {
          case PLAY_1:
            drawCircle(i, j);
            break;
          case PLAY_2:
            drawLine(i, j);
            break;
          case EMPTY:
            // do nothing
            break;
          default:
            System.err.println("Invalid value!");
        }
      }
    }
  }

  private void drawCircle(int i, int j) {
    Circle circle = new Circle();
    base_square.getChildren().add(circle);
    circle.setCenterX(i * BOUND + BOUND / 2.0 + OFFSET);
    circle.setCenterY(j * BOUND + BOUND / 2.0 + OFFSET);
    circle.setRadius(BOUND / 2.0 - OFFSET / 2.0);
    circle.setStroke(Color.RED);
    circle.setFill(Color.TRANSPARENT);
    flag[i][j] = true;
  }

  private void drawLine(int i, int j) {
    Line line_a = new Line();
    Line line_b = new Line();
    base_square.getChildren().add(line_a);
    base_square.getChildren().add(line_b);
    line_a.setStartX(i * BOUND + OFFSET * 1.5);
    line_a.setStartY(j * BOUND + OFFSET * 1.5);
    line_a.setEndX((i + 1) * BOUND + OFFSET * 0.5);
    line_a.setEndY((j + 1) * BOUND + OFFSET * 0.5);
    line_a.setStroke(Color.BLUE);

    line_b.setStartX((i + 1) * BOUND + OFFSET * 0.5);
    line_b.setStartY(j * BOUND + OFFSET * 1.5);
    line_b.setEndX(i * BOUND + OFFSET * 1.5);
    line_b.setEndY((j + 1) * BOUND + OFFSET * 0.5);
    line_b.setStroke(Color.BLUE);
    flag[i][j] = true;
  }

  public void setSocket(Socket socket) throws IOException {
    this.socket = socket;
    this.out = new DataOutputStream(socket.getOutputStream());
    this.in = new DataInputStream(socket.getInputStream());

    clientThread = new ClientThread(socket);
    clientThread.start();
  }


  public class ClientThread extends Thread {
    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;

    public ClientThread(Socket socket) throws IOException {
      this.socket = socket;
      this.out = new DataOutputStream(socket.getOutputStream());
      this.in = new DataInputStream(socket.getInputStream());
    }

    public void exit() {
      try {
        clientThread.interrupt();
        socket.shutdownOutput();
        socket.shutdownInput();
        socket.close();
        System.exit(0);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    @Override
    public void run() {
      try {
        String player_number = in.readUTF();
        System.out.println("Game start! Player number: " + player_number);
        TURN = player_number.equals("1");
      } catch (IOException e) {
        e.printStackTrace();
      }
      while (true) {
        try {
          String[] data = in.readUTF().split(" ");
          if (data[0].equals("Your")) {
            System.out.println("Opponent exit!");
            Platform.runLater(() -> {
              Alert alert = new Alert(Alert.AlertType.INFORMATION);
              alert.setTitle("Game Over");
              alert.setHeaderText(null);
              alert.setContentText("Opponent exit!");
              alert.showAndWait();
              exit();

            });
            break;
          }
          System.out.println("Opponent's move: " + data[0] + " " + data[1]);
          int x = Integer.parseInt(data[0]);
          int y = Integer.parseInt(data[1]);
          Platform.runLater(() -> {
            refreshBoard(x, y);
            TURN = true;
            if (checkWin()) {
              Alert alert = new Alert(Alert.AlertType.INFORMATION);
              alert.setTitle("Game Over");
              alert.setHeaderText(null);
              alert.setContentText("You lose!");
              alert.showAndWait();
              exit();
            }
            if (checkDraw()) {
              Alert alert = new Alert(Alert.AlertType.INFORMATION);
              alert.setTitle("Game Over");
              alert.setHeaderText(null);
              alert.setContentText("Draw!");
              alert.showAndWait();
              exit();
            }
          });
        } catch (IOException e) {
          System.out.println("Server disconnected!");
          Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Game Over");
            alert.setHeaderText(null);
            alert.setContentText("Server disconnected!");
            alert.showAndWait();
            exit();
          });
          break;
        }
      }
    }
  }

}
