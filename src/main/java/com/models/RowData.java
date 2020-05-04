package com.models;

import org.apache.poi.ss.usermodel.Row;

/**
 * This class represents a single row of data.
 */
public class RowData {

  /**
   *
   * @param row - Row to converto to the corresponding data row format.
   * @throws Exception - If there is any error while parsing the data.
   */
  public RowData(Row row) throws Exception {
    if(row == null) {
      throw new Exception("Exception thrown while trying to build a DataRow object = Row is null.");
    }
    try {
      rowNumber = row.getRowNum();
      date = row.getCell(0).toString();
      time = row.getCell(1).getDateCellValue().getTime();
      length = (int) row.getCell(3).getNumericCellValue();
      average = (float)row.getCell(5).getNumericCellValue();
      maxAverage = (float)row.getCell(6).getNumericCellValue();
      cycle = (int) row.getCell(8).getNumericCellValue();
      start = (int) row.getCell(9).getNumericCellValue();
      max = (int) row.getCell(10).getNumericCellValue();

    } catch(Exception e) {
      throw new Exception("Exception thrown while trying to build a DataRow object = Error while parsing Row data for row #"+row.getRowNum());
    }
  }

  public int getRowNumber() {
    return rowNumber;
  }

  public String getDate() {
    return date;
  }

  public long getTime() {
    return time;
  }

  public int getLength() {
    return length;
  }

  public float getAverage() {
    return average;
  }

  public float getMaxAverage() {
    return maxAverage;
  }

  public int getCycle() {
    return cycle;
  }

  public int getStart() {
    return start;
  }

  public int getMax() {
    return max;
  }

  @Override
  public String toString() {
    return "DataRow [rowNumber=" + rowNumber + ", date=" + date + ", time=" + time + ", length="
        + length + ", average=" + average + ", maxAverage=" + maxAverage + ", cycle=" + cycle
        + ", start=" + start + ", max=" + max + "]";
  }

  private int rowNumber;
  // Column 0
  private String date;

  // Column 1
  private long time;

  // Column 3
  private int length;

  // Column 5
  private float average;

  // Column
  private float maxAverage;

  // Column 0
  private int cycle;

  // Column 0
  private int start;

  // Column 0
  private int max;
}
