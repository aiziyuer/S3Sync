package com.aiziyuer.app;

import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import com.aiziyuer.app.common.S3Utils;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CompleteMultipartUploadRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadResult;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.PartETag;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectIdBuilder;
import com.amazonaws.services.s3.model.UploadPartRequest;
import com.amazonaws.services.s3.model.UploadPartResult;
import com.amazonaws.util.SdkHttpUtils;
import com.google.common.collect.Lists;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class DemoCaseTest {

	@Test
	@Ignore
	public void testList() {

		log.info("testList ... ");
		AmazonS3Client s3 = S3Utils.getClient("s1");

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

	public void transfer(AmazonS3Client s1, AmazonS3Client s2, S3ObjectIdBuilder src, S3ObjectIdBuilder dst) {

		// 获取原始源文件的大小
		long contentLength = s1.getObjectMetadata(src.getBucket(), src.getKey()).getContentLength();

		// 分段的大小
		long partSize = 5 * 1024 * 1024;

		List<PartETag> partEtags = Lists.newArrayList();

		// 生成多段上传的请求, 主要拿到upload id用来后面的多段上传
		InitiateMultipartUploadResult initResponse = s2.initiateMultipartUpload(//
				new InitiateMultipartUploadRequest(dst.getBucket(), dst.getKey()) //
		);

		for (int i = 0; i * partSize < contentLength; i++) {

			// 起始位置
			long pos = i * partSize;

			// 防止最后一组数据小于partSize
			long actualPartSize = Math.min(partSize, contentLength - pos);

			// 构造上传请求
			UploadPartRequest uploadRequest = new UploadPartRequest() //
					.withBucketName(dst.getBucket()) //
					.withKey(dst.getKey()) //
					.withUploadId(initResponse.getUploadId()) //
					.withPartNumber(i + 1) // 自然数从1开始有别于程序计数
					.withPartSize(actualPartSize);

			// 设置上传源
			S3Object srcPartObject = s1.getObject(//
					new GetObjectRequest(src.getBucket(), src.getKey()) //
							.withRange(pos, pos + actualPartSize) // seek位置
			);
			uploadRequest.setInputStream(srcPartObject.getObjectContent());

			// 开始多段上传
			UploadPartResult uploadResult = s2.uploadPart(uploadRequest);
			partEtags.add(uploadResult.getPartETag());

		}

		s2.completeMultipartUpload(//
				new CompleteMultipartUploadRequest( //
						dst.getBucket(), // 目标桶
						dst.getKey(), // 目标桶内路径
						initResponse.getUploadId(), // 本次上传的id
						partEtags // 各段的etag值
				));

	}

	@Test
	public void testUpload() {

		log.info("");

		AmazonS3Client s1 = S3Utils.getClient("s1");
		AmazonS3Client s2 = S3Utils.getClient("s2");

		S3ObjectIdBuilder src = new S3ObjectIdBuilder().withBucket("l00249932").withKey("old.part2.rar");
		S3ObjectIdBuilder dst = new S3ObjectIdBuilder().withBucket("l00249932-test").withKey("old.part2.rar");

		transfer(s1, s2, src, dst);

	}

}
