package com.ecommerce.project.security.jwt;

import com.ecommerce.project.security.services.UserDetailsImpl;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtils {       // Handles all JWT operations.

    @Value("${spring.app.expirationTimeMs}")
    private int expirationTimeMS;

    @Value("${spring.app.secretToken}")
    private String secretToken;

    @Value("${spring.ecom.app.jwtCookieName}")
    private String jwtCookie;

    //Generate / get JWT from headers
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);
//    public String getJWTFromHeader(HttpServletRequest request) {
//
//        String bearerToken = request.getHeader("Authorization");
//        logger.debug("Authorization Header: {}", bearerToken);
//        if(bearerToken!=null && bearerToken.startsWith("Bearer ")) {
//
//            return bearerToken.substring(7);  //Remove Bearer prefix
//        }
//        return null;
//    }

    public String getJwtFromCookie(HttpServletRequest request) {

        Cookie cookie= WebUtils.getCookie(request, jwtCookie);
        if (cookie != null) {
            return cookie.getValue();
        } else {
            return null;
        }
    }


    public ResponseCookie generatejwtCookie(UserDetailsImpl userDetails) {              // We can use refresh tokens , these are stored in db.
        String jwt = generateTokenFromUserName(userDetails.getUsername());
        ResponseCookie cookie = ResponseCookie.from(jwtCookie, jwt)
                .path("/api")
                .maxAge(24* 60* 60)
                .httpOnly(true)  // false --> true;
                .build();
        return cookie;
    }

    public ResponseCookie getCleanCookie() {
        ResponseCookie cookie = ResponseCookie.from(jwtCookie, null)
                .path("/api")
                .build();
        return cookie;
    }
    //Generate token from the userName

    public String generateTokenFromUserName(String username) {
//        String username = userDetails.getUsername();
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(new Date().getTime()+ expirationTimeMS))
                .signWith(key())
                .compact();
    }

    // Generate username from token

    public String generateUsernameFromToken (String token) {

        return Jwts.parser()
                .verifyWith((SecretKey) key())
                .build().parseSignedClaims(token)
                .getPayload().getSubject();
    }
    //Generate signing key

    public Key key() {

        return Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(secretToken)
        );
    }


    //JWT Validation

    public boolean validateJWTToken(String authToken) {

       try {
           System.out.println("validate");
           Jwts.parser()
                   .verifyWith((SecretKey) key())
                   .build().parseSignedClaims(authToken);
           return true;
       }
       catch (MalformedJwtException e) {
           logger.error("Invalid JWT Token : {}", e.getMessage());
       }
       catch (ExpiredJwtException e) {
           logger.error("JWT Token expired : {}", e.getMessage());
       }
       catch (UnsupportedJwtException e) {
           logger.error("JWT Token unsupported : {}", e.getMessage());
       }
       catch (IllegalArgumentException e) {
           logger.error("JWT claims string is empty {}", e.getMessage());
       }
       return false;
    }
}
