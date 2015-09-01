/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2015 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.db.internal;

import java.util.Arrays;
import java.util.List;

import org.hibernate.proxy.HibernateProxy;

import com.axelor.db.Model;
import com.axelor.db.annotations.HashKey;
import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;
import com.axelor.db.mapper.PropertyType;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/**
 * This class provides helper methods for model objects.
 *
 */
public final class EntityHelper {

	private static boolean isSimple(Property field) {
		if (field.isTransient() || field.isPrimary() || field.isVersion()
				|| field.isCollection() || field.isReference()
				|| field.isVirtual() || field.isImage()
				|| field.getType() == PropertyType.TEXT
				|| field.getType() == PropertyType.BINARY) {
			return false;
		}
		return true;
	}

	/**
	 * The toString helper method.
	 *
	 * @param entity
	 *            generate toString for the given entity
	 * @return string
	 */
	public static <T extends Model> String toString(T entity) {
		if (entity == null) {
			return null;
		}
		final Mapper mapper = Mapper.of(entity.getClass());
		final MoreObjects.ToStringHelper helper = MoreObjects.toStringHelper(entity);

		helper.add("id", entity.getId());
		for (Property field : mapper.getProperties()) {
			if (isSimple(field)) {
				helper.add(field.getName(), field.get(entity));
			}
		}
		return helper.omitNullValues().toString();
	}

	/**
	 * The hashCode helper method.
	 *
	 * This method searches for all the fields marked with {@link HashKey} and
	 * uses them to generate the hash code.
	 *
	 * @param entity
	 *            generate the hashCode for the given entity
	 * @return hashCode
	 */
	public static <T extends Model> int hashCode(T entity) {
		if (entity == null) {
			return 0;
		}
		final Mapper mapper = Mapper.of(entity.getClass());
		final List<Object> values = Lists.newArrayList();

		for (Property p : mapper.getProperties()) {
			if (isSimple(p) && p.isHashKey()) {
				values.add(p.get(entity));
			}
		}

		if (values.isEmpty()) {
			return 0;
		}
		return Arrays.hashCode(values.toArray());
	}

	/**
	 * The equals helper method.
	 *
	 * This method searches for all the fields marked with {@link HashKey} and
	 * uses them to check for the equality.
	 *
	 * @param entity
	 *            the current entity
	 * @param other
	 *            the other entity
	 *
	 * @return true if both the objects are equals by their hashing keys
	 */
	public static <T extends Model> boolean equals(T entity, Object other) {
		if (entity == other)
			return true;
		if (entity == null || other == null)
			return false;
		if (!entity.getClass().isInstance(other))
			return false;

		final Model that = (Model) other;
		if (entity.getId() != null || that.getId() != null) {
			return Objects.equal(entity.getId(), that.getId());
		}

		final Mapper mapper = Mapper.of(entity.getClass());
		boolean hasHashKeys = false;

		for (Property field : mapper.getProperties()) {
			if (field.isHashKey()) {
				hasHashKeys = true;
				if (!Objects.equal(field.get(entity), field.get(other))) {
					return false;
				}
			}
		}

		return !hasHashKeys;
	}

	/**
	 * Get the real persistence class of the given entity.
	 * 
	 * <p>
	 * This method can be used to find real class name of a proxy object
	 * returned by hibernate entity manager.
	 * </p>
	 * 
	 * @param entity
	 *            an entity instance
	 * @return real class of the entity
	 */
	@SuppressWarnings("unchecked")
	public static <T> Class<T> getEntityClass(T entity) {
		Preconditions.checkNotNull(entity);
		if (entity instanceof HibernateProxy) {
			return ((HibernateProxy) entity).getHibernateLazyInitializer().getPersistentClass();
		}
		return (Class<T>) entity.getClass();
	}

	/**
	 * Get unproxied instance of the given entity.
	 * 
	 * <p>
	 * This method can be used to convert hibernate proxy object to real
	 * implementation instance.
	 * </p>
	 * 
	 * @param entity
	 *            proxied entity
	 * @return unproxied instance of the entity
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getEntity(T entity) {
		if (entity == null) {
			return null;
		}
		if (entity instanceof HibernateProxy) {
			return (T) ((HibernateProxy) entity).getHibernateLazyInitializer().getImplementation();
		}
		return entity;
	}

	/**
	 * Check whether the given lazy loading proxy instance is uninitialized.
	 * 
	 * @param entity
	 *            the lazy loading entity instance
	 * @return true if uninitialized false otherwise
	 */
	public static <T> boolean isUninitialized(T entity) {
		return entity instanceof HibernateProxy
				&& ((HibernateProxy) entity).getHibernateLazyInitializer()
						.isUninitialized();
	}
}
