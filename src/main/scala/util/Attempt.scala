package util

import play.api.libs.json.{JsError, JsResult, JsSuccess}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

/**
  * This is like an Option that provides a reason string instead of just None in the case of failure.
  * Or it is like a Try that can fail without needing a thrown exception.
  * Or it is like an Either without the cumbersome "Left" and "Right" semantics.
  * Instances contain either a value of type A, or a string justifying the lack thereof.
  *
  * val sum = Attempt(3 * 2).orFail("Can't do math")
  * sum match {
  *   case Succeeded(num) => println(s"I can multiply! I got $sum. I can also divide: ${sum / 2}")
  *   case Failed(why) => println(s"Got no result because $why")
  * }
  *
  * val anotherWay = Attempt.orFail("Can't do math") {
  *   3 * 2
  * }
  *
  */
object Attempt {
  def apply[A[_]: IsAttemptLike, X](a: A[X]): Attempt[X] = implicitly[IsAttemptLike[A]].asAttempt(a)

  def orFail[A[_]: IsAttemptLike, X](failReason: String)(a: A[X]): Attempt[X] = apply(a) match {
    case Succeeded(value) => success(value)
    case Failed(_) => failure(failReason)
  }

  def notNull[X](x: X): Attempt[X] = if (x == null)
    failure("null")
  else
    success(x)

  def failure[A](failReason: String) = new Attempt[A](new Left(failReason))

  def success[A](a: => A) = new Attempt(new Right(a))

  // typeclass
  trait IsAttemptLike[F[_]] {
    def isSuccess[X](fx: F[X]): Boolean
    def successValue[X](fx: F[X]): X
    def failureValue[X](fx: F[X]): String

    def asAttempt[X](fx: F[X]): Attempt[X] = if (isSuccess(fx))
      success(successValue(fx))
    else {
      failure(failureValue(fx))
    }
  }

  implicit object AttemptIsLikeAttempt extends IsAttemptLike[Attempt] {
    override def isSuccess[X](x: Attempt[X]) = x.succeeded
    override def successValue[X](x: Attempt[X]) = x.value
    override def failureValue[X](x: Attempt[X]) = x.reason
  }

  implicit object OptionIsLikeAttempt extends IsAttemptLike[Option] {
    override def isSuccess[X](x: Option[X]) = x.nonEmpty
    override def successValue[X](x: Option[X]) = x.get
    override def failureValue[X](x: Option[X]) = "None"
  }

  implicit object TryIsLikeAttempt extends IsAttemptLike[Try] {
    override def isSuccess[X](x: Try[X]) = x.isSuccess
    override def successValue[X](x: Try[X]) = x.get
    override def failureValue[X](x: Try[X]) = x.failed.get.getMessage
  }

  implicit object JsResultIsLikeAttempt extends IsAttemptLike[JsResult] {
    override def isSuccess[X](x: JsResult[X]) = x.isSuccess
    override def successValue[X](x: JsResult[X]) = x.get
    override def failureValue[X](x: JsResult[X]) = x.asEither.left.get.foldLeft("") { (acc, err) =>
      val path = err._1.path.map(_.toJsonString).mkString(".")
      val thisError = path + ": " + err._2.map(_.message).mkString(";")
      acc + " " + thisError
    }
  }
}

case class Attempt[+A](either: Either[String, A]) extends AnyVal {
  def reason = either.left.get
  def value = either.right.get
  def succeeded = either.isRight
  def failed = either.isLeft

  def orFail(failReason: => String) = new Attempt[A](either.left.map(e => failReason))

  def map[B](f: A => B) = new Attempt[B](either.right.map(e => f(e)))
  def flatMap[B](f: A => Attempt[B]) = either match {
    case Left(a) => new Attempt(Left(a))
    case Right(b) => f(b)
  }

  override def toString = if (succeeded) value.toString else reason
}

object Failed {
  def unapply[A](attempt: Attempt[A]): Option[String] = if (attempt.failed) Some(attempt.reason) else None
}

object Succeeded {
  def unapply[A](attempt: Attempt[A]): Option[A] = if (attempt.succeeded) Some(attempt.value) else None
}

object FutureAttempt {
  def apply[A](a: Attempt[A])(implicit ec: ExecutionContext): FutureAttempt[A] = new FutureAttempt(Future(a))
  def apply[A](f: Future[Attempt[A]])(implicit ec: ExecutionContext): FutureAttempt[A] = new FutureAttempt(f)

  def failure[A](failReason: String)(implicit ec: ExecutionContext) = apply(Attempt failure failReason)
  def success[A](a: => A)(implicit ec: ExecutionContext) = apply(Attempt success a)

  implicit def futureToFutureAttempt[A](fa: Future[Attempt[A]])(implicit ec: ExecutionContext): FutureAttempt[A] = apply(fa)
  implicit def attemptToFutureAttempt[A](a: Attempt[A])(implicit ec: ExecutionContext): FutureAttempt[A] = apply(a)
}

class FutureAttempt[+A](val future: Future[Attempt[A]]) extends AnyVal {
  def flatMap[B](f: A => FutureAttempt[B])(implicit ec: ExecutionContext): FutureAttempt[B] = {
    val newFuture = future.flatMap {
      case Succeeded(a) => f(a).future
      case Failed(why) => Future.successful(Attempt.failure(why))
    }

    FutureAttempt(newFuture)
  }

  def map[B](f: A => B)(implicit ec: ExecutionContext): FutureAttempt[B] = {
    FutureAttempt(future.map(att => att map f))
  }
}
