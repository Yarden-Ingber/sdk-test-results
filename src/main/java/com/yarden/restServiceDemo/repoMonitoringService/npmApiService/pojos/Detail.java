
package com.yarden.restServiceDemo.repoMonitoringService.npmApiService.pojos;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Detail {

    @SerializedName("quality")
    @Expose
    private Double quality;
    @SerializedName("popularity")
    @Expose
    private Double popularity;
    @SerializedName("maintenance")
    @Expose
    private Double maintenance;

    public Double getQuality() {
        return quality;
    }

    public void setQuality(Double quality) {
        this.quality = quality;
    }

    public Double getPopularity() {
        return popularity;
    }

    public void setPopularity(Double popularity) {
        this.popularity = popularity;
    }

    public Double getMaintenance() {
        return maintenance;
    }

    public void setMaintenance(Double maintenance) {
        this.maintenance = maintenance;
    }

}
