package com.cs;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.List;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.util.StringUtils;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;

/**
 * Copyright (c) 2013-2014 Abakus, Inc. All rights reserved.
 * User: Alexander Dudarenko
 * Date: 2014-12-10
 */

/**
 * 
 */
public class TestS3 {
    
    private static final String accessKey = "***";
    private static final String secretKey = "***";

    private AmazonS3 conn;

    public TestS3() {
        AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
        ClientConfiguration clientConfig = new ClientConfiguration();
        //clientConfig.setProtocol(Protocol.HTTP);
        //conn = new AmazonS3Client(credentials, clientConfig);
        System.out.println(System.getProperty("aws.credentials"));
        conn = new AmazonS3Client(new ProfileCredentialsProvider(System.getProperty("aws.credentials"), "default"));

        //conn.setEndpoint("endpoint.com");
    }

    public void listBuckets() {
        List<Bucket> buckets = conn.listBuckets();
        for (Bucket bucket : buckets) {
            System.out.printf("%-30s %-20s %s\n",
                bucket.getName(),
                bucket.getOwner().getDisplayName(),
                StringUtils.fromDate(bucket.getCreationDate())
            );
        }
    }

    public void listBucketFiles(String bucketName) {
        ObjectListing objects = conn.listObjects(bucketName);
        do {
            for (S3ObjectSummary objectSummary : objects.getObjectSummaries()) {
                System.out.println(objectSummary.getKey() + "\t" +
                   objectSummary.getSize() + "\t" +
                   StringUtils.fromDate(objectSummary.getLastModified()));
            }
            objects = conn.listNextBatchOfObjects(objects);
        } while (objects.isTruncated());
    }

    public void listBucketDirFiles(String bucketName, String dir) {
        if (dir.equals("/")) {
            dir = "";
        } else if (!dir.endsWith("/")) {
            dir = dir + "/";
        }
        ListObjectsRequest listRequest = new ListObjectsRequest();
        listRequest.setBucketName(bucketName);
        listRequest.setPrefix(dir);
        listRequest.setDelimiter("/");
        ObjectListing objects = conn.listObjects(listRequest);
        do {
            for (String name : objects.getCommonPrefixes()) {
                if (name.equals("/")) {
                    continue;
                }
                System.out.println("DIR:  " + name);
            }
            for (S3ObjectSummary objectSummary : objects.getObjectSummaries()) {
                String key = objectSummary.getKey();
                if (key.equals(dir) || key.equals("/")) {
                    continue;
                }
                System.out.println("FILE: " + objectSummary.getKey() + " " + objectSummary.getSize());
            }
            objects = conn.listNextBatchOfObjects(objects);
        } while (objects.isTruncated());
    }
    
    public static void main(String[] args) {
        System.out.println ("== TEST STARTED");
        TestS3 s3 = new TestS3();
        System.out.println ("== BUCKETS");
        s3.listBuckets();
        System.out.println ("== BUCKET FILES");
//        s3.listBucketFiles("abakusdist");
        System.out.println ("== BUCKET DIR FILES");
        s3.listBucketDirFiles("abakusclientdata", "test_dev/alexd");
        //s3.listBucketDirFiles("abakusclientdata", "/");
        System.out.println ("== TEST DONE");
    }

}
