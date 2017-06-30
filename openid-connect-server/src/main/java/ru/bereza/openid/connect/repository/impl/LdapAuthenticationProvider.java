package ru.bereza.openid.connect.repository.impl;

import org.mitre.openid.connect.model.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Igor Bereza
 */
public class LdapAuthenticationProvider implements AuthenticationProvider {
	private LdapService ldapService;

	private static final Logger logger = LoggerFactory.getLogger(LdapAuthenticationProvider.class);

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) authentication;
		String login = (String) token.getPrincipal();
		String password = (String) token.getCredentials();
		UserInfo userInfo = ldapService.findByLdapLoginAndPassword(login, password);
		if (userInfo == null) {
			throw new BadCredentialsException("Login or password is incorrect!");
		}
		List<GrantedAuthority> authorityList = new ArrayList<>();
		authorityList.add(new SimpleGrantedAuthority("ROLE_USER"));
		UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
			login, password, authorityList
		);
		return authenticationToken;
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
	}

	public void setLdapService(LdapService ldapService) {
		this.ldapService = ldapService;
	}

}
