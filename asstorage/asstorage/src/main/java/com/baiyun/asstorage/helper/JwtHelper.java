package com.baiyun.asstorage.helper;

import io.jsonwebtoken.*;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

public class JwtHelper {
    //过期时间
    private static long tokenExpiration = 24*60*60*1000;
    //签名秘钥
    private static String tokenSignKey = "123456";

    public static String getUserEmail(HttpServletRequest request){
        //从header中获取token
        String token = request.getHeader("token");
        String emial = JwtHelper.getUserEmail(token);
        return emial;
    }

    //根据参数生成token
    public static String createToken(String email, String userName) {
        String token = Jwts.builder()
                .setSubject("YYGH-USER")
                .setExpiration(new Date(System.currentTimeMillis() + tokenExpiration))
                .claim("email", email)
                .claim("userName", userName)
                .signWith(SignatureAlgorithm.HS512, tokenSignKey)
                .compressWith(CompressionCodecs.GZIP)
                .compact();
        return token;
    }

    //根据token字符串得到用户id
    public static String getUserEmail(String token) {
        if(StringUtils.isEmpty(token)) return null;

        Jws<Claims> claimsJws = Jwts.parser().setSigningKey(tokenSignKey).parseClaimsJws(token);
        Claims claims = claimsJws.getBody();
//        Integer userId = (Integer)claims.get("userId");
//        return userId.longValue();
        return (String) claims.get("email");
    }

    //根据token字符串得到用户名称
    public static String getUserName(String token) {
        if(StringUtils.isEmpty(token)) return "";

        Jws<Claims> claimsJws = Jwts.parser().setSigningKey(tokenSignKey).parseClaimsJws(token);
        Claims claims = claimsJws.getBody();
        return (String)claims.get("userName");
    }
}