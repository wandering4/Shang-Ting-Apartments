package com.atguigu.lease.web.admin.service.impl;

import com.atguigu.lease.common.minio.MinioProperties;
import com.atguigu.lease.web.admin.service.FileService;
import io.minio.*;
import io.minio.errors.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private MinioProperties properties;

    @Override
    public String upload(MultipartFile file) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        String bucket = properties.getBucketName();
        boolean b = false;


            //如果不存在桶则创建
            b = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!b) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
                //设置权限：允许自写，所有人读
                minioClient.setBucketPolicy(SetBucketPolicyArgs.builder().bucket(bucket).
                        config(createBucketPolicyConfig(bucket)).build());
            }

            //对象名
            String filename = new SimpleDateFormat("yyyyMMdd").format(new Date()) + "/" + UUID.randomUUID() + "-" + file.getOriginalFilename();

            //上传文件
            minioClient.putObject(
                    PutObjectArgs.builder().
                    bucket(bucket).
                    stream(file.getInputStream(),file.getSize(),-1).
                    object(filename).
                            contentType(file.getContentType()).
                            build());

            //url
            return properties.getEndpoint()+"/"+bucket+"/"+filename;
    }



    private String createBucketPolicyConfig(String bucketName) {

        return """
            {
              "Statement" : [ {
                "Action" : "s3:GetObject",
                "Effect" : "Allow",
                "Principal" : "*",
                "Resource" : "arn:aws:s3:::%s/*"
              } ],
              "Version" : "2012-10-17"
            }
            """.formatted(bucketName);
    }

}
