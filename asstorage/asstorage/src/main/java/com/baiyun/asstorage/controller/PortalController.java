package com.baiyun.asstorage.controller;

import com.baiyun.asstorage.helper.JwtHelper;
import com.baiyun.asstorage.service.PortalService;
import com.baiyun.asstorage.service.UserService;
import com.baiyun.asstorage.vo.CheckVO;
import io.netty.util.internal.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@RestController
@CrossOrigin
public class PortalController {

    @Resource
    private PortalService portalService;

    @Resource
    private UserService userService;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    public static Logger logger = LoggerFactory.getLogger(PortalController.class);

    @GetMapping("/as_check/{asPath}")
    public @ResponseBody
    CheckVO checkAsPath(@PathVariable String asPath, HttpServletRequest request){
        String userEmail = JwtHelper.getUserEmail(request);
        return portalService.singleCheck(asPath, userEmail);
    }

    @GetMapping("/send/{phone}")
    public boolean phone(@PathVariable String phone){
        String code = redisTemplate.opsForValue().get(phone);

        if (!StringUtil.isNullOrEmpty(code)){
            return true;
        }
        code = new DecimalFormat("000000").format(new Random().nextInt(1000000));

        boolean ifSend = userService.send(phone,code);
        if (ifSend){
            redisTemplate.opsForValue().set(phone,code,2, TimeUnit.MINUTES);
        }
        return ifSend;
    }

    @GetMapping("/login/{phone}/{code}")
    public Map<String,Object> login(@PathVariable String phone, @PathVariable String code){
        return userService.login(phone, code);
    }

    @PostMapping("/batchCheck/{email}")
    public @ResponseBody
    List<CheckVO> batchCheck(MultipartFile file, @PathVariable String email){
        assert file.getOriginalFilename().isEmpty();
        return portalService.bactchCheck(file,email);
    }

    @GetMapping("/user/getHistory")
    public List<CheckVO> getHistory(HttpServletRequest request){
        String userEmail = JwtHelper.getUserEmail(request);
        return userService.findAll(userEmail);
    }
}
