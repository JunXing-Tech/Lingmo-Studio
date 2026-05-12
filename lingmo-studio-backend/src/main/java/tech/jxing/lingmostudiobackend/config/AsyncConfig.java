package tech.jxing.lingmostudiobackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步任务配置类。
 * 通过 @EnableAsync 开启 Spring 的异步执行能力，并自定义线程池。
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * 自定义文章生成的异步线程池。
     * 专门用于处理耗时较长的 AI 文章生成任务，避免阻塞 Web 容器的主线程。
     *
     * @return 线程池执行器
     */
    @Bean(name = "articleExecutor")
    public Executor articleExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // 核心线程数：池中始终保持的活跃线程数量
        executor.setCorePoolSize(5);
        
        // 最大线程数：当队列满时，池中允许创建的最大线程数量
        executor.setMaxPoolSize(10);
        
        // 队列容量：用于缓存等待执行任务的队列大小
        executor.setQueueCapacity(100);
        
        // 线程名称前缀：方便在日志中识别不同的线程任务
        executor.setThreadNamePrefix("article-async-");
        
        // 拒绝策略：当队列和最大线程都满时，由提交任务的线程（通常是 HTTP 线程）直接执行
        // 这种策略可以起到一种简单的“流控”作用，避免任务丢失
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        // 优雅停机：在 JVM 关闭时，等待所有正在执行的任务完成后再销毁线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        
        // 停机等待时间：如果 60 秒内任务还没跑完，则强制关闭
        executor.setAwaitTerminationSeconds(60);
        
        // 初始化线程池
        executor.initialize();
        
        return executor;
    }
}
