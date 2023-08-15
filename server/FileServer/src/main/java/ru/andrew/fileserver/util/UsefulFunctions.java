package ru.andrew.fileserver.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import jakarta.servlet.http.HttpServletRequest;

public class UsefulFunctions {

    public static boolean isAuthenticated(HttpServletRequest request, String jwtKey) {
        String tokenHeader = request.getHeader("authorization");
        if (tokenHeader == null) {
            return false;
        }
        String userToken = tokenHeader.split(" ")[1];
        try {
            Algorithm algorithm = Algorithm.HMAC256(jwtKey);
            JWTVerifier verifier = JWT.require(algorithm).withIssuer("auth0").build();
            verifier.verify(userToken);
            return true;
        } catch (JWTVerificationException exception) {
            return false;
        }
    }
}
