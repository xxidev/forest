package com.rymcu.forest.service;

import com.rymcu.forest.dto.TokenUser;
import com.rymcu.forest.dto.UserDTO;
import com.rymcu.forest.entity.User;
import com.rymcu.forest.entity.UserExtend;
import org.apache.ibatis.exceptions.TooManyResultsException;

public interface AccountService {
    /**
     * 通过账号查询用户信息
     *
     * @param account
     * @return User
     * @throws TooManyResultsException
     */
    User findByAccount(String account) throws TooManyResultsException;
    /**
     * 登录接口
     *
     * @param account  邮箱
     * @param password 密码
     * @return Map
     */
    TokenUser login(String account, String password);
    /**
     * 通过 account 获取用户信息接口
     *
     * @param account 昵称
     * @return UserDTO
     */
    UserDTO findUserDTOByAccount(String account);
    /**
     * 获取用户扩展信息
     *
     * @param account
     * @return
     */
    UserExtend selectUserExtendByAccount(String account);
    /**
     * 通过邮箱更新用户最后登录时间
     *
     * @param account
     * @return
     */
    Integer updateLastOnlineTimeByAccount(String account);
    boolean hasAdminPermission(String account);
}
