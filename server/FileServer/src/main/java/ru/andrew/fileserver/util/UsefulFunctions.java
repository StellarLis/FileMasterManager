package ru.andrew.fileserver.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.http.HttpServletRequest;

public class UsefulFunctions {
    public static DecodedJWT isAuthenticated(
            HttpServletRequest request,
            String jwtKey
    ) {
        String tokenHeader = request.getHeader("authorization");
        if (tokenHeader == null) {
            return null;
        }
        String token = tokenHeader.split(" ")[1];
        Algorithm algorithm = Algorithm.HMAC256(jwtKey);
        JWTVerifier verifier = JWT.require(algorithm).withIssuer("auth0").build();
        try {
            DecodedJWT decodedJWT = verifier.verify(token);
            return decodedJWT;
        } catch (JWTVerificationException exception) {
            return null;
        }
    }
}