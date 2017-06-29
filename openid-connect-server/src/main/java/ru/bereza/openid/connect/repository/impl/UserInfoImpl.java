package ru.bereza.openid.connect.repository.impl;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.mitre.openid.connect.model.UserInfo;
import org.mitre.openid.connect.model.convert.JsonObjectStringConverter;

import javax.persistence.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * @author Igor Bereza
 */
public class UserInfoImpl implements UserInfo {

	public UserInfoImpl(String login, String fio, String sub) {
		this.login = login;
		this.fio = fio;
		this.sub = sub;
	}

	public UserInfoImpl() {
	}

	private static final long serialVersionUID = 6078310513185681918L;

	private String login;
	private String fio;
	private String sub;
	private transient JsonObject src; // source JSON if this is loaded remotely



	@Override
	public String getSub() {
		return sub;
	}

	@Override
	public void setSub(String sub) {
		this.sub = sub;
	}

	@Override
	public String getLogin() {
		return login;
	}

	@Override
	public void setLogin(String login) {
		this.login = login;
	}

	@Override
	public String getFio() {
		return fio;
	}

	@Override
	public void setFio(String fio) {
		this.fio = fio;
	}

	@Override
	public JsonObject toJson() {
		if (src == null) {

			JsonObject obj = new JsonObject();
			obj.addProperty("login", this.getLogin());

			obj.addProperty("fio", this.getFio());
			return obj;
		} else {
			return src;
		}

	}

	/**
	 * Parse a JsonObject into a UserInfo.
	 *
	 * @param o
	 * @return
	 */
	public static UserInfo fromJson(JsonObject obj) {
		UserInfoImpl ui = new UserInfoImpl();
		ui.setSource(obj);

		ui.setFio(nullSafeGetString(obj, "fio"));
		ui.setLogin(nullSafeGetString(obj, "login"));


		return ui;

	}

	/**
	 * @return the jsonString
	 */
	@Override
	@Basic
	@Column(name = "src")
	@Convert(converter = JsonObjectStringConverter.class)
	public JsonObject getSource() {
		return src;
	}

	/**
	 * @param jsonString the jsonString to set
	 */
	public void setSource(JsonObject src) {
		this.src = src;
	}


	private static String nullSafeGetString(JsonObject obj, String field) {
		return obj.has(field) && obj.get(field).isJsonPrimitive() ? obj.get(field).getAsString() : null;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((login == null) ? 0 : login.hashCode());
		result = prime * result + ((fio == null) ? 0 : fio.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof UserInfoImpl)) {
			return false;
		}
		UserInfoImpl other = (UserInfoImpl) obj;
		if (login == null) {
			if (other.login != null) {
				return false;
			}
		} else if (!login.equals(other.login)) {
			return false;
		}
		if (fio == null) {
			if (other.fio != null) {
				return false;
			}
		} else if (!fio.equals(other.fio)) {
			return false;
		}
		return true;
	}


	/*
	 * Custom serialization to handle the JSON object
	 */
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
		if (src == null) {
			out.writeObject(null);
		} else {
			out.writeObject(src.toString());
		}
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		Object o = in.readObject();
		if (o != null) {
			JsonParser parser = new JsonParser();
			src = parser.parse((String) o).getAsJsonObject();
		}
	}

}
