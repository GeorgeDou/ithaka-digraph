/*
 * Copyright 2012 Odysseus Software GmbH
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
package de.odysseus.ithaka.digraph;

import java.util.Collection;

/**
 * Unmodifiable digraph adapter.
 * Overrides <code>add</code>, <code>put</code>, <code>remove</code>, <code>removeAll</code> to throw an exception.
 *
 * @param <V> vertex type
 * @param <E> edge type
 */
public class UnmodifiableDigraph<V, E> extends DigraphAdapter<V, E> {
	public UnmodifiableDigraph(Digraph<V, E> digraph) {
		super(digraph);
	}
	
	/**
	 * @throws UnsupportedOperationException
	 */
	@Override
	public final boolean add(V vertex) {
		throw new UnsupportedOperationException("This digraph is readonly!");
	}

	/**
	 * @throws UnsupportedOperationException
	 */
	@Override
	public final E put(V source, V target, E edge) {
		throw new UnsupportedOperationException("This digraph is readonly!");
	}
	
	/**
	 * @throws UnsupportedOperationException
	 */
	@Override
	public final boolean remove(V vertex) {
		throw new UnsupportedOperationException("This digraph is readonly!");
	}
	
	/**
	 * @throws UnsupportedOperationException
	 */
	@Override
	public final E remove(V source, V target) {
		throw new UnsupportedOperationException("This digraph is readonly!");
	}
	
	/**
	 * @throws UnsupportedOperationException
	 */
	@Override
	public final void removeAll(Collection<V> vertices) {
		throw new UnsupportedOperationException("This digraph is readonly!");
	}
}
