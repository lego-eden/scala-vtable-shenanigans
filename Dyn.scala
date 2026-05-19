import scala.language.implicitConversions

trait Dyn[F[_]]:
  given F[Typ] = instance
  type Typ
  val value: Typ
  val instance: F[Typ]
  override def toString = value.toString
  override def equals(obj: Any) =
    value == obj

object Dyn:
  def fromType[F[_], A: F as typeClass](a: A): Dyn[F] =
    new Dyn[F]:
      type Typ = A
      val value = a
      val instance = typeClass

  def apply[F[_], A: F](a: A): Dyn[F] = fromType(a)

  given [F[_], A: F] => Conversion[A, Dyn[F]] = a => Dyn.fromType(a)
  inline given [F[_]] => F[Dyn[F]] = createDynInstanceFrom[F]

