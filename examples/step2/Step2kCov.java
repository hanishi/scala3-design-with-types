// Step2kCov.java — Java's use-site covariance: ? extends

import java.util.List;

class Item {
    String name;
    double price;
    Item(String name, double price) { this.name = name; this.price = price; }
    public String toString() { return name + " ($" + price + ")"; }
}

class Book extends Item {
    Book(String name, double price, String isbn) { super(name, price); }
}

class DVD extends Item {
    DVD(String name, double price) { super(name, price); }
}

public class Step2kCov {
    // In Scala: def cheapest[A <: Item](items: List[A]): A
    // List[+A] is covariant — List[Book] IS a List[Item].
    //
    // In Java: List<Book> is NOT List<Item> — generics are invariant.
    // To accept a List of any Item subtype, you write ? extends EVERY TIME:
    static Item cheapest(List<? extends Item> items) {
        Item min = items.get(0);
        for (Item item : items) {
            if (item.price < min.price) min = item;
        }
        return min;
        // items.add(new DVD("X", 1.0));  // Compile error! Can't add to ? extends
    }

    public static void main(String[] args) {
        List<Book> books = List.of(
            new Book("Scala", 45.0, "978-1"),
            new Book("FP", 35.0, "978-2")
        );
        System.out.println("Cheapest book: " + cheapest(books));

        List<DVD> dvds = List.of(
            new DVD("The Matrix", 19.99),
            new DVD("Inception", 24.99)
        );
        System.out.println("Cheapest DVD: " + cheapest(dvds));

        // Without ? extends, this would NOT compile:
        // static Item cheapest(List<Item> items) — only accepts List<Item>, not List<Book>
    }
}
