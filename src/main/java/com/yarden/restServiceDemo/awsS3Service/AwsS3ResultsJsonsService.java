package com.yarden.restServiceDemo.awsS3Service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.yarden.restServiceDemo.Enums;
import com.yarden.restServiceDemo.pojos.EyesResultRequestJson;
import com.yarden.restServiceDemo.pojos.SdkResultRequestJson;
import javassist.NotFoundException;

public class AwsS3ResultsJsonsService {

    private static final String SdkRequestFileNamePrefix = "Sdk";
    private static final String EyesRequestFileNamePrefix = "Eyes";

    public static void addSdkRequestToS3File(String json) {
        SdkResultRequestJson sdkResultRequestJson = new Gson().fromJson(json, SdkResultRequestJson.class);
        if ((sdkResultRequestJson.getSandbox() != null) && sdkResultRequestJson.getSandbox()) {
            return;
        }
        SdkResultRequestJson sdkResultRequestJsonFromS3 = null;
        try {
            sdkResultRequestJsonFromS3 = new Gson().fromJson(getCurrentSdkRequestFromS3(sdkResultRequestJson.getId(), sdkResultRequestJson.getGroup()), SdkResultRequestJson.class);
            JsonArray testResultsFromS3 = sdkResultRequestJsonFromS3.getResults();
            testResultsFromS3.addAll(sdkResultRequestJson.getResults());
            sdkResultRequestJsonFromS3.setResults(testResultsFromS3);
        } catch (NotFoundException t) {
            sdkResultRequestJsonFromS3 = sdkResultRequestJson;
        }
        AwsS3Provider.writeStringToFile(Enums.EnvVariables.AwsS3ResultsJsonsBucketName.value, getResultRequestJsonFileName(sdkResultRequestJson.getId(), sdkResultRequestJson.getGroup(), SdkRequestFileNamePrefix), new Gson().toJson(sdkResultRequestJsonFromS3));
    }

    public static void addEyesRequestToS3File(String json) {
        EyesResultRequestJson eyesResultRequestJson = new Gson().fromJson(json, EyesResultRequestJson.class);
        if ((eyesResultRequestJson.getSandbox() != null) && eyesResultRequestJson.getSandbox()) {
            return;
        }
        EyesResultRequestJson eyesResultRequestJsonFromS3 = null;
        try {
            eyesResultRequestJsonFromS3 = new Gson().fromJson(getCurrentEyesRequestFromS3(eyesResultRequestJson.getId(), eyesResultRequestJson.getGroup()), EyesResultRequestJson.class);
            JsonArray testResultsFromS3 = eyesResultRequestJsonFromS3.getResults();
            testResultsFromS3.addAll(eyesResultRequestJson.getResults());
            eyesResultRequestJsonFromS3.setResults(testResultsFromS3);
        } catch (NotFoundException t) {
            eyesResultRequestJsonFromS3 = eyesResultRequestJson;
        }
        AwsS3Provider.writeStringToFile(Enums.EnvVariables.AwsS3ResultsJsonsBucketName.value, getResultRequestJsonFileName(eyesResultRequestJson.getId(), eyesResultRequestJson.getGroup(), EyesRequestFileNamePrefix), new Gson().toJson(eyesResultRequestJsonFromS3));
    }

    public static String getCurrentSdkRequestFromS3(String id, String group) throws NotFoundException {
        try {
            return AwsS3Provider.getStringFromFile(Enums.EnvVariables.AwsS3ResultsJsonsBucketName.value, getResultRequestJsonFileName(id, group, SdkRequestFileNamePrefix));
        } catch (Throwable t) {
            throw new NotFoundException("");
        }
    }

    public static String getCurrentEyesRequestFromS3(String id, String group) throws NotFoundException {
        try {
            return AwsS3Provider.getStringFromFile(Enums.EnvVariables.AwsS3ResultsJsonsBucketName.value, getResultRequestJsonFileName(id, group, EyesRequestFileNamePrefix));
        } catch (Throwable t) {
            throw new NotFoundException("");
        }
    }

    private static String getResultRequestJsonFileName(String id, String group, String requestFileNamePrefix){
        return requestFileNamePrefix + "-" + id + "-" + group;
    }

}
