package ru.bereza.openid.connect.repository.impl;

import org.mitre.openid.connect.model.UserInfo;
import org.mitre.openid.connect.repository.UserInfoRepository;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Igor Bereza
 */

public class LdapUserInfoRepository implements UserInfoRepository {

	private Map<String, UserInfo> userInfoCash = new HashMap<>();

	public LdapService getLdapService() {
		return ldapService;
	}

	public void setLdapService(LdapService ldapService) {
		this.ldapService = ldapService;
	}

	private LdapService ldapService;

	@Override
	public UserInfo getByUsername(String username) {
		//cash results from ldap
		if (userInfoCash.get(username) == null) {
			UserInfo userInfo = ldapService.findByLdapLogin(username);
			userInfoCash.put(username, userInfo);
		}
		return userInfoCash.get(username);
	}
}
