import java.io.File;
import java.io.FileOutputStream;
import org.apache.poi.ss.usermodel.Workbook;
import com.utils.Analyzer;

public class Main {

  public static void main(String[] args) {
    try {
      Workbook outputFile = Analyzer.startAnalyzer("data"+File.separator+"input"+File.separator+"testInput.xlsx", 7);
      FileOutputStream out = new FileOutputStream(new File("data"+File.separator+"output"+File.separator+"example_output.xlsx"));
      outputFile.write(out);
      out.close();
    }
    catch(Exception e) {
      e.getStackTrace();
    }
  }
}
