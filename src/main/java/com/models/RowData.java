package com.models;

import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;

/**
 * This class represents a single row of data.
 */
public class RowData implements Comparable<RowData> {

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
      if(row.getCell(3).getCellType().equals(CellType.NUMERIC)) {
        length = (int) row.getCell(3).getNumericCellValue();
      }
      else {
        length = 0;
      }
      if(row.getCell(5).getCellType().equals(CellType.NUMERIC)) {
        average = (int) row.getCell(5).getNumericCellValue();
      }
      else {
        average = 0;
      }
      if(row.getCell(6).getCellType().equals(CellType.NUMERIC)) {
        maxAverage = (int) row.getCell(6).getNumericCellValue();
      }
      else {
        maxAverage = 0;
      }
      if(row.getCell(8).getCellType().equals(CellType.NUMERIC)) {
        cycle = (int) row.getCell(8).getNumericCellValue();
      }
      else {
        cycle = 0;
      }
      if(row.getCell(9).getCellType().equals(CellType.NUMERIC)) {
        start = (int) row.getCell(9).getNumericCellValue();
      }
      else {
        start = 0;
      }
      if(row.getCell(10).getCellType().equals(CellType.NUMERIC)) {
        max = (int) row.getCell(10).getNumericCellValue();
      }
      else {
        max = 0;
      }
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

  @Override
  public int compareTo(RowData o) {
    if(rowNumber > o.getRowNumber()) {
      return 1;
    }
    else if(rowNumber < o.getRowNumber()) {
      return -1;
    }

    return 0;
  }
}
