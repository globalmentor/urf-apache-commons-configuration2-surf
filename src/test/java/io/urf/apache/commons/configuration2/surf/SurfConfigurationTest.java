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
import com.globalmentor.java.CodePointCharacter;

import io.urf.surf.parser.SurfObject;
import io.urf.surf.parser.SurfParser;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.nio.file.*;
import java.time.*;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

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
		final File configFile = tempFolder.newFile("serializer-configuration-file.surf");

		final FileBasedConfigurationBuilder<SurfConfiguration> configBuilder = new FileBasedConfigurationBuilder<SurfConfiguration>(SurfConfiguration.class)
				.configure(new Parameters().fileBased().setFile(configFile));

		final SurfConfiguration config = (SurfConfiguration)configBuilder.getConfiguration();

		assertThat(config.isEmpty(), is(true));

		config.addProperty("name", "Jane Doe");
		config.addProperty("account", "jane_doe@example.com");

		configBuilder.save();

		SurfObject surfDocument = (SurfObject)new SurfParser().parse(Files.newBufferedReader(configFile.toPath())).get();

		assertThat(config.size(), equalTo(2));

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
		final File configFile = tempFolder.newFile("serializer-empty-configuration-file.surf");

		final FileBasedConfigurationBuilder<SurfConfiguration> configBuilder = new FileBasedConfigurationBuilder<SurfConfiguration>(SurfConfiguration.class)
				.configure(new Parameters().fileBased().setFile(configFile));

		final SurfConfiguration config = (SurfConfiguration)configBuilder.getConfiguration();

		assertThat(config.isEmpty(), is(true));

		configBuilder.save();

		SurfObject surfDocument = (SurfObject)new SurfParser().parse(Files.newBufferedReader(configFile.toPath())).get();

		assertThat(surfDocument.getPropertyCount(), equalTo(0));

		assertThat(surfDocument.getTypeName().get(), equalTo("Configuration"));
	}

	/**
	 * Test whether the configuration is working with an empty file.
	 * 
	 * @throws ConfigurationException if any error occur while configuring the file.
	 */
	@Test
	public void testReadEmptySurfFileWithTypeName() throws ConfigurationException {
		final File configFile = new File(this.getClass().getResource("empty-file.surf").getFile());

		final SurfConfiguration config = new FileBasedConfigurationBuilder<SurfConfiguration>(SurfConfiguration.class)
				.configure(new Parameters().fileBased().setFile(configFile)).getConfiguration();

		assertThat(config.isEmpty(), is(true));

		assertThat(((SurfObject)config.getSurfDocument()).getTypeName().get(), equalTo("Configuration"));
	}

	/**
	 * Test whether the configuration is working with an empty file.
	 * 
	 * @throws ConfigurationException if any error occur while configuring the file.
	 */
	@Test
	public void testReadEmptySurfFileWithoutTypeName() throws ConfigurationException {
		final File configFile = new File(this.getClass().getResource("empty-configuration-file.surf").getFile());

		final SurfConfiguration config = new FileBasedConfigurationBuilder<SurfConfiguration>(SurfConfiguration.class)
				.configure(new Parameters().fileBased().setFile(configFile)).getConfiguration();

		assertThat(config.isEmpty(), is(true));

		assertThat(((SurfObject)config.getSurfDocument()).getTypeName(), equalTo(Optional.empty()));
		assertThat(((SurfObject)config.getSurfDocument()).getIri(), equalTo(Optional.empty()));
	}

	/**
	 * Test whether the configuration is working with an empty configuration file.
	 * 
	 * @throws ConfigurationException if any error occur while configuring the file.
	 */
	@Test
	public void testReadEmptySurfConfiguration() throws ConfigurationException {
		final File configFile = new File(this.getClass().getResource("empty-configuration-file.surf").getFile());

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
	public void testReadEmptySurfConfigurationFromMap() throws ConfigurationException {
		final File configFile = new File(this.getClass().getResource("map-based-empty-configuration-file.surf").getFile());

		final Configuration config = new FileBasedConfigurationBuilder<SurfConfiguration>(SurfConfiguration.class)
				.configure(new Parameters().fileBased().setFile(configFile)).getConfiguration();

		assertThat(config.isEmpty(), is(true));

		assertThat(((SurfConfiguration)config).getSurfDocument(), instanceOf(Map.class));
	}

	/**
	 * Test whether the configuration is throwing an exception if a link for a non-existing file is provided to it.
	 * 
	 * @throws ConfigurationException if a non-existing file is provided to the {@link ConfigurationBuilder}, this is what we expect.
	 */
	@Test(expected = ConfigurationException.class)
	public void testReadNonExistentSurfFile() throws ConfigurationException {
		final String configPath = Paths.get(tempFolder.getRoot().getPath()).resolve("non-existing-configuration-file.surf").toString();

		new FileBasedConfigurationBuilder<SurfConfiguration>(SurfConfiguration.class).configure(new Parameters().fileBased().setPath(configPath))
				.getConfiguration();
	}

	/**
	 * Test whether the configuration is working with properties of the type {@link String}.
	 * 
	 * @throws ConfigurationException if any error occur while configuring the file.
	 */
	@Test
	public void testReadSurfConfigurationWithStringProperties() throws ConfigurationException {
		final File configFile = new File(this.getClass().getResource("configuration-file-strings.surf").getFile());

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
		final File configFile = new File(this.getClass().getResource("invalid-configuration-file.surf").getFile());

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
		final File configFile = new File(this.getClass().getResource("configuration-file-without-type.surf").getFile());

		final Configuration config = new FileBasedConfigurationBuilder<SurfConfiguration>(SurfConfiguration.class)
				.configure(new Parameters().fileBased().setFile(configFile)).getConfiguration();

		assertThat(config.isEmpty(), is(false));

		assertThat(config.getProperty("authenticated"), is(true));
		assertThat(config.getProperty("sort"), equalTo(CodePointCharacter.of('d')));
		assertThat(config.getProperty("name"), equalTo("Jane Doe"));
		assertThat(config.getProperty("account"), equalTo("jane_doe@example.com"));
		assertThat(config.getProperty("id"), equalTo(UUID.fromString("bb8e7dbe-f0b4-4d94-a1cf-46ed0e920832")));
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
		final File configFile = new File(this.getClass().getResource("configuration-file.surf").getFile());

		final Configuration config = new FileBasedConfigurationBuilder<SurfConfiguration>(SurfConfiguration.class)
				.configure(new Parameters().fileBased().setFile(configFile)).getConfiguration();

		assertThat(config.isEmpty(), is(false));

		assertThat(config.getProperty("authenticated"), is(true));
		assertThat(config.getProperty("sort"), equalTo(CodePointCharacter.of('d')));
		assertThat(config.getProperty("name"), equalTo("Jane Doe"));
		assertThat(config.getProperty("account"), equalTo("jane_doe@example.com"));
		assertThat(config.getProperty("id"), equalTo(UUID.fromString("bb8e7dbe-f0b4-4d94-a1cf-46ed0e920832")));
		assertThat(config.getProperty("aliases"), equalTo(Collections.createHashSet("jdoe", "janed")));
		assertThat(config.getProperty("homePage"), equalTo(new URI("http://www.example.com/jdoe/")));
		assertThat(config.getProperty("salt"), equalTo(new byte[] {102, 111, 111, 98, 97, 114}));
		assertThat(config.getProperty("joined"), equalTo(LocalDate.parse("2016-01-23")));
		assertThat(config.getProperty("credits"), equalTo(123));

		assertThat(config.size(), equalTo(10));
	}

	/**
	 * Test whether the configuration is working with properties of every type when the root object is a representation of a map.
	 * 
	 * @throws ConfigurationException if any error occur while configuring the file.
	 * @throws URISyntaxException if there's an error while trying to get an URI.
	 */
	@Test
	public void testReadSurfConfigurationFromMap() throws ConfigurationException, URISyntaxException {
		final File configFile = new File(this.getClass().getResource("map-based-configuration-file.surf").getFile());

		final Configuration config = new FileBasedConfigurationBuilder<SurfConfiguration>(SurfConfiguration.class)
				.configure(new Parameters().fileBased().setFile(configFile)).getConfiguration();

		assertThat(config.isEmpty(), is(false));

		assertThat(config.getProperty("authenticated"), is(true));
		assertThat(config.getProperty("sort"), equalTo(CodePointCharacter.of('d')));
		assertThat(config.getProperty("name"), equalTo("Jane Doe"));
		assertThat(config.getProperty("account"), equalTo("jane_doe@example.com"));
		assertThat(config.getProperty("id"), equalTo(UUID.fromString("bb8e7dbe-f0b4-4d94-a1cf-46ed0e920832")));
		assertThat(config.getProperty("aliases"), equalTo(Collections.createHashSet("jdoe", "janed")));
		assertThat(config.getProperty("homePage"), equalTo(new URI("http://www.example.com/jdoe/")));
		assertThat(config.getProperty("salt"), equalTo(new byte[] {102, 111, 111, 98, 97, 114}));
		assertThat(config.getProperty("joined"), equalTo(LocalDate.parse("2016-01-23")));
		assertThat(config.getProperty("credits"), equalTo(123));

		assertThat(config.size(), equalTo(10));
	}

	/**
	 * Test whether the configuration is clearing properties properly.
	 * 
	 * @throws ConfigurationException if any error occur while configuring the file.
	 * @throws URISyntaxException if there's an error while trying to get an URI.
	 */
	@Test
	public void testSurfConfigurationClearProperty() throws ConfigurationException, URISyntaxException {
		final File configFile = new File(this.getClass().getResource("configuration-file.surf").getFile());

		final Configuration config = new FileBasedConfigurationBuilder<SurfConfiguration>(SurfConfiguration.class)
				.configure(new Parameters().fileBased().setFile(configFile)).getConfiguration();

		assertThat(config.isEmpty(), is(false));

		assertThat(config.getProperty("authenticated"), is(true));

		assertThat(config.size(), equalTo(10));

		config.clearProperty("authenticated");

		assertThat(config.getProperty("authenticated"), equalTo(null));

		assertThat(config.size(), equalTo(9));

		assertThat(config.getProperty("sort"), equalTo(CodePointCharacter.of('d')));
		assertThat(config.getProperty("name"), equalTo("Jane Doe"));
		assertThat(config.getProperty("account"), equalTo("jane_doe@example.com"));
		assertThat(config.getProperty("id"), equalTo(UUID.fromString("bb8e7dbe-f0b4-4d94-a1cf-46ed0e920832")));
		assertThat(config.getProperty("aliases"), equalTo(Collections.createHashSet("jdoe", "janed")));
		assertThat(config.getProperty("homePage"), equalTo(new URI("http://www.example.com/jdoe/")));
		assertThat(config.getProperty("salt"), equalTo(new byte[] {102, 111, 111, 98, 97, 114}));
		assertThat(config.getProperty("joined"), equalTo(LocalDate.parse("2016-01-23")));
		assertThat(config.getProperty("credits"), equalTo(123));

		config.clear();

		assertThat(config.isEmpty(), is(true));
		assertThat(config.size(), equalTo(0));
	}

	/**
	 * Test whether {@link SurfConfiguration#getProperty(String)} is working properly based on the hierarchy properties to look for nodes in lower levels.
	 * 
	 * @throws ConfigurationException if any error occur while configuring the file.
	 * @throws URISyntaxException if there's an error while trying to get an URI.
	 */
	@Test
	public void testReadSurfConfigurationHierarchy() throws ConfigurationException, URISyntaxException {
		final File configFile = new File(this.getClass().getResource("configuration-file-hierarchy.surf").getFile());

		final Configuration config = new FileBasedConfigurationBuilder<SurfConfiguration>(SurfConfiguration.class)
				.configure(new Parameters().fileBased().setFile(configFile)).getConfiguration();

		assertThat(config.isEmpty(), is(false));

		assertThat(config.size(), equalTo(11));

		assertThat(config.getProperty("authenticated"), is(true));
		assertThat(config.getProperty("sort"), equalTo(CodePointCharacter.of('d')));

		final SurfObject nameObject = new SurfObject("Name");
		nameObject.setPropertyValue("firstName", "Jane");
		nameObject.setPropertyValue("lastName", "Doe");

		assertThat(config.getProperty("name"), equalTo(nameObject));
		assertThat(config.getProperty("name.firstName"), equalTo("Jane"));
		assertThat(config.getProperty("name.lastName"), equalTo("Doe"));

		assertThat(config.getProperty("account"), equalTo("jane_doe@example.com"));
		assertThat(config.getProperty("id"), equalTo(UUID.fromString("bb8e7dbe-f0b4-4d94-a1cf-46ed0e920832")));
		assertThat(config.getProperty("aliases"), equalTo(Collections.createHashSet("jdoe", "janed")));
		assertThat(config.getProperty("homePage"), equalTo(new URI("http://www.example.com/jdoe/")));
		assertThat(config.getProperty("salt"), equalTo(new byte[] {102, 111, 111, 98, 97, 114}));
		assertThat(config.getProperty("joined"), equalTo(LocalDate.parse("2016-01-23")));
		assertThat(config.getProperty("credits"), equalTo(123));
	}

	/**
	 * Test whether the configuration is clearing properties properly with lower levels on the hierarchy.
	 * 
	 * @throws ConfigurationException if any error occur while configuring the file.
	 * @throws URISyntaxException if there's an error while trying to get an URI.
	 */
	@Test
	public void testSurfConfigurationHierarchyClearProperty() throws ConfigurationException, URISyntaxException {
		final File configFile = new File(this.getClass().getResource("configuration-file-hierarchy.surf").getFile());

		final Configuration config = new FileBasedConfigurationBuilder<SurfConfiguration>(SurfConfiguration.class)
				.configure(new Parameters().fileBased().setFile(configFile)).getConfiguration();

		assertThat(config.isEmpty(), is(false));

		assertThat(config.size(), equalTo(11));

		assertThat(config.getProperty("authenticated"), is(true));

		config.clearProperty("authenticated");

		assertThat(config.getProperty("authenticated"), equalTo(null));

		assertThat(config.size(), equalTo(10));

		assertThat(config.getProperty("sort"), equalTo(CodePointCharacter.of('d')));
		assertThat(config.getProperty("name.firstName"), equalTo("Jane"));
		assertThat(config.getProperty("name.lastName"), equalTo("Doe"));

		config.clearProperty("name.lastName");

		assertThat(config.getProperty("name.firstName"), equalTo("Jane"));
		assertThat(config.getProperty("name.lastName"), equalTo(null));

		assertThat(config.size(), equalTo(9));

		assertThat(config.getProperty("account"), equalTo("jane_doe@example.com"));
		assertThat(config.getProperty("id"), equalTo(UUID.fromString("bb8e7dbe-f0b4-4d94-a1cf-46ed0e920832")));
		assertThat(config.getProperty("aliases"), equalTo(Collections.createHashSet("jdoe", "janed")));
		assertThat(config.getProperty("homePage"), equalTo(new URI("http://www.example.com/jdoe/")));
		assertThat(config.getProperty("salt"), equalTo(new byte[] {102, 111, 111, 98, 97, 114}));
		assertThat(config.getProperty("joined"), equalTo(LocalDate.parse("2016-01-23")));
		assertThat(config.getProperty("credits"), equalTo(123));

		config.clear();

		assertThat(config.isEmpty(), is(true));

		assertThat(config.size(), equalTo(0));
	}

	/**
	 * Test whether the configuration is returning a property in a correct type, when it's asked.
	 * 
	 * @throws ConfigurationException if any error occur while configuring the file.
	 * @throws URISyntaxException if there's an error while trying to get an URI.
	 */
	@Test
	public void testGetSurfPropertyInCorrectType() throws ConfigurationException, URISyntaxException {
		final File configFile = new File(this.getClass().getResource("configuration-file.surf").getFile());

		final Configuration config = new FileBasedConfigurationBuilder<SurfConfiguration>(SurfConfiguration.class)
				.configure(new Parameters().fileBased().setFile(configFile)).getConfiguration();

		assertThat(config.isEmpty(), is(false));

		assertThat(config.get(boolean.class, "authenticated"), is(true));
		assertThat(config.get(CodePointCharacter.class, "sort"), equalTo(CodePointCharacter.of('d')));
		assertThat(config.get(String.class, "name"), equalTo("Jane Doe"));
		assertThat(config.getProperty("id"), equalTo(UUID.fromString("bb8e7dbe-f0b4-4d94-a1cf-46ed0e920832")));
		//assertThat(config.get(HashSet.class, "aliases"), equalTo(Collections.createHashSet("jdoe", "janed"))); HashSet.class isn't compatible to this property value, why?
		assertThat(config.get(URI.class, "homePage"), equalTo(new URI("http://www.example.com/jdoe/")));
		assertThat(config.get(byte[].class, "salt"), equalTo(new byte[] {102, 111, 111, 98, 97, 114}));
		assertThat(config.get(LocalDate.class, "joined"), equalTo(LocalDate.parse("2016-01-23")));
		assertThat(config.get(int.class, "credits"), equalTo(123));
	}

	/**
	 * Test whether the configuration is returning a property in a different, compatible type, when it's asked.
	 * 
	 * @throws ConfigurationException if any error occur while configuring the file.
	 * @throws URISyntaxException if there's an error while trying to get an URI.
	 */
	@Test
	public void testGetSurfPropertyInCompatibleType() throws ConfigurationException, URISyntaxException {
		final File configFile = new File(this.getClass().getResource("configuration-file.surf").getFile());

		final Configuration config = new FileBasedConfigurationBuilder<SurfConfiguration>(SurfConfiguration.class)
				.configure(new Parameters().fileBased().setFile(configFile)).getConfiguration();

		assertThat(config.isEmpty(), is(false));

		assertThat(config.get(String.class, "joined"), equalTo("2016-01-23"));
	}

	/**
	 * Test whether the configuration is returning a property in a different, non-compatible type, when it's asked.
	 * 
	 * @throws ConfigurationException if any error occur while configuring the file.
	 * @throws URISyntaxException if there's an error while trying to get an URI.
	 */
	@Test(expected = ConversionException.class)
	public void testGetSurfPropertyInNonCompatibleType() throws ConfigurationException, URISyntaxException {
		final File configFile = new File(this.getClass().getResource("configuration-file.surf").getFile());

		final Configuration config = new FileBasedConfigurationBuilder<SurfConfiguration>(SurfConfiguration.class)
				.configure(new Parameters().fileBased().setFile(configFile)).getConfiguration();

		assertThat(config.isEmpty(), is(false));

		assertThat(config.get(Instant.class, "joined"), equalTo("2016-01-23"));
	}

}
