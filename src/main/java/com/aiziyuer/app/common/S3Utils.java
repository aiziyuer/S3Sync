package com.aiziyuer.app.common;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.util.Strings;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.S3ClientOptions;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class S3Utils {

	public static AmazonS3Client getClient(String s3Name) {

		S3Config s3Config = S3ConfigRegistry.S3_CONFIG_MAP.get(s3Name);

		ClientConfiguration opts = new ClientConfiguration();

		// 设置代理
		String proxy = s3Config.getProxy();
		if (Strings.isNotBlank(proxy)) {
			Matcher m = Pattern.compile("(?:http://)?(?<host>[^:]+)(?::(?<port>.+))?").matcher(proxy);
			if (m.matches()) {
				opts.setProxyHost(m.group("host"));
				opts.setProxyPort(NumberUtils.toInt(m.group("port"), 80));
			} else {
				log.error(String.format("proxy(%s) parse error", proxy));
			}
		}

		opts.setSignerOverride("S3SignerType");

		// 设置AK/SK
		AWSCredentials credentials = new BasicAWSCredentials(s3Config.getAk(), s3Config.getSk());
		AmazonS3Client client = new AmazonS3Client(credentials, opts);

		// 设置Endpoint
		client.setEndpoint(s3Config.getEndpoint());

		S3ClientOptions options = new S3ClientOptions();
		options.setPathStyleAccess(true);
		client.setS3ClientOptions(options);

		return client;
	}

}
