package com.Main.web.information;

import com.Main.entity.information.User;
import com.Main.service.information.UserService;

import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController // 使用 @RestController 来简化代码，自动返回 JSON
public class UserController {

    public static final String KEY_USER = "__user__";

    final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    UserService userService;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @GetMapping("/get_user")
    public ResponseEntity<User> search_user(@RequestParam("id") long id) {
        User user = userService.getUserById(id);
        System.out.println(user.toString());
        return ResponseEntity.ok(user); // 返回 JSON 数据
    }

}
