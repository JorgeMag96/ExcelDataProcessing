package com.controller;

import java.io.File;
import java.io.FileOutputStream;
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
import javafx.stage.Stage;

public class Controller implements Initializable{

  @Override
  public void initialize(URL location, ResourceBundle resources) {
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

    fileLabel.setText("");
    FileChooser fc = new FileChooser();
    fc.setTitle("Save the output file");
    FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Excel files (*.xlsx)", "*.xlsx");
    fc.getExtensionFilters().add(extFilter);
    //fc.setInitialDirectory(new File("data"+File.separator+"output"));
    fc.setInitialFileName("output.xlsx");

    try (
        //Write the workbook in file system
        FileOutputStream out = new FileOutputStream(new File(fc.showSaveDialog(stage).getAbsolutePath()))) {
      outputFile.write(out);
      outputFile.close();
    }
    catch(Exception e) {
      // Workbook already exists
      Alert alert = new Alert(AlertType.ERROR);
      alert.setTitle("Error while attempting to save file.");
      alert.setContentText(e.getMessage());
      alert.showAndWait();
      return;
    }

    Alert alert = new Alert(AlertType.INFORMATION);
    alert.setTitle("Success !");
    alert.setContentText("Output file successfully saved.");
    alert.showAndWait();
    System.out.println("Successfully deleted output_temp file = "+new File(Analyzer.tempFilePath).delete());

  }

  public void chooseFileImpl() {
    FileChooser fc = new FileChooser();
    //fc.setInitialDirectory(new File("data"+File.separator+"input"));
    FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Excel files (*.xlsx)", "*.xlsx");
    fc.getExtensionFilters().add(extFilter);
    File selectedInstanceFile = fc.showOpenDialog(stage);
    fileLabel.setText((selectedInstanceFile != null)? selectedInstanceFile.toString():"");
  }

  public void setStage(Stage stage) {
    this.stage = stage;
  }

  private Stage stage;

  @FXML
  private Label fileLabel;

  @FXML
  private TextField lengthTxt;

  @FXML
  private Button chooseFileBtn;

  @FXML
  private Button startBtn;
}
