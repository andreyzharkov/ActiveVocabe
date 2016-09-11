package dron.tree;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Andrey on 11.09.2016.
 */
public class Tree<T> {
    private Node<T> root;

    public Tree(T rootData) {
        root = new Node<>(rootData);
    }

    public static class Node<T> {
        private T data;
        private Node<T> parent;
        private List<Node<T>> children;

        public Node(T data){
            this.data = data;
            parent = null;
            children = new ArrayList<>();
        }

        public Node(T data, Node<T> parent){
            parent.children.add(this);
            this.data = data;
            this.parent = parent;
            this.children = new ArrayList<>();
        }
    }
}
