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

import org.apache.commons.configuration2.*;
import org.apache.commons.configuration2.builder.*;
import org.apache.commons.configuration2.builder.fluent.*;
import org.apache.commons.configuration2.ex.*;

import org.junit.*;

import com.globalmentor.collections.*;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.net.*;
import java.nio.file.*;
import java.time.*;

import javax.annotation.*;

/**
 * Tests to see if the {@link SurfConfiguration} is working correctly.
 * 
 * @author Magno N A Cruz
 */
public class SurfConfigurationTest {

	/**
	 * Test whether the configuration is working with an empty file.
	 * 
	 * @throws ConfigurationException if any error occur while configuring the file.
	 * @throws URISyntaxException if there's an error while trying to get the URI of the configuration file.
	 */
	@Test
	public void testEmptySurfFile() throws ConfigurationException, URISyntaxException {
		final Path configPath = Paths.get(this.getClass().getResource("empty_file.surf").toURI());

		final Configuration config = createConfiguration(configPath);

		assertThat(config.isEmpty(), is(true));
	}

	/**
	 * Test whether the configuration is working with an empty configuration file.
	 * 
	 * @throws ConfigurationException if any error occur while configuring the file.
	 * @throws URISyntaxException if there's an error while trying to get the URI of the configuration file.
	 */
	@Test
	public void testEmptySurfConfiguration() throws ConfigurationException, URISyntaxException {
		final Path configPath = Paths.get(this.getClass().getResource("empty_configuration_file.surf").toURI());

		final Configuration config = createConfiguration(configPath);

		assertThat(config.isEmpty(), is(true));
	}

	/**
	 * Test whether the configuration is working with properties of the type {@link String}.
	 * 
	 * @throws ConfigurationException if any error occur while configuring the file.
	 * @throws URISyntaxException if there's an error while trying to get the URI of the configuration file.
	 */
	@Test
	public void testSurfConfigurationWithStringProperties() throws ConfigurationException, URISyntaxException {
		final Path configPath = Paths.get(this.getClass().getResource("strings_configuration_file.surf").toURI());

		final Configuration config = createConfiguration(configPath);

		assertThat(config.isEmpty(), is(false));

		assertThat(config.getProperty("name"), is("Jane"));
		assertThat(config.getProperty("lastName"), is("Doe"));
	}
	
	/**
	 * Test whether the configuration is working with properties of every type.
	 * 
	 * @throws ConfigurationException if any error occur while configuring the file.
	 * @throws URISyntaxException if there's an error while trying to get the URI of the configuration file.
	 */
	@Test
	public void testSurfConfiguration() throws ConfigurationException, URISyntaxException {
		final Path configPath = Paths.get(this.getClass().getResource("configuration_file.surf").toURI());

		final Configuration config = createConfiguration(configPath);

		assertThat(config.isEmpty(), is(false));

		//TODO add UUID
		assertThat(config.getProperty("authenticated"), is(true));
		assertThat(config.getProperty("sort"), equalTo('d'));
		assertThat(config.getProperty("name"), equalTo("Jane Doe"));
		assertThat(config.getProperty("account"), equalTo("jane_doe@example.com"));
		assertThat(config.getProperty("aliases"), equalTo(Collections.createHashSet("jdoe", "janed")));
		assertThat(config.getProperty("homePage"), equalTo(new URI("http://www.example.com/jdoe/")));
		assertThat(config.getProperty("salt"), equalTo(new byte[] {102, 111, 111, 98, 97, 114}));
		assertThat(config.getProperty("joined"), equalTo(LocalDate.parse("2016-01-23")));
		assertThat(config.getProperty("credits"), equalTo(123));
	}

	/**
	 * Method to help with the creation of the {@link SurfConfiguration}.
	 * 
	 * @param configPath The {@link Path} of the file that will be used to store the configurations.
	 * 
	 * @return The {@link SurfConfiguration} ready to be used.
	 * @throws ConfigurationException if any error occur while configuring the file.
	 */
	private Configuration createConfiguration(@Nonnull final Path configPath) throws ConfigurationException {
		return new FileBasedConfigurationBuilder<SurfConfiguration>(SurfConfiguration.class).configure(new Parameters().fileBased().setFile(configPath.toFile()))
				.getConfiguration();
	}

}
