package com.example.moodtail.global.config.security.auth;

import com.example.moodtail.domain.user.entity.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@AllArgsConstructor
public class PrincipalDetails implements UserDetails {

	@Getter
	private final Long userId;
	private final UserRole role;

	public String getRole() {
		return role == null ? null : role.name();
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		if (role == null) {
			return Collections.emptyList();
		}
		return Collections.singletonList(new SimpleGrantedAuthority(role.toAuthority()));
	}

	@Override
	public String getPassword() {
		return "";
	}

	@Override
	public String getUsername() {
		return String.valueOf(userId);
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
