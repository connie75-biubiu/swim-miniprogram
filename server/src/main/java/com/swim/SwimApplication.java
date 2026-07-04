package com.swim;

import com.swim.config.DotenvLoader;
import com.swim.config.WxConfig;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.CommandLineRunner;

@SpringBootApplication
@MapperScan("com.swim.mapper")
public class SwimApplication {
    public static void main(String[] args) {
        // 在 Spring 启动前加载 server/.env，把配置写入系统属性。
        // 必须在 SpringApplication.run 之前，这样 application.yml 的 ${VAR:default}
        // 占位符才能从系统属性取到 .env 的真实值。
        DotenvLoader.loadAndApplyAsSystemProperties();
        SpringApplication.run(SwimApplication.class, args);
    }

    @Bean
    public CommandLineRunner printWxAppid(WxConfig wxConfig) {
        return args -> System.out.println("微信小程序 appid: " + wxConfig.getAppid());
    }
}
