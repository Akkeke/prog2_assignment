package se.su.inlupp;

import java.util.*;

public class ListGraph<T> implements Graph<T> {

private Map<T, Set<Edge>> cities = new HashMap<>();

  //hello world
  //

  @Override
  public void add(T node) {
    cities.putIfAbsent(node, new HashSet<>());
    throw new UnsupportedOperationException("Unimplemented method 'add'");
  }

  @Override
  public void connect(T node1, T node2, String name, int weight) {
    add(node1);
    add(node2);

    Set<Edge> fromNodes = cities.get(node1);
    Set<Edge> toNodes = cities.get(node2);

    fromNodes.add(new Edge(node2, name, weight));
    toNodes.add(new Edge(node1, name, weight));
    throw new UnsupportedOperationException("Unimplemented method 'connect'");
  }

  @Override
  public void setConnectionWeight(T node1, T node2, int weight) {
    throw new UnsupportedOperationException("Unimplemented method 'setConnectionWeight'");
  }

  @Override
  public Set<T> getNodes() {
    throw new UnsupportedOperationException("Unimplemented method 'getNodes'");
  }

  @Override
  public Collection<Edge<T>> getEdgesFrom(T node) {
    throw new UnsupportedOperationException("Unimplemented method 'getEdgesFrom'");
  }

  @Override
  public Edge<T> getEdgeBetween(T node1, T node2) {


    throw new UnsupportedOperationException("Unimplemented method 'getEdgeBetween'");
  }

  @Override
  public void disconnect(T node1, T node2) {
    throw new UnsupportedOperationException("Unimplemented method 'disconnect'");
  }

  @Override
  public void remove(T node) {
    throw new UnsupportedOperationException("Unimplemented method 'remove'");
  }

  @Override
  public boolean pathExists(T from, T to) {
    throw new UnsupportedOperationException("Unimplemented method 'pathExists'");
  }

  @Override
  public List<Edge<T>> getPath(T from, T to) {
    throw new UnsupportedOperationException("Unimplemented method 'getPath'");
  }
}
