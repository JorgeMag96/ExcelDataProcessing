import java.io.File;
import java.util.Iterator;
import java.util.List;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import com.models.Batch;
import com.models.RowData;
import com.utils.Analyzer;

public class Main {

  public static void main(String[] args) {
    try {
      File file = new File("data"+File.separator+"input"+File.separator+"example_input.xlsx");
      Workbook wb = WorkbookFactory.create(file);
      // Always working with the first sheet of the excel file. (Assuming we always have single sheet files)
      Sheet sheet = wb.getSheetAt(0);
      int i = 0;

      // Initialize the first date and time values.
      String previousDate = sheet.getRow(1).getCell(0).toString();
      long previousTime = sheet.getRow(1).getCell(1).getDateCellValue().getTime();

      // Initialize a batch object.
      Batch batch = new Batch();

      // Instantiate our iterator.
      Iterator<Row> iterator = sheet.iterator();

      // Need to skip the first row since it is the header.
      iterator.next();

      while(iterator.hasNext() && i < 117) {

        // Get the rowData.
        RowData rowData = new RowData(iterator.next());

        //Get current date and time.
        String date = rowData.getDate();
        long time = rowData.getTime();

        // Check if we are on a new date or a new time.
        if(!previousDate.equals(date) || previousTime != time) {

          // Process the batch and get the valid data rows.
          List<Integer> validDataRows = Analyzer.processBatch(batch, 7);

          //TODO: Write the valid data rows to the output file.

          // Restore the batch object.
          batch = new Batch();

          // Update previousDate and previousTime values.
          previousDate = date;
          previousTime = time;
        }

        // Add rowData to the batch.
        batch.addRowData(rowData);
        i++;
      }

      wb.close();
    }
    catch (Exception e) {
      System.out.println(e.getMessage());
    }
  }
}
