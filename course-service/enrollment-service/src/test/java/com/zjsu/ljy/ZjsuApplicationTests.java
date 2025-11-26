package com.zjsu.ljy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StringUtils;

@SpringBootTest
class ZjsuApplicationTests {

	@Value("${catalog-service.url:}")  // 添加默认值避免注入失败
	private String catalogServiceUrl;

	@Test
	void contextLoads() {
	}

	@Test
	void testCatalogServiceUrlInjection() {
		// 详细输出catalogServiceUrl的各种属性
		System.out.println("\n========== 测试 catalogServiceUrl 注入 ==========");
		System.out.println("catalogServiceUrl值: '" + catalogServiceUrl + "'");
		System.out.println("catalogServiceUrl是否为null: " + (catalogServiceUrl == null));
		System.out.println("catalogServiceUrl是否为空字符串: " + StringUtils.isEmpty(catalogServiceUrl));

		if (catalogServiceUrl != null) {
			System.out.println("catalogServiceUrl长度: " + catalogServiceUrl.length());
			System.out.println("catalogServiceUrl是否以http://开头: " + catalogServiceUrl.startsWith("http://"));
			System.out.println("catalogServiceUrl是否以https://开头: " + catalogServiceUrl.startsWith("https://"));
			System.out.println("catalogServiceUrl是否以/结尾: " + catalogServiceUrl.endsWith("/"));
		}

		// 测试URL拼接逻辑
		testUrlConcatenation();
	}

	private void testUrlConcatenation() {
		String courseId = "821fc70e-b3d3-11f0-beb5-00ff5c9af41d";
		System.out.println("\n========== 测试 URL 拼接逻辑 ==========");
		System.out.println("使用的courseId: '" + courseId + "'");

		// 测试模拟的URL拼接场景
		testConcatenationWithUrl("http://localhost:8081", courseId);  // 不以/结尾
		testConcatenationWithUrl("http://localhost:8081/", courseId); // 以/结尾

		// 使用实际注入的URL测试
		if (catalogServiceUrl != null && !catalogServiceUrl.isEmpty()) {
			System.out.println("\n使用实际注入的URL进行拼接测试:");
			String finalUrl = catalogServiceUrl.endsWith("/") ?
					catalogServiceUrl + "api/courses/" + courseId :
					catalogServiceUrl + "/api/courses/" + courseId;
			System.out.println("最终拼接的URL: '" + finalUrl + "'");

			// 验证最终URL格式是否正确
			boolean isValidFormat = finalUrl.matches("^https?://.*");
			System.out.println("URL格式是否有效: " + isValidFormat);
		}
	}

	private void testConcatenationWithUrl(String baseUrl, String courseId) {
		System.out.println("\n测试基础URL: '" + baseUrl + "'");
		System.out.println("基础URL是否以/结尾: " + baseUrl.endsWith("/"));

		String concatenatedUrl = baseUrl.endsWith("/") ?
				baseUrl + "api/courses/" + courseId :
				baseUrl + "/api/courses/" + courseId;

		System.out.println("拼接后URL: '" + concatenatedUrl + "'");
		// 检查URL中是否有重复的斜杠
		boolean hasDuplicateSlashes = concatenatedUrl.contains("//api") || concatenatedUrl.contains("courses//");
		System.out.println("URL中是否有重复斜杠: " + hasDuplicateSlashes);
	}
}
