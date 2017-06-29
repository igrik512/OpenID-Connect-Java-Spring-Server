package ru.bereza.openid.connect.repository.impl;

import org.mitre.openid.connect.model.DefaultUserInfo;
import org.mitre.openid.connect.model.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;

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

	private LdapContextSource contextSource() {
		LdapContextSource contextSource = new LdapContextSource();
		contextSource.setUrl(ldapUrl);
		contextSource.setBase(ldapBase);
		contextSource.setUserDn(ldapLogin);
		contextSource.setPassword(ldapPassword);
		contextSource.afterPropertiesSet();
		return contextSource;
	}

	@PostConstruct
	public void init() {
		template = new LdapTemplate(contextSource());
	}


	private String createUsernameFilter(String username) {
		AndFilter filter = new AndFilter();
		filter
			.and(new EqualsFilter(accountNameAttribute, username))
			.and(new EqualsFilter(objectClassAttribute, personObjectClass));

		return filter.encode();
	}


	//todo IGBE refactor to find
	private UserInfo findByIdentity(String username, LdapTemplate template) {
		List result = template.search(DistinguishedName.EMPTY_PATH, createUsernameFilter(username), new UserInfoMapper(username));

		if (result.size() > 0) {
			return (UserInfo) result.get(0);
		}

		return null;
	}

	public UserInfo findByLdapLogin(String username) {
		List result = template.search(DistinguishedName.EMPTY_PATH, createUsernameFilter(username), new UserInfoMapper(username));

		if (result.size() > 0) {
			return (UserInfo) result.get(0);
		}

		return null;
	}

	private class UserInfoMapper implements AttributesMapper {
		private String login;

		private UserInfoMapper(String login) {
			this.login = login;
		}

		public UserInfo mapFromAttributes(Attributes attributes) throws NamingException {
			DefaultUserInfo userInfo = new DefaultUserInfo();
			userInfo.setLogin(login);
			userInfo.setFio(getValue(attributes, lastNameAttribute) + " " + getValue(attributes, firstNameAttribute));
			userInfo.setSub(UUID.randomUUID().toString());
			//todo IGBE refactor
			return userInfo;
		}

		protected String getValue(Attributes attributes, String name) {
			try {
				return (String) attributes.get(name).get();
			} catch (Exception ignore) {
				return "";
			}
		}
	}
}
