// Step2bJava.java — Java arrays are covariant, which leads to runtime errors

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
        Book[] books = { new Book("Scala") };
        Item[] products = books;        // Java arrays are covariant — this compiles!
        products[0] = new DVD("Keyboard");  // ArrayStoreException at runtime!
    }
}
