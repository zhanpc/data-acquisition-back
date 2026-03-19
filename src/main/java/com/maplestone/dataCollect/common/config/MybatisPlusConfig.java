package com.maplestone.dataCollect.common.config;

import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @description: Mybatisplus 分页插件拦截器
 * @Author hmx
 * @CreateTime 2021-06-23 17:26
 */

@EnableTransactionManagement // 开启事务管理
@MapperScan("com.maplestone.**.mapper.**") // 扫描mapper接口
@Configuration
public class MybatisPlusConfig {

    /**
     * 注入一个bean
     * 
     * @return
     */
    @Bean
    public PaginationInterceptor paginationInterceptor() {
        return new PaginationInterceptor();
    }

}
