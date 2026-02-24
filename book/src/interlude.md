# Interlude: Exploring Type Design with a Compiler Conversation

The previous steps taught specific type-level features one at a time. But in practice,
the hard part isn't knowing what `+A` or `<:` means — it's knowing **which tool to reach for**
when you're designing something new.

This is where pairing the Scala compiler with an AI coding assistant becomes genuinely useful.
Not as a crutch, but as a way to **iterate faster through design alternatives**.

Here's a real scenario: you're building an order summary system and need a function
that formats different product types for invoices. You know several approaches exist,
but which one fits best?

## The Problem

```scala
// explore.scala

// Different product types have different invoice details
case class PhysicalItem(name: String, price: Double, weight: Double, shippingCost: Double)
case class DigitalItem(name: String, price: Double, downloadUrl: String)
case class Subscription(name: String, price: Double, billingCycle: String, nextBillingDate: String)

// You want a single `invoiceLine` function that handles all of them.
// But how should you parameterize it?
```

## Attempt 1: Upper Bound

Your first instinct might be a shared trait:

```scala
// explore1.scala

trait HasPrice:
  def price: Double

case class PhysicalItem(name: String, price: Double, weight: Double) extends HasPrice
case class DigitalItem(name: String, price: Double, downloadUrl: String) extends HasPrice

def invoiceLine[A <: HasPrice](item: A): String =
  s"Item: $$${item.price}"
  // ...but how do I access weight or downloadUrl? I only know about price.

@main def explore1(): Unit =
  println(invoiceLine(PhysicalItem("Keyboard", 79.99, 0.5)))
```

**Compile and observe:** It works, but `invoiceLine` can only use `price`.
The bound gives you access to the *shared* interface, nothing more.

```
> scala-cli compile explore1.scala
> # It compiles, but the function is too generic to be useful.
```

*If you're working with an AI assistant, this is a good moment to share the code and ask:*
*"This compiles but I can't access type-specific fields. What are my options?"*

## Attempt 2: Typeclass

The AI (or your own experience) might suggest a typeclass:

```scala
// explore2.scala

case class PhysicalItem(name: String, price: Double, weight: Double, shippingCost: Double)
case class DigitalItem(name: String, price: Double, downloadUrl: String)
case class Subscription(name: String, price: Double, billingCycle: String, nextBillingDate: String)

// Define the capability: "can produce an invoice line"
trait InvoiceLine[A]:
  def format(item: A): String

object InvoiceLine:
  given InvoiceLine[PhysicalItem] with
    def format(item: PhysicalItem): String =
      s"${item.name} | $$${item.price} + $$${item.shippingCost} shipping (${item.weight}kg)"

  given InvoiceLine[DigitalItem] with
    def format(item: DigitalItem): String =
      s"${item.name} | $$${item.price} (download: ${item.downloadUrl})"

  given InvoiceLine[Subscription] with
    def format(item: Subscription): String =
      s"${item.name} | $$${item.price}/${item.billingCycle} (next: ${item.nextBillingDate})"

def invoiceLine[A: InvoiceLine](item: A): String =
  summon[InvoiceLine[A]].format(item)

@main def explore2(): Unit =
  println(invoiceLine(PhysicalItem("Keyboard", 79.99, 0.5, 5.99)))
  println(invoiceLine(DigitalItem("E-book", 14.99, "https://example.com/dl/123")))
  println(invoiceLine(Subscription("Cloud Storage", 9.99, "month", "2024-03-01")))

  // New type without InvoiceLine? Compile error.
  // case class GiftCard(name: String, value: Double)
  // invoiceLine(GiftCard("Amazon", 50.0))
```

This works well. Each type gets its own formatting logic. New types can be added without
modifying existing code. The compiler ensures every type passed to `invoiceLine` has an
`InvoiceLine` instance.

## Attempt 3: Match Type (Spoiler — Wrong Tool)

What if you tried a match type instead?

```scala
// explore3.scala

case class PhysicalItem(name: String, price: Double, weight: Double)
case class DigitalItem(name: String, price: Double, downloadUrl: String)

type InvoiceString[A] = A match
  case PhysicalItem => String
  case DigitalItem  => String

// The return type is always String regardless of input...
// Match type adds nothing here. It's the wrong tool for this job.
```

**Compile and think about it.** A match type computes a *different output type* based on input type.
When the output is always the same type (`String`), there's no type-level computation to do.

*This is exactly the kind of dead end that's worth hitting.* It teaches you when NOT to use a tool.

## The Decision Framework

After exploring these attempts, here's the pattern:

```
"I need access to shared fields"
  → Upper bound (A <: Trait)

"I need different behavior per type, output type is the same"
  → Typeclass (given/using)

"I need the output TYPE to change based on input type"
  → Match type

"I need to reject invalid values at compile time"
  → inline + compiletime.error

"I need a method that only exists when types satisfy a condition"
  → =:= or <:< evidence

"I need a type that's an internal detail, not visible to callers"
  → Type member

"I need to prevent mixing values from different instances"
  → Path-dependent type (type member)
```

This framework doesn't come from reading documentation. It comes from
**trying the wrong tool, reading the compiler's response, and understanding why it didn't fit.**

An AI assistant accelerates this loop: you share compiler output, describe your intent,
and iterate through alternatives in minutes instead of hours. But the understanding
comes from you reading the compiler's feedback at each step — the AI just helps you
navigate faster.
