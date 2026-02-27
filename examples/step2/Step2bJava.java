// Step2bJava.java — Java arrays are covariant, but generics are invariant

import java.util.List;
import java.util.ArrayList;

class Item {
    String name;
    Item(String name) { this.name = name; }
}

class Book extends Item {
    Book(String name) { super(name); }
}

class DVD extends Item {
    DVD(String name) { super(name); }
}

public class Step2bJava {
    public static void main(String[] args) {
        // --- Arrays: covariant (unsafe) ---
        Book[] books = { new Book("Scala") };
        Item[] items = books;              // Compiles — Java arrays are covariant
        items[0] = new DVD("The Matrix");  // ArrayStoreException at runtime!

        // --- Generics: invariant (safe) ---
        // List<Book> bookList = new ArrayList<>();
        // List<Item> itemList = bookList;  // Compile error! Java generics are invariant.
        // Java learned from the array mistake — generics don't allow this.
    }
}
