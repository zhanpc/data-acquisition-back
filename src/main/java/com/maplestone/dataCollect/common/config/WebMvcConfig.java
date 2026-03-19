package com.maplestone.dataCollect.common.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.maplestone.dataCollect.common.filter.GlobalFilter;
import com.maplestone.dataCollect.common.interceptor.GlobalInterceptor;
import com.maplestone.dataCollect.common.interceptor.JwtInterceptor;

import java.util.List;

/**
 * @author hmx 2020/3/26 15:51
 * @description
 */

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * 注册一个拦截器
     * 
     * @return
     */
    @Bean
    public GlobalInterceptor globalInterceptor() {
        return new GlobalInterceptor();
    }

    /**
     * 注册 jwt拦截器
     * 
     * @return
     */
    @Bean
    public JwtInterceptor jwtInterceptor() {
        return new JwtInterceptor();
    }

    /**
     * 添加拦截器
     * 
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(globalInterceptor()).addPathPatterns("/api/**");
        registry.addInterceptor(jwtInterceptor()).addPathPatterns("/**")
                // 然后添加释放路径
                .excludePathPatterns("/swagger-resources/**")
                .excludePathPatterns("/swagger-ui.html/**")
                .excludePathPatterns("/webjars/**");
        // .excludePathPatterns("/user/login")
    }

    /**
     * 注册一个过滤器
     * 
     * @return
     */
    @Bean
    public FilterRegistrationBean registerFilter() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new GlobalFilter());
        registration.addUrlPatterns("/api/*");
        registration.setName("GlobalFilter");
        registration.setOrder(1);
        return registration;
    }

    /**
     * 添加过滤器
     * 
     * @return
     */
    @Bean
    public FilterRegistrationBean corsFilter() {
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        final CorsConfiguration config = new CorsConfiguration();
        // 允许cookies跨域
        config.setAllowCredentials(true);
        // #允许向该服务器提交请求的URI，*表示全部允许，在SpringMVC中，如果设成*，会自动转成当前请求头中的Origin
        config.addAllowedOrigin("*");
        // #允许访问的头信息,*表示全部
        config.addAllowedHeader("*");
        // 预检请求的缓存时间（秒），即在这个时间段里，对于相同的跨域请求不会再预检了
        config.setMaxAge(18000L);
        // 允许提交请求的方法，*表示全部允许
        config.addAllowedMethod("OPTIONS");
        config.addAllowedMethod("HEAD");
        config.addAllowedMethod("GET");
        config.addAllowedMethod("PUT");
        config.addAllowedMethod("POST");
        config.addAllowedMethod("DELETE");
        config.addAllowedMethod("PATCH");
        source.registerCorsConfiguration("/**", config);

        FilterRegistrationBean bean = new FilterRegistrationBean(new CorsFilter(source));
        // 设置监听器的优先级
        bean.setOrder(0);

        return bean;
    }

    /** 添加格式化的converters */
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        // 解决返回字符串带引号问题
        // converters.add(fastJsonHttpMessageConverter());
        converters.add(stringHttpMessageConverter());
    }

    /** 解决返回字符串带引号问题 */
    @Bean
    public StringHttpMessageConverter stringHttpMessageConverter() {
        return new StringHttpMessageConverter();
    }

    /** 引入fastjson 格式化返回值 */
    // @Bean
    // public HttpMessageConverter fastJsonHttpMessageConverter(){
    // FastJsonHttpMessageConverter converter = new FastJsonHttpMessageConverter();
    // FastJsonConfig fastJsonConfig = new FastJsonConfig();
    // fastJsonConfig.setSerializerFeatures(
    // //List字段如果为null,输出为[],而非null
    // SerializerFeature.WriteNullListAsEmpty,
    // //是否输出值为null的字段,默认为false
    // SerializerFeature.WriteMapNullValue,
    // //字符串null返回空字符串
    // SerializerFeature.WriteNullStringAsEmpty,
    // //数值null返回0
    // //SerializerFeature.WriteNullNumberAsZero,
    // //空布尔值返回false
    // SerializerFeature.WriteNullBooleanAsFalse,
    // //结果是否格式化,默认为false
    // SerializerFeature.PrettyFormat,
    // SerializerFeature.DisableCircularReferenceDetect
    // );
    // fastJsonConfig.getSerializeConfig().put(String.class,MyStringSerializer.instance);
    // //格式化日期
    // fastJsonConfig.setDateFormat("yyyy-MM-dd HH:mm:ss");
    //
    // converter.setFastJsonConfig(fastJsonConfig);
    // //3处理中文乱码问题
    // List<MediaType> fastMediaTypes = new ArrayList<>();
    // //fastMediaTypes.add(MediaType.TEXT_HTML);
    // //fastMediaTypes.add(MediaType.valueOf(MediaType.TEXT_HTML_VALUE +
    // ";charset=UTF-8"));
    // fastMediaTypes.add(MediaType.valueOf(MediaType.APPLICATION_JSON_VALUE +
    // ";charset=UTF-8"));
    // //4.在convert中添加配置信息.
    // converter.setSupportedMediaTypes(fastMediaTypes);
    // return converter;
    // }

}
