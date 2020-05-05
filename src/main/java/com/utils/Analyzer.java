package com.utils;

import java.io.File;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.poi.ss.usermodel.CellCopyPolicy;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import com.models.Batch;
import com.models.RowData;

/**
 * This class will process the batch according to the algorithm provided.
 *
 */
public class Analyzer {

  public static final String tempFilePath = "data"+File.separator+"temp"+File.separator+"temp_output.xlsx";

  public static Workbook startAnalyzer(String pathToFile, int inputLength) {

    System.out.println("Analyzer started..");

    try {
      if(new File(tempFilePath).exists()) {
        new File(tempFilePath).delete();
      }
      File inputWbFile = new File(pathToFile);
      File outputWbFile = new File(tempFilePath);
      Files.copy(inputWbFile.toPath(), outputWbFile.toPath());

      // Create the output sheet.
      XSSFWorkbook outputWorkbook = new XSSFWorkbook(outputWbFile);
      XSSFSheet inputSheet = outputWorkbook.getSheetAt(0);
      XSSFSheet outputSheet = outputWorkbook.createSheet("Output");
      int outputRowsCount = 0;

      // Build the header
      XSSFRow outputHeader = outputSheet.createRow(outputRowsCount++);
      buildHeader(outputHeader);

      // Initialize the first date and time values.
      String previousDate = inputSheet.getRow(1).getCell(0).toString();
      long previousTime = inputSheet.getRow(1).getCell(1).getDateCellValue().getTime();

      // Initialize a batch object.
      Batch batch = new Batch();

      // Instantiate our iterator.
      Iterator<Row> iterator = inputSheet.iterator();

      // Need to skip the first row since it is the header.
      iterator.next();

      List<Integer> validDataRows = new ArrayList<>();

      while(iterator.hasNext()) {

        // Get the rowData.
        RowData rowData = new RowData(iterator.next());

        //Get current date and time.
        String date = rowData.getDate();
        long time = rowData.getTime();

        // Check if we are on a new date or a new time.
        if(!previousDate.equals(date) || previousTime != time) {

          // Process the batch and get the valid data rows.
          validDataRows.addAll(Analyzer.processBatch(batch, inputLength));

          // Restore the batch object.
          batch = new Batch();

          // Update previousDate and previousTime values.
          previousDate = date;
          previousTime = time;
        }
        batch.addRowData(rowData);
      }

      // Process the last batch of data. Since the last batch of data doesn't enters the condition of the previous date comparison.
      validDataRows.addAll(Analyzer.processBatch(batch, inputLength));

      if(!validDataRows.isEmpty()) {
        for(Integer rowIndex : validDataRows) {
          XSSFRow inputRow = inputSheet.getRow(rowIndex);
          XSSFRow outputRow = outputSheet.createRow(outputRowsCount++);
          outputRow.copyRowFrom(inputRow, new CellCopyPolicy());
          DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
          outputRow.getCell(1).setCellValue(dateFormat.format(inputRow.getCell(1).getDateCellValue()));
        }
      }

      outputWorkbook.removeSheetAt(0);
      return outputWorkbook;
    }
    catch (Exception e) {
      e.printStackTrace();
    }

    return null;
  }

  /**
   * This function will process the batch and return a List of rows to print to the output file.
   * @param batch - The Batch object to process.
   * @param input - The input length for this processing.
   * @return A list of rows that represent valid data.
   */
  private static List<Integer> processBatch(Batch batch, int inputLength) {
    //TODO: Here we need to write the fun stuff.
    List<Integer> listOfValidRows = new ArrayList<>();
    System.out.println("Processing batch with size of = "+batch.size());

    // Here we apply the first filter of making sure we just take into account the ones with average greater or equal than max average.
    List<RowData> firstFilterList = batch.getRowsOfData().stream().filter(r -> r.getAverage() >= r.getMaxAverage()).collect(Collectors.toList());
    if(firstFilterList.isEmpty()) {
      return listOfValidRows;
    }

    //TODO: Need to remove the RowData with average greater than the min already collected.

    //TODO: Remove this line when program is finished.
    firstFilterList.forEach(System.out::println);

    // Position on the first row with length == inputLength, if we didn't find any, return null.
    RowData startingRow;
    Optional<RowData> optionalStartingRow = firstFilterList.stream().filter(r -> r.getLength() == inputLength).findFirst();
    if(optionalStartingRow.isPresent()) {
      startingRow = optionalStartingRow.get();
    }else {
      return listOfValidRows;
    }

    int startingPosition = firstFilterList.indexOf(startingRow);
    int startingFowardPosition = startingPosition;

    // Add this row number, since is a valid data.
    listOfValidRows.add(startingRow.getRowNumber());

    // Need to add all the rows with the input length.
    int i = 0;
    int nextIndex = ++startingFowardPosition;
    while(true) {

      if(nextIndex >= firstFilterList.size()) {
        // Reaching this means we got to the end of the firstFilterList, so just return.
        return listOfValidRows;
      }

      RowData nextRow = firstFilterList.get(nextIndex);
      if(nextRow.getLength() == startingRow.getLength()) {
        listOfValidRows.add(nextRow.getRowNumber());
        i++;
        nextIndex++;
      }
      else {
        break;
      }
    }

    //TODO: Remove this line when program is finished.
    System.out.println("Row position to start foward processing = "+startingFowardPosition);

    processFoward(firstFilterList, startingFowardPosition, listOfValidRows);

    //TODO: Remove this line when program is finished.
    if(startingPosition > 0) {
      int startingBackwardPosition = startingPosition - 1;
      System.out.println("Row position to start backward processing = "+startingBackwardPosition);

      processBackward(firstFilterList, startingBackwardPosition, listOfValidRows);
    }


    return listOfValidRows;
  }

  /**
   * Process the rows of data that are greater than the initial length input.
   *
   * @param rowList - The rows of the batch that are after the valid initial length input row.
   * @param startingPosition - The position of the first row to start analyzing.
   * @param listOfValidRows - Reference to the list which contains the numbers of the valid rows.
   */
  private static void processFoward(List<RowData> rowList, int startingPosition, List<Integer> listOfValidRows) {

    if(startingPosition >= rowList.size()) return;
    int previous = startingPosition - 1;

    RowData current = rowList.get(startingPosition++);
    if(current.getStart() < rowList.get(previous).getMax()) {
      listOfValidRows.add(current.getRowNumber());
    }
    else {
      return;
    }

    processFoward(rowList,startingPosition,listOfValidRows);

  }

  /**
   * Process the rows of data that are less than the initial length input.
   *
   * @param rowList - The rows of the batch that are after the valid initial length input row.
   * @param startingPosition - The position of the first row to start analyzing.
   * @param listOfValidRows - Reference to the list which contains the numbers of the valid rows.
   */
  private static void processBackward(List<RowData> rowList, int startingPosition, List<Integer> listOfValidRows) {
    if(startingPosition < 0) return;
    int previous = startingPosition + 1;

    RowData current = rowList.get(startingPosition--);
    if(current.getMax() > rowList.get(previous).getStart()) {
      listOfValidRows.add(current.getRowNumber());
    }
    else {
      return;
    }

  }

  private static void buildHeader(XSSFRow header) {
    header.createCell(0).setCellValue("Date");
    header.createCell(1).setCellValue("Time");
    header.createCell(2).setCellValue("Direction");
    header.createCell(3).setCellValue("Length");
    header.createCell(4).setCellValue("Candles");
    header.createCell(5).setCellValue("Average");
    header.createCell(6).setCellValue("MaxAvrg");
    header.createCell(7).setCellValue("Main Value");
    header.createCell(8).setCellValue("Cycle");
    header.createCell(9).setCellValue("Start");
    header.createCell(10).setCellValue("Max");
    header.createCell(11).setCellValue("Center");
    header.createCell(12).setCellValue("Before");
    header.createCell(13).setCellValue("Main VB");
    header.createCell(14).setCellValue("After");
    header.createCell(15).setCellValue("Main VA");
    header.createCell(16).setCellValue("Mini");
    header.createCell(17).setCellValue("Max");
    header.createCell(18).setCellValue("Cycle Time");
  }
}
