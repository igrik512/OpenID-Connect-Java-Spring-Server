package ru.bereza.openid.connect.repository.impl;

import org.mitre.openid.connect.model.DefaultUserInfo;
import org.mitre.openid.connect.model.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.AuthenticationSource;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.ldap.query.LdapQueryBuilder;

import javax.annotation.PostConstruct;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import java.util.List;
import java.util.UUID;

/**
 * @author Igor Bereza
 */
public class LdapService {
	public static final String ACCOUNT_NAME_ATTRIBUTE = "sAMAccountName";
	public static final String GIVEN_NAME_ATTRIBUTE = "givenName";
	public static final String SN_ATTRIBUTE = "sn";
	public static final String OBJECT_CLASS_ATTRIBUTE = "objectClass";
	public static final String PERSON_OBJECT_CLASS = "person";

	private String accountNameAttribute = ACCOUNT_NAME_ATTRIBUTE;
	private String firstNameAttribute = GIVEN_NAME_ATTRIBUTE;
	private String lastNameAttribute = SN_ATTRIBUTE;
	private String objectClassAttribute = OBJECT_CLASS_ATTRIBUTE;
	private String personObjectClass = PERSON_OBJECT_CLASS;

	private static final Logger logger = LoggerFactory.getLogger(LdapService.class);

	private LdapTemplate template;

	private String ldapUrl;
	private String ldapLogin;
	private String ldapPassword;
	private String ldapBase;
	private static final String LDAP_DOMAIN = "";

	private AuthenticationSource staticAutheticationSource;

	public String getLdapBase() {
		return ldapBase;
	}

	public void setLdapBase(String ldapBase) {
		this.ldapBase = ldapBase;
	}

	public String getLdapUrl() {
		return ldapUrl;
	}

	public void setLdapUrl(String ldapUrl) {
		this.ldapUrl = ldapUrl;
	}

	public String getLdapLogin() {
		return ldapLogin;
	}

	public void setLdapLogin(String ldapLogin) {
		this.ldapLogin = ldapLogin;
	}

	public String getLdapPassword() {
		return ldapPassword;
	}

	public void setLdapPassword(String ldapPassword) {
		this.ldapPassword = ldapPassword;
	}

	private LdapContextSource createContextSource(AuthenticationSource authenticationSource) {
		LdapContextSource contextSource = new LdapContextSource();
		contextSource.setUrl(ldapUrl);
		contextSource.setBase(ldapBase);
		contextSource.setAuthenticationSource(authenticationSource);
		contextSource.afterPropertiesSet();
		return contextSource;
	}

	@PostConstruct
	public void init() {
		template = new LdapTemplate(createContextSource(new SimpleAuthenticationSource(ldapLogin, ldapPassword)));
		staticAutheticationSource = new SimpleAuthenticationSource(ldapLogin, ldapPassword);
	}


	private String createUsernameFilter(String username) {
		AndFilter filter = new AndFilter();
		filter
			.and(new EqualsFilter(accountNameAttribute, username))
			.and(new EqualsFilter(objectClassAttribute, personObjectClass));

		return filter.encode();
	}

	public UserInfo findByLogin(String username) {
		return findByLogin(username, createLdapTemplate(ldapLogin, ldapPassword));
	}


	public UserInfo findByLdapLoginAndPassword(String username, String password) {
		return findByLogin(username, createLdapTemplate(username, password));
	}

	private UserInfo findByLogin(String username, LdapTemplate template) {
		List<UserInfo> searchResult;
		try {
			searchResult =
				template.search(buildLdapQuery(username), new UserInfoMapper());
		} catch (Exception ex) {
			logger.error("", ex);
			return null;
		}

		if (!searchResult.isEmpty()) {
			return searchResult.get(0);
		}
		return null;
	}

	private class UserInfoMapper implements AttributesMapper<UserInfo> {
		public UserInfo mapFromAttributes(Attributes attributes) throws NamingException {
			DefaultUserInfo userInfo = new DefaultUserInfo();
			userInfo.setLogin(getValue(attributes, accountNameAttribute));
			userInfo.setFio(getValue(attributes, lastNameAttribute) + " " + getValue(attributes, firstNameAttribute));
			userInfo.setSub(UUID.randomUUID().toString());
			//todo IGBE refactor
			return userInfo;
		}

		private String getValue(Attributes attributes, String name) {
			try {
				return (String) attributes.get(name).get();
			} catch (Exception ignore) {
				return "";
			}
		}
	}

	private LdapQuery buildLdapQuery(String username) {
		return LdapQueryBuilder.query().filter(createUsernameFilter(username));
	}


	private LdapTemplate createLdapTemplate(ContextSource ldapContextSource) {
		LdapTemplate template = new LdapTemplate(ldapContextSource);
		template.setIgnorePartialResultException(true);

		return template;
	}

	private LdapTemplate createLdapTemplate(String username, String password) {
		String login = LDAP_DOMAIN + "\\" + username;
		AuthenticationSource authenticationSource = new SimpleAuthenticationSource(login, password);
		LdapContextSource ldapContextSource = createContextSource(authenticationSource);
		return createLdapTemplate(ldapContextSource);
	}


	class SimpleAuthenticationSource implements AuthenticationSource {
		String username;
		String password;

		private SimpleAuthenticationSource(String username, String password) {
			this.username = username;
			this.password = password;
		}


		@Override
		public String getPrincipal() {
			return username;
		}

		@Override
		public String getCredentials() {
			return password;
		}
	}

}
