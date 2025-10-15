package com.darum.ng.auth_service.service.impl;

//import com.darum.ng.auth.model.User;
//import com.darum.ng.auth.repository.UserRepository;
//import com.darum.ng.auth.security.model.UserPrincipal;

import com.darum.ng.auth_service.entity.User;
import com.darum.ng.auth_service.entity.UserPrincipal;
import com.darum.ng.auth_service.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

   private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return new UserPrincipal(user);
    }
}