package ai.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.function.Function;
import java.util.stream.Stream;

public class RootedTree<T> {
    private final T value;
    private RootedTree<T> parent;
    private final ArrayList<RootedTree<T>> children;

    public RootedTree(final T value) {
        this.value = value;
        parent = null;
        children = new ArrayList<>();
    }

    public RootedTree(final RootedTree<T> node) {
        this(node.get());
    }

    @SuppressWarnings("unused")
    public static <T> RootedTree<T> prunedTree(final RootedTree<T> root, final ArrayList<RootedTree<T>> leaves) {
        final HashMap<RootedTree<T>, RootedTree<T>> map2newTree = new HashMap<>();
        final RootedTree<T> newRoot = new RootedTree<>(root);
        map2newTree.put(root, newRoot);
        for (final RootedTree<T> leaf : leaves) {
            RootedTree<T> currentNode = leaf, childNewNode = null;
            while (currentNode != null) {
                boolean update = false;
                RootedTree<T> currentNewNode = map2newTree.get(currentNode);
                if (currentNewNode == null) {
                    currentNewNode = new RootedTree<>(currentNode);
                    map2newTree.put(currentNode, currentNewNode);
                    update = true;
                }
                if (childNewNode != null) {
                    currentNewNode.addChild(childNewNode);
                }
                childNewNode = currentNewNode;
                currentNode = currentNode.getParent();
                if (!update) {
                    break;
                }
            }
        }
//		MazAI.logger.println(newRoot);
        return newRoot;
    }

    public T get() {
        return value;
    }

    public RootedTree<T> getParent() {
        return parent;
    }

    void cutParent() {
        parent = null;
    }

    public ArrayList<RootedTree<T>> getChildren() {
        return children;
    }

    public static <T> ArrayList<RootedTree<T>> prunedPath(RootedTree<T> leaf) {
        final ArrayList<RootedTree<T>> al = new ArrayList<>();
        while (leaf != null) {
            al.add(leaf);
            leaf = leaf.parent;
        }
        Collections.reverse(al);
        return al;
    }

    private static <T> void buildStream(final RootedTree<T> v, final Stream.Builder<RootedTree<T>> builder) {
        builder.add(v);
        for (final RootedTree<T> u : v.getChildren()) {
            buildStream(u, builder);
        }
    }

    private static <T> void buildSliceStream(final RootedTree<T> v, final int currentLevel, final int targetLevel, final Stream.Builder<RootedTree<T>> builder) {
        if (currentLevel == targetLevel) {
            builder.add(v);
            return;
        }
        for (final RootedTree<T> u : v.getChildren()) {
            buildSliceStream(u, currentLevel + 1, targetLevel, builder);
        }
    }

    public void addChild(final RootedTree<T> child) {
        child.parent = this;
        children.add(child);
    }

    public <U> RootedTree<U> map(final Function<T, U> func) {
        final RootedTree<U> r = new RootedTree<>(func.apply(value));
        for (final RootedTree<T> child : children) {
            r.addChild(child.map(func));
        }
        return r;
    }

    public Stream<RootedTree<T>> stream() {
        final Stream.Builder<RootedTree<T>> builder = Stream.builder();
        buildStream(this, builder);
        return builder.build();
    }

    @SuppressWarnings("unused")
    public Stream<RootedTree<T>> sliceStream(final int level) {
        final Stream.Builder<RootedTree<T>> builder = Stream.builder();
        buildSliceStream(this, 0, level, builder);
        return builder.build();
    }

    public String toString() {
        return value.toString();
    }
}
