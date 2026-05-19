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
