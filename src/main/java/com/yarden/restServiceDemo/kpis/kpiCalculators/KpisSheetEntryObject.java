package com.yarden.restServiceDemo.kpis.kpiCalculators;

import java.util.LinkedHashMap;

public class KpisSheetEntryObject {

    public String team;
    public String subProject;
    public LinkedHashMap<KpiCalculator.KpisColumns, Object> kpisList;

    public KpisSheetEntryObject(String team, String subProject) {
        this.team = team;
        this.subProject = subProject;
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
