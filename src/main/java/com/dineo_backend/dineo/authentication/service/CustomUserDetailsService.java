package com.dineo_backend.dineo.authentication.service;

import com.dineo_backend.dineo.authentication.model.User;
import com.dineo_backend.dineo.authentication.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Custom UserDetailsService implementation for Spring Security
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        return UserPrincipal.create(user);
    }

    /**
     * Custom UserDetails implementation
     */
    public static class UserPrincipal implements UserDetails {
        private UUID id;
        private String firstName;
        private String lastName;
        private String email;
        private String password;
        private Collection<? extends GrantedAuthority> authorities;

        public UserPrincipal(UUID id, String firstName, String lastName, String email, String password, 
                            Collection<? extends GrantedAuthority> authorities) {
            this.id = id;
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
            this.password = password;
            this.authorities = authorities;
        }

        public static UserPrincipal create(User user) {
            // For now, we'll use a default CUSTOMER role since the User entity doesn't seem to have roles directly
            // You may need to modify this based on how you handle roles in your application
            List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER"));

            return new UserPrincipal(
                    user.getId(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getEmail(),
                    "N/A", // No password in passwordless authentication
                    authorities
            );
        }

        public UUID getId() { return id; }
        public String getFirstName() { return firstName; }
        public String getLastName() { return lastName; }
        public String getEmail() { return email; }

        @Override
        public String getUsername() { return email; }

        @Override
        public String getPassword() { return password; }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }

        @Override
        public boolean isAccountNonExpired() { return true; }

        @Override
        public boolean isAccountNonLocked() { return true; }

        @Override
        public boolean isCredentialsNonExpired() { return true; }

        @Override
        public boolean isEnabled() { return true; }
    }
}