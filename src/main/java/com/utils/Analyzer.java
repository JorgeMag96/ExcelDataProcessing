package com.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
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

      // Load the excel workbook.
      Workbook wb = WorkbookFactory.create(file);

      // Always working with the first sheet of the excel file. (Assuming we always have single sheet files).
      Sheet sheet = wb.getSheetAt(0);

      // Initialize the first date and time values.
      String previousDate = sheet.getRow(1).getCell(0).toString();
      long previousTime = sheet.getRow(1).getCell(1).getDateCellValue().getTime();

      // Initialize a batch object.
      Batch batch = new Batch();

      // Instantiate our iterator.
      Iterator<Row> iterator = sheet.iterator();

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

          //TODO: Write the valid data rows to the output file.

          // Restore the batch object.
          batch = new Batch();

          // Update previousDate and previousTime values.
          previousDate = date;
          previousTime = time;
        }

        // Add rowData to the batch.
        batch.addRowData(rowData);
      }

      wb.close();
    }
    catch (Exception e) {
      System.out.println(e.getMessage());
    }

    //TODO: Return the output file.
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
      return null;
    }

    //TODO: Remove this line when program is finished.
    firstFilterList.forEach(System.out::println);

    // Position on the first row with length == inputLength, if we didn't find any, return null.
    RowData startingRow = firstFilterList.stream().filter(r -> r.getLength() == inputLength).findFirst().get();
    if(startingRow == null) {
      return null;
    }

    int startingPosition = firstFilterList.indexOf(startingRow);
    // Add this row number, since is a valid data.
    listOfValidRows.add(startingRow.getRowNumber());

    //TODO: Remove this line when program is finished.
    System.out.println("Position to start = "+startingPosition);



    return listOfValidRows;
  }
}
