package mf


import cats._
import cats.data._
import cats.implicits._

object Examples {

  val og1 = 1.some
  val og2 = 2.some
  val og3 = 3.some
  val og4: Option[Int] = None

  val goodOgs = List(og1, og2, og3)
  val mixedOgs = List(og1, og2, og3, og4)

  val e1 = 1.asRight[String]
  val e2 = 2.asRight[String]
  val e3bad = "nope".asLeft[Int]
  val esgood = List(e1, e2)
  val esbad = List(e1, e2, e3bad)


  val el1 = List(1, 2).asRight[String]
  val el2 = List(3, 4).asRight[String]
  val el3empty = List().asRight[String]

  val el4 = List(5,6).asRight[String]

  val eels = List(el1, el2, el3empty, el4)

  val el5 = "nope".asLeft[List[Int]]

  val eelsBad = List(el1, el2, el3empty, el4, el5)


}
