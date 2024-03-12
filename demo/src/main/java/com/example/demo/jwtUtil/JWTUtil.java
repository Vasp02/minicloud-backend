package com.example.demo.jwtUtil;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.demo.account.Account;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JWTUtil {
    @Value("${jwt.secret}")
    private String secretKey;

    public String createToken(Account account){
        String token = JWT.create()
                .withSubject(account.getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + 864_000_000)) // 10 days
                .sign(Algorithm.HMAC512(secretKey));
        return token;
    }

    public boolean isTokenValid(String jwtToken) {
        String token = jwtToken.startsWith("Bearer ") ? jwtToken.substring(7).trim() : jwtToken.trim();
        try {
            Algorithm algorithm = Algorithm.HMAC512(secretKey);
            JWTVerifier verifier = JWT.require(algorithm)
                    .build();
            DecodedJWT jwt = verifier.verify(token);

            if (jwt.getExpiresAt().before(new Date())) {
                System.out.println("Token expired");
                return false;
            }
            return true;

        } catch (JWTVerificationException exception) {
            System.out.println("Token invalid: " + exception.getMessage());
            return false;
        }
    }

    public String extractSubject(String jwtToken) {
        try {
            String token = jwtToken.startsWith("Bearer ") ? jwtToken.substring(7).trim() : jwtToken.trim();
            Algorithm algorithm = Algorithm.HMAC512(secretKey);
            DecodedJWT jwt = JWT.require(algorithm).build().verify(token);
            return jwt.getSubject();
        } catch (JWTVerificationException | IllegalArgumentException exception) {
            System.err.println("JWT Verification failed: " + exception.getMessage());
            return null;
        }
    }

}
