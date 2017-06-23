/*******************************************************************************
 * Copyright 2017 The MIT Internet Trust Consortium
 *
 * Portions copyright 2011-2013 The MITRE Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package org.mitre.openid.connect.model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.mitre.openid.connect.model.convert.JsonObjectStringConverter;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Entity
@Table(name = "user_info")
@NamedQueries({
	@NamedQuery(name = DefaultUserInfo.QUERY_BY_USERNAME, query = "select u from DefaultUserInfo u WHERE u.login = :" + DefaultUserInfo.PARAM_USERNAME),
})
public class DefaultUserInfo implements UserInfo {

	public static final String QUERY_BY_USERNAME = "DefaultUserInfo.getByUsername";

	public static final String PARAM_USERNAME = "login";

	private static final long serialVersionUID = 6078310513185681918L;


	private transient JsonObject src; // source JSON if this is loaded remotely

	/**
	 * @return the id
	 */
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "id")
	public Long getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	private String login;
	private String fio;
	private String sub;
	private Long id;

	


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
		DefaultUserInfo ui = new DefaultUserInfo();
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
		if (!(obj instanceof DefaultUserInfo)) {
			return false;
		}
		DefaultUserInfo other = (DefaultUserInfo) obj;
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
