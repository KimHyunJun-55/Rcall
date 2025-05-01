package random.call.global.security.userDetails;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import random.call.domain.member.Member;

import java.util.Collection;
import java.util.List;

public record CustomUserDetails(Member member) implements UserDetails {

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getPassword() {
        return member.getPassword();
    }

    @Override
    public String getUsername() {
        return member.getUsername();
    }

    public String getMemberNickname() {
        return member.getNickname();
    }
}
