package com.models;

import java.util.ArrayList;
import java.util.List;

public class Batch {

  public void addRowData(RowData rowData) {
    rowsOfData.add(rowData);
  }

  public int size() {
    return rowsOfData.size();
  }

  // Represents all the DataRows for a given point in time.
  private List<RowData> rowsOfData = new ArrayList<>();

}
