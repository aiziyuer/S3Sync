package com.aiziyuer.app.common;

import java.io.InputStream;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;

public class S3ConfigRegistry {

	public static final Map<String, S3Config> S3_CONFIG_MAP = Maps.newHashMap();

	static {

		String configFile = "s3cfg/config.yml";

		String activeProfile = System.getenv("spring.profiles.active");
		if (Strings.isNullOrEmpty(activeProfile))
			configFile = String.format("s3cfg/config-%s.yml", activeProfile);

		InputStream inputStream = S3ConfigRegistry.class.getClassLoader()//
				.getResourceAsStream(configFile);

		new Yaml(new Constructor(S3Config.class)).loadAll(inputStream).forEach(doc_obj -> {

			if (doc_obj instanceof S3Config) {
				S3Config s3Config = (S3Config) doc_obj;
				S3_CONFIG_MAP.put(s3Config.getName(), s3Config);
			}

		});

	}

}
