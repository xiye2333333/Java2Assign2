package application;

import application.controller.Controller;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class Main extends Application {
  String serverName = "127.0.0.1";
  int serverPort = 8080;
  Socket client = null;

  //client
  @Override
  public void start(Stage primaryStage) {

    try {
      client = new Socket(serverName, serverPort);
      InputStream inFromServer = client.getInputStream();
      DataInputStream in = new DataInputStream(inFromServer);

      System.out.println(in.readUTF());
//            client.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

    try {
      FXMLLoader fxmlLoader = new FXMLLoader();

      fxmlLoader.setLocation(getClass().getClassLoader().getResource("mainUI.fxml"));
      Pane root = fxmlLoader.load();
      primaryStage.setTitle("Tic Tac Toe");
      primaryStage.setScene(new Scene(root));
      primaryStage.setResizable(false);

      primaryStage.show();

      Controller controller = fxmlLoader.getController();
      controller.setSocket(client);


    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void stop() throws Exception {
    super.stop();
    System.exit(0);
  }

  public static void main(String[] args) {

    launch(args);

  }
}
