package com.aiziyuer.app.common;

import lombok.Data;

@Data
public class S3Config {

	/** 别名 */
	private String name;

	/** 访问密钥 */
	private String ak;

	/** 访问私钥 */
	private String sk;

	/** 访问入口 */
	private String endpoint;
	
	/** 代理地址 */
	private String proxy;

}
