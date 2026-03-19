package com.maplestone.dataCollect.common.scurity;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;

import com.maplestone.dataCollect.common.constant.SecurityConst;

import java.util.Date;

/**
 * @description:
 * @Author hmx
 * @CreateTime 2021-06-25 9:50
 */

@Component
public class JwtUtil {

    /**
     * 生成token
     * 
     * @param subject 用户id
     * @return
     */
    public static String createToken(String subject) {
        Date nowDate = new Date();
        Date expireDate = new Date(nowDate.getTime() + SecurityConst.TOKEN_EXPIRE * 1000);
        return Jwts.builder()
                .setHeaderParam("typ", "JWT")
                .setSubject(subject)
                .setIssuedAt(nowDate)
                .setExpiration(expireDate)
                .signWith(SignatureAlgorithm.HS512, SecurityConst.TOKEN_SECRET)
                .compact();
    }

    /**
     * 获取token中注册信息
     * 
     * @param token
     * @return
     */
    public static Claims getTokenClaim(String token) {
        try {
            return Jwts.parser().setSigningKey(SecurityConst.TOKEN_SECRET).parseClaimsJws(token).getBody();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 验证token是否过期失效
     * 
     * @param expirationTime
     * @return
     */
    public static boolean isTokenExpired(Date expirationTime) {
        return expirationTime.before(new Date());
    }

    /**
     * 获取token失效时间
     * 
     * @param token
     * @return
     */
    public static Date getExpirationDateFromToken(String token) {
        return getTokenClaim(token).getExpiration();
    }

    /**
     * 获取用户名从token中
     */
    public static String getUserFromToken(String token) {
        return getTokenClaim(token).getSubject();
    }

    /**
     * 获取jwt发布时间
     */
    public static Date getIssuedAtDateFromToken(String token) {
        return getTokenClaim(token).getIssuedAt();
    }

}
