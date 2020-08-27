
package com.yarden.restServiceDemo.repoMonitoringService.npmApiService.pojos;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Object {

    @SerializedName("package")
    @Expose
    private Package _package;
    @SerializedName("score")
    @Expose
    private Score score;
    @SerializedName("searchScore")
    @Expose
    private Double searchScore;

    public Package getPackage() {
        return _package;
    }

    public void setPackage(Package _package) {
        this._package = _package;
    }

    public Score getScore() {
        return score;
    }

    public void setScore(Score score) {
        this.score = score;
    }

    public Double getSearchScore() {
        return searchScore;
    }

    public void setSearchScore(Double searchScore) {
        this.searchScore = searchScore;
    }

}
