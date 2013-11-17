package app.xml;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

/**
 * An implementation of <a href="http://java.sun.com/javase/6/docs/api/javax/xml/namespace/NamespaceContext.html">
 * NamespaceContext </a>. Instances are immutable.
 * 
 * @author McDowell
 */
public final class NamespaceContextMap implements NamespaceContext {

	private static Map<String, String> toMap(final String... mappingPairs) {
		final Map<String, String> prefixMappings = new HashMap<String, String>(mappingPairs.length / 2);
		for (int i = 0; i < mappingPairs.length; i++) {
			prefixMappings.put(mappingPairs[i], mappingPairs[++i]);
		}
		return prefixMappings;
	}

	private final Map<String, String> prefixMap;

	private final Map<String, Set<String>> nsMap;

	/**
	 * Constructor that takes a map of XML prefix-namespaceURI values. A defensive copy is made of the map. An
	 * IllegalArgumentException will be thrown if the map attempts to remap the standard prefixes defined in the
	 * NamespaceContext contract.
	 * 
	 * @param prefixMappings
	 *            a map of prefix:namespaceURI values
	 */
	public NamespaceContextMap(final Map<String, String> prefixMappings) {
		prefixMap = createPrefixMap(prefixMappings);
		nsMap = createNamespaceMap(prefixMap);
	}

	/**
	 * Convenience constructor.
	 * 
	 * @param mappingPairs
	 *            pairs of prefix-namespaceURI values
	 */
	public NamespaceContextMap(final String... mappingPairs) {
		this(toMap(mappingPairs));
	}

	private void addConstant(final Map<String, String> prefixMap, final String prefix, final String nsURI) {
		final String previous = prefixMap.put(prefix, nsURI);
		if (previous != null && !previous.equals(nsURI)) {
			throw new IllegalArgumentException(prefix + " -> " + previous + "; see NamespaceContext contract");
		}
	}

	private void checkNotNull(final String value) {
		if (value == null) {
			throw new IllegalArgumentException("null");
		}
	}

	private Map<String, Set<String>> createNamespaceMap(final Map<String, String> prefixMap) {
		final Map<String, Set<String>> nsMap = new HashMap<String, Set<String>>();
		for (final Map.Entry<String, String> entry : prefixMap.entrySet()) {
			final String nsURI = entry.getValue();
			Set<String> prefixes = nsMap.get(nsURI);
			if (prefixes == null) {
				prefixes = new HashSet<String>();
				nsMap.put(nsURI, prefixes);
			}
			prefixes.add(entry.getKey());
		}
		for (final Map.Entry<String, Set<String>> entry : nsMap.entrySet()) {
			final Set<String> readOnly = Collections.unmodifiableSet(entry.getValue());
			entry.setValue(readOnly);
		}
		return nsMap;
	}

	private Map<String, String> createPrefixMap(final Map<String, String> prefixMappings) {
		final Map<String, String> prefixMap = new HashMap<String, String>(prefixMappings);
		addConstant(prefixMap, XMLConstants.XML_NS_PREFIX, XMLConstants.XML_NS_URI);
		addConstant(prefixMap, XMLConstants.XMLNS_ATTRIBUTE, XMLConstants.XMLNS_ATTRIBUTE_NS_URI);
		return Collections.unmodifiableMap(prefixMap);
	}

	/**
	 * @return an unmodifiable map of the mappings in the form prefix-namespaceURI
	 */
	public Map<String, String> getMap() {
		return prefixMap;
	}

	@Override
	public String getNamespaceURI(final String prefix) {
		checkNotNull(prefix);
		final String nsURI = prefixMap.get(prefix);
		return nsURI == null ? XMLConstants.XML_NS_URI : nsURI;
	}

	@Override
	public String getPrefix(final String namespaceURI) {
		checkNotNull(namespaceURI);
		final Set<String> set = nsMap.get(namespaceURI);
		return set == null ? null : set.iterator().next();
	}

	@Override
	public Iterator<String> getPrefixes(final String namespaceURI) {
		checkNotNull(namespaceURI);
		final Set<String> set = nsMap.get(namespaceURI);
		return set.iterator();
	}

}