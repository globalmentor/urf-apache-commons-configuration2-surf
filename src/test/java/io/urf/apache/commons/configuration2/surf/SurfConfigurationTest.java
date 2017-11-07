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
import com.globalmentor.collections.Collections;
import com.globalmentor.java.CodePointCharacter;

import io.urf.surf.*;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.time.*;
import java.util.*;

/**
 * Tests to see if the {@link SurfConfiguration} is working correctly.
 * 
 * @author Magno N A Cruz
 */
public class SurfConfigurationTest {

	@Rule
	public final TemporaryFolder tempFolder = new TemporaryFolder();

	/**
	 * Test whether the configuration is working with a configuration file. This test also sees if {@link SurfConfiguration#addProperty(String, Object)} is
	 * working properly and not adding duplicated objects.
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

		final SurfObject contactInfoSurfObject = new SurfObject("ContactInformation");
		contactInfoSurfObject.setPropertyValue("email", "jane_doe@example.com");
		contactInfoSurfObject.setPropertyValue("phone", "+12015550123");

		config.addProperty("contactInfo", contactInfoSurfObject);

		final Map<String, String> favoriteThingsMap = new HashMap<String, String>();

		favoriteThingsMap.put("5", "User's favorite number.");
		favoriteThingsMap.put("aliquot", "User's favorite word.");

		config.addProperty("favoriteThings", favoriteThingsMap);

		final List<String> favoriteColorsList = Arrays.asList("red", "orange", "yellow", "green", "blue", "indigo", "violet");

		config.addProperty("favoriteColors", favoriteColorsList);

		assertThat(config.getProperty("favoriteColors"), equalTo(favoriteColorsList));

		final Set<String> aliasesSet = Sets.immutableSetOf("jdoe", "janed");

		config.addProperty("aliases", aliasesSet);

		assertThat(config.getProperty("aliases"), equalTo(aliasesSet));

		configBuilder.save();

		SurfObject surfDocument = (SurfObject)new SurfParser().parse(Files.newBufferedReader(configFile.toPath())).get();

		assertThat(config.size(), equalTo(9));

		assertThat(surfDocument.getPropertyValue("name").get(), equalTo("Jane Doe"));
		assertThat(surfDocument.getPropertyValue("contactInfo").get(), equalTo(contactInfoSurfObject));
		assertThat(surfDocument.getPropertyValue("favoriteThings").get(), equalTo(favoriteThingsMap));
		assertThat(surfDocument.getPropertyValue("favoriteColors").get(), equalTo(favoriteColorsList));
		assertThat(surfDocument.getPropertyValue("aliases").get(), equalTo(aliasesSet));
	}

	/**
	 * Test whether {@link SurfConfiguration#setProperty(String, Object)} is working properly.
	 * 
	 * @throws ConfigurationException if any error occur while configuring the file.
	 * @throws IOException if an I/O error occur.
	 * @throws URISyntaxException if there's an error while trying to get an URI.
	 */
	@Test
	public void testSetProperty() throws ConfigurationException, IOException, URISyntaxException {
		final FileBasedConfigurationBuilder<SurfConfiguration> configBuilder = new FileBasedConfigurationBuilder<SurfConfiguration>(SurfConfiguration.class)
				.configure(new Parameters().fileBased());

		final SurfConfiguration config = (SurfConfiguration)configBuilder.getConfiguration();

		assertThat(config.isEmpty(), is(true));

		config.addProperty("name", "Jane Dee");

		assertThat(config.getProperty("name"), equalTo("Jane Dee"));

		config.addProperty("name", "Jane Doe"); //this call should override the previous value of "name".

		assertThat(config.getProperty("name"), equalTo("Jane Doe"));

		config.addProperty("name", null); //this call should remove the property.

		//Tests with properties in the root node.

		{ //tests with SurfObjects
			final SurfObject userSurfObject = new SurfObject("User");
			userSurfObject.setPropertyValue("name", "Jane Dee");
			userSurfObject.setPropertyValue("email", "jane_doe@example.com");
			userSurfObject.setPropertyValue("phone", "+12015550123");

			config.addProperty("user", userSurfObject);

			assertThat(config.getProperty("user"), equalTo(userSurfObject));
		}

		{ //tests with Maps
			final Map<String, String> favoriteThingsMap = new HashMap<String, String>();

			favoriteThingsMap.put("5", "User's favorite number.");
			favoriteThingsMap.put("aliquot", "User's favorite word.");

			config.addProperty("favoriteThings", favoriteThingsMap);

			assertThat(config.getProperty("favoriteThings"), equalTo(favoriteThingsMap));
		}

		{ //Test with Lists
			final List<String> favoriteColorsList = Arrays.asList("red", "orange", "yellow", "green", "blue", "indigo", "violet");

			config.addProperty("favoriteColors", favoriteColorsList);

			assertThat(config.getProperty("favoriteColors"), equalTo(favoriteColorsList));
		}

		{ //Test with Sets
			final Set<String> aliasesSet = Sets.immutableSetOf("jdoe", "janed");

			config.addProperty("aliases", aliasesSet);

			assertThat(config.getProperty("aliases"), equalTo(aliasesSet));
		}

		config.clear();

		//Tests whether properties in lower levels are fully navigable.

		{ //tests with Maps
			final Map<String, Object> favoriteThingsMap = new HashMap<String, Object>();

			final List<SurfObject> favoriteColorsList = Lists.listOf(new ArrayList<SurfObject>(), new SurfObject("Color"), new SurfObject("Color"),
					new SurfObject("Color"), new SurfObject("Color"), new SurfObject("Color"), new SurfObject("Color"));

			final Set<String> aliasesSet = Sets.immutableSetOf("jdoe", "janed");

			config.addProperty("favoriteThings", favoriteThingsMap);

			config.addProperty("favoriteThings.aliquot", "User's favorite word.");
			config.addProperty("favoriteThings.favoriteColor", new SurfObject("Color"));
			config.addProperty("favoriteThings.favoriteColors", favoriteColorsList);
			config.addProperty("favoriteThings.aliases", aliasesSet);

			favoriteThingsMap.put("aliquot", "User's favorite word.");
			favoriteThingsMap.put("favoriteColor", new SurfObject("Color"));
			favoriteThingsMap.put("favoriteColors", favoriteColorsList);
			favoriteThingsMap.put("aliases", aliasesSet);

			assertThat(config.getProperty("favoriteThings"), equalTo(favoriteThingsMap));
		}

		config.clear();

		{ //Test with Lists
			final List<SurfObject> favoriteColorsList = Lists.listOf(new ArrayList<SurfObject>(), new SurfObject("Color"), new SurfObject("Color"),
					new SurfObject("Color"), new SurfObject("Color"), new SurfObject("Color"), new SurfObject("Color"));

			config.addProperty("favoriteColors", favoriteColorsList);

			assertThat(config.getProperty("favoriteColors"), equalTo(favoriteColorsList));

			config.addProperty("favoriteColors.Color(0).name", "red");
			config.addProperty("favoriteColors.Color(1).name", "orange");
			config.addProperty("favoriteColors.Color(2).name", "yellow");
			config.addProperty("favoriteColors.Color(3).name", "green");
			config.addProperty("favoriteColors.Color(4).name", "blue");
			config.addProperty("favoriteColors.Color(5).name", "violet");

			assertThat(config.getProperty("favoriteColors.Color(0).name"), equalTo("red"));
			assertThat(config.getProperty("favoriteColors.Color(1).name"), equalTo("orange"));
			assertThat(config.getProperty("favoriteColors.Color(2).name"), equalTo("yellow"));
			assertThat(config.getProperty("favoriteColors.Color(3).name"), equalTo("green"));
			assertThat(config.getProperty("favoriteColors.Color(4).name"), equalTo("blue"));
			assertThat(config.getProperty("favoriteColors.Color(5).name"), equalTo("violet"));

			assertThat(config.getProperty("favoriteColors"), not(equalTo(favoriteColorsList)));

			favoriteColorsList.get(0).setPropertyValue("name", "red");
			favoriteColorsList.get(1).setPropertyValue("name", "orange");
			favoriteColorsList.get(2).setPropertyValue("name", "yellow");
			favoriteColorsList.get(3).setPropertyValue("name", "green");
			favoriteColorsList.get(4).setPropertyValue("name", "blue");
			favoriteColorsList.get(5).setPropertyValue("name", "indigo");

			assertThat(config.getProperty("favoriteColors"), not(equalTo(favoriteColorsList)));

			config.addProperty("favoriteColors.Color(5)", favoriteColorsList.get(5));

			assertThat(config.getProperty("favoriteColors.Color(5).name"), equalTo("indigo"));
			assertThat(config.getProperty("favoriteColors"), equalTo(favoriteColorsList));

			config.addProperty("favoriteColors.Color(-1)", new SurfObject("Color"));
			config.addProperty("favoriteColors.Color(6).name", "violet");

			final SurfObject violetColorSurfObject = new SurfObject("Color");

			violetColorSurfObject.setPropertyValue("name", "violet");

			favoriteColorsList.add(violetColorSurfObject);

			assertThat(config.getProperty("favoriteColors"), equalTo(favoriteColorsList));

			config.addProperty("favoriteColors.Color(-1).name", "white");

			final SurfObject whiteColorSurfObject = new SurfObject("Color");
			whiteColorSurfObject.setPropertyValue("name", "white");

			favoriteColorsList.add(whiteColorSurfObject);

			assertThat(config.getProperty("favoriteColors"), equalTo(favoriteColorsList));

			final SurfObject redColorComposition = new SurfObject("Composition");

			redColorComposition.setPropertyValue("red", 255);
			redColorComposition.setPropertyValue("green", 0);
			redColorComposition.setPropertyValue("blue", 0);

			config.addProperty("favoriteColors.Color(-1).composition", redColorComposition);

			assertThat(config.getProperty("favoriteColors.Color(8).composition"), equalTo(redColorComposition));

			config.addProperty("favoriteColors.Color(-1).Foo(-1).Bar(-1).Foobar(-1).composition", redColorComposition);

			assertThat(config.getProperty("favoriteColors.Color(9).Foo(0).Bar(0).Foobar(0).composition"), equalTo(redColorComposition));

			//Test to see if the index (-1) works in an empty List
			config.addProperty("favoriteMoments", new ArrayList<>());

			config.addProperty("favoriteMoments.Moment(-1)", new SurfObject("Momentum"));

			assertThat(config.getProperty("favoriteMoments.Moment(0)"), equalTo(new SurfObject("Momentum")));

			assertThat(config.getProperty("favoriteMoments.Momentum(0)"), equalTo(null));
		}

	}

	/**
	 * Test whether {@link SurfConfiguration#setProperty(String, Object)} is throwing an exception when trying to insert an object in an indexed key that does not
	 * exist.
	 * 
	 * @throws ConfigurationException if any error occur while configuring the file.
	 * @throws IOException if an I/O error occur.
	 * @throws URISyntaxException if there's an error while trying to get an URI.
	 */
	@Test(expected = NoSuchElementException.class)
	public void testSetPropertyFFWithIndexOutOfBound() throws ConfigurationException, IOException, URISyntaxException {
		final FileBasedConfigurationBuilder<SurfConfiguration> configBuilder = new FileBasedConfigurationBuilder<SurfConfiguration>(SurfConfiguration.class)
				.configure(new Parameters().fileBased());

		final SurfConfiguration config = (SurfConfiguration)configBuilder.getConfiguration();

		config.addProperty("Color(8)", new SurfObject("Color"));
	}

	/**
	 * Test whether {@link SurfConfiguration#setProperty(String, Object)} is throwing an exception when trying to insert an object in the middle of a hierarchy
	 * without using the index <code>(-1)</code>.
	 * 
	 * @throws ConfigurationException if any error occur while configuring the file.
	 * @throws IOException if an I/O error occur.
	 * @throws URISyntaxException if there's an error while trying to get an URI.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testSetPropertyFFWithoutIndexAndStringObject() throws ConfigurationException, IOException, URISyntaxException {
		final FileBasedConfigurationBuilder<SurfConfiguration> configBuilder = new FileBasedConfigurationBuilder<SurfConfiguration>(SurfConfiguration.class)
				.configure(new Parameters().fileBased());

		final SurfConfiguration config = (SurfConfiguration)configBuilder.getConfiguration();

		config.addProperty("favoriteThings.color", "red");
	}
	
	/**
	 * Test whether {@link SurfConfiguration#setProperty(String, Object)} is throwing an exception when trying to insert an object in the middle of a hierarchy
	 * without using the index <code>(-1)</code>.
	 * 
	 * @throws ConfigurationException if any error occur while configuring the file.
	 * @throws IOException if an I/O error occur.
	 * @throws URISyntaxException if there's an error while trying to get an URI.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testSetPropertyFFWithoutIndexAndNewSurfObject() throws ConfigurationException, IOException, URISyntaxException {
		final FileBasedConfigurationBuilder<SurfConfiguration> configBuilder = new FileBasedConfigurationBuilder<SurfConfiguration>(SurfConfiguration.class)
				.configure(new Parameters().fileBased());

		final SurfConfiguration config = (SurfConfiguration)configBuilder.getConfiguration();

		config.addProperty("favoriteThings.Color(-1)", new SurfObject("Color"));
	}

	/**
	 * Test whether {@link SurfConfiguration#setProperty(String, Object)} is throwing an exception when trying to insert an object in the middle of a hierarchy
	 * without using the index <code>(-1)</code>.
	 * 
	 * @throws ConfigurationException if any error occur while configuring the file.
	 * @throws IOException if an I/O error occur.
	 * @throws URISyntaxException if there's an error while trying to get an URI.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testSetPropertyFFWithoutIndexAndSurfObject() throws ConfigurationException, IOException, URISyntaxException {
		final FileBasedConfigurationBuilder<SurfConfiguration> configBuilder = new FileBasedConfigurationBuilder<SurfConfiguration>(SurfConfiguration.class)
				.configure(new Parameters().fileBased());

		final SurfConfiguration config = (SurfConfiguration)configBuilder.getConfiguration();

		config.addProperty("favoriteThings.Color(0)", new SurfObject("Color"));
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

		assertThat(surfDocument.getTypeHandle().get(), equalTo("Configuration"));
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

		assertThat(((SurfObject)config.getSurfDocument()).getTypeHandle().get(), equalTo("Configuration"));
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

		assertThat(((SurfObject)config.getSurfDocument()).getTypeHandle(), equalTo(Optional.empty()));
		assertThat(((SurfObject)config.getSurfDocument()).getTag(), equalTo(Optional.empty()));
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

		assertThat(config.size(), equalTo(10));

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

		assertThat(config.size(), equalTo(12));

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

		assertThat(config.size(), equalTo(12));

		assertThat(config.getProperty("authenticated"), is(true));

		config.clearProperty("authenticated");

		assertThat(config.getProperty("authenticated"), equalTo(null));

		assertThat(config.size(), equalTo(12));

		assertThat(config.getProperty("sort"), equalTo(CodePointCharacter.of('d')));
		assertThat(config.getProperty("name.firstName"), equalTo("Jane"));
		assertThat(config.getProperty("name.lastName"), equalTo("Doe"));

		config.clearProperty("name.lastName");

		assertThat(config.getProperty("name.firstName"), equalTo("Jane"));
		assertThat(config.getProperty("name.lastName"), equalTo(null));

		assertThat(config.size(), equalTo(12));

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

	/**
	 * Test whether {@link SurfConfiguration#sizeInternal()} is working properly.
	 * 
	 * @throws ConfigurationException if any error occur while configuring the file.
	 * @throws URISyntaxException if there's an error while trying to get an URI.
	 * @throws IOException if an I/O error occur.
	 */
	@Test
	public void testSizeInternal() throws ConfigurationException, URISyntaxException, IOException {
		final File configFile = tempFolder.newFile("serializer-empty-configuration-file.surf");

		final Configuration config = new FileBasedConfigurationBuilder<SurfConfiguration>(SurfConfiguration.class)
				.configure(new Parameters().fileBased().setFile(configFile)).getConfiguration();

		assertThat(config.size(), is(0));

		config.addProperty("name", "Jane Dee");

		assertThat(config.size(), equalTo(1));

		config.addProperty("name", "Jane Doe"); //this call should override the previous value of "name".

		assertThat(config.size(), equalTo(1));

		config.addProperty("name", null); //this call should remove the property.

		assertThat(config.size(), equalTo(0));

		//Tests with properties in the root node.

		{ //tests with SurfObjects
			final SurfObject userSurfObject = new SurfObject("User");
			userSurfObject.setPropertyValue("name", "Jane Dee");
			userSurfObject.setPropertyValue("email", "jane_doe@example.com");
			userSurfObject.setPropertyValue("phone", "+12015550123");

			config.addProperty("user", userSurfObject);
		}

		assertThat(config.size(), equalTo(4));

		{ //tests with Maps
			final Map<String, String> favoriteThingsMap = new HashMap<String, String>();

			favoriteThingsMap.put("5", "User's favorite number.");
			favoriteThingsMap.put("aliquot", "User's favorite word.");

			config.addProperty("favoriteThings", favoriteThingsMap);
		}

		assertThat(config.size(), equalTo(7));

		{ //Test with Lists
			final List<String> favoriteColorsList = Arrays.asList("red", "orange", "yellow", "green", "blue", "indigo", "violet");

			config.addProperty("favoriteColors", favoriteColorsList);
		}

		assertThat(config.size(), equalTo(8));

		{ //Test with Lists with SurfObjects in it
			final List<Object> favoriteColorsList = Arrays.asList("red", "orange", "yellow", "green", "blue", "indigo", "violet", new SurfObject(), new SurfObject());

			config.addProperty("favoriteColorsWithSimpleSurfObjects", favoriteColorsList);
		}

		assertThat(config.size(), equalTo(9));

		{ //Test with Lists with SurfObjects in it
			final List<Object> favoriteColorsList = Arrays.asList("red", "orange", "yellow", "green", "blue", "indigo", "violet", new SurfObject("DummyObject"),
					new SurfObject("DummyObject"));

			config.addProperty("favoriteColorsWithNamesSurfObjects", favoriteColorsList);
		}

		assertThat(config.size(), equalTo(12));

		{ //Test with Sets
			final Set<String> aliasesSet = Sets.immutableSetOf("jdoe", "janed");

			config.addProperty("aliases", aliasesSet);
		}

		assertThat(config.size(), equalTo(13));
	}

}
