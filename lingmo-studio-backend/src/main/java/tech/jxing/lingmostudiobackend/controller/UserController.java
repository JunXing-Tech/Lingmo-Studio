package tech.jxing.lingmostudiobackend.controller;


import cn.hutool.core.bean.BeanUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import tech.jxing.lingmostudiobackend.annotation.AuthCheck;
import tech.jxing.lingmostudiobackend.common.BaseResponse;
import tech.jxing.lingmostudiobackend.common.DeleteRequest;
import tech.jxing.lingmostudiobackend.common.ResultUtils;
import tech.jxing.lingmostudiobackend.constant.UserConstant;
import tech.jxing.lingmostudiobackend.exception.BusinessException;
import tech.jxing.lingmostudiobackend.exception.ErrorCode;
import tech.jxing.lingmostudiobackend.exception.ThrowUtils;
import tech.jxing.lingmostudiobackend.model.dto.user.UserAddRequest;
import tech.jxing.lingmostudiobackend.model.dto.user.UserLoginRequest;
import tech.jxing.lingmostudiobackend.model.dto.user.UserRegisterRequest;
import tech.jxing.lingmostudiobackend.model.entity.User;
import tech.jxing.lingmostudiobackend.model.vo.LoginUserVO;
import tech.jxing.lingmostudiobackend.service.UserService;

@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 用户注册
     *
     * @param userRegisterRequest 用户注册请求
     * @return 注册结果
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(UserRegisterRequest userRegisterRequest) {
        // 注册参数校验，如果参数不存在，则抛出参数错误异常
        ThrowUtils.throwIf(userRegisterRequest == null, ErrorCode.PARAMS_ERROR);
        // 获取注册参数
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        // 调用实现方法
        Long result = userService.userRegister(userAccount, userPassword, checkPassword);
        // 返回注册结果
        return ResultUtils.success(result);
    }

    /**
     * 用户登录
     *
     * @param userLoginRequest 用户登录请求
     * @param request          请求对象
     * @return 脱敏后的用户登录信息
     */
    @PostMapping("/login")
    public BaseResponse<LoginUserVO> userLogin(UserLoginRequest userLoginRequest, HttpServletRequest request) {
        // 校验用户登录参数
        ThrowUtils.throwIf(userLoginRequest == null, ErrorCode.PARAMS_ERROR);
        // 获取用户登录参数
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        // 调用实现方法
        LoginUserVO loginUserVO = userService.userLogin(userAccount, userPassword, request);
        // 返回登录结果
        return ResultUtils.success(loginUserVO);
    }

    /**
     * 获取当前登录用户
     *
     * @param request 请求对象
     * @return 脱敏后的用户信息
     */
    @GetMapping("/get/login")
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        // 调用实现方法
        User loginUser = userService.getLoginUser(request);
        // 返回脱敏后的登录用户信息
        return ResultUtils.success(userService.getLoginUserVO(loginUser));
    }

    /**
     * 用户注销
     *
     * @param request 请求对象
     * @return 是否注销成功
     */
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        // 调用实现方法
        boolean result = userService.userLogout(request);
        // 返回用户注销结果
        return ResultUtils.success(result);
    }

    /**
     * 管理员创建用户，需要管理员权限
     *
     * @param userAddRequest 创建用户请求
     * @return 创建的用户Id
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest) {
        ThrowUtils.throwIf(userAddRequest == null, ErrorCode.PARAMS_ERROR);
        User user = new User();
        // 拷贝参数
        BeanUtil.copyProperties(userAddRequest, user);
        // 默认密码 12345678
        final String DEFAULT_PASSWORD = "12345678";
        String encryptPassword = userService.getEncryptPassword(DEFAULT_PASSWORD);
        user.setUserPassword(encryptPassword);
        // 插入数据库
        boolean result = userService.save(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(user.getId());
    }

    /**
     * 管理员删除用户，需要管理员权限
     *
     * @param deleteRequest 删除用户请求
     * @return 是否删除成功
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = userService.removeById(deleteRequest.getId());
        return ResultUtils.success(result);
    }

}
