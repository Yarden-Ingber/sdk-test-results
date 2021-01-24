package com.yarden.restServiceDemo.awsS3Service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.yarden.restServiceDemo.Enums;
import com.yarden.restServiceDemo.pojos.EyesResultRequestJson;
import com.yarden.restServiceDemo.pojos.RequestInterface;
import com.yarden.restServiceDemo.pojos.SdkResultRequestJson;
import javassist.NotFoundException;

public class AwsS3ResultsJsonsService {

    private static final String SdkRequestFileNamePrefix = "Sdk";
    private static final String EyesRequestFileNamePrefix = "Eyes";

    public static void addSdkRequestToS3File(String json) {
        SdkResultRequestJson sdkResultRequestJson = new Gson().fromJson(json, SdkResultRequestJson.class);
        addRequestToS3File(sdkResultRequestJson, S3PrefixStrings.Sdk);
        System.gc();
    }

    public static void addEyesRequestToS3File(String json) {
        EyesResultRequestJson eyesResultRequestJson = new Gson().fromJson(json, EyesResultRequestJson.class);
        addRequestToS3File(eyesResultRequestJson, S3PrefixStrings.Eyes);
        System.gc();
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

    private static void addRequestToS3File(RequestInterface request, S3PrefixStrings fileNamePrefixInS3){
        if ((request.getSandbox() != null) && request.getSandbox()) {
            return;
        }
        RequestInterface resultRequestJsonFromS3 = null;
        String resultRequestJsonFileName = getResultRequestJsonFileName(request.getId(), request.getGroup(), fileNamePrefixInS3.value);
        try {
            String currentRequestFromS3File = AwsS3Provider.getStringFromFile(Enums.EnvVariables.AwsS3ResultsJsonsBucketName.value, resultRequestJsonFileName);
            resultRequestJsonFromS3 = new Gson().fromJson(currentRequestFromS3File, request.getClass());
            JsonArray testResultsFromS3 = resultRequestJsonFromS3.getResults();
            testResultsFromS3.addAll(request.getResults());
            resultRequestJsonFromS3.setResults(testResultsFromS3);
        } catch (Throwable t) {
            resultRequestJsonFromS3 = request;
        }
        AwsS3Provider.writeStringToFile(Enums.EnvVariables.AwsS3ResultsJsonsBucketName.value, resultRequestJsonFileName, new Gson().toJson(resultRequestJsonFromS3));
    }

    private static String getResultRequestJsonFileName(String id, String group, String requestFileNamePrefix){
        return requestFileNamePrefix + "-" + id + "-" + group.toLowerCase();
    }

    private enum S3PrefixStrings {
        Sdk("Sdk"), Eyes("Eyes");

        public final String value;

        S3PrefixStrings(String value) {
            this.value = value;
        }
    }

}
