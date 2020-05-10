package com.utils;

import java.io.File;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
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

    //System.out.println("Analyzer started..");
    long startTime = System.currentTimeMillis();

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
          List<Integer> dataRows = Analyzer.processBatch(batch, inputLength);
          if(dataRows != null) {
            validDataRows.addAll(dataRows);
          }

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

      //System.out.println("Elapsed time = "+(System.currentTimeMillis()-startTime)/1000+" seconds.");

      return outputWorkbook;
    }
    catch (Exception e) {
      e.printStackTrace();
    }

    return null;
  }

  /**
   * This function will process the batch and return a List of rows to print to the output file.
   * @param mainBatch - The Batch of rows data for a specific date.
   * @param input - The input length for this processing.
   * @return A list of rows that represent valid data.
   */
  private static List<Integer> processBatch(Batch mainBatch, int inputLength) {

    List<Batch> subBatches = getSubBatchesFromAverageValues(mainBatch);
    Iterator<Batch> batchesIterator = subBatches.iterator();

    while(batchesIterator.hasNext()) {
      List<Integer> listOfValidRows = new ArrayList<>();
      Batch currentBatch = batchesIterator.next();
      //System.out.println("Processing sub-batch with size of = "+currentBatch.size());

      // Check if there's the input length in this sub batch.
      List<RowData> rowsOfData = currentBatch.getRowsOfData();
      Optional<RowData> optionalStartingRow = rowsOfData.stream().filter(r -> r.getLength() == inputLength).findFirst();
      if(!optionalStartingRow.isPresent()) {
        continue;
      }

      RowData startingRow = optionalStartingRow.get();


      int startingPosition = rowsOfData.indexOf(startingRow);

      int startingFowardPosition = (int) rowsOfData.stream().filter(r -> r.getLength() == inputLength).count() + startingPosition;

      // Make sure our input length rows meet the requirement of average must be greater or equal than max average.
      boolean isValidInput = true;
      for(int i = startingPosition; i < startingFowardPosition; i++) {
        RowData rowData = rowsOfData.get(i);
        if(rowData.getAverage() < rowData.getMaxAverage()) {
          isValidInput = false;
          break;
        }
        listOfValidRows.add(rowsOfData.get(i).getRowNumber());
      }

      if(!isValidInput) continue;

      boolean processBackwardRows = startingRow.getStart() != startingRow.getMax();

      //rowsOfData.forEach(System.out::println);

      //System.out.println("Row position to start foward processing = "+startingFowardPosition);

      List<Integer> nextValidRowData = new ArrayList<>();
      processFoward(rowsOfData, startingFowardPosition, nextValidRowData);
      if(maxAvgChanged) {
        //System.out.println("Average is less than the max average !");
        maxAvgChanged = false;
        continue;
      }
      listOfValidRows.addAll(nextValidRowData);

      int startingBackwardPosition = startingPosition - 1;

      //System.out.println("Row position to start backward processing = "+startingBackwardPosition);

      if(processBackwardRows) {
        List<Integer> previousValidRowData = new ArrayList<>();
        processBackward(rowsOfData, startingBackwardPosition, previousValidRowData);
        if(maxAvgChanged) {
          //System.out.println("Average is less than the max average !");
          maxAvgChanged = false;
          continue;
        }
        listOfValidRows.addAll(previousValidRowData);
      }

      // If we reached this it means we got a list of valid rows, or an empty list meaning no valuable data was found in this batch.
      Collections.sort(listOfValidRows);
      return listOfValidRows;
    }

    return null;
  }


  /**
   * Process the rows of data that are greater than the initial length input.
   *
   * @param rowList - The rows of the batch that are after the valid initial length input row.
   * @param startingPosition - The position of the first row to start analyzing.
   * @param listOfValidRows - Reference to the list which contains the numbers of the valid rows.
   */
  private static void processFoward(List<RowData> rowList, int startingPosition, List<Integer> nextValidRowData) {

    if(startingPosition >= rowList.size()) return;

    int previousIndex = startingPosition - 1;

    int[] arr = rowList.stream().filter(r -> r.getLength() == rowList.get(startingPosition).getLength()).mapToInt(r -> r.getRowNumber()).toArray();
    int amountOfRowsWithSameLength = arr.length;
    RowData previous = rowList.get(previousIndex);
    RowData lastRowDataWithSameLength = rowList.get(previousIndex + amountOfRowsWithSameLength);

    if(lastRowDataWithSameLength.getStart() == previous.getMax() && (previous.getStart() == previous.getMax())) {
      for(int i : arr) {
        nextValidRowData.add(i);
      }
    }
    else if(lastRowDataWithSameLength.getStart() < previous.getMax()) {
      // We need to validate that the condition that the average should be greater or equal than max average.
      // If this condition isn't met, then all the rows selected for this are wrong data.
      if(lastRowDataWithSameLength.getAverage() < lastRowDataWithSameLength.getMaxAverage()) {
        // Turn boolean flag on to point out that the data is all wrong, because of a change in the max average.
        maxAvgChanged = true;
        return;
      }
      for(int i : arr) {
        nextValidRowData.add(i);
      }
    }
    else {
      return;
    }

    processFoward(rowList, (startingPosition + amountOfRowsWithSameLength) ,nextValidRowData);

  }

  /**
   * Process the rows of data that are less than the initial length input.
   *
   * @param rowList - The rows of the batch that are after the valid initial length input row.
   * @param startingPosition - The position of the first row to start analyzing.
   * @param listOfValidRows - Reference to the list which contains the numbers of the valid rows.
   */
  private static void processBackward(List<RowData> rowList, int startingPosition, List<Integer> previousValidRowData) {
    if(startingPosition < 0) return;

    // ( startingPosition + 1) is the position of the first row of the previous length, but we need to get the last row of the previous length.
    int[] previousRows = rowList.stream().filter(r -> r.getLength() == rowList.get(startingPosition + 1).getLength()).mapToInt(r -> r.getRowNumber()).toArray();
    RowData previous = rowList.get(startingPosition + previousRows.length);

    int[] arr = rowList.stream().filter(r -> r.getLength() == rowList.get(startingPosition).getLength()).mapToInt(r -> r.getRowNumber()).toArray();
    int amountOfRowsWithSameLength = arr.length;

    RowData lastRowDataWithSameLength = rowList.get(startingPosition);

    if(lastRowDataWithSameLength.getMax() > previous.getStart()) {
      if(lastRowDataWithSameLength.getAverage() < lastRowDataWithSameLength.getMaxAverage()) {
        // Turn boolean flag on to point out that the data is all wrong, because of a change in the max average.
        maxAvgChanged = true;
        return;
      }
      for(int i : arr) {
        previousValidRowData.add(i);
      }
    }
    else {
      return;
    }

    processBackward(rowList,startingPosition - amountOfRowsWithSameLength,previousValidRowData);

  }

  /**
   * This method will separate main batch that is for a specific date into sub batches which correspond to a single average value.
   *
   * @param mainBatch - The batch that corresponds to a specific date.
   * @return
   */
  private static List<Batch> getSubBatchesFromAverageValues(Batch mainBatch){
    List<Batch> subBatches = new ArrayList<>();

    Batch subBatch = new Batch();
    // Initial average.
    float avg = mainBatch.getRowsOfData().get(0).getAverage();
    for(RowData r: mainBatch.getRowsOfData()) {
      if(r.getAverage() == avg) {
        subBatch.addRowData(r);
      }
      else {
        // Add the already built batch, before reset.
        subBatches.add(subBatch);

        //Reset and add new subBatch
        avg = r.getAverage();
        subBatch = new Batch();
        subBatch.addRowData(r);
      }
    }

    // Add the last batch.
    subBatches.add(subBatch);

    return subBatches;
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

  private static boolean maxAvgChanged = false;
}
