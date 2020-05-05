package com.utils;

import java.io.File;
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

  public static Workbook startAnalyzer(String pathToFile, int inputLength) {

    System.out.println("Analyzer started..");

    try {
      File file = new File(pathToFile);

      // Load the excel workbook input.
      XSSFWorkbook workbook = new XSSFWorkbook(file);
      XSSFSheet inputSheet = workbook.getSheetAt(0);

      // Create the output sheet.
      XSSFSheet outputSheet = workbook.createSheet("Output");
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

      while(iterator.hasNext()) {

        // Get the rowData.
        RowData rowData = new RowData(iterator.next());

        //Get current date and time.
        String date = rowData.getDate();
        long time = rowData.getTime();

        // Check if we are on a new date or a new time.
        if(!previousDate.equals(date) || previousTime != time) {

          // Process the batch and get the valid data rows.
          List<Integer> validDataRows = Analyzer.processBatch(batch, inputLength);

          if(!validDataRows.isEmpty()) {
            for(Integer rowIndex : validDataRows) {
              XSSFRow inputRow = inputSheet.getRow(rowIndex);
              XSSFRow outputRow = outputSheet.createRow(outputRowsCount++);
              outputRow.copyRowFrom(inputRow, new CellCopyPolicy());
              DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
              outputRow.getCell(1).setCellValue(dateFormat.format(inputRow.getCell(1).getDateCellValue()));
            }
          }

          // Restore the batch object.
          batch = new Batch();

          // Update previousDate and previousTime values.
          previousDate = date;
          previousTime = time;
        }

        batch.addRowData(rowData);

      }
      return workbook;
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
    // Add this row number, since is a valid data.
    listOfValidRows.add(startingRow.getRowNumber());

    // Need to add all the rows with the input length.
    int i = 0;
    int nextIndex = ++startingPosition;
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
    System.out.println("Position to start next length rows = "+startingPosition);

    processFoward(firstFilterList, startingPosition, listOfValidRows);

    processBackward(firstFilterList, startingPosition, listOfValidRows);

    return listOfValidRows;
  }

  /**
   * Process the rows of data that are greater than the initial length input.
   *
   * @param nextRows - The rows of the batch that are after the valid initial length input row.
   * @param previousRowIndex
   * @param listOfValidRows
   */
  private static void processFoward(List<RowData> nextRows, int startingPosition, List<Integer> listOfValidRows) {
    // This function might be a recursive one, we need to keep analyzing the next rows, while the condition is meth
    //if()
  }

  private static void processBackward(List<RowData> previousRows, int startingPosition, List<Integer> listOfValidRows) {

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
