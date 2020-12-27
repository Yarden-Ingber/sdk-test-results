package com.yarden.restServiceDemo.awsS3Service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.yarden.restServiceDemo.Enums;
import com.yarden.restServiceDemo.pojos.EyesResultRequestJson;
import javassist.NotFoundException;

import java.io.IOException;

public class AwsS3ResultsJsonsService {

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
        AwsS3Provider.writeStringToFile(Enums.EnvVariables.AwsS3ResultsJsonsBucketName.value, getEyesResultRequestJsonFileName(eyesResultRequestJson.getId(), eyesResultRequestJson.getGroup()), new Gson().toJson(eyesResultRequestJsonFromS3));
    }

    public static String getCurrentEyesRequestFromS3(String id, String group) throws NotFoundException {
        try {
            return AwsS3Provider.getStringFromFile(Enums.EnvVariables.AwsS3ResultsJsonsBucketName.value, getEyesResultRequestJsonFileName(id, group));
        } catch (Throwable t) {
            throw new NotFoundException("");
        }
    }

    private static String getEyesResultRequestJsonFileName(String id, String group){
        final String EyesRequestFileNamePrefix = "Eyes";
        return EyesRequestFileNamePrefix + "-" + id + "-" + group;
    }

}
