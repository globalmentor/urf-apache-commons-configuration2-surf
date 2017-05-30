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

import io.urf.surf.parser.*;
import io.urf.surf.serializer.*;

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
 * In order to provide backward compatibility to JSON files, a {@link Map} might be used as the root object of the SURF configuration file.
 * </p>
 * 
 * i.e.,
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
 * This {@link Map} version of {@link SurfConfiguration} is available to use only if the file already has been created as a JSON map. An instance of
 * {@link SurfConfiguration} will never use this approach by default.
 * </p>
 * 
 * <p>
 * The SURF document serialized will always be formatter. See {@link SurfSerializer#setFormatted(boolean)}.
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

			final ImmutableNode rootNode;

			if(surfDocument != null) {
				rootNode = createHierarchy(surfDocument);
			} else {
				rootNode = createHierarchy(new SurfObject(DEFAULT_ROOT_TYPE_NAME));
			}

			getModel().setRootNode(rootNode);
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
	 */
	private Object toObject(@Nonnull final ImmutableNode hierarchyRootNode) {
		requireNonNull(hierarchyRootNode, "The provided node to be transformed might not be <null>.");

		final Object hierarchyRootNodeType = hierarchyRootNode.getAttributes().get(NODE_TYPE_LABEL);

		if(NodeType.SURF_OBJECT.equals(hierarchyRootNodeType)) {
			return toObject(new SurfObject(((URI)hierarchyRootNode.getAttributes().get(SURF_OBJECT_IRI_ATTRIBUTE_LABEL)),
					((String)hierarchyRootNode.getAttributes().get(SURF_OBJECT_TYPE_NAME_ATTRIBUTE_LABEL))), hierarchyRootNode.getChildren());
		}

		else if(NodeType.MAP.equals(hierarchyRootNodeType)) {
			return toObject(new HashMap<String, Object>(), hierarchyRootNode.getChildren());
		}

		else if(NodeType.SET.equals(hierarchyRootNodeType)) {
			return toObject(new HashSet<Object>(), hierarchyRootNode.getChildren());
		}

		else if(NodeType.LIST.equals(hierarchyRootNodeType)) {
			return toObject(new ArrayList<Object>(), hierarchyRootNode.getChildren());
		}

		else {
			return hierarchyRootNode.getValue();
		}

	}

	/**
	 * Transform all the hierarchy below the given collection of nodes and add it to the given parent object. The given object needs to be a data structure of the
	 * type {@link SurfObject}, {@link Map}, {@link Set}, or {@link List}.
	 * 
	 * @param parentStructure The parent data structure object where the child nodes will be added as objects into it.
	 * @param childrenNodes The children nodes to be transformed to objects and be added to its parent data structure.
	 * 
	 * @return The same parent structure provided with all its children nodes added into it as their object representation.
	 * @see #toObject(ImmutableNode)
	 */
	@SuppressWarnings("unchecked")
	private Object toObject(@Nonnull final Object parentStructure, @Nullable final List<ImmutableNode> childrenNodes) {
		requireNonNull(parentStructure, "The provided parent data structure object might not be <null>.");
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
			} else if(NodeType.SET.equals(childNodeType)) {
				childObject = new HashSet<Object>();
			} else if(NodeType.LIST.equals(childNodeType)) {
				childObject = new ArrayList<Object>();
			} else {
				childObject = childNode.getValue();
			}

			//in this block we add the obtained child object to the parent structure.
			if(parentStructure instanceof SurfObject) {
				((SurfObject)parentStructure).setPropertyValue(childNode.getNodeName(), toObject(childObject, childNode.getChildren()));
			} else if(parentStructure instanceof Map) {
				((Map<String, Object>)parentStructure).put(childNode.getNodeName(), toObject(childObject, childNode.getChildren()));
			} else if(parentStructure instanceof Set) {
				((Set<Object>)parentStructure).add(toObject(childObject, childNode.getChildren()));
			} else if(parentStructure instanceof List) {
				((List<Object>)parentStructure).add(toObject(childObject, childNode.getChildren()));
			} else {
				throw new AssertionError("This method is not compatible with the provided data structure object.");
			}

		}

		return parentStructure;
	}

	/**
	 * Transform the given {@link SurfObject} or {@link Map} object and all the hierarchy below that.
	 * 
	 * @param rootObject The root object to be converted to a hierarchy of {@link ImmutableNode ImmutableNodes}.
	 * 
	 * @return The object provided and all its hierarchy below represented by {@link ImmutableNode ImmutableNodes}.
	 */
	@SuppressWarnings("unchecked")
	private ImmutableNode createHierarchy(@Nonnull final Object rootObject) {
		requireNonNull(rootObject, "The given object must not be <null>.");

		final ImmutableNode.Builder rootNodeBuilder = new ImmutableNode.Builder();

		final List<Map.Entry<String, Object>> entries = new LinkedList<Map.Entry<String, Object>>();

		if(rootObject instanceof SurfObject) {
			rootNodeBuilder.addAttribute(NODE_TYPE_LABEL, NodeType.SURF_OBJECT);
			rootNodeBuilder.addAttribute(SURF_OBJECT_TYPE_NAME_ATTRIBUTE_LABEL, ((SurfObject)rootObject).getTypeName().orElse(null));
			rootNodeBuilder.addAttribute(SURF_OBJECT_IRI_ATTRIBUTE_LABEL, ((SurfObject)rootObject).getIri().orElse(null));

			((SurfObject)rootObject).getPropertyNameValuePairs().forEach(entry -> entries.add(new NameValuePairMapEntry<String, Object>(entry)));
		}

		else if(rootObject instanceof Map) {
			rootNodeBuilder.addAttribute(NODE_TYPE_LABEL, NodeType.MAP);

			entries.addAll(((Map<String, Object>)rootObject).entrySet());
		}

		else {
			throw new AssertionError("The root object must be an instance of SurfObject or Map.");
		}

		return createHierarchy(rootNodeBuilder, entries).create();
	}

	/**
	 * Creates all the hierarchy of the given {@link ImmutableNode} based on the given properties.
	 * 
	 * @param nodeBuilder The {@link ImmutableNode} that will be built based on the given properties.
	 * @param nodeProperties The properties to be added to the given {@link ImmutableNode.Builder}.
	 * 
	 * @return The given {@link ImmutableNode.Builder} with all the properties provided added into it in order to allow method chaining.
	 */
	@SuppressWarnings("unchecked")
	private ImmutableNode.Builder createHierarchy(@Nonnull final ImmutableNode.Builder nodeBuilder,
			@Nonnull final Iterable<? extends Map.Entry<String, Object>> nodeProperties) {
		requireNonNull(nodeBuilder, "The given node to be built must not be <null>.");
		requireNonNull(nodeProperties, "The iterable with the properties to be added to the given node must not be <null>.");

		for(final Map.Entry<String, Object> childProperty : nodeProperties) {
			final ImmutableNode.Builder childNodeBuilder = new ImmutableNode.Builder().name(childProperty.getKey());
			final Object childNodeValue = childProperty.getValue();

			List<Map.Entry<String, Object>> entries = new LinkedList<>();

			if(childNodeValue instanceof SurfObject) {
				childNodeBuilder.addAttribute(NODE_TYPE_LABEL, NodeType.SURF_OBJECT);
				childNodeBuilder.addAttribute(SURF_OBJECT_TYPE_NAME_ATTRIBUTE_LABEL, ((SurfObject)childNodeValue).getTypeName().orElse(null));
				childNodeBuilder.addAttribute(SURF_OBJECT_TYPE_NAME_ATTRIBUTE_LABEL, ((SurfObject)childNodeValue).getIri().orElse(null));

				((SurfObject)childNodeValue).getPropertyNameValuePairs().forEach(entry -> entries.add(new NameValuePairMapEntry<String, Object>(entry)));
			}

			else if(childNodeValue instanceof Map) {
				childNodeBuilder.addAttribute(NODE_TYPE_LABEL, NodeType.MAP);

				entries.addAll(((Map<String, Object>)childNodeValue).entrySet());
			}

			else if(childNodeValue instanceof Set) {
				childNodeBuilder.addAttribute(NODE_TYPE_LABEL, NodeType.SET);

				((Set<Object>)childNodeValue).forEach(entry -> entries.add(new NameValuePairMapEntry<String, Object>(null, entry)));
			}

			else if(childNodeValue instanceof List) {
				childNodeBuilder.addAttribute(NODE_TYPE_LABEL, NodeType.LIST);

				((Set<Object>)childNodeValue).forEach(entry -> entries.add(new NameValuePairMapEntry<String, Object>(null, entry)));
			}

			else {
				childNodeBuilder.value(childNodeValue);
			}

			nodeBuilder.addChild(createHierarchy(childNodeBuilder, entries).create());
		}

		return nodeBuilder;
	}

	@Override
	protected Object getPropertyInternal(@Nonnull final String key) {
		List<QueryResult<ImmutableNode>> results = fetchNodeList(key);

		if(results.isEmpty()) {
			return null;
		} else {
			final List<ImmutableNode> resultList = new ArrayList<ImmutableNode>();

			for(QueryResult<ImmutableNode> result : results) {
				resultList.add(result.getNode());
			}

			return toObject(resultList.get(0));
		}

	}

	/** Calculates the size of the {@link SurfConfiguration} based on the number of values stored in it. */
	@Override
	protected int sizeInternal() {
		return sizeInternal(getNodeModel().getRootNode());
	}

	/**
	 * Recursive method to help with the implementation of {@link #sizeInternal()}.
	 * 
	 * @param node The node that will be used to check for the size.
	 * @return The number of child nodes of the given {@link ImmutableNode}. This method considers all the levels below the level of the provided
	 *         {@link ImmutableNode}.
	 */
	private int sizeInternal(@Nonnull final ImmutableNode node) {
		if(node.getValue() != null) {
			return 1;
		}

		int numberOfChilds = 0;

		for(final ImmutableNode childNode : node.getChildren()) {
			numberOfChilds += sizeInternal(childNode);
		}

		return numberOfChilds;
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
	 * Class created to represent the node types supported by a SURF document.
	 * 
	 * @author Magno N A Cruz
	 */
	private enum NodeType {
		SURF_OBJECT, MAP, SET, LIST;
	}

}
