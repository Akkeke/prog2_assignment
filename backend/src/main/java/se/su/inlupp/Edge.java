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

  public int getWeight() {
    return weight;
  }

  public void setWeight(int weight) {
    this.weight = weight;
  }

  public T getDestination() {
    return destination;
  }

  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return String.format("Edge: [Destination: %s; Weight: %d; Name: %s;]", getDestination(), getWeight(), getName());
  }
}
