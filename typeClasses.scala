// example typeclass with an extension method
trait Show[A]:
  extension (a: A)
    def show: String

object Show:
  given Show[String]:
    extension (a: String)
      def show: String = a

  given Show[Int]:
    extension (a: Int)
      def show: String = a.toString

// example typeclass with method that returns subject type
trait Duplicate[A]:
  def dup(a: A): A

object Duplicate:
  given Duplicate[Int]:
    def dup(a: Int): Int = 2*a

  given Duplicate[String]:
    def dup(a: String): String = a+a
