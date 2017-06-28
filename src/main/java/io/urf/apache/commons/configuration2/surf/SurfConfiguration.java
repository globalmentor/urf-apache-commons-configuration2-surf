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
import java.net.URI;
import java.util.*;

import javax.annotation.*;

import org.apache.commons.configuration2.*;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.FileHandler;
import org.apache.commons.configuration2.tree.*;

import com.globalmentor.collections.NameValuePairMapEntry;
import com.globalmentor.java.Conditions;

import io.urf.surf.parser.*;
import io.urf.surf.serializer.*;

import static java.util.Objects.*;

import static org.apache.commons.configuration2.tree.DefaultExpressionEngineSymbols.*;

/**
 * An implementation for a {@link FileBasedConfiguration} that uses a SURF file to store information.
 * 
 * <p>
 * If a SURF configuration file is created by an instance of this class, it will be composed by a root {@link SurfObject} with the type name defined as
 * {@value #DEFAULT_ROOT_TYPE_NAME}, and every property added to this {@link SurfConfiguration} instance will be a child of this {@link SurfObject}. i.e.:
 * </p>
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
 * In order to provide some backward compatibility, a {@link Map} may be used as the root object of the SURF configuration file. i.e.:
 * </p>
 * 
 * <pre>
 * {
 *   "property1" : value1
 *   "property2" : value2
 *   "property3" : value3
 * }
 * </pre>
 * 
 * <p>
 * This {@link Map} version of {@link SurfConfiguration} is available to use only if the file has been already created. An instance of {@link SurfConfiguration}
 * will never use this approach by default.
 * </p>
 * 
 * <p>
 * An instance of {@link SurfConfiguration} may be gotten by using the following statement:
 * </p>
 * 
 * <pre>
 * <code>
 * final Configuration config = new FileBasedConfigurationBuilder<SurfConfiguration>(SurfConfiguration.class)
				.configure(new Parameters().fileBased().setFile(<var>configurationFile<var>)).getConfiguration();
 * </code>
 * </pre>
 * 
 * <p>
 * To add properties there is the following statement:
 * </p>
 * 
 * <pre>
 * <code>
 * config.addProperty(<var>propertyName</var>, <var>propertyValue</var>);
 * </code>
 * </pre>
 * 
 * <p>
 * To get properties there is the following statement:
 * </p>
 * 
 * <pre>
 * <code>
 * config.getProperty(<var>propertyName</var>);
 * </code>
 * </pre>
 * 
 * <p>
 * The hierarchy model provided by Apache is also supported for {@link SurfObject SurfObjects} and {@link Map Maps}:
 * </p>
 * 
 * <pre>
 * <code>
 * config.getProperty("surfObjectName.propertyName");
 * </code>
 * </pre>
 * 
 * <p>
 * or
 * </p>
 * 
 * <pre>
 * <code>
 * config.getProperty("mapName.propertyName");
 * </code>
 * </pre>
 * 
 * <p>
 * The current implementation lookup has a behave similar to the one of {@link XMLConfiguration}, what makes it possible to query the items of a {@link List}
 * using {@link SurfObject SurfObject's} type name. <em>This lookup is only supported by {@link SurfObject SurfObjects} that have a type name</em>:
 * </p>
 * 
 * <pre>
 * <code>
 * config.getProperty("listName.SurfObjectTypeName(0)");
 * </code>
 * </pre>
 * 
 * <p>
 * Here is an example of SURF file and how we would use the methods above:
 * </p>
 * 
 * <pre>
 * <code>
 * *Configuration:
 *   authenticated = true
 *   sort = 'd'
 *   name = "Jane Doe"
 *   id = &bb8e7dbe-f0b4-4d94-a1cf-46ed0e920832
 *   email = ^jane_doe@example.com
 *   phone = +12015550123
 *   aliases = [
 *     "jdoe"
 *     "janed"
 *   ]
 *   homePage = <http://www.example.com/jdoe/>
 *   salt = %Zm9vYmFy
 *   joined = @2016-01-23,
 *   credits = 123
 *   favoriteThings = {
 *     "aliquot" : "User's favorite word."
 *     "amethyst" : "User's favorite stone."
 *   }
 *   !the list of colors said to make up the spectrum of a rainbow, in order
 *   rainbow = [
 *     *Color: 
 *       name = "red"
 *     ;
 *     *Color: 
 *       name = "orange"
 *     ;
 *     *Color: 
 *       name = "yellow"
 *     ;
 *     *Color: 
 *       name = "green"
 *     ;
 *     *Color: 
 *       name = "blue"
 *     ;
 *     *Color: 
 *       name = "indigo"
 *     ;
 *     *Color: 
 *       name = "violet"
 *     ;
 *   ]
 * ;
 * </code>
 * </pre>
 * 
 * <pre>
 * <code>
 * {
 *   authenticated : true
 *   sort : 'd'
 *   name : "Jane Doe"
 *   id : &bb8e7dbe-f0b4-4d94-a1cf-46ed0e920832
 *   email : ^jane_doe@example.com
 *   phone : +12015550123
 *   aliases : [
 *     "jdoe"
 *     "janed"
 *   ]
 *   homePage : <http://www.example.com/jdoe/>
 *   salt : %Zm9vYmFy
 *   joined : @2016-01-23,
 *   credits : 123
 *   favoriteThings : {
 *     "aliquot" : "User's favorite word."
 *     "amethyst" : "User's favorite stone."
 *   }
 *   !the list of colors said to make up the spectrum of a rainbow, in order
 *   rainbow : [
 *     *Color: 
 *       name = "red"
 *     ;
 *     *Color: 
 *       name = "orange"
 *     ;
 *     *Color: 
 *       name = "yellow"
 *     ;
 *     *Color: 
 *       name = "green"
 *     ;
 *     *Color: 
 *       name = "blue"
 *     ;
 *     *Color: 
 *       name = "indigo"
 *     ;
 *     *Color: 
 *       name = "violet"
 *     ;
 *   ]
 * }
 * </code>
 * </pre>
 * 
 * <em> The following examples should work for both types of {@link SurfConfiguration}, the one with a {@link SurfObject}, and the one with a {@link Map} as
 * root object. </em>
 * 
 * <p>
 * Here is an example of how to get a simple string property:
 * </p>
 * 
 * <pre>
 * <code>
 * config.getProperty("name"); //returns "Jane Doe"
 * </pre>
 * </code>
 * 
 * <p>
 * To get a property in its specific class, the class must be provided to the following method:
 * </p>
 * 
 * <pre>
 * <code>
 * config.getProperty("favoriteThings", Boolean.class, "authenticated"); //returns true. <em>If the class would not be provided, a string representation of the value would be returned.</em>
 * </pre>
 * </code>
 * 
 * <p>
 * Here is an example of how to get a property in a lower level in the hierarchy:
 * </p>
 * 
 * <pre>
 * <code>
 * config.getProperty("favoriteThings.aliquot"); //returns "User's favorite word.".
 * </pre>
 * </code>
 * 
 * <p>
 * Here is an example of how to make a lookup in a {@link List}:
 * </p>
 * 
 * <pre>
 * <code>
 * config.getProperty("rainbow.Color(5)"); //returns "indigo". <em>The lookup is zero-based, and it only works with {@link SurfObject SurfObjects} with their type names properly set up.</em>
 * </pre>
 * </code>
 * 
 * <p>
 * The SURF document serialized will always be formatted. See {@link SurfSerializer#setFormatted(boolean)} for more information.
 * </p>
 * 
 * @author Magno N A Cruz
 */
public class SurfConfiguration extends BaseHierarchicalConfiguration implements FileBasedConfiguration {

	/** Constant for the default root object type name. */
	private static final String DEFAULT_ROOT_TYPE_NAME = "Configuration";

	/** The label used as key to the type of the node. */
	private static final String NODE_TYPE_LABEL = "nodeType";

	/** The label used as key to the type name of a {@link SurfObject} */
	private static final String SURF_OBJECT_TYPE_NAME_ATTRIBUTE_LABEL = "typeName";

	/** The label used as key to the iri of a {@link SurfObject} */
	private static final String SURF_OBJECT_IRI_ATTRIBUTE_LABEL = "iri";

	/**
	 * {@inheritDoc}
	 * 
	 * <p>
	 * If an empty file is provided to the {@link FileHandler}, then we create the root object using {@value #DEFAULT_ROOT_TYPE_NAME} as the type name.
	 * </p>
	 * 
	 * @throws ConfigurationException if the root element is not an instance of {@link SurfObject} or {@link Map}, or if the {@link Reader} provided refers to a
	 *           non-existing file.
	 * @throws IOException if an I/O error occurs.
	 */
	@Override
	public void read(@Nonnull final Reader in) throws ConfigurationException, IOException {

		try (final BufferedReader bufferedIn = new BufferedReader(requireNonNull(in))) {
			Object surfDocument = new SurfParser().parse(bufferedIn).orElse(null);

			if((!(surfDocument instanceof SurfObject)) && (!(surfDocument instanceof Map)) && (surfDocument != null)) {
				throw new ConfigurationException("The element on the file is not a valid SURF configuration file.");
			}

			final ImmutableNode.Builder rootNodeBuilder;

			if(surfDocument != null) {
				rootNodeBuilder = createHierarchy(surfDocument);
			} else {
				rootNodeBuilder = createHierarchy(new SurfObject(DEFAULT_ROOT_TYPE_NAME));
			}

			getModel().setRootNode(rootNodeBuilder.create());
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * <p>
	 * The configuration hierarchy is written to the given {@link Writer}, with {@link SurfSerializer SurfSerializer's} formatter set up.
	 * </p>
	 * 
	 * @throws ConfigurationException if the root element is not an instance of {@link SurfObject} or {@link Map}, or if the {@link Reader} provided refers to a
	 *           non-existing file.
	 * @throws IOException if an I/O error occurs.
	 * @see SurfSerializer#setFormatted(boolean)
	 */
	@Override
	public void write(@Nonnull final Writer out) throws ConfigurationException, IOException {

		try (final BufferedWriter bufferedOut = new BufferedWriter(requireNonNull(out))) {
			final SurfSerializer serializer = new SurfSerializer();

			serializer.setFormatted(true);

			bufferedOut.write(serializer.serialize(toObject(getNodeModel().getRootNode())));
		}

	}

	/**
	 * Transform the given {@link ImmutableNode} and all its hierarchy below that into objects.
	 * 
	 * <p>
	 * This method is important to transform a hierarchy of {@link ImmutableNode ImmutableNodes} to objects ready to be serialized by {@link SurfSerializer}.
	 * </p>
	 * 
	 * @param hierarchyRootNode The root {@link ImmutableNode} of the hierarchy to be transformed back to an object.
	 * 
	 * @return The object representation of the given {@link ImmutableNode} and all its hierarchy below that.
	 * @throws IllegalArgumentException If the given node is not a literal value or an instance of {@link SurfObject} or {@link Map}.
	 */
	private static Object toObject(@Nonnull final ImmutableNode hierarchyRootNode) {
		requireNonNull(hierarchyRootNode, "The provided node to be transformed must not be <null>.");

		final Object hierarchyRootNodeType = hierarchyRootNode.getAttributes().get(NODE_TYPE_LABEL);

		if(hierarchyRootNodeType instanceof NodeType) {

			switch((NodeType)hierarchyRootNodeType) {
				case SURF_OBJECT:
					return toObject(new SurfObject(((URI)hierarchyRootNode.getAttributes().get(SURF_OBJECT_IRI_ATTRIBUTE_LABEL)),
							((String)hierarchyRootNode.getAttributes().get(SURF_OBJECT_TYPE_NAME_ATTRIBUTE_LABEL))), hierarchyRootNode.getChildren());
				case MAP:
					return toObject(new HashMap<String, Object>(), hierarchyRootNode.getChildren());
				case LIST:
					return toObject(new LinkedList<Object>(), hierarchyRootNode.getChildren());
				case SET:
					return toObject(new HashSet<Object>(), hierarchyRootNode.getChildren());
				default:
					throw new IllegalArgumentException("This method is not compatible with the provided data structure object.");
			}

		} else {
			return hierarchyRootNode.getValue();
		}

	}

	/**
	 * Transform all the hierarchy below the given collection of nodes and add it to the given parent object. The given object needs to be a data structure of the
	 * type {@link SurfObject}, {@link Map}, {@link List} or {@link Set}.
	 * 
	 * @param parentStructure The parent data structure object where the child nodes will be added as objects into it.
	 * @param childrenNodes The children nodes to be transformed to objects and be added to its parent data structure.
	 * 
	 * @return The same parent structure provided with all its children nodes added into it as their object representation.
	 * @throws IllegalArgumentException If the given parent structure is not an instance of {@link SurfObject} or {@link Map}.
	 * 
	 * @see #toObject(ImmutableNode)
	 */
	@SuppressWarnings("unchecked")
	private static Object toObject(@Nonnull final Object parentStructure, @Nullable final List<ImmutableNode> childrenNodes) {
		requireNonNull(parentStructure, "The provided parent data structure object must not be <null>.");
		requireNonNull(childrenNodes, "The provided list of children nodes might not be <null>.");

		for(final ImmutableNode childNode : childrenNodes) {

			final Object childNodeType = childNode.getAttributes().get(NODE_TYPE_LABEL);

			Object childObject;

			//in this block we get the object of the current child node.
			if(NodeType.SURF_OBJECT.equals(childNodeType)) {
				childObject = new SurfObject(((URI)childNode.getAttributes().get(SURF_OBJECT_IRI_ATTRIBUTE_LABEL)),
						((String)childNode.getAttributes().get(SURF_OBJECT_TYPE_NAME_ATTRIBUTE_LABEL)));
			} else if(NodeType.MAP.equals(childNodeType)) {
				childObject = new HashMap<String, Object>();
			} else if(NodeType.LIST.equals(childNodeType)) {
				childObject = new LinkedList<Object>();
			} else if(NodeType.SET.equals(childNodeType)) {
				childObject = new HashSet<Object>();
			} else {
				childObject = childNode.getValue();
			}

			//in this block we add the obtained child object to the parent structure.
			if(parentStructure instanceof SurfObject) {
				((SurfObject)parentStructure).setPropertyValue(childNode.getNodeName(), toObject(childObject, childNode.getChildren()));
			} else if(parentStructure instanceof Map) {
				((Map<String, Object>)parentStructure).put(childNode.getNodeName(), toObject(childObject, childNode.getChildren()));
			} else if(parentStructure instanceof List) {
				((List<Object>)parentStructure).add(toObject(childObject, childNode.getChildren()));
			} else if(parentStructure instanceof Set) {
				((Set<Object>)parentStructure).add(toObject(childObject, childNode.getChildren()));
			} else {
				throw new IllegalArgumentException("This method is not compatible with the provided data structure object.");
			}

		}

		return parentStructure;
	}

	/**
	 * Transform the given object and all the hierarchy below that.
	 * 
	 * @param rootObject The root object to be converted to a hierarchy of {@link ImmutableNode ImmutableNodes}.
	 * 
	 * @return The object provided and all its hierarchy below represented by {@link ImmutableNode ImmutableNodes}.
	 * @throws IllegalArgumentException If the given parent structure is not an instance of {@link SurfObject} or {@link Map}.
	 */
	public static ImmutableNode.Builder createHierarchy(@Nonnull final Object rootObject) {
		requireNonNull(rootObject, "The given object must not be <null>.");

		return createHierarchy(null, Arrays.asList(new NameValuePairMapEntry<String, Object>(null, rootObject)));
	}

	/**
	 * Creates all the hierarchy of the given {@link ImmutableNode} based on the given properties.
	 * 
	 * <p>
	 * <strong> The <code>nodeBuilder</code> is allowed to be <code>null</code> if, and only if, the current node that's being built is the root node. </strong>
	 * Otherwise, the hierarchy will be built incomplete.
	 * </p>
	 * 
	 * @param nodeBuilder The {@link ImmutableNode} that will be built based on the given properties, or {@code null} if the node to be build is the root node.
	 * @param nodeProperties The properties to be added to the given {@link ImmutableNode.Builder}.
	 * 
	 * @return The given {@link ImmutableNode.Builder} with all the properties provided added into it in order to allow method chaining.
	 */
	@SuppressWarnings("unchecked")
	private static ImmutableNode.Builder createHierarchy(@Nullable final ImmutableNode.Builder nodeBuilder,
			@Nonnull final Iterable<? extends Map.Entry<String, Object>> nodeProperties) {
		requireNonNull(nodeProperties, "The iterable with the properties to be added to the given node must not be <null>.");

		for(final Map.Entry<String, Object> childProperty : nodeProperties) {
			final ImmutableNode.Builder childNodeBuilder = new ImmutableNode.Builder().name(childProperty.getKey());
			final Object childNodeValue = childProperty.getValue();

			List<Map.Entry<String, Object>> entries = new LinkedList<>();

			if(childNodeValue instanceof SurfObject) {
				((SurfObject)childNodeValue).getTypeName().ifPresent(typeName -> childNodeBuilder.addAttribute(SURF_OBJECT_TYPE_NAME_ATTRIBUTE_LABEL, typeName));
				((SurfObject)childNodeValue).getIri().ifPresent(iri -> childNodeBuilder.addAttribute(SURF_OBJECT_IRI_ATTRIBUTE_LABEL, iri));

				((SurfObject)childNodeValue).getPropertyNameValuePairs().forEach(entry -> entries.add(new NameValuePairMapEntry<String, Object>(entry)));
			}

			else if(childNodeValue instanceof Map) {
				entries.addAll(((Map<String, Object>)childNodeValue).entrySet());
			}

			else if(childNodeValue instanceof List) {
				((List<Object>)childNodeValue).forEach(entry -> {
					String typeName = null;

					if(entry instanceof SurfObject) {
						typeName = ((SurfObject)entry).getTypeName().orElse(null); //if the child node is a SurfObject, we use its type name as node name to make sure that the index lookup will work.
					}

					entries.add(new NameValuePairMapEntry<String, Object>(typeName, entry));
				});
			}

			else if(childNodeValue instanceof Set) {
				((Set<Object>)childNodeValue).forEach(entry -> entries.add(new NameValuePairMapEntry<String, Object>(null, entry)));
			}

			else {
				childNodeBuilder.value(childNodeValue);
			}

			childNodeBuilder.addAttribute(NODE_TYPE_LABEL, NodeType.of(childNodeValue));

			if(nodeBuilder == null) {
				return createHierarchy(childNodeBuilder, entries); //if we are handling with the root node we simply return the first child node builder.
			}

			nodeBuilder.addChild(createHierarchy(childNodeBuilder, entries).create());
		}

		return nodeBuilder;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * <p>
	 * This implementation delegates to {@link #setPropertyInternal(String, Object)} in order to not accept duplicated keys in the same level.
	 * </p>
	 */
	@Override
	protected void addPropertyInternal(@Nonnull String key, @Nullable Object obj) {
		setPropertyInternal(key, obj);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * If the object provided is one of the data structures supported by this implementation, it manually converts and adds it to the hierarchy, otherwise, this
	 * method just delegates to {@link AbstractHierarchicalConfiguration#setPropertyInternal(String, Object)}.
	 * </p>
	 * 
	 * <strong>The support for addition of properties by the use of index <code>(-1)</code> is not yet implemented.</strong>
	 * @throws UnsupportedOperationException If a key with the index <code>-1</code> is provided as arguments of this method.
	 */
	@Override
	protected void setPropertyInternal(@Nonnull String key, @Nullable Object obj) {
		requireNonNull(key, "The key of the object to be added to the configuration must not be <null>.");

		if(key.contains(String.format("%s-1%s", DEFAULT_INDEX_START, DEFAULT_INDEX_END))) {
			throw new UnsupportedOperationException("Adding properties using (-1) index is not yet supported.");
		}

		if(!NodeType.isNavigable(obj)) {
			super.setPropertyInternal(key, obj); //if the default implementation handles with the type of the given object properly, we just delegate to it.

			//fixNodeProperties(key); introduce it again when index (-1) is enabled.

			return;
		}

		final boolean keyExists = containsKey(key);

		final ImmutableNode.Builder nodeBuilder = createHierarchy(obj);

		if(key.contains(DEFAULT_PROPERTY_DELIMITER)) {
			final String parentKey = key.substring(0, key.lastIndexOf(DEFAULT_PROPERTY_DELIMITER));

			final HierarchicalConfiguration<ImmutableNode> subHierarchy = configurationAt(parentKey, true);

			String newNodeKey = key.substring(key.lastIndexOf(DEFAULT_PROPERTY_DELIMITER) + 1, key.length());

			ImmutableNode subHierarchyRootNode = subHierarchy.getNodeModel().getNodeHandler().getRootNode();

			ImmutableNode subHierarchyParentNode = null;

			for(ImmutableNode node : subHierarchyRootNode.getChildren()) {
				if(node.getNodeName().equals(parentKey)) {
					subHierarchyParentNode = node;
				}
			}

			if(subHierarchyParentNode == null) {
				throw new AssertionError("The node to be replaced shouldn't be null at this point.");
			}

			final List<ImmutableNode> childrenNodes = subHierarchyParentNode.getChildren();

			final List<ImmutableNode> newChildrenNodes = new LinkedList<ImmutableNode>();

			if(newNodeKey.contains(DEFAULT_INDEX_START)) {
				Conditions.checkArgument(obj instanceof SurfObject, "Indexes are only supported with SurfObject instances.");

				int lookupIndex = Integer.parseInt(newNodeKey.substring(newNodeKey.indexOf(DEFAULT_INDEX_START) + 1, newNodeKey.indexOf(DEFAULT_INDEX_END)));

				newNodeKey = newNodeKey.substring(0, newNodeKey.indexOf(DEFAULT_INDEX_START));

				int lookupCount = 0;

				for(final ImmutableNode child : childrenNodes) {

					if(child.getNodeName().equals(newNodeKey)) {
						if(lookupCount == lookupIndex) {
							newChildrenNodes.add(nodeBuilder.name(newNodeKey).create());
						} else {
							newChildrenNodes.add(child);
						}

						lookupCount++;
					} else {
						newChildrenNodes.add(child);
					}

				}

				subHierarchyRootNode = subHierarchyRootNode.replaceChild(subHierarchyParentNode, subHierarchyParentNode.replaceChildren(newChildrenNodes));
			} else {

				if(keyExists) {
					for(final ImmutableNode child : childrenNodes) {

						if(child.getNodeName().equals(newNodeKey)) {
							newChildrenNodes.add(nodeBuilder.name(newNodeKey).create());
						} else {
							newChildrenNodes.add(child);
						}

					}

					subHierarchyRootNode = subHierarchyRootNode.replaceChild(subHierarchyParentNode, subHierarchyParentNode.replaceChildren(newChildrenNodes));
				} else {
					subHierarchyRootNode = subHierarchyRootNode.replaceChild(subHierarchyParentNode,
							subHierarchyParentNode.addChild(nodeBuilder.name(newNodeKey).create()));
				}

			}

			//TODO This code is part of a draft for implementing the lookup using index (-1), it must be implemented or deleted in the future.
			//			if(parentKey.contains(String.format("%s-1%s", DEFAULT_INDEX_START, DEFAULT_INDEX_END))) {
			//				final String splittedKey = key.split(DEFAULT_PROPERTY_DELIMITER)[key.length() - 2];
			//
			//				parentNode = parentNode.setAttribute(SURF_OBJECT_TYPE_NAME_ATTRIBUTE_LABEL, NodeType.SURF_OBJECT);
			//				parentNode = parentNode.setName(splittedKey.substring(0, splittedKey.length() - 3));
			//			}

			subHierarchy.getNodeModel().setRootNode(subHierarchyRootNode);
		} else {
			final ImmutableNode parentNode = getNodeModel().getRootNode();

			if(keyExists) {

				for(final ImmutableNode childNode : parentNode.getChildren()) {
					if(childNode.getNodeName().equals(key)) {
						getNodeModel().setRootNode(parentNode.replaceChild(childNode, nodeBuilder.name(key).create()));
					}
				}

			} else {
				getNodeModel().setRootNode(parentNode.addChild(nodeBuilder.name(key).create()));
			}
		}

	}

	//TODO This code is part of a draft for implementing the lookup using index (-1), it must be implemented or deleted in the future.
	//	private void fixNodeProperties(@Nonnull String key) {
	//
	//		if(key.contains(String.format("%s-1%s", DEFAULT_INDEX_START, DEFAULT_INDEX_END))) {
	//
	//			final String keyWithoutIndex = key.substring(0, key.lastIndexOf(String.format("%s-1%s.", DEFAULT_INDEX_START, DEFAULT_INDEX_END)));
	//			final HierarchicalConfiguration<ImmutableNode> parentNodeHierarchy;
	//
	//			List<ImmutableNode> childrenNodes;
	//
	//			if(keyWithoutIndex.contains(DEFAULT_PROPERTY_DELIMITER)) {
	//				parentNodeHierarchy = configurationAt(keyWithoutIndex.substring(keyWithoutIndex.indexOf("."), keyWithoutIndex.lastIndexOf(".")), true);
	//				childrenNodes = parentNodeHierarchy.getNodeModel().getNodeHandler().getRootNode().getChildren();
	//				for(int i = 0; i < childrenNodes.size(); i++) {
	//					if(childrenNodes.get(i).getNodeName().equals(keyWithoutIndex.substring(keyWithoutIndex.indexOf("."), keyWithoutIndex.lastIndexOf(".")))) {
	//						childrenNodes = Arrays.asList(childrenNodes.get(i));
	//					}
	//				}
	//			} else {
	//				parentNodeHierarchy = (HierarchicalConfiguration<ImmutableNode>)this;
	//				childrenNodes = parentNodeHierarchy.getNodeModel().getNodeHandler().getRootNode().getChildren();
	//			}
	//
	//			for(ImmutableNode childNode : childrenNodes) {
	//				if(childNode.getNodeName().equals(key.substring(key.lastIndexOf(DEFAULT_PROPERTY_DELIMITER), key.length())) && childNode.getAttributes().isEmpty()) {
	//					parentNodeHierarchy.getNodeModel().setRootNode(parentNodeHierarchy.getNodeModel().getNodeHandler().getRootNode().replaceChild(childNode,
	//							childNode.setAttribute(NODE_TYPE_LABEL, NodeType.SURF_OBJECT)));
	//
	//					parentNodeHierarchy.getNodeModel().setRootNode(parentNodeHierarchy.getNodeModel().getNodeHandler().getRootNode().replaceChild(childNode,
	//							childNode.setAttribute(SURF_OBJECT_TYPE_NAME_ATTRIBUTE_LABEL, key.substring(key.lastIndexOf(DEFAULT_PROPERTY_DELIMITER), key.length()))));
	//				}
	//			}
	//
	//			//elementsWithoutNodeTypeAttribute.forEach(hierarchy -> hierarchy.getNodeModel()
	//			//		.setRootNode(hierarchy.getNodeModel().getNodeHandler().getRootNode().setAttribute(NODE_TYPE_LABEL, NodeType.SURF_OBJECT)));
	//
	//			//			.forEach(hierarchy -> {
	//			//
	//			//				if(!hierarchy.getNodeModel().getNodeHandler().getRootNode().getAttributes().containsKey(NODE_TYPE_LABEL)) {
	//			//					hierarchy.getNodeModel()
	//			//							.setRootNode(hierarchy.getNodeModel().getNodeHandler().getRootNode().setAttribute(NODE_TYPE_LABEL, NavigableNodeType.SURF_OBJECT));
	//			//				}
	//			//
	//			//			});
	//
	//		}
	//
	//	}

	@Override
	protected Object getPropertyInternal(@Nonnull final String key) {
		final List<QueryResult<ImmutableNode>> queryResults = fetchNodeList(key);

		if(queryResults.isEmpty()) {
			return null;
		}

		else if(queryResults.size() == 1) {
			return toObject(queryResults.iterator().next().getNode());
		}

		else {
			final List<Object> resultObjects = new LinkedList<Object>();
			queryResults.forEach(result -> resultObjects.add(toObject(result.getNode())));
			return resultObjects;
		}

	}

	@Override
	protected int sizeInternal() {
		return sizeInternal(getNodeModel().getRootNode());
	}

	/**
	 * Recursive method to help with the implementation of {@link #size()}.
	 * 
	 * @param node The node that will be used to check for the size.
	 * @return The number of child nodes of the given {@link ImmutableNode}. This method considers all the levels below the level of the provided
	 *         {@link ImmutableNode}.
	 */
	private static int sizeInternal(@Nonnull final ImmutableNode node) {
		int childCount = 0;

		if(NodeType.SET.equals(node.getAttributes().get(NODE_TYPE_LABEL))) {
			return childCount;
		}

		for(final ImmutableNode childNode : node.getChildren()) {

			if(NodeType.LIST.equals(node.getAttributes().get(NODE_TYPE_LABEL))) {

				if(NodeType.SURF_OBJECT.equals(childNode.getAttributes().get(NODE_TYPE_LABEL)) && childNode.getNodeName() != null) {
					childCount += sizeInternal(childNode) + 1;
				}

			}

			else {
				childCount += sizeInternal(childNode) + 1;
			}

		}

		return childCount;
	}

	@Override
	protected boolean isEmptyInternal() {
		return getModel().getNodeHandler().getRootNode().getChildren().isEmpty();
	}

	/**
	 * Returns the SURF document of this configuration.
	 *
	 * @return The SURF document of this configuration. This SURF document may be an instance of {@link SurfObject} or {@link Map}.
	 */
	public Object getSurfDocument() {
		return toObject(getNodeModel().getRootNode());
	}

	/**
	 * Enum created to represent the node types navigable by a SURF document.
	 * 
	 * @author Magno N A Cruz
	 */
	private enum NodeType {
		SURF_OBJECT, MAP, LIST, SET;

		/**
		 * Returns the instance of {@link NodeType} that will represent the type of the object provided in its node form.
		 * 
		 * @param obj The object that needs to be represented by an instance of this class.
		 * @return An instance representing the type of the object provided, {@code null} if the object isn't supported by {@link SurfConfiguration} or it's a
		 *         literal.
		 */
		public static NodeType of(@Nullable final Object obj) {

			if(obj instanceof SurfObject) {
				return SURF_OBJECT;
			} else if(obj instanceof Map) {
				return MAP;
			} else if(obj instanceof List) {
				return LIST;
			} else if(obj instanceof Set) {
				return SET;
			} else {
				return null;
			}

		}

		/**
		 * Returns whether {@link NodeType} may represent the type of the object provided in its node form.
		 * 
		 * @param obj The object that needs to be represented by an instance of this class.
		 * @return {@code true} if {@link NodeType} has an instance for the type of the object provided, {@link false} if not.
		 */
		public static boolean isNavigable(@Nullable final Object obj) {

			if(obj instanceof SurfObject || obj instanceof Map || obj instanceof List || obj instanceof Set) {
				return true;
			} else {
				return false;
			}

		}
	}

}
