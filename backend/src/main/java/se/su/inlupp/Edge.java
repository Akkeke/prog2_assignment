package se.su.inlupp;

public class Edge<T> {

  private final T destination;
  private final String name;
  private int weight;

  public Edge(T destination, int weight, String name) {
    if (weight < 0) throw new IllegalArgumentException();
    this.destination = destination;
    this.weight = weight;
    this.name = name;
  }

  public int getWeight() {
    return weight;
  }

  public void setWeight(int weight) {
    if (weight < 0) throw new IllegalArgumentException();
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
    return String.format("till %s med %s tar %d", getDestination(), getName(), getWeight());
  }
}
