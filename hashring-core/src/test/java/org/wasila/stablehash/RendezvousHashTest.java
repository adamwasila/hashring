/**
 * (C) Copyright 2017 Adam Wasila.
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
package org.wasila.stablehash;

import org.junit.Assert;
import org.junit.Test;
import org.wasila.stablehash.internal.RendezvousHash;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class RendezvousHashTest {

    StableHash<String> hash;

    @Test
    public void testSingleNodeABC() {
        List<String> nodes = Arrays.asList(new String[] {"a", "b", "c"});
        hash = new RendezvousHash<>(nodes);

        expectNode("test", "b");
        expectNode("test1", "b");
        expectNode("test2", "a");
        expectNode("test3", "a");
        expectNode("test4", "c");
        expectNode("test5", "c");
        expectNode("aaaa", "b");
        expectNode("bbbb", "a");

    }

    @Test
    public void testAddNode() {
        List<String> nodes = Arrays.asList(new String[] {"a", "b", "c"});
        hash = new RendezvousHash<>(nodes);
        hash = hash.addNode("d");

        expectNode("test", "b");
        expectNode("test1", "d"); // *
        expectNode("test2", "a");
        expectNode("test3", "a");
        expectNode("test4", "c");
        expectNode("test5", "c");
        expectNode("aaaa", "d"); // *
        expectNode("bbbb", "d"); // *

        hash = hash.removeNode("d");

        expectNode("test", "b");
        expectNode("test1", "b");
        expectNode("test2", "a");
        expectNode("test3", "a");
        expectNode("test4", "c");
        expectNode("test5", "c");
        expectNode("aaaa", "b");
        expectNode("bbbb", "a");

    }

    @Test
    public void testRemoveNode() {
        List<String> nodes = Arrays.asList(new String[] {"a", "b", "c", "d"});
        hash = new RendezvousHash<>(nodes);

        expectNode("test", "b");
        expectNode("test1", "d");
        expectNode("test2", "a");
        expectNode("test3", "a");
        expectNode("test4", "c");
        expectNode("test5", "c");
        expectNode("aaaa", "d");
        expectNode("bbbb", "d");

        hash = hash.removeNode("a");

        expectNode("test", "b");
        expectNode("test1", "d");
        expectNode("test4", "c");
        expectNode("test5", "c");
        expectNode("aaaa", "d");
        expectNode("bbbb", "d");

        hash = hash.removeNode("c");

        expectNode("test", "b");
        expectNode("test1", "d");
        expectNode("aaaa", "d");
        expectNode("bbbb", "d");

        hash = hash.removeNode("b");

        expectNode("test1", "d");
        expectNode("aaaa", "d");
        expectNode("bbbb", "d");

    }

    @Test
    public void expectNodeRangesABC() {
        List<String> nodes = Arrays.asList(new String[] {"a", "b", "c"});
        hash = StableHash.newRendezvousHash(nodes);

        expectNodes("test", new String[] {"b", "c"});
        expectNodes("test1", new String[] {"b", "c"});
        expectNodes("test2", new String[] {"a", "b"});
        expectNodes("test3", new String[] {"a", "c"});
        expectNodes("test4", new String[] {"a", "c"});
        expectNodes("test5", new String[] {"a", "c"});
        expectNodes("aaaa", new String[] {"a", "b"});
        expectNodes("bbbb", new String[] {"a", "c"});
    }

    private void expectNode(String key, String expectedNode) {
        Optional<String> node = hash.getNode(key);
        Assert.assertEquals(expectedNode, node.get());
    }

    private void expectNodes(String key, String... expectedNodes) {
        String[] nodes = hash.getNodes(key, expectedNodes.length).toArray(new String[0]);
        Assert.assertArrayEquals(expectedNodes, nodes);
    }

}
