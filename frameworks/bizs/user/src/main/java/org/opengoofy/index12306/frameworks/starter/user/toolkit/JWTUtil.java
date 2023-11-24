/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opengoofy.index12306.frameworks.starter.user.toolkit;

import com.alibaba.fastjson2.JSON;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.opengoofy.index12306.frameworks.starter.user.core.UserInfoDTO;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.opengoofy.index12306.framework.starter.bases.constant.UserConstant.REAL_NAME_KEY;
import static org.opengoofy.index12306.framework.starter.bases.constant.UserConstant.USER_ID_KEY;
import static org.opengoofy.index12306.framework.starter.bases.constant.UserConstant.USER_NAME_KEY;

/**
 * JWT 工具类
 */
@Slf4j
public final class JWTUtil {

    private static final long EXPIRATION = 86400L;
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String ISS = "index12306";
    public static final String SECRET = "SecretKey039245678901232039487623456783092349288901402967890140939827";

    /**
     * 生成用户 Token
     *
     * @param userInfo 用户信息
     * @return 用户访问 Token
     */
    public static String generateAccessToken(UserInfoDTO userInfo) {
        // 创建一个用于存储用户信息的Map
        Map<String, Object> customerUserMap = new HashMap<>();
        // 将用户ID放入Map中
        customerUserMap.put(USER_ID_KEY, userInfo.getUserId());
        // 将用户名放入Map中
        customerUserMap.put(USER_NAME_KEY, userInfo.getUsername());
        // 将真实姓名放入Map中
        customerUserMap.put(REAL_NAME_KEY, userInfo.getRealName());
        // 使用JWT（JSON Web Token）构建令牌
        String jwtToken = Jwts.builder()
                // 使用HS512算法进行签名，密钥为SECRET
                .signWith(SignatureAlgorithm.HS512, SECRET)
                // 设置令牌的签发时间为当前时间
                .setIssuedAt(new Date())
                // 设置令牌的签发者为ISS
                .setIssuer(ISS)
                // 设置令牌的主题为存储用户信息的Map的JSON字符串表示
                .setSubject(JSON.toJSONString(customerUserMap))
                // 设置令牌的过期时间为当前时间加上EXPIRATION秒
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION * 1000))
                // 构建并获取紧凑的JWT字符串
                .compact();
        // 返回带有TOKEN_PREFIX前缀的JWT令牌
        return TOKEN_PREFIX + jwtToken;
    }

    /**
     * 解析用户 Token
     *
     * @param jwtToken 用户访问 Token
     * @return 用户信息
     */
    public static UserInfoDTO parseJwtToken(String jwtToken) {
        if (StringUtils.hasText(jwtToken)) {
            String actualJwtToken = jwtToken.replace(TOKEN_PREFIX, "");
            try {
                Claims claims = Jwts.parser().setSigningKey(SECRET).parseClaimsJws(actualJwtToken).getBody();
                Date expiration = claims.getExpiration();
                if (expiration.after(new Date())) {
                    String subject = claims.getSubject();
                    return JSON.parseObject(subject, UserInfoDTO.class);
                }
            } catch (ExpiredJwtException ignored) {
            } catch (Exception ex) {
                log.error("JWT Token解析失败，请检查", ex);
            }
        }
        return null;
    }
}