package org.mitre.web;

import org.mitre.openid.connect.client.OIDCAuthenticationProvider;
import org.mitre.openid.connect.model.PendingOIDCAuthenticationToken;
import org.mitre.openid.connect.model.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Igor Bereza
 */
public class CustomAuthenticationProvider extends OIDCAuthenticationProvider {
	private static final Logger logger = LoggerFactory.getLogger(CustomAuthenticationProvider.class);


	@Override
	protected Authentication createAuthenticationToken(PendingOIDCAuthenticationToken token, Collection<? extends GrantedAuthority> authorities, UserInfo userInfo) {
		logger.debug("auth ", authorities);
		logger.debug("user ", userInfo);

		GrantedAuthority grantedAuthority = new SimpleGrantedAuthority("ROLE_SUPER_USER");
		List<GrantedAuthority> tempList = new ArrayList<>();
		tempList.addAll(authorities);
		tempList.add(grantedAuthority);
		Authentication authenticationToken = super.createAuthenticationToken(token, tempList, userInfo);

		return authenticationToken;

	}
}
