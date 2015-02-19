package com.github.imliar.getstream.client

trait GetStreamSerializer {

  def serialize[A <: AnyRef](a: A)(implicit m: Manifest[A]): String

  def deserialize[A <: AnyRef](s: String)(implicit m: Manifest[A]): A

}

object GetStreamDefaultSerializer extends GetStreamSerializer {

    import org.json4s._
    import org.json4s.jackson.JsonMethods._
    import org.json4s.ext._
    import org.json4s.jackson.Serialization.write

    implicit val formats = DefaultFormats ++ JodaTimeSerializers.all

    override def serialize[A <: AnyRef](a: A)(implicit m: Manifest[A]): String = write(a)

    override def deserialize[A <: AnyRef](s: String)(implicit m: Manifest[A]): A = parse(s).extract[A]

}
