//package com.rcloud.server.sealtalk.filter;
//
//import com.rcloud.server.sealtalk.configuration.SealtalkConfig;
//import io.micrometer.core.instrument.util.StringUtils;
//import javax.servlet.*;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.io.IOException;
//
//public class CorsFilter implements Filter {
//
//    private SealtalkConfig sealtalkConfig;
//
//    @Override
//    public void init(FilterConfig filterConfig) {
//
//    }
//
//    @Override
//    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
//
//        HttpServletResponse response = (HttpServletResponse) res;
//        HttpServletRequest request = (HttpServletRequest) req;
//        // 不使用*，自动适配跨域域名，避免携带Cookie时失效
//
//        response.setHeader("Access-Control-Allow-Origin", sealtalkConfig.getCorsHosts());
////        String origin = request.getHeader("Origin");
////        if (StringUtils.isNotBlank(origin)) {
////            response.setHeader("Access-Control-Allow-Origin", origin);
////        }
//        // 明确许可客户端发送Cookie，不允许删除字段即可
//        response.setHeader("Access-Control-Allow-Credentials", "true");
//        // 允许跨域的请求方法类型
//        response.setHeader("Access-Control-Allow-Methods", sealtalkConfig.getAllowMethods());
//        // 预检命令（OPTIONS）缓存时间，单位：秒
//        response.setHeader("Access-Control-Max-Age", sealtalkConfig.getAuthCookieMaxAge());
//        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
//        // 自适应所有自定义头
////        String headers = request.getHeader("Access-Control-Request-Headers");
////        if (StringUtils.isNotBlank(headers)) {
////            response.setHeader("Access-Control-Allow-Headers", "*");
////            response.setHeader("Access-Control-Expose-Headers", headers);
////        }
//
//        chain.doFilter(request, response);
//    }
//
//    @Override
//    public void destroy() {
//
//    }
//
//    public void setSealtalkConfig(SealtalkConfig sealtalkConfig) {
//        this.sealtalkConfig = sealtalkConfig;
//    }
//}