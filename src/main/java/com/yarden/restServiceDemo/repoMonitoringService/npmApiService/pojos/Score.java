
package com.yarden.restServiceDemo.repoMonitoringService.npmApiService.pojos;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Score {

    @SerializedName("final")
    @Expose
    private Double _final;
    @SerializedName("detail")
    @Expose
    private Detail detail;

    public Double getFinal() {
        return _final;
    }

    public void setFinal(Double _final) {
        this._final = _final;
    }

    public Detail getDetail() {
        return detail;
    }

    public void setDetail(Detail detail) {
        this.detail = detail;
    }

}
