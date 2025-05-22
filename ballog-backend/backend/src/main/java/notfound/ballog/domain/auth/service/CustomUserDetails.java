package notfound.ballog.domain.auth.service;

import notfound.ballog.domain.auth.entity.Auth;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

public class CustomUserDetails implements UserDetails {

    private final Auth auth;

    public CustomUserDetails(Auth auth) {
        this.auth = auth;
    }

    public Auth getAuth() {
        return this.auth;
    }

    public Integer getAuthId() {
        return this.auth.getAuthId();
    }

    public UUID getUserId() {
        return auth.getUser().getUserId();
    }

    // 추후 권한 추가되면 설정
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(() -> "ROLE_USER");
    }

    @Override
    public String getPassword() {
        return auth.getPassword();
    }

    @Override
    public String getUsername() {
        return auth.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
