package se.su.inlupp;

public class Edge<T> {

  private T destination;
  private int weight;
  private String name;

  public Edge(T destination, int weight, String name) {
    this.destination = destination;
    this.weight = weight;
    this.name = name;
  }

  int getWeight() {
    return weight;
  }

  void setWeight(int weight) {
    this.weight = weight;
  }

  T getDestination() {
    return destination;
  }

  String getName() {
    return name;
  }
}
