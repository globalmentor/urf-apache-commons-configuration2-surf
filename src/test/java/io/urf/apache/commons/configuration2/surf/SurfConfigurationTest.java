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
import org.junit.rules.TemporaryFolder;

import com.globalmentor.collections.*;

import io.urf.surf.parser.SurfObject;
import io.urf.surf.parser.SurfParser;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.nio.file.*;
import java.time.*;

/**
 * Tests to see if the {@link SurfConfiguration} is working correctly.
 * 
 * @author Magno N A Cruz
 */
public class SurfConfigurationTest {

	@Rule
	public final TemporaryFolder tempFolder = new TemporaryFolder();

	/**
	 * Test whether the configuration is working with a configuration file.
	 * 
	 * @throws ConfigurationException if any error occur while configuring the file.
	 * @throws IOException if an I/O error occur.
	 * @throws URISyntaxException if there's an error while trying to get an URI.
	 */
	@Test
	public void testWriteSurfConfiguration() throws ConfigurationException, IOException, URISyntaxException {
		final File configFile = tempFolder.newFile("serializer_configuration_file.surf");

		final FileBasedConfigurationBuilder<SurfConfiguration> configBuilder = new FileBasedConfigurationBuilder<SurfConfiguration>(SurfConfiguration.class)
				.configure(new Parameters().fileBased().setFile(configFile));

		final SurfConfiguration config = (SurfConfiguration)configBuilder.getConfiguration();

		assertThat(config.isEmpty(), is(true));

		config.addProperty("name", "Jane Doe");
		config.addProperty("account", "jane_doe@example.com");

		configBuilder.save();

		SurfObject surfDocument = (SurfObject)new SurfParser().parse(Files.newBufferedReader(configFile.toPath())).get();

		assertThat(surfDocument.getPropertyCount(), equalTo(2));

		assertThat(surfDocument.getPropertyValue("name").get(), equalTo("Jane Doe"));
		assertThat(surfDocument.getPropertyValue("account").get(), equalTo("jane_doe@example.com"));
	}

	/**
	 * Test whether an empty configuration is working with a configuration file.
	 * 
	 * @throws ConfigurationException if any error occur while configuring the file.
	 * @throws IOException if an I/O error occur.
	 * @throws URISyntaxException if there's an error while trying to get an URI.
	 */
	@Test
	public void testWriteEmptySurfConfiguration() throws ConfigurationException, IOException, URISyntaxException {
		final File configFile = tempFolder.newFile("serializer_empty_configuration_file.surf");

		final FileBasedConfigurationBuilder<SurfConfiguration> configBuilder = new FileBasedConfigurationBuilder<SurfConfiguration>(SurfConfiguration.class)
				.configure(new Parameters().fileBased().setFile(configFile));

		final SurfConfiguration config = (SurfConfiguration)configBuilder.getConfiguration();

		assertThat(config.isEmpty(), is(true));

		configBuilder.save();

		SurfObject surfDocument = (SurfObject)new SurfParser().parse(Files.newBufferedReader(configFile.toPath())).get();

		assertThat(surfDocument.getPropertyCount(), equalTo(0));

		assertThat(surfDocument.getTypeName().get(), equalTo("Config"));
	}

	/**
	 * Test whether the configuration is working with an empty file.
	 * 
	 * @throws ConfigurationException if any error occur while configuring the file.
	 */
	@Test
	public void testReadEmptySurfFile() throws ConfigurationException {
		final File configFile = new File(this.getClass().getResource("empty_file.surf").getFile());

		final Configuration config = new FileBasedConfigurationBuilder<SurfConfiguration>(SurfConfiguration.class)
				.configure(new Parameters().fileBased().setFile(configFile)).getConfiguration();

		assertThat(config.isEmpty(), is(true));
	}

	/**
	 * Test whether the configuration is working with an empty configuration file.
	 * 
	 * @throws ConfigurationException if any error occur while configuring the file.
	 */
	@Test
	public void testReadEmptySurfConfiguration() throws ConfigurationException {
		final File configFile = new File(this.getClass().getResource("empty_configuration_file.surf").getFile());

		final Configuration config = new FileBasedConfigurationBuilder<SurfConfiguration>(SurfConfiguration.class)
				.configure(new Parameters().fileBased().setFile(configFile)).getConfiguration();

		assertThat(config.isEmpty(), is(true));
	}

	/**
	 * Test whether the configuration is working with properties of the type {@link String}.
	 * 
	 * @throws ConfigurationException if any error occur while configuring the file.
	 */
	@Test
	public void testReadSurfConfigurationWithStringProperties() throws ConfigurationException {
		final File configFile = new File(this.getClass().getResource("configuration_file_strings.surf").getFile());

		final Configuration config = new FileBasedConfigurationBuilder<SurfConfiguration>(SurfConfiguration.class)
				.configure(new Parameters().fileBased().setFile(configFile)).getConfiguration();

		assertThat(config.isEmpty(), is(false));

		assertThat(config.getProperty("name"), is("Jane"));
		assertThat(config.getProperty("lastName"), is("Doe"));
	}

	/**
	 * Test whether the configuration is throwing an exception if the resource isn't a valid configuration file.
	 * 
	 * @throws ConfigurationException if any error occur while configuring the file.
	 */
	@Test(expected = ConfigurationException.class)
	public void testReadInvalidSurfConfiguration() throws ConfigurationException {
		final File configFile = new File(this.getClass().getResource("invalid_configuration_file.surf").getFile());

		new FileBasedConfigurationBuilder<SurfConfiguration>(SurfConfiguration.class).configure(new Parameters().fileBased().setFile(configFile))
				.getConfiguration();
	}

	/**
	 * Test whether the configuration is working with properties of every type when the root object has no type.
	 * 
	 * @throws ConfigurationException if any error occur while configuring the file.
	 * @throws URISyntaxException if there's an error while trying to get an URI.
	 */
	@Test
	public void testReadSurfConfigurationWithoutType() throws ConfigurationException, URISyntaxException {
		final File configFile = new File(this.getClass().getResource("configuration_file_no_type.surf").getFile());

		final Configuration config = new FileBasedConfigurationBuilder<SurfConfiguration>(SurfConfiguration.class)
				.configure(new Parameters().fileBased().setFile(configFile)).getConfiguration();

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
	 * Test whether the configuration is working with properties of every type.
	 * 
	 * @throws ConfigurationException if any error occur while configuring the file.
	 * @throws URISyntaxException if there's an error while trying to get an URI.
	 */
	@Test
	public void testReadSurfConfiguration() throws ConfigurationException, URISyntaxException {
		final File configFile = new File(this.getClass().getResource("configuration_file.surf").getFile());

		final Configuration config = new FileBasedConfigurationBuilder<SurfConfiguration>(SurfConfiguration.class)
				.configure(new Parameters().fileBased().setFile(configFile)).getConfiguration();

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

}
