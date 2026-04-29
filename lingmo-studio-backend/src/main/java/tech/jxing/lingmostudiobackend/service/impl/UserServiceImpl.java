package tech.jxing.lingmostudiobackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import tech.jxing.lingmostudiobackend.exception.BusinessException;
import tech.jxing.lingmostudiobackend.exception.ErrorCode;
import tech.jxing.lingmostudiobackend.mapper.UserMapper;
import tech.jxing.lingmostudiobackend.model.dto.user.UserQueryRequest;
import tech.jxing.lingmostudiobackend.model.entity.User;
import tech.jxing.lingmostudiobackend.model.enums.UserRoleEnum;
import tech.jxing.lingmostudiobackend.model.vo.LoginUserVO;
import tech.jxing.lingmostudiobackend.model.vo.UserVO;
import tech.jxing.lingmostudiobackend.service.UserService;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static tech.jxing.lingmostudiobackend.constant.UserConstant.USER_LOGIN_STATE;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 新用户 id
     */
    @Override
    public Long userRegister(String userAccount, String userPassword, String checkPassword) {

        // 1 校验参数
        if(StrUtil.hasBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户注册参数为空");

        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户注册账号长度过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户注册密码长度过短");
        }
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户注册两次输入的密码不一致");
        }

        // 2 查询用户是否存在
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("userAccount", userAccount);
        long count = this.mapper.selectCountByQuery(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号已存在");
        }
        // 3 密码加密
        String encryptPassword = getEncryptPassword(userPassword);
        // 4 创建用户并将数据插入数据库
        User registerUser = new User();
        registerUser.setUserAccount(userAccount);
        registerUser.setUserPassword(encryptPassword);
        // 默认用户名
        registerUser.setUserName("lingmo_" + RandomUtil.randomNumbers(6));
        registerUser.setUserRole(UserRoleEnum.USER.getValue());
        boolean saveResult = this.save(registerUser);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "注册失败，数据库发生错误");
        }
        // 5 返回注册用户id
        return registerUser.getId();
    }

    /**
     * 用户登录
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param request       请求
     * @return 脱敏后的用户信息
     */
    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1 校验参数
        if (StrUtil.hasBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号长度过短");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度过短");
        }
        // 2 登录密码加密
        String encryptPassword = getEncryptPassword(userPassword);
        // 3 查询用户是否存在
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = this.mapper.selectOneByQuery(queryWrapper);
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        // 4 用户存在，则记录用户的登录态，把User对象存入Session
        request.getSession().setAttribute(USER_LOGIN_STATE, user);
        // 5 返回脱敏后的用户信息
        return this.getLoginUserVO(user);
    }

    /**
     * 获取当前登录用户，确保返回的用户数据是最新的
     *
     * @param request 请求
     * @return 登录用户
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 1 通过session判断用户是否登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 2 从数据库中获取最新用户数据
        Long userId = currentUser.getId();
        // 最新的用户数据
        currentUser = this.getById(userId);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }

    /**
     * 用户注销
     *
     * @param request 登录请求
     * @return 是否注销成功
     */
    @Override
    public boolean userLogout(HttpServletRequest request) {
        // 1 先判断用户是否登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        if (userObj == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "用户未登录");
        }
        // 2 移除登录态 session
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;
    }

    /**
     * 用户密码盐值加密
     * @param userPassword 用户密码
     * @return 加密后的密码
     */
    @Override
    public String getEncryptPassword(String userPassword) {
        // 盐值，混淆密码
        final String SALT = "jxing";
        return DigestUtils.md5DigestAsHex((userPassword + SALT).getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 返回脱敏用户信息
     *
     * @param user 用户
     * @return 脱敏后用户信息
     */
    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        // 脱敏
        BeanUtil.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    /**
     * 获取脱敏用户信息
     *
     * @param user 用户
     * @return 脱敏后用户信息
     */
    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtil.copyProperties(user, userVO);
        return userVO;
    }

    /**
     * 获取脱敏用户信息列表
     *
     * @param userList 用户列表
     * @return 脱敏后用户信息列表
     */
    @Override
    public List<UserVO> getUserVOList(List<User> userList) {
        if (CollUtil.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream()
                .map(this::getUserVO)
                .collect(Collectors.toList());
    }

    @Override
    public QueryWrapper getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = userQueryRequest.getId();
        String userAccount = userQueryRequest.getUserAccount();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        return QueryWrapper.create()
                // where id = ${id}
                .eq("id", id)
                // and userRole = ${userRole}
                .eq("userRole", userRole)
                .like("userAccount", userAccount)
                .like("userName", userName)
                .like("userProfile", userProfile)
                .orderBy(sortField, "ascend".equals(sortOrder));
    }
}
