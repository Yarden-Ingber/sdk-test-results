package com.yarden.restServiceDemo.awsS3Service;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.yarden.restServiceDemo.Enums;
import com.yarden.restServiceDemo.Logger;

import java.io.File;

public class AwsS3Provider {

    public static String uploadFileToS3(String fileNameInBucket, String localFileName){
        Logger.info("Uploading file: " + localFileName + " to AWS S3 bucket with name: " + fileNameInBucket);
        final AmazonS3 s3Client = AmazonS3ClientBuilder.standard().withRegion(Regions.DEFAULT_REGION).build();
        s3Client.putObject(new PutObjectRequest(Enums.EnvVariables.AwsS3SdkReportsBucketName.value, fileNameInBucket, new File(localFileName)).withCannedAcl(CannedAccessControlList.PublicRead));
        return s3Client.getUrl(Enums.EnvVariables.AwsS3SdkReportsBucketName.value, fileNameInBucket).toString();
    }

}
