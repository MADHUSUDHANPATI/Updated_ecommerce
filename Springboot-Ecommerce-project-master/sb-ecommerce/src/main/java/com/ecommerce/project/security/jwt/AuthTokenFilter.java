package com.ecommerce.project.security.jwt;

import com.ecommerce.project.security.services.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class AuthTokenFilter extends OncePerRequestFilter {     //Intercepts every HTTP request and checks if a JWT token is present/ valid or not.

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    //UserDetailsService is an interface used to load user data (username, password, roles) from a database or other source during authentication.
    private UserDetailsServiceImpl userDetailsService;  //service that fetches user information for authentication.

    private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);
    @Override   // HttpServletRequest: It allows the server to read data from the incoming HTTP request.
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        logger.debug("AuthToken called for URI : {}", request.getRequestURI());  // a String containing the path part of the URL from after the authority[post] to before the query string.(?)

        try {
            String jwt = parseJwt(request);
            if(jwt!=null && jwtUtils.validateJWTToken(jwt)) {

                String username = jwtUtils.generateUsernameFromToken(jwt);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);   //  UserDetails = Object that holds authenticated user information.

//                null → The credentials (like password). Since you already validated the JWT, you don’t need to store the password here.
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(  // UsernamePasswordAuthenticationToken is a class used by Spring Security to represent an authentication object.
                        userDetails, null, userDetails.getAuthorities()
                );

                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)  // Attach request-related details (IP, session info) to the authentication object.
                );
                SecurityContextHolder.getContext().setAuthentication(authentication);  // SecurityContextHolder is a core class in Spring Security that stores the current authenticated user for the request.
                logger.debug("Roles from JWT : {}", userDetails.getAuthorities());   // Spring Security now considers the user authenticated.
            }

        } catch (Exception e) {

            logger.error("Cannot set the user authentication : {}", e);
        }
        filterChain.doFilter(request, response);// continue for other filer, after this filter done.
//        Above line is critical in a custom filter like your AuthTokenFilter. It tells Spring Security (and the servlet container) to continue processing the request through the rest of the filter chain after your filter has done its work.
    }

    private String parseJwt(HttpServletRequest request) {

        String jwt = jwtUtils.getJwtFromCookie(request);
        logger.debug("AuthToken filter .java :  {}", jwt);
        return jwt;
    }
}
