/*
 * Copyright © 2017 GlobalMentor, Inc. <http://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.urf.apache.commons.configuration2.surf;

import java.io.*;

import javax.annotation.*;

import org.apache.commons.configuration2.BaseHierarchicalConfiguration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.FileHandler;

import com.globalmentor.java.Conditions;

import io.urf.surf.parser.*;
import io.urf.surf.serializer.SurfSerializer;

import static java.util.Objects.*;

/**
 * An implementation for a {@link FileBasedConfiguration} that uses a SURF file to store information.
 * 
 * <p>
 * If a SURF configuration file is created by an instance of this class, it will be composed by a root {@link SurfObject} with the type name defined as
 * <code>"Configuration"</code>, and every property added to this {@link SurfConfiguration} instance will be a child of this {@link SurfObject}.
 * </p>
 * 
 * i.e.,
 * 
 * <pre>
 * *Configuration:
 *   property1 = value1
 *   property2 = value2
 *   property3 = value3
 * ;
 * </pre>
 * 
 * <p>
 * The SURF document serialized will always be formatter. See {@link SurfSerializer#setFormatted(boolean)}.
 * </p>
 * 
 * @author Magno N A Cruz
 */
public class SurfConfiguration extends BaseHierarchicalConfiguration implements FileBasedConfiguration {

	/** Constant for the default root object type name. */
	private static final String DEFAULT_ROOT_NAME = "Configuration";

	/** The root object where the properties will be added. */
	private SurfObject surfDocument;

	/**
	 * {@inheritDoc}
	 * 
	 * <p>
	 * If an empty file is provided to the {@link FileHandler}, then we create the root object using {@value #DEFAULT_ROOT_NAME} as the type name.
	 * </p>
	 * 
	 * @throws ConfigurationException if the root element is not an instance of {@link SurfObject} or if the {@link Reader} provided refers to a non-existing
	 *           file.
	 */
	@Override
	public void read(@Nonnull Reader in) throws ConfigurationException, IOException {

		try (final BufferedReader bufferedIn = new BufferedReader(requireNonNull(in))) {
			final Object surfDocument = new SurfParser().parse(bufferedIn).orElse(null);

			if(surfDocument instanceof SurfObject && ((SurfObject)surfDocument).getPropertyCount() != 0) {
				this.surfDocument = (SurfObject)surfDocument;
			} else if(surfDocument == null || ((SurfObject)surfDocument).getPropertyCount() == 0) {
				this.surfDocument = new SurfObject(DEFAULT_ROOT_NAME);
			} else {
				throw new ConfigurationException("The element on the file is not a valid SURF configuration file.");
			}
		}

	}

	@Override
	public void write(@Nonnull Writer out) throws ConfigurationException, IOException {

		try (final BufferedWriter bufferedOut = new BufferedWriter(requireNonNull(out))) {
			final SurfSerializer serializer = new SurfSerializer();

			serializer.setFormatted(true);

			bufferedOut.write(serializer.serialize(surfDocument));
		}

	}

	@Override
	protected Object getPropertyInternal(@Nonnull String key) {
		return surfDocument.getPropertyValue(requireNonNull(key, "The key to be retrieved from the configuration file cannot be <null>.")).orElse(null);
	}

	@Override
	protected void addPropertyInternal(@Nonnull String key, @Nullable Object obj) {
		Conditions.checkArgument(key != null, "The key of the property being added cannot be <null>.");

		surfDocument.setPropertyValue(key, obj);
	}

	@Override
	protected void clearPropertyDirect(String key) {
		surfDocument.setPropertyValue(key, null);
	}

	@Override
	protected boolean isEmptyInternal() {
		return surfDocument == null || size() == 0;
	}

	@Override
	protected int sizeInternal() {
		if(surfDocument == null) {
			return 0;
		}

		return surfDocument.getPropertyCount();
	}

	/**
	 * Returns the SURF document of this configuration.
	 *
	 * @return The SURF document this configuration or <code>null</code> if it wasn't loaded from a file.
	 */
	public SurfObject getSurfDocument() {
		return this.surfDocument;
	}

}
