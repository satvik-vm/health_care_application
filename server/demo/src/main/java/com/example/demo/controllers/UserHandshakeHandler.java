package com.example.demo.controllers;

import com.sun.security.auth.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;
import org.springframework.web.util.UriComponentsBuilder;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;

@CrossOrigin(origins = "*")
public class UserHandshakeHandler extends DefaultHandshakeHandler {
    private final Logger LOG = LoggerFactory.getLogger(UserHandshakeHandler.class);

//    @Override
//    public void afterConnectionEstablished(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
//        // Get the email from the headers
//        String email = request.getHeaders().getFirst("Email");
//
//        // Store the email in the attributes map
//        attributes.put("email", email);
//
//        super.afterConnectionEstablished(request, response, wsHandler, attributes);
//    }

//    @Override
//    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
////        final String randomId = UUID.randomUUID().toString();
////        LOG.info("User with ID '{}' opened the page", randomId);
////
////        return new UserPrincipal(randomId);
//        String email = request.getHeaders().getFirst("Email");
//
//        // If the email is not found in the headers, try to get it from the parameters
//        if (email == null) {
//            email = request.getURI().getQuery();
//        }
//
//        // If the email is still not found, generate a random ID
//        if (email == null) {
//            email = UUID.randomUUID().toString();
//        }
//
//        LOG.info("User with email '{}' opened the page", email);
//
//        return new UserPrincipal(email);
//    }

    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        // Extract the email from the URL query parameters
        MultiValueMap<String, String> parameters = UriComponentsBuilder.fromUri(request.getURI()).build().getQueryParams();
        String email = parameters.getFirst("email");

        // If the email is not found, generate a random ID
        if (email == null) {
            email = UUID.randomUUID().toString();
        }

        LOG.info("User with email '{}' opened the page. ", email);

        return new UserPrincipal(email);
    }
}
