package com.cs;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.List;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.util.StringUtils;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
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
        conn = new AmazonS3Client(credentials, clientConfig);
        //conn.setEndpoint("endpoint.com");
    }

    public void listBuckets() {
        List<Bucket> buckets = conn.listBuckets();
        System.out.println ("== BUCKET LIST");
        for (Bucket bucket : buckets) {
                System.out.println(bucket.getName() + "\t" + StringUtils.fromDate(bucket.getCreationDate()));
        }
    }
    
    public static void main(String[] args) {
        System.out.println ("== TEST STARTED");
        TestS3 s3 = new TestS3();
        s3.listBuckets();
        System.out.println ("== TEST DONE");
    }

}
