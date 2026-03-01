// Step2kCon.java — Java's use-site contravariance: ? super

import java.util.List;
import java.util.ArrayList;
import java.util.function.Predicate;

class Item {
    String name;
    double price;
    Item(String name, double price) { this.name = name; this.price = price; }
    public String toString() { return name + " ($" + price + ")"; }
}

class Book extends Item {
    Book(String name, double price) { super(name, price); }
}

class DVD extends Item {
    DVD(String name, double price) { super(name, price); }
}

public class Step2kCon {
    // In Scala: Function1[-A, +B] — a function on Item works where Book is expected.
    // The class declares -A, and it just works everywhere.
    //
    // In Java: Predicate<Item> is NOT Predicate<Book> — generics are invariant.
    // But Predicate declares @FunctionalInterface with ? super,
    // so filter() accepts Predicate<? super Book>.

    // Use-site contravariance: ? super Book (write-only — like Scala's -A)
    static void addBooks(List<? super Book> target) {
        target.add(new Book("Scala", 45.0));
        target.add(new Book("FP", 35.0));
        // Book b = target.get(0);  // Compile error! Can only get Object
    }

    public static void main(String[] args) {
        // --- ? super: contravariance at the call site ---
        List<Item> items = new ArrayList<>();
        addBooks(items);  // List<Item> → List<? super Book>
        System.out.println("Items: " + items);

        // --- Predicate: contravariant in its input ---
        List<Book> books = List.of(
            new Book("Scala", 45.0),
            new Book("FP", 35.0)
        );
        Predicate<Item> cheap = item -> item.price < 40.0;
        long count = books.stream().filter(cheap).count();
        System.out.println("Cheap books: " + count);
        // In Scala this is just: books.filter(_.price < 40.0)
        // Function1[-A, +B] makes Item => Boolean usable as Book => Boolean.
    }
}
