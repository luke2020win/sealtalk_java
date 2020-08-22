package com.rcloud.server.sealtalk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import tk.mybatis.spring.annotation.MapperScan;

@EnableTransactionManagement
@ServletComponentScan(basePackages = {"com.rcloud.server.sealtalk.filter"})
@SpringBootApplication
@MapperScan("com.rcloud.server.sealtalk.dao")
public class SealtalkServerApplication extends SpringBootServletInitializer {
    public static void main(String[] args) {
        SpringApplication.run(SealtalkServerApplication.class, args);
    }

    //为了打包springboot项目
    @Override
    protected SpringApplicationBuilder configure(
            SpringApplicationBuilder builder) {
        return builder.sources(this.getClass());
    }
}
