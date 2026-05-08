package tech.jxing.lingmostudiobackend.manager;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static tech.jxing.lingmostudiobackend.constant.ArticleConstant.SSE_RECONNECT_TIME_MS;
import static tech.jxing.lingmostudiobackend.constant.ArticleConstant.SSE_TIMEOUT_MS;

/**
 * SSE Emitter 管理器。
 */
@Component
@Slf4j
public class SseEmitterManager {

    /**
     * 线程安全的 Map，用于存储所有活跃的 SseEmitter 实例。
     * Key 为任务 ID (taskId)，Value 为对应的 SseEmitter 对象。
     */
    private final Map<String, SseEmitter> emitterMap = new ConcurrentHashMap<>();

    /**
     * 为指定的任务创建一个新的 SseEmitter 连接。
     * 包含超时、完成和错误的回调处理，确保资源能够被及时释放。
     *
     * @param taskId 任务唯一标识 ID
     * @return 创建好的 SseEmitter 实例
     */
    public SseEmitter createEmitter(String taskId) {
        // 创建 Emitter 并设置预定义的超时时间
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);
        
        // 注册超时回调：当连接超过预定时间未活动时触发
        emitter.onTimeout(() -> {
            log.warn("SSE 连接超时, taskId={}", taskId);
            emitterMap.remove(taskId);
        });
        
        // 注册完成回调：当连接正常关闭或服务端主动调用 complete() 时触发
        emitter.onCompletion(() -> {
            log.info("SSE 连接完成, taskId={}", taskId);
            emitterMap.remove(taskId);
        });
        
        // 注册错误回调：当网络异常或发送失败时触发
        emitter.onError((e) -> {
            log.error("SSE 连接错误, taskId={}", taskId, e);
            emitterMap.remove(taskId);
        });
        
        // 将新创建的连接存入管理 Map
        emitterMap.put(taskId, emitter);
        log.info("SSE 连接已创建并加入管理器, taskId={}", taskId);
        
        return emitter;
    }

    /**
     * 向指定任务的客户端推送一条消息。
     *
     * @param taskId  目标任务 ID
     * @param message 推送的消息内容字符串
     */
    public void send(String taskId, String message) {
        // 从管理器中获取对应的 Emitter
        SseEmitter emitter = emitterMap.get(taskId);
        if (emitter == null) {
            log.warn("发送失败：SSE Emitter 不存在或已失效, taskId={}", taskId);
            return;
        }
        
        try {
            // 发送数据事件，并设置重连时间（如果连接中断，浏览器会在该时间后尝试重连）
            emitter.send(SseEmitter.event()
                    .data(message)
                    .reconnectTime(SSE_RECONNECT_TIME_MS));
            log.debug("SSE 消息推送成功, taskId={}, message={}", taskId, message);
        } catch (IOException e) {
            // 发送失败通常意味着客户端已断开连接，此时应移除该 Emitter
            log.error("SSE 消息发送异常, taskId={}", taskId, e);
            emitterMap.remove(taskId);
        }
    }

    /**
     * 关闭指定任务的 SSE 连接。
     * 通知客户端数据传输已结束，并从管理器中移除该连接。
     *
     * @param taskId 任务唯一标识 ID
     */
    public void complete(String taskId) {
        SseEmitter emitter = emitterMap.get(taskId);
        if (emitter == null) {
            log.warn("关闭失败：SSE Emitter 不存在, taskId={}", taskId);
            return;
        }
        
        try {
            // 发送完成信号给客户端
            emitter.complete();
            log.info("SSE 连接已正常关闭, taskId={}", taskId);
        } catch (Exception e) {
            log.error("关闭 SSE 连接时发生异常, taskId={}", taskId, e);
        } finally {
            // 无论是否异常，最后都必须从 Map 中移除，防止内存泄漏
            emitterMap.remove(taskId);
        }
    }

    /**
     * 检查当前是否存在针对该任务 ID 的活跃 SSE 连接。
     *
     * @param taskId 任务 ID
     * @return true 表示连接活跃，false 表示不存在
     */
    public boolean exists(String taskId) {
        return emitterMap.containsKey(taskId);
    }
}