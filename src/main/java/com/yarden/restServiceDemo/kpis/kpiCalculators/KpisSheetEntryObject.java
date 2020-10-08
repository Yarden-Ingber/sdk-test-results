package com.yarden.restServiceDemo.kpis.kpiCalculators;

import java.util.LinkedHashMap;

public class KpisSheetEntryObject {

    public String team;
    public String subProject;
    public boolean isOnlyBugs;
    public LinkedHashMap<KpiCalculator.KpisColumns, Object> kpisList;

    public KpisSheetEntryObject(String team, String subProject, boolean isOnlyBugs) {
        this.team = team;
        this.subProject = subProject;
        this.isOnlyBugs = isOnlyBugs;
        kpisList = new LinkedHashMap<>();
    }

    public boolean isEqual(KpisSheetEntryObject obj) {
        if(this == obj)
            return true;
        if(obj == null || obj.getClass()!= this.getClass())
            return false;
        return obj.team.equals(this.team) && obj.subProject.equals(this.subProject);
    }

}
