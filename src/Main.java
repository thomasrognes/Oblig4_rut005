/**
 * Obligatorisk oppgave 4 for Thomas Sebastian Rognes (Rut005)
 */

public class Main {
    public static void main(String[] args) {
        SortedTreeMap<Integer, String> myTree = new SortedTreeMap<Integer, String>();

        myTree.add(5,"Ford");
        myTree.add(9, "Peugeot");
        myTree.add(4, "Fiat");
        myTree.add(2, "Toyota");
        myTree.add(34, "Mercedes");

        System.out.println(myTree.size());
    }
}