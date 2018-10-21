package com.aiziyuer.app;

import org.junit.Ignore;
import org.junit.Test;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.S3ClientOptions;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.util.SdkHttpUtils;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class DemoCaseTest {

	public AmazonS3Client getClient() {

		ClientConfiguration opts = new ClientConfiguration();
		opts.setProxyHost("127.0.0.1");
		opts.setProxyPort(8888);

		opts.setSignerOverride("S3SignerType");
		AWSCredentials credentials = new BasicAWSCredentials(System.getenv("S3_AK"), System.getenv("S3_SK"));
		AmazonS3Client client = new AmazonS3Client(credentials, opts);
		client.setEndpoint("http://obs.cn-east-2.myhwclouds.com");
		S3ClientOptions options = new S3ClientOptions();
		options.setPathStyleAccess(true);
		client.setS3ClientOptions(options);

		return client;
	}

	@Test
	public void testList() {

		log.info("testList ... ");
		AmazonS3Client s3 = getClient();

		ListObjectsRequest request = new ListObjectsRequest().withBucketName("l00249932").withPrefix("")
				.withMaxKeys(1000).withEncodingType("url");

		ObjectListing objects;
		do {
			objects = s3.listObjects(request);

			String encodingType = objects.getEncodingType();

			// 遍历结果
			objects.getObjectSummaries().stream().forEach(o -> {

				String key = o.getKey();
				
				log.info(String.format("relative path: %s",
						"url".equals(encodingType) ? SdkHttpUtils.urlDecode(key) : key));

			});

			// 设置下一页
			request.setMarker(objects.getNextMarker());

		} while (objects.isTruncated());

	}

	@Test
	@Ignore
	public void testUpload() {

		log.info("");

	}

}
