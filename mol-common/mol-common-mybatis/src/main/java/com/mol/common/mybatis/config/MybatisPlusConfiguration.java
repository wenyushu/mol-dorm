package com.mol.common.mybatis.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * MyBatis-Plus 全局配置类
 * * 注释：我们不需要手写 PaginationInnerInterceptor 的代码，
 * 我们只需要把它 new 出来并交给 MybatisPlusInterceptor 管理即可。
 */
@Configuration
@EnableTransactionManagement // 开启声明式事务支持
// 告诉 IDEA：别检查这个 Mapper 扫描路径了，我知道它现在找不到，但运行时能行
@SuppressWarnings("SpringMybatisPlusMapperScanInspection")
@MapperScan("com.mol.**.mapper")
public class MybatisPlusConfiguration {
    
    /**
     * MyBatis-Plus 插件大管家
     * 将分页插件、乐观锁插件等都注册在这里
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        
        /*
         * 添加分页拦截器，这一步就是告诉 MP：我要开启 MySQL 环境下的分页功能
         * 必须指定 DbType.MYSQL，否则 MP 无法根据数据库方言生成正确的 count 和 limit 语句
         */
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        
        return interceptor;
    }
}