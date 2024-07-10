package com.rymcu.forest.service.impl;

import com.github.f4b6a3.ulid.UlidCreator;
import com.rymcu.forest.auth.JwtConstants;
import com.rymcu.forest.auth.TokenManager;
import com.rymcu.forest.dto.TokenUser;
import com.rymcu.forest.dto.UserDTO;
import com.rymcu.forest.entity.User;
import com.rymcu.forest.entity.UserExtend;
import com.rymcu.forest.mapper.UserExtendMapper;
import com.rymcu.forest.mapper.UserMapper;
import com.rymcu.forest.service.AccountService;
import com.rymcu.forest.service.LoginRecordService;
import com.rymcu.forest.util.Utils;
import org.apache.ibatis.exceptions.TooManyResultsException;
import org.apache.shiro.authc.AccountException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;
@Service
public class AccountServiceImpl implements AccountService {
    @Resource
    private UserMapper userMapper;
    @Resource
    private TokenManager tokenManager;

    @Autowired
    private StringRedisTemplate redisTemplate;
    @Resource
    private LoginRecordService loginRecordService;
    @Resource
    private UserExtendMapper userExtendMapper;

    @Override
    public User findByAccount(String account) throws TooManyResultsException {
        return userMapper.selectByAccount(account);
    }
    @Override
    public TokenUser login(String account, String password) {
        User user = userMapper.selectByAccount(account);
        if (user != null) {
            if (Utils.comparePwd(password, user.getPassword())) {
                userMapper.updateLastLoginTime(user.getIdUser());
                userMapper.updateLastOnlineTimeByAccount(user.getAccount());
                TokenUser tokenUser = new TokenUser();
                tokenUser.setToken(tokenManager.createToken(user.getAccount()));
                tokenUser.setRefreshToken(UlidCreator.getUlid().toString());
                redisTemplate.boundValueOps(tokenUser.getRefreshToken()).set(account, JwtConstants.REFRESH_TOKEN_EXPIRES_HOUR, TimeUnit.HOURS);
                // 保存登录日志
                loginRecordService.saveLoginRecord(user.getIdUser());
                return tokenUser;
            }
        }
        throw new AccountException();
    }
    @Override
    public UserDTO findUserDTOByAccount(String account) {
        return userMapper.selectUserDTOByAccount(account);
    }
    @Override
    public UserExtend selectUserExtendByAccount(String account) {
        return userExtendMapper.selectUserExtendByAccount(account);
    }
    @Override
    public Integer updateLastOnlineTimeByAccount(String account) {
        return userMapper.updateLastOnlineTimeByAccount(account);
    }
    @Override
    public boolean hasAdminPermission(String account) {
        return userMapper.hasAdminPermission(account);
    }
}
