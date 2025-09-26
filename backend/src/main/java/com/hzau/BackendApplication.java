package com.hzau;

import com.hzau.common.utils.GitUtil;
import com.hzau.common.utils.SpringBeanFactory;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * AI角色扮演网站启动类
 */
@SpringBootApplication
@MapperScan("com.hzau.mapper")
@Slf4j
public class BackendApplication {

	private static String[] args;
	private static ConfigurableApplicationContext context;

	public static void main(String[] args) {
		log.info("正在启动 AI角色扮演网站...");
		BackendApplication.args = args;
		BackendApplication.context = SpringApplication.run(BackendApplication.class, args);
		
		// 输出版本信息
		GitUtil gitUtil = SpringBeanFactory.getBean("gitUtil");
		if (gitUtil == null) {
			log.warn("获取版本信息失败，git.properties文件可能不存在");
		} else {
			log.info("=== AI角色扮演网站启动成功 ===");
			log.info("应用名称： AI角色扮演网站");
			log.info("构建版本： {}", gitUtil.getBuildVersion() != null ? gitUtil.getBuildVersion() : "未知版本");
			log.info("构建时间： {}", gitUtil.getBuildDate() != null ? gitUtil.getBuildDate() : "未知时间");
			log.info("Git分支： {}", gitUtil.getBranch() != null ? gitUtil.getBranch() : "未知分支");
			log.info("提交ID： {}", gitUtil.getCommitIdShort() != null ? gitUtil.getCommitIdShort() : "未知提交");
			log.info("提交时间： {}", gitUtil.getCommitTime() != null ? gitUtil.getCommitTime() : "未知时间");
			log.info("==============================");
		}
		log.info("AI角色扮演网站启动完成，可以开始使用了！");
	}

	// 项目重启
	public static void restart() {
		context.close();
		BackendApplication.context = SpringApplication.run(BackendApplication.class, args);
	}
}
