
package com.yarden.restServiceDemo.repoMonitoringService.npmApiService.pojos;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class NpmPackagesListPojo {

    @SerializedName("objects")
    @Expose
    private List<Object> objects = null;
    @SerializedName("total")
    @Expose
    private Integer total;
    @SerializedName("time")
    @Expose
    private String time;

    public List<Object> getObjects() {
        return objects;
    }

    public void setObjects(List<Object> objects) {
        this.objects = objects;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

}
