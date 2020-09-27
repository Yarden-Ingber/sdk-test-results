package com.yarden.restServiceDemo.kpis.kpiCalculators;

public class SingleKpiObject {

    public KpiCalculator.KpisColumns kpiColumn;
    public String value;

    public SingleKpiObject(KpiCalculator.KpisColumns kpiColumn, String value) {
        this.kpiColumn = kpiColumn;
        this.value = value;
    }

}
