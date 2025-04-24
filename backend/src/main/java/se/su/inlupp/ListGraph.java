package se.su.inlupp;

import java.util.*;

public class ListGraph<T> implements Graph<T> {

private final Map<T, Set<Edge<T>>> connectionMap = new HashMap<>();


  @Override
  public void add(T node) {
    connectionMap.putIfAbsent(node, new HashSet<>());
  }

  @Override
  public void connect(T node1, T node2, String name, int weight) {
    add(node1);
    add(node2);

    Set<Edge> fromNodes = cities.get(node1);
    Set<Edge> toNodes = cities.get(node2);

    fromNodes.add(new Edge<T>(node2, weight, name));
    toNodes.add(new Edge<T>(node1, weight, name));
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
    if (!nodeExists(from) || !nodeExists(to)) return false;
    Set<T> visited = new HashSet<>();
    recursiveVisitAllDepthFirst(from, to, visited);
    return visited.contains(to);
  }

  @Override
  public List<Edge<T>> getPath(T from, T to) {
    if(!pathExists(from, to)) return null;
    List<Edge<T>> path = new ArrayList<>();
    Set<T> visited = new HashSet<>();
    if(depthFirstSearch(from, to, visited, path)) return path;

    throw new UnsupportedOperationException("Unimplemented method 'getPath'");
  }
}
