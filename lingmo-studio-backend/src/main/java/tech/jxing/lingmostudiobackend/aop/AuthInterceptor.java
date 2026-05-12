package tech.jxing.lingmostudiobackend.aop;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import tech.jxing.lingmostudiobackend.annotation.AuthCheck;
import tech.jxing.lingmostudiobackend.exception.BusinessException;
import tech.jxing.lingmostudiobackend.exception.ErrorCode;
import tech.jxing.lingmostudiobackend.model.entity.User;
import tech.jxing.lingmostudiobackend.model.enums.UserRoleEnum;
import tech.jxing.lingmostudiobackend.service.UserService;

/**
 * 权限校验 AOP
 *
 */
@Aspect
@Component
public class AuthInterceptor {

    @Resource
    private UserService userService;

    /**
     * 执行拦截
     *
     * @param joinPoint 切入点
     * @param authCheck 权限校验注解
     */
    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        String mustRole = authCheck.mustRole();
        /**
         * 在 AOP 拦截器中获取当前的 HTTP 请求对象
         * AOP 拦截器（不是 Controller），方法参数里没有直接传入 HttpServletRequest
         * 拿不到 request 参数，但又需要 request 来获取 session 中的登录用户
         *
         * 获取请求属性 - RequestContextHolder 工具类，从当前线程上下文中获取请求对象
         */
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        /**
         * 转换为 HTTP 请求
         *
         * RequestAttributes 接口没有 getRequest() 方法
         * ServletRequestAttributes 类才有 getRequest() 方法
         */
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        UserRoleEnum mustRoleEnum = UserRoleEnum.getEnumByValue(mustRole);
        // 不需要权限，直接放行
        if (mustRoleEnum == null) {
            return joinPoint.proceed();
        }
        // 获取当前用户的角色权限
        UserRoleEnum userRoleEnum = UserRoleEnum.getEnumByValue(loginUser.getUserRole());
        // 没有权限，直接拒绝
        if (userRoleEnum == null) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 要求必须有管理员权限，但当前登录用户没有
        if (UserRoleEnum.ADMIN.equals(mustRoleEnum) && !UserRoleEnum.ADMIN.equals(userRoleEnum)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 通过权限校验，放行
        return joinPoint.proceed();
    }
}