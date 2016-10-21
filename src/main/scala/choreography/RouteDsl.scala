package choreography

import com.twitter.finagle.http._

/**
  * Gateway
  * User: michael.felix
  * Date: 5/10/16
  */


class Get(path: String*) extends RouteMatch(Method.Get, path: _*)

object Get {
  def apply(path: String): Get = apply(RouteMatch.open(path): _*)

  def apply(path: String*): Get = new Get(path: _*)

  def unapplySeq(route: Get): Option[Seq[String]] = Some(route.path)
}

class Post(path: String*) extends RouteMatch(Method.Post, path: _*)

object Post {
  def apply(path: String): Post = apply(RouteMatch.open(path): _*)

  def apply(path: String*): Post = new Post(path: _*)

  def unapplySeq(route: RouteMatch) = Some(route.path)
}

class Put(path: String*) extends RouteMatch(Method.Put, path: _*)

object Put {
  def apply(path: String): Put = apply(RouteMatch.open(path): _*)

  def apply(path: String*): Put = new Put(path: _*)

  def unapplySeq(route: RouteMatch) = Some(route.path)
}

class Patch(path: String*) extends RouteMatch(Method.Patch, path: _*)

object Patch {
  def apply(path: String): Patch = apply(RouteMatch.open(path): _*)

  def apply(path: String*): Patch = new Patch(path: _*)

  def unapplySeq(route: RouteMatch) = Some(route.path)
}

class Delete(path: String*) extends RouteMatch(Method.Delete, path: _*)

object Delete {
  def apply(path: String): Delete = apply(RouteMatch.open(path): _*)

  def apply(path: String*): Delete = new Delete(path: _*)

  def unapplySeq(route: RouteMatch) = Some(route.path)
}

sealed abstract class RouteMatch(val method: Method, val path: String*)

object RouteMatch extends ((Method, Seq[String]) => RouteMatch) {
  private[choreography] def open(path: String) = path.replaceAll("^/", "").split("/")

  def apply(method: Method, path: String): RouteMatch = apply(method, open(path))

  def apply(method: Method, seq: Seq[String]): RouteMatch = {
    val path = seq.reduce(_ + "/" + _)
    method match {
      case Method.Get => Get(path)
      case Method.Post => Post(path)
      case Method.Put => Put(path)
      case Method.Patch => Patch(path)
      case Method.Delete => Delete(path)
      case _ => throw new IllegalArgumentException
    }
  }

  def unapplySeq(route: RouteMatch): Option[(Method, Seq[String])] = {
    Some(route.method, route.path)
    /*
        val split = route.path.replaceAll("^/", "").split("/")
        if (split.length == 1)
          None
        else {
          val s = split.toSeq
          val head: String = s.head
          val tail: List[String] = s.tail.toList
          val seq: List[String] = head :: tail
          println(s"Split is ${seq.reduce(_ + " / " + _)}")
          Some(seq)
        }
    */
  }

}
