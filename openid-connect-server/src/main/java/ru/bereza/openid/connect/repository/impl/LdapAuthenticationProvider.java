package ru.bereza.openid.connect.repository.impl;

import org.mitre.openid.connect.repository.UserInfoRepository;
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

	private UserInfoRepository ldapUserInfoRepository;

	private LdapService ldapService;

	private static final Logger logger = LoggerFactory.getLogger(LdapAuthenticationProvider.class);

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) authentication;
		logger.debug("{} ", token);

		String login = (String) token.getPrincipal();
		String password = (String) token.getCredentials();

		if (!login.equals("berezaim")) {
			throw new BadCredentialsException("Failed");
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
}
