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

import io.urf.surf.parser.*;

import static java.util.Objects.*;

/**
 * An implementation for a {@link FileBasedConfiguration} that uses a SURF file to store information.
 * 
 * @author Magno N A Cruz
 */
public class SurfConfiguration extends BaseHierarchicalConfiguration implements FileBasedConfiguration {

	/** The root object where the properties will be added. */
	private SurfObject surfObject;

	@Override
	public void read(@Nonnull Reader in) throws ConfigurationException, IOException {
		final BufferedReader bufferedIn = new BufferedReader(requireNonNull(in));

		final Object surfDocument = new SurfParser().parse(bufferedIn).orElse(null);

		if(surfDocument instanceof SurfObject) {
			this.surfObject = (SurfObject)surfDocument;
		} else if(surfDocument == null) {
			clear(); //TODO implement clearInternal() to make this work, this have to verify if the surfObject is null, if so, it won't be touched, but if surfObject is not null, it'll only clean all the properties inside of it
		} else {
			throw new ConfigurationException("The root element of the configuration file must be a SurfObject");
		}

	}

	@Override
	public void write(@Nonnull Writer out) throws ConfigurationException, IOException {
		throw new UnsupportedOperationException("Serializer for SURF not yet implemented.");
	}

	@Override
	protected Object getPropertyInternal(@Nonnull String key) {
		return surfObject.getPropertyValue(requireNonNull(key, "The key to be retrieved from the configuration file cannot be <null>")).orElse(null);
	}

	@Override
	protected boolean isEmptyInternal() {
		if(surfObject == null) {
			return true;
		}

		return surfObject.getPropertyCount() == 0;
	}

}
