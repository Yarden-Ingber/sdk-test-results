package com.yarden.restServiceDemo.kpis.kpiCalculators;

import com.google.gson.JsonElement;
import com.yarden.restServiceDemo.Enums;
import com.yarden.restServiceDemo.reportService.SheetData;
import com.yarden.restServiceDemo.reportService.SheetTabIdentifier;
import org.junit.Test;

import java.util.LinkedHashMap;

public class KpiSheetEntries {

    protected SheetData rawSheetData = new SheetData(new SheetTabIdentifier(Enums.SpreadsheetIDs.KPIS.value, Enums.KPIsSheetTabsNames.RawData.value));
    private LinkedHashMap<String, KpisSheetEntryObject> kpiSheetEntries = new LinkedHashMap<>();

    public LinkedHashMap<String, KpisSheetEntryObject> getEntries() {
        if (kpiSheetEntries.isEmpty()) {
            createKpiSheetEntries();
        }
        return kpiSheetEntries;
    }

    private void createKpiSheetEntries(){
        for (JsonElement sheetEntry: rawSheetData.getSheetData()){
            String team = sheetEntry.getAsJsonObject().get(Enums.KPIsSheetColumnNames.Team.value).getAsString();
            String subProject = sheetEntry.getAsJsonObject().get(Enums.KPIsSheetColumnNames.SubProject.value).getAsString();
            KpisSheetEntryObject kpisSheetEntryObjectFromSheet = new KpisSheetEntryObject(team, subProject);
            if (!isKpiEntryExistInEntriesList(kpisSheetEntryObjectFromSheet)) {
                kpiSheetEntries.put(kpisSheetEntryObjectFromSheet.team + kpisSheetEntryObjectFromSheet.subProject, kpisSheetEntryObjectFromSheet);
            }
        }
    }

    protected boolean isKpiEntryExistInEntriesList(KpisSheetEntryObject kpisSheetEntryObjectFromSheet){
        for (KpisSheetEntryObject kpisSheetEntryObject : kpiSheetEntries.values()) {
            if (kpisSheetEntryObject.isEqual(kpisSheetEntryObjectFromSheet)) {
                return true;
            }
        }
        return false;
    }

}
