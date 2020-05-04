package com.controller;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import org.apache.poi.ss.usermodel.Workbook;
import com.utils.Analyzer;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public class Controller implements Initializable{

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    fc = new FileChooser();
    fc.setInitialDirectory(new File("data"+File.separator+"input"));
    fc.setSelectedExtensionFilter(new ExtensionFilter("Microsft Excel File","*.xlsx"));
    startBtn.setOnAction(e -> startImpl());
    chooseFileBtn.setOnAction(e -> chooseFileImpl());
  }

  public void startImpl() {
    int inputLength = 0;
    try {
      inputLength = Integer.parseInt(lengthTxt.getText());
      lengthTxt.setText("");
    }catch(Exception e) {
      Alert alert = new Alert(AlertType.ERROR);
      alert.setTitle("Invalid input length.");
      alert.setContentText("Please enter a valid input length.");
      alert.showAndWait();
      lengthTxt.setText("");
      return;
    }
    if(fileLabel.getText().isEmpty()) {
      chooseFileImpl();
      if(fileLabel.getText().isEmpty()) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("No file selected.");
        alert.setContentText("Please select a file to process.");
        alert.showAndWait();
        return;
      }
    }
    Workbook outputFile =  Analyzer.startAnalyzer(fileLabel.getText(), inputLength);
    //TODO: Selec where to save the outputfile.
    fileLabel.setText("");
  }

  public void chooseFileImpl() {
    File selectedInstanceFile = fc.showOpenDialog(stage);
    fileLabel.setText((selectedInstanceFile != null)? selectedInstanceFile.toString():"");
  }

  public void setStage(Stage stage) {
    this.stage = stage;
  }

  private Stage stage;

  private FileChooser fc;

  @FXML
  private Label fileLabel;

  @FXML
  private TextField lengthTxt;

  @FXML
  private Button chooseFileBtn;

  @FXML
  private Button startBtn;
}
