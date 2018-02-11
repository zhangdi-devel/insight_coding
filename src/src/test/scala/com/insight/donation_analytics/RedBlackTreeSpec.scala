package com.insight.donation_analytics

import com.insight.donation_analytics.RedBlackTree.Color

import scala.util.Random

class RedBlackTreeSpec extends UnitSpec {

  def height(rbt: RedBlackTree): Int = {
    rbt match {
      case Leaf => 0
      case Node(_, _, _, l, r) => 1 + math.max(height(l), height(r))
    }
  }

  def count(rbt: RedBlackTree): Int = {
    rbt match {
      case Leaf => 0
      case Node(_, _, _, l, r) => 1 + count(l) + count(r)
    }
  }

  def check(rbt: RedBlackTree)(f: RedBlackTree => Boolean): Unit = {
    rbt match {
      case Leaf => f(Leaf) should be (true)
      case Node(_,_,_,l,r) =>
        f(rbt) should be (true)
        check(l)(f)
        check(r)(f)
    }
  }

  def inOrderSeq(rbt: RedBlackTree): Seq[(Int, Color, Int)] = {
    rbt match {
      case Leaf => Seq[(Int, Color, Int)]()
      case Node(x, c, n, l, r) =>
        inOrderSeq(l) ++ Seq[(Int, Color, Int)]((x, c, n)) ++ inOrderSeq(r)
    }
  }

  def printTree(rbt: RedBlackTree, indent: Int): String = {
    rbt match {
      case Leaf => "Leaf"
      case Node(x, c, cnt, l, r) =>
        s"\n${"\t"*indent}Node($x, $c, $cnt, ${printTree(l, indent + 1)}, ${printTree(r, indent + 1)})"
    }
  }

  "A RedBlackTree" should "be balanced if the input is sorted" in {
    (1 to 20).foreach{n =>
      val rbt = RedBlackTree(1 until (1 << n): _*)
      val h = height(rbt)
      logger.debug(s"sorted design height: $n actual height: $h")
      h should be <= 2 * n
    }
  }

  it should "be balanced if the input is random" in {
    (1 to 20).foreach{n =>
      val sorted = 1 until(1 << n)
      val shuffled = Random.shuffle(sorted.toList).toArray
      val rbt = RedBlackTree(shuffled: _*)
      val h = height(rbt)
      logger.debug(s"random design height: $n actual height: $h")
      h should be <= 2 * n
    }
  }

  it should "maintain the order correctly" in {
    val h = 10
    val input = 0 until (1 << h)
    val rbt1 = RedBlackTree(input: _*)
    val rbt2 = RedBlackTree(Random.shuffle(input.toList):_*)

    val seq1 = inOrderSeq(rbt1)
    val seq2 = inOrderSeq(rbt2)

    seq1.length should be (1 << h)
    seq2.length should be (1 << h)

    seq1.zip(0 until (1 << h)).foreach(p => p._1._1 should be (p._2))
    seq2.zip(0 until (1 << h)).foreach(p => p._1._1 should be (p._2))

  }

  it should "maintain the augmented count field correctly" in {
    val h = 10
    val input = 0 until (1 << h)
    val rbt1 = RedBlackTree(input: _*)
    val rbt2 = RedBlackTree(Random.shuffle(input.toList):_*)

    check(rbt1)(rbt => count(rbt) == rbt.count)
    check(rbt2)(rbt => count(rbt) == rbt.count)

  }

  it should "look up the kth smallest correctly" in {
    val h = 10
    val input = 0 until (1 << h)
    val rbt1 = RedBlackTree(input: _*)
    val rbt2 = RedBlackTree(Random.shuffle(input.toList):_*)

    for (i <- 0 until rbt1.count) {
      val x = RedBlackTree.lookup(rbt1, i)
      //logger.debug(s"k: $i, element: $x")
      x should be (i)
    }

    for (i <- 0 until rbt2.count) {
      val x = RedBlackTree.lookup(rbt1, i)
      //logger.debug(s"k: $i, element: $x")
      x should be (i)
    }

  }

}
