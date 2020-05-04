package com.main;

import com.controller.Controller;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Launcher extends Application {

  public static void main(String[] args) {
    launch(args);
  }
  @Override
  public void start(Stage stage) throws Exception {
    FXMLLoader loader = new FXMLLoader(getClass().getResource("MainGUI.fxml"));
    Parent root = (Parent)loader.load();
    Scene scene = new Scene (root);
    stage.setScene(scene);
    stage.setTitle("Excel Data Analyzer");
    stage.setResizable(false);
    Controller controller = (Controller)loader.getController();
    controller.setStage(stage);
    stage.show();
  }

}
