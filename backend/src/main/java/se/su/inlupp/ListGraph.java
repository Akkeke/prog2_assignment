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
    if (!nodeExists(node1) || !nodeExists(node2)) throw new NoSuchElementException();
    if (weight < 0) throw new IllegalArgumentException();
    for (Edge<T> e : connectionMap.get(node1)) {
      if (e.getDestination().equals(node2)) throw new IllegalStateException();
    }

    connectionMap.get(node1).add(new Edge<T>(node2, weight, name));
    connectionMap.get(node2).add(new Edge<T>(node1, weight, name));
  }

  @Override
  public void setConnectionWeight(T node1, T node2, int weight) {
    if (weight < 0) throw new IllegalArgumentException();
    if (!nodeExists(node1) || !nodeExists(node2)) throw new NoSuchElementException();
    Edge<T> edge1 = getEdgeBetween(node1, node2);
    Edge<T> edge2 = getEdgeBetween(node2, node1);

    if (edge1 != null && edge2 != null) {
      edge1.setWeight(weight);
      edge2.setWeight(weight);
    } else {
      throw new NoSuchElementException();
    }
  }

  @Override
  public Set<T> getNodes() {
    return Collections.unmodifiableSet(connectionMap.keySet());
  }

  @Override
  public Collection<Edge<T>> getEdgesFrom(T node) {
    if (!nodeExists(node)) throw new NoSuchElementException();
    return Collections.unmodifiableCollection(connectionMap.get(node));
  }

  @Override
  public Edge<T> getEdgeBetween(T node1, T node2) {
    if (!nodeExists(node1) || !nodeExists(node2)) throw new NoSuchElementException();
    for(Edge<T> e : connectionMap.get(node1)) {
      if(e.getDestination().equals(node2)) {
        return e;
      }
    }
    return null;
  }

  @Override
  public void disconnect(T node1, T node2) {
    if (!nodeExists(node1) || !nodeExists(node2)) throw new NoSuchElementException();
    Edge<T> edge1 = getEdgeBetween(node1, node2);
    Edge<T> edge2 = getEdgeBetween(node2, node1);
    if (edge1 != null && edge2 != null) {
      connectionMap.get(node1).remove(edge1);
      connectionMap.get(node2).remove(edge2);
    } else {
      throw new IllegalStateException();
    }

  }

  @Override
  public void remove(T node) {
    if (!nodeExists(node)) throw new NoSuchElementException();
    connectionMap.remove(node);
    for (T n : connectionMap.keySet()) {
      connectionMap.get(n).removeIf(e -> e.getDestination().equals(node));
    }
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

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (Map.Entry<T, Set<Edge<T>>> kv : connectionMap.entrySet()) {
      sb.append(kv.getKey()).append(": ").append(kv.getValue()).append("\n");
    }
    return sb.toString();
  }

  // HELP METHODS

  private boolean nodeExists(T node) {
    return connectionMap.containsKey(node);
  }

  private void recursiveVisitAllDepthFirst(T from, T to, Set<T> visited) {
    visited.add(from);
    for (Edge<T> e: connectionMap.get(from)) {
      T currentNode = e.getDestination();
      if (currentNode.equals(to)) {
        visited.add(currentNode);
        return;
      }
      if (!visited.contains(currentNode)) {
        recursiveVisitAllDepthFirst(currentNode, to, visited);
      }
    }
  }

  private boolean depthFirstSearch(T from, T to, Set<T> visited, List<Edge<T>> path) {
    visited.add(from);
    if (from.equals(to)) {
      return true;
    }
    for (Edge<T> e : connectionMap.get(from)) {
      if (!visited.contains(e.getDestination())) {
        path.add(e);
        if (depthFirstSearch(e.getDestination(), to, visited, path)) return true;
        path.remove(path.size()-1);
      }
    }
    return false;
  }

  private void visitAllWidthFirst(T from, T to, LinkedList<T> queue, Map<T, T> connections) {
    queue.add(from);
    connections.put(from, null);

    while (!queue.isEmpty()) {
      T currentNode = queue.pop();
      for (Edge<T> e : connectionMap.get(currentNode)) {
        T nextNode = e.getDestination();
        if (!connections.containsKey(nextNode)) {
          connections.put(nextNode, currentNode);
          queue.add(nextNode);
          if (nextNode.equals(to)) {
            queue.clear();
            break;
          }
        }
      }
    }
  }

}