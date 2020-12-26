package com.yarden.restServiceDemo.awsS3Service;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.yarden.restServiceDemo.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class AwsS3Provider {

    private static final AmazonS3 s3Client = AmazonS3ClientBuilder.standard().withRegion(Regions.DEFAULT_REGION).build();

    public static void uploadFileToS3(String bucketName, String fileNameInBucket, String localFileName){
        Logger.info("Uploading file: " + localFileName + " to AWS S3 bucket with name: " + fileNameInBucket);
        s3Client.putObject(new PutObjectRequest(bucketName, fileNameInBucket, new File(localFileName)).withCannedAcl(CannedAccessControlList.PublicRead));
    }

    public static String getUrlToFileInS3(String bucketName, String fileNameInBucket){
        return s3Client.getUrl(bucketName, fileNameInBucket).toString();
    }

    public static void writeStringToFile(String bucketName, String fileNameInBucket, String string){
        Logger.info("writing string: " + string + " to file " + fileNameInBucket);
        s3Client.putObject(bucketName, fileNameInBucket, string);
    }

    public static String getStringFromFile(String bucketName, String fileNameInBucket) throws IOException {
        S3Object object = s3Client.getObject(new GetObjectRequest(bucketName, fileNameInBucket));
        final InputStreamReader streamReader = new InputStreamReader(object.getObjectContent(), StandardCharsets.UTF_8);
        final BufferedReader reader = new BufferedReader(streamReader);
        String result = "";
        try {
            result = reader.lines().collect(Collectors.joining());
        } finally {
            streamReader.close();
            reader.close();
        }
        return result;
    }

}
