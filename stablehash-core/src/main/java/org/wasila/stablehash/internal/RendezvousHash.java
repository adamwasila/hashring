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
package org.wasila.stablehash.internal;

import org.wasila.stablehash.StableHash;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Weighted Rendezvous hashing implementation base on algorithm presented by Jason Resch.
 *
 * For more info and comparison to different methods, please visit:
 * http://www.snia.org/sites/default/files/SDC15_presentations/dist_sys/Jason_Resch_New_Consistent_Hashings_Rev.pdf
 *
 * @param <N> Node type
 */
public class RendezvousHash<N> implements StableHash<N> {

    private HashUtil hashUtil;

    private final Map<N, Integer> nodes;

    public RendezvousHash() {
        hashUtil = new HashUtil();
        nodes = new HashMap<>();
    }

    public RendezvousHash(Collection<N> nodesList) {
        this();
        nodes.putAll(nodesList.stream().collect(Collectors.toMap(node -> node, node -> 1)));
    }

    public RendezvousHash(Map<N, Integer> weightedNodesList) {
        this();
        this.nodes.putAll(weightedNodesList);
    }

    @Override
    public Optional<N> getNode(String stringKey) {
        double highestScore = -1;
        N champion = null;
        for (Map.Entry<N, Integer> entry : nodes.entrySet()) {
            double newScore = getWeightedScore(stringKey, entry.getKey(), entry.getValue());
            if (newScore > highestScore) {
                champion = entry.getKey();
                highestScore = newScore;
            }
        }
        return Optional.ofNullable(champion);
    }

    @Override
    public Set<N> getNodes(String stringKey, int size) {
        Set<Pair<N, Double>> sortedSet = new TreeSet<>(Collections.reverseOrder(Comparator.comparingDouble(Pair::getLast)));

        for (Map.Entry<N, Integer> entry : nodes.entrySet()) {
            sortedSet.add(new Pair<>(entry.getKey(), getWeightedScore(stringKey, entry.getKey(), entry.getValue())));
        }

        return sortedSet.stream().limit(size).map(pair -> pair.getFirst()).collect(Collectors.toSet());
    }

    @Override
    public RendezvousHash<N> addNode(N nodeName) {
        return addWeightedNode(nodeName, 1);
    }

    @Override
    public RendezvousHash<N> addWeightedNode(N nodeName, int weight) {
        if (nodeName == null) {
            throw new NullPointerException("nodeName must not be null");
        }
        if (weight <= 0) {
            throw new IllegalArgumentException("Invalid weight value: " + weight);
        }

        Integer oldWeight = nodes.get(nodeName);
        if (oldWeight != null && oldWeight == 1) {
            return this;
        }
        Map<N, Integer> newNodes = new HashMap<>(nodes);
        newNodes.put(nodeName, 1);
        return new RendezvousHash<>(newNodes);
    }

    @Override
    public RendezvousHash<N> updateWeightedNode(N nodeName, int weight) {
        return addWeightedNode(nodeName, weight);
    }

    @Override
    public RendezvousHash<N> removeNode(N nodeName) {
        if (!nodes.containsKey(nodeName)) {
            return this;
        }
        Map<N, Integer> newNodes = new HashMap<>();
        newNodes.putAll(nodes);
        newNodes.remove(nodeName);
        return new RendezvousHash<>(newNodes);
    }

    private double getWeightedScore(String keyString, N node, int weight) {
        HashKey key = hashUtil.genKey(node.toString() + keyString);
        double score = 1.0 / -Math.log(key.toDouble());
        return weight * score;
    }

}
