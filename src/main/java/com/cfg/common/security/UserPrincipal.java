package com.cfg.common.security;

import com.cfg.user.domain.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Getter
public class UserPrincipal implements UserDetails {

    private final UUID id;
    private final UUID restaurantId;
    private final String email;
    private final String phone;
    private final String password;
    private final String role;
    private final boolean active;

    public UserPrincipal(User user) {
        this.id = user.getId();
        this.restaurantId = user.getRestaurantId();
        this.email = user.getEmail();
        this.phone = user.getPhone();
        this.password = user.getPasswordHash();
        this.role = user.getRole().name();
        this.active = user.isActive();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public String getPassword() { return password; }

    @Override
    public String getUsername() { return email != null ? email : phone; }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return active; }
}
