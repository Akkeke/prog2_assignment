package se.su.inlupp;

public class test {
    public static void main(String[] args) {
        ListGraph<String> graph = new ListGraph<>();
        String stockholm = "Stockholm";
        String goteborg = "Göteborg";
        String malmo = "Malmö";
        String nykoping = "Nyköping";

        graph.add(stockholm);
        graph.add(goteborg);
        graph.add(malmo);
        graph.add(nykoping);

        graph.connect(stockholm, goteborg, "E4", 100);
        graph.connect(stockholm, malmo, "E20", 15);
        graph.connect(goteborg, nykoping, "E18", 150);

        System.out.println(graph);

    }
}
