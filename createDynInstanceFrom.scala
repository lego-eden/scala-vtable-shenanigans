import scala.quoted.*

inline def createDynInstanceFrom[F[_]]: F[Dyn[F]] = ${createDynInstanceFromImpl[F]}
def createDynInstanceFromImpl[F[_]: Type](using Quotes): Expr[F[Dyn[F]]] =
  import quotes.*
  import reflect.*

  val dynRepr = TypeRepr.of[Dyn[F]]
  val dynTerm = '{ Dyn }.asTerm
  val dynSym = dynRepr.typeSymbol
  val fRepr = TypeRepr.of[F[Dyn[F]]]
  val fSym = fRepr.typeSymbol

  val fMethods = fSym.declaredMethods.filter(_.flags.is(Flags.Deferred))
  if fMethods.isEmpty then report.error(s"there are no methods to be implemented for ${Type.show[F[Dyn[F]]]}")

  val anonClsName = "$anon"
  val anonClsParents = List(TypeTree.of[Object], TypeTree.of[F[Dyn[F]]])
  def decls(cls: Symbol): List[Symbol] =
    fMethods.map(m =>
      val flags =
        m.flags & (
          Flags.Artifact     | Flags.Final      | Flags.Given     |
          Flags.Implicit     | Flags.JavaStatic | Flags.Local     |
          Flags.Method       | Flags.Override   | Flags.Private   |
          Flags.PrivateLocal | Flags.Protected  | Flags.Synthetic |
          Flags.ExtensionMethod
        )
      Symbol.newMethod(cls, m.name, fRepr.memberType(m), flags, Symbol.noSymbol)
    )

  val anonCls = Symbol.newClass(Symbol.spliceOwner, anonClsName, parents = anonClsParents.map(_.tpe), decls, selfType = None)

  val defDefs = anonCls.declaredMethods.map(m =>
    def wrap(dynArg: Ident, instance: Ref, expr: Apply): Term =
      fRepr.memberType(m) match
        case MethodType(_, _, `dynRepr`) => 
          val fromType = Select.unique(dynTerm, "fromType")
          val innerType = TypeSelect(dynArg, "Typ")
          val typeClassIdent = TypeIdent(fSym)
          val typeApplied = TypeApply(fromType, List(typeClassIdent, innerType))
          val wrappedExpr = Apply(typeApplied, List(expr))
          val wrappedExprWithGiven = Apply(wrappedExpr, List(instance))
          wrappedExprWithGiven
        case _ => expr

    DefDef(m, paramss =>
      val params = paramss.flatten
      val dynArg: Ident = params.head match
        case ident@Ident(s) => ident
        case unexpected => report.errorAndAbort(s"expected Ident, got ${unexpected.show(using Printer.TreeStructure)}")

      val instance = Select.unique(dynArg, "instance")
      val value = Select.unique(dynArg, "value")
      val method = Select.unique(instance, m.name)
      Some(wrap(dynArg, instance, Apply(method, value :: params.tail.map(_.asInstanceOf[Term]))))
    )
  )

  val anonClsDef = ClassDef(anonCls, anonClsParents, body = defDefs)
  val newAnonCls = Typed(Apply(Select(New(TypeIdent(anonCls)), anonCls.primaryConstructor), Nil), TypeTree.of[F[Dyn[F]]])

  val finalInstance = Block(List(anonClsDef), newAnonCls)
  finalInstance.asExprOf[F[Dyn[F]]]

end createDynInstanceFromImpl
