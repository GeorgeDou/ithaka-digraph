/*
 * Copyright 2008 Odysseus Software GmbH, Frankfurt am Main/Germany.
 */
package de.odysseus.ithaka.digraph;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Directed graph implementation. Nodes are added automatically if they appear
 * in an edge.
 * 
 * @author Christoph Beck
 * @author Oliver Stuhr
 */
public class MapDigraph<V, E> implements Digraph<V, E> {
	public static interface VertexMapFactory<V, E> {
		public Map<V, Map<V, E>> create();
	}

	public static interface EdgeMapFactory<V, E> {
		public Map<V, E> create(V source);
	}

	private static final <V, E> VertexMapFactory<V, E> getDefaultVertexMapFactory(final Comparator<? super V> comparator) {
		return new VertexMapFactory<V, E>() {
			@Override
			public Map<V, Map<V, E>> create() {
				if (comparator == null) {
					return new LinkedHashMap<V, Map<V, E>>(16);
				} else {
					return new TreeMap<V, Map<V, E>>(comparator);
				}
			}
		};
	};

	private static final <V, E> EdgeMapFactory<V, E> getDefaultEdgeMapFactory(final Comparator<? super V> comparator) {
		return new EdgeMapFactory<V, E>() {
			@Override
			public Map<V, E> create(V ignore) {
				if (comparator == null) {
					return new LinkedHashMap<V, E>(16);
				} else {
					return new TreeMap<V, E>(comparator);
				}
			}
		};
	};

	public static <V, E> DigraphFactory<MapDigraph<V, E>> getDefaultDigraphFactory() {
		return new DigraphFactory<MapDigraph<V, E>>() {
			@Override
			public MapDigraph<V, E> create() {
				return new MapDigraph<V, E>();
			}
		};
	}

	private final VertexMapFactory<V, E> vertexMapFactory;
	private final EdgeMapFactory<V, E> edgeMapFactory;
	private final Map<V, Map<V, E>> vertexMap;

	private int edgeCount;

	public MapDigraph() {
		this(null);
	}

	public MapDigraph(final Comparator<? super V> comparator) {
		this(comparator, comparator);
	}

	public MapDigraph(final Comparator<? super V> vertexComparator, final Comparator<? super V> edgeComparator) {
		this(MapDigraph.<V, E> getDefaultVertexMapFactory(vertexComparator), MapDigraph.<V, E> getDefaultEdgeMapFactory(edgeComparator));
	}

	public MapDigraph(VertexMapFactory<V, E> vertexMapFactory, EdgeMapFactory<V, E> edgeMapFactory) {
		this.vertexMapFactory = vertexMapFactory;
		this.edgeMapFactory = edgeMapFactory;

		vertexMap = vertexMapFactory.create();
	}

	@Override
	public boolean add(V vertex) {
		if (!vertexMap.containsKey(vertex)) {
			vertexMap.put(vertex, Collections.<V, E> emptyMap());
			return true;
		}
		return false;
	}

	@Override
	public E put(V source, V target, E edge) {
		Map<V, E> edgeMap = vertexMap.get(source);
		if (edgeMap == null || edgeMap.isEmpty()) {
			vertexMap.put(source, edgeMap = edgeMapFactory.create(source));
		}
		E result = edgeMap.put(target, edge);
		if (result == null) {
			add(target);
			edgeCount++;
		}
		return result;
	}

	@Override
	public E get(Object source, Object target) {
		Map<V, E> edgeMap = vertexMap.get(source);
		if (edgeMap == null) {
			return null;
		}
		return edgeMap.get(target);
	}

	@Override
	public E remove(V source, V target) {
		Map<V, E> edgeMap = vertexMap.get(source);
		if (edgeMap == null || !edgeMap.containsKey(target)) {
			return null;
		}
		E result = edgeMap.remove(target);
		edgeCount--;
		if (edgeMap.isEmpty()) {
			vertexMap.put(source, Collections.<V, E> emptyMap());
		}
		return result;
	}

	@Override
	public boolean remove(V vertex) {
		Map<V, E> edgeMap = vertexMap.get(vertex);
		if (edgeMap == null) {
			return false;
		}
		edgeCount -= edgeMap.size();
		vertexMap.remove(vertex);
		for (V source : vertexMap.keySet()) {
			remove(source, vertex);
		}
		return true;
	}

	@Override
	public void removeAll(Collection<V> vertices) {
		for (V vertex : vertices) {
			Map<V, E> edgeMap = vertexMap.get(vertex);
			if (edgeMap != null) {
				edgeCount -= edgeMap.size();
				vertexMap.remove(vertex);
			}
		}
		for (V source : vertexMap.keySet()) {
			Map<V, E> edgeMap = vertexMap.get(source);
			Iterator<V> iter = edgeMap.keySet().iterator();
			while (iter.hasNext()) {
				if (vertices.contains(iter.next())) {
					iter.remove();
					edgeCount--;
				}
			}
			if (edgeMap.isEmpty()) {
				vertexMap.put(source, Collections.<V, E> emptyMap());
			}
		}
	}

	@Override
	public boolean contains(Object source, Object target) {
		Map<V, E> edgeMap = vertexMap.get(source);
		if (edgeMap == null) {
			return false;
		}
		return edgeMap.containsKey(target);
	}

	@Override
	public boolean contains(Object vertex) {
		return vertexMap.containsKey(vertex);
	}

	@Override
	public Iterable<V> vertices() {
		if (vertexMap.isEmpty()) {
			return Collections.emptySet();
		}
		return new Iterable<V>() {
			@Override
			public Iterator<V> iterator() {
				return new Iterator<V>() {
					Iterator<V> delegate = vertexMap.keySet().iterator();
					V vertex = null;

					@Override
					public boolean hasNext() {
						return delegate.hasNext();
					}

					@Override
					public V next() {
						return vertex = delegate.next();
					}

					@Override
					public void remove() {
						Map<V, E> edgeMap = vertexMap.get(vertex);
						delegate.remove();
						edgeCount -= edgeMap.size();
						for (V source : vertexMap.keySet()) {
							MapDigraph.this.remove(source, vertex);
						}
					}
				};
			}

			@Override
			public String toString() {
				return vertexMap.keySet().toString();
			}
		};
	}

	@Override
	public Iterable<V> targets(final Object source) {
		final Map<V, E> edgeMap = vertexMap.get(source);
		if (edgeMap == null || edgeMap.isEmpty()) {
			return Collections.emptySet();
		}
		return new Iterable<V>() {
			@Override
			public Iterator<V> iterator() {
				return new Iterator<V>() {
					Iterator<V> delegate = edgeMap.keySet().iterator();

					@Override
					public boolean hasNext() {
						return delegate.hasNext();
					}

					@Override
					public V next() {
						return delegate.next();
					}

					@Override
					public void remove() {
						delegate.remove();
						edgeCount--;
						if (edgeMap.isEmpty()) {
							@SuppressWarnings("unchecked")
							V v = (V) source;
							vertexMap.put(v, Collections.<V, E> emptyMap());
						}
					}
				};
			}

			@Override
			public String toString() {
				return edgeMap.keySet().toString();
			}
		};
	}

	@Override
	public int getVertexCount() {
		return vertexMap.size();
	}

	@Override
	public int getOutDegree(Object vertex) {
		Map<V, E> edgeMap = vertexMap.get(vertex);
		if (edgeMap == null) {
			return 0;
		}
		return edgeMap.size();
	}

	@Override
	public int getEdgeCount() {
		return edgeCount;
	}

	public DigraphFactory<? extends MapDigraph<V, E>> getDigraphFactory() {
		return new DigraphFactory<MapDigraph<V, E>>() {
			@Override
			public MapDigraph<V, E> create() {
				return new MapDigraph<V, E>(vertexMapFactory, edgeMapFactory);
			}
		};
	}

	@Override
	public MapDigraph<V, E> reverse() {
		return Digraphs.<V, E, MapDigraph<V, E>> reverse(this, getDigraphFactory());
	}

	@Override
	public MapDigraph<V, E> subgraph(Set<V> vertices) {
		return Digraphs.<V, E, MapDigraph<V, E>> subgraph(this, vertices, getDigraphFactory());
	}

	@Override
	public boolean isAcyclic() {
		return Digraphs.isAcyclic(this);
	}

	@Override
	public String toString() {
		StringBuffer b = new StringBuffer();
		b.append(getClass().getName().substring(getClass().getName().lastIndexOf('.') + 1));
		b.append("(");
		Iterator<V> vertices = vertices().iterator();
		while (vertices.hasNext()) {
			V v = vertices.next();
			b.append(v);
			b.append(targets(v));
			if (vertices.hasNext()) {
				b.append(", ");
				if (b.length() > 1000) {
					b.append("...");
					break;
				}
			}
		}
		b.append(")");
		return b.toString();
	}
}
