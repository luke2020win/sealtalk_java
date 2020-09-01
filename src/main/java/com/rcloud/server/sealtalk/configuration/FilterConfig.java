//package com.rcloud.server.sealtalk.configuration;
//
//import com.rcloud.server.sealtalk.filter.CorsFilter;
//import org.springframework.boot.web.servlet.FilterRegistrationBean;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//import javax.annotation.Resource;
//
//@Configuration
//public class FilterConfig {
//
//    @Resource
//    private SealtalkConfig sealtalkConfig;
//
//    @Bean
//    public FilterRegistrationBean registFilter() {
//        FilterRegistrationBean registration = new FilterRegistrationBean();
//
//        CorsFilter corsFilter = new CorsFilter();
//        corsFilter.setSealtalkConfig(sealtalkConfig);
//        registration.setFilter(corsFilter);
//        registration.addUrlPatterns("*");
//        registration.setName("CorsFilter");
//        registration.setOrder(1);
//        return registration;
//    }
//}
