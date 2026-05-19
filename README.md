# Trait objects from Rust but in scala!
A so called *trait object* in rust is a way to have, for example, a collection of *things* that implement a certain trait, but without knowing the specific type of those things. This means that you could have however many different types of objects you want inside one single collection that can hold them all. A trait in rust is almost exactly the same as a type class in scala or haskell. This is my attempt at enabling this functionality in scala with minimum eye-snags or syntactic overhead at the use site.

**Note:** This is really just an excuse for me to practice reflection and macros. I don't really think this is very useful in scala, because it already has subtype polymorphism, which already does this more elegantly and with better language support. See this more as an esoteric example of what is possible in scala3!

**Another note:** As of writing this, compiling with `-Xcheck-macros` fails due to `Flags.ExtensionMethod` not being allowed in calls to `Symbol.newMethod`. I think this might be wrong, but have not found a way to circumvent this.

## Example

```scala
//> using scala 3.8.2
import scala.language.implicitConversions

def put[A: Show](a: A): Unit =
  println(a.show)

def printDuplicated[A: Duplicate as d](a: A) =
  println(d.dup(a))

@main def main(): Unit =
  val num = 3
  val str = "hej"

  println("Showable things:")
  val xs: Seq[Dyn[Show]] = Seq(num, str)
  xs.foreach(put)

  println("\nDuplicatable things:")
  val ys: Seq[Dyn[Duplicate]] = Seq(num, str)
  ys.foreach(printDuplicated)
```
