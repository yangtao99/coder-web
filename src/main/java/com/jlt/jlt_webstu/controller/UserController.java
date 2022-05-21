package com.jlt.jlt_webstu.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jlt.jlt_webstu.mapper.UserMapper;
import com.jlt.jlt_webstu.model.domain.User;
import com.jlt.jlt_webstu.model.domain.request.UserLoginRequest;
import com.jlt.jlt_webstu.model.domain.request.UserRegisterRequest;
import com.jlt.jlt_webstu.model.domain.request.UserSearchRequest;
import com.jlt.jlt_webstu.service.UserService;
import com.jlt.jlt_webstu.service.impl.UserServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.jlt.jlt_webstu.contant.UserConstant.ADMIN_ROLE;
import static com.jlt.jlt_webstu.contant.UserConstant.USER_LOGIN_STATE;

/**
 * @author jinlintao
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    @PostMapping("/register")
    public Long userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            return null;
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            return null;
        }
        return userService.userRegister(userAccount, userPassword, checkPassword);
    }

    @GetMapping("/current")
    public User getCurrentUser(HttpServletRequest request){
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null){
            return null;
        }
        long userId = currentUser.getId();
        return userService.getSafetyUser(userService.getById(userId));
    }

    @PostMapping("/login")
    public User userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            return null;
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            return null;
        }
        return userService.userLogin(userAccount, userPassword, request);
    }

    @PostMapping("/logout")
    public Integer userLogout( HttpServletRequest request) {
        if (request == null) {
            return 1;
        }
        return userService.userLogout(request);
    }

    @PostMapping("/search")
    public List<User> searchUsers(@RequestBody UserSearchRequest userSearchRequest, HttpServletRequest request) {

//        if (!isAdmin(request)) {
//            return new ArrayList<>();
//        }
//        username = request.getParameter("username");
        String username = userSearchRequest.getUsername();

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(username)) {
            queryWrapper.like("username", username);
        }

        List<User> userList = userService.list(queryWrapper);
        return userList.stream().map(user -> userService.getSafetyUser(user)).collect(Collectors.toList());
    }

    @PostMapping("/delete")
    public boolean deleteUsers(@RequestBody long id,HttpServletRequest request) {

        if (id <= 0) {
            return false;
        }
        return userService.removeById(id);
    }
    @PostMapping("/changeSex")
    public boolean changeSex(@RequestBody long id,HttpServletRequest request) {

        if (id <= 0) {
            return false;
        }
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("id", id);
        User user = userService.getOne(userQueryWrapper);

        if (user.getGender()==1){
            user.setGender(0);
        }else {
            user.setGender(1);
        }

        userService.updateById(user);

        return true;
    }



    private boolean isAdmin(HttpServletRequest request){
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        return user != null && user.getUserRole() == ADMIN_ROLE;
    }

}
