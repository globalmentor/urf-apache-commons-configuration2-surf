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

import com.globalmentor.java.Conditions;

import io.urf.surf.parser.*;
import io.urf.surf.serializer.SurfSerializer;

import static java.util.Objects.*;

/**
 * An implementation for a {@link FileBasedConfiguration} that uses a SURF file to store information.
 * 
 * @author Magno N A Cruz
 */
public class SurfConfiguration extends BaseHierarchicalConfiguration implements FileBasedConfiguration {

	/** Constant for the default root element name. */
	private static final String DEFAULT_ROOT_NAME = "Config";

	/** The root object where the properties will be added. */
	private SurfObject surfObject;

	@Override
	public void read(@Nonnull Reader in) throws ConfigurationException, IOException {

		try (final BufferedReader bufferedIn = new BufferedReader(requireNonNull(in))) {
			final Object surfDocument = new SurfParser().parse(bufferedIn).orElse(null);

			if(surfDocument instanceof SurfObject) {
				this.surfObject = (SurfObject)surfDocument;
			} else {
				this.surfObject = new SurfObject(DEFAULT_ROOT_NAME);
			}
		}

	}

	@Override
	public void write(@Nonnull Writer out) throws ConfigurationException, IOException {

		try (final BufferedWriter bufferedOut = new BufferedWriter(requireNonNull(out))) {
			final SurfSerializer serializer = new SurfSerializer();

			serializer.setFormatted(true);

			bufferedOut.write(serializer.serialize(surfObject));
		}

	}

	@Override
	protected Object getPropertyInternal(@Nonnull String key) {
		return surfObject.getPropertyValue(requireNonNull(key, "The key to be retrieved from the configuration file cannot be <null>.")).orElse(null);
	}

	@Override
	protected void addPropertyInternal(@Nonnull String key, @Nullable Object obj) {
		Conditions.checkArgument(key != null, "The key of the property being added cannot be <null>.");

		surfObject.setPropertyValue(key, obj);
	}

	@Override
	protected void clearPropertyDirect(String key) {
		surfObject.setPropertyValue(key, null);
	}

	@Override
	protected boolean isEmptyInternal() {
		return surfObject == null || size() == 0;
	}

	@Override
	protected int sizeInternal() {
		if(surfObject == null) {
			return 0;
		}

		return surfObject.getPropertyCount();
	}

}
