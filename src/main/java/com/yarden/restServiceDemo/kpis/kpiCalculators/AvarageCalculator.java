package com.yarden.restServiceDemo.kpis.kpiCalculators;

import java.util.ArrayList;
import java.util.List;

public class AvarageCalculator {

    public List<Long> members = new ArrayList<>();

    @Override
    public String toString() {
        return String.valueOf(getAvarage());
    }

    public Long getAvarage(){
        Double sum = 0.0;
        for (Long member : members) {
            sum += member;
        }
        return Math.round(sum / members.size());
    }

}
