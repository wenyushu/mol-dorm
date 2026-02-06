package com.mol.common.mybatis.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
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
        
        // 分页插件配置
        PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
        // 🛡️ 防刁民设计：如果请求页码大于总页数，自动跳回最后一页（或第一页）
        paginationInnerInterceptor.setOverflow(true);
        // 🛡️ 防刁民设计：限制单页最大查询量，防止有刁民一次查 10 万条导致 OOM
        paginationInnerInterceptor.setMaxLimit(500L);
        
        /*
         * 添加分页拦截器，这一步就是告诉 MP：我要开启 MySQL 环境下的分页功能
         * 必须指定 DbType.MYSQL，否则 MP 无法根据数据库方言生成正确的 count 和 limit 语句
         */
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        
        // 添加乐观锁插件
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        return interceptor;
    }
}