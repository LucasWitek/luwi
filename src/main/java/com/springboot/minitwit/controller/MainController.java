package com.springboot.minitwit.controller;

import com.springboot.minitwit.model.LoginResult;
import com.springboot.minitwit.model.Message;
import com.springboot.minitwit.model.User;
import com.springboot.minitwit.service.impl.MiniTwitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class MainController {
    private static final String USER_SESSION_ID = "user";

    @Autowired
    private MiniTwitService service;

    @GetMapping("/")
    public ModelAndView showTimeline(HttpServletRequest request) {
        User user = getAuthenticatedUser(request);
        if(user == null) {
            return new ModelAndView("redirect:/public");
        }
        Map<String, Object> map = new HashMap<>();
        map.put("pageTitle", "Timeline");
        map.put("user", user);
        List<Message> messages = service.getUserFullTimelineMessages(user);
        map.put("messages", messages);
        return new ModelAndView("timeline", map);
    }

    @GetMapping("/public")
    public ModelAndView showPublicTimeline(HttpServletRequest request) {
        User user = getAuthenticatedUser(request);
        Map<String, Object> map = new HashMap<>();
        map.put("pageTitle", "Public Timeline");
        map.put("user", user);
        List<Message> messages = service.getPublicTimelineMessages();
        map.put("messages", messages);
        return new ModelAndView("timeline", map);
    }

    @GetMapping("/t/{username}")
    public ModelAndView showUserTimeline(HttpServletRequest request, @PathVariable String username) {
        User profileUser = service.getUserbyUsername(username);

        if(profileUser == null) {
            return new ModelAndView("redirect:/error");
        }

        User authUser = getAuthenticatedUser(request);
        boolean followed = false;
        if(authUser != null) {
            followed = service.isUserFollower(authUser, profileUser);
        }
        List<Message> messages = service.getUserTimelineMessages(profileUser);

        Map<String, Object> map = new HashMap<>();
        map.put("pageTitle", username + "'s Timeline");
        map.put("user", authUser);
        map.put("profileUser", profileUser);
        map.put("followed", followed);
        map.put("messages", messages);
        return new ModelAndView("timeline", map);
    }

    @GetMapping("/t/{username}/follow")
    public ModelAndView addUserAsFollower(HttpServletRequest request, @PathVariable String username) {
        User profileUser = service.getUserbyUsername(username);
        User authUser = getAuthenticatedUser(request);

        if(authUser == null) {
            return new ModelAndView("redirect:/login");
        } else if(profileUser == null) {
            return new ModelAndView("redirect:/error");
        }

        service.followUser(authUser, profileUser);
        return new ModelAndView("redirect:/t/" + username);
    }

    @GetMapping("/t/{username}/unfollow")
    public ModelAndView removeUserAsFollower(HttpServletRequest request, @PathVariable String username) {
        User profileUser = service.getUserbyUsername(username);
        User authUser = getAuthenticatedUser(request);

        if(authUser == null) {
            return new ModelAndView("redirect:/login");
        } else if(profileUser == null) {
            return new ModelAndView("redirect:/error");
        }

        service.unfollowUser(authUser, profileUser);
        return new ModelAndView("redirect:/t/" + username);
    }

    @GetMapping("/login")
    public ModelAndView showLoginPage(HttpServletRequest request){
        Map<String, Object> map = new HashMap<>();
        if(request.getParameter("r") != null) {
            map.put("message", "You were successfully registered and can login now");
        }
        return new ModelAndView("login", map);
    }

    @PostMapping("/login")
    public ModelAndView signInUser(HttpServletRequest request, User user){
        User authUser = getAuthenticatedUser(request);
        if(authUser != null) {
            return new ModelAndView("redirect:/");
        }
        Map<String, Object> map = new HashMap<>();
        LoginResult result = service.checkUser(user);
        if(result.getUser() != null) {
            addAuthenticatedUser(request, result.getUser());
            return new ModelAndView("redirect:/");
        } else {
            map.put("error", result.getError());
        }
        map.put("username", user.getUsername());
        return new ModelAndView("login", map);
    }


    @GetMapping("/register")
    public ModelAndView showRegisterPage(HttpServletRequest request){
        Map<String, Object> map = new HashMap<>();
        return new ModelAndView("register", map);
    }

    @PostMapping("/register")
    public ModelAndView signUpUser(HttpServletRequest request, User user){
        User authUser = getAuthenticatedUser(request);
        if(authUser != null) {
            return new ModelAndView("redirect:/");
        }
        Map<String, Object> map = new HashMap<>();

        String error = user.validate();
        if(StringUtils.isEmpty(error)) {
            User existingUser = service.getUserbyUsername(user.getUsername());
            if(existingUser == null) {
                service.registerUser(user);
                return new ModelAndView("redirect:/login?r=1");
            } else {
                error = "The username is already taken";
            }
        }
        map.put("error", error);
        map.put("username", user.getUsername());
        map.put("email", user.getEmail());
        return new ModelAndView("register", map);
    }

    @PostMapping("/message")
    public ModelAndView registerNewMessage(HttpServletRequest request, Message message){
        User authUser = getAuthenticatedUser(request);
        if(authUser == null) {
            return new ModelAndView("redirect:/login");
        }
        message.setUserId(authUser.getId());
        message.setPubDate(new Date());
        service.addMessage(message);
        return new ModelAndView("redirect:/");
    }

    @GetMapping("/logout")
    public ModelAndView logoutUser(HttpServletRequest request){
        removeAuthenticatedUser(request);
        return new ModelAndView("redirect:/public");
    }

    private void addAuthenticatedUser(HttpServletRequest request, User user) {
        request.getSession().setAttribute(USER_SESSION_ID, user);

    }

    private void removeAuthenticatedUser(HttpServletRequest request) {

        request.getSession().removeAttribute(USER_SESSION_ID);

    }

    private User getAuthenticatedUser(HttpServletRequest request) {
        return (User)request.getSession().getAttribute(USER_SESSION_ID);
    }
}
