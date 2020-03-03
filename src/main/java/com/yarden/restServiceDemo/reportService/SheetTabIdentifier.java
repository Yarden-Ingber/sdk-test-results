package com.yarden.restServiceDemo.reportService;

import java.util.Objects;

public class SheetTabIdentifier {

    public final String sheetTabName;
    public final String spreadsheetID;

    public SheetTabIdentifier(String spreadsheetID, String sheetTabName){
        this.sheetTabName = sheetTabName;
        this.spreadsheetID = spreadsheetID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SheetTabIdentifier that = (SheetTabIdentifier) o;
        return sheetTabName.equals(that.sheetTabName) &&
                spreadsheetID.equals(that.spreadsheetID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sheetTabName, spreadsheetID);
    }
}
