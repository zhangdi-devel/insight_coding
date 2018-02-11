/*
 * Copyright 2018 Zhang Di
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.insight.donation_analytics

import RedBlackTree._

import scala.annotation.tailrec

/**
  * A functional RedBlackTree that is described in this paper:
  *    Chris Okasaki. Red-Black Trees in a Functional Setting. J. Functional Programming 9(4) 471-477, July 1999
  *
  * Here it is augmented with a "count" field to allow for O(log(n)) looking-up of Kth smallest value
  *
  * */

sealed trait RedBlackTree {
  def color: Color
  def count: Int
}

case object Leaf extends RedBlackTree {
  def color: Color = B
  def count = 0
}

case class Node(data: Long,
                color: Color,
                count: Int,
                left: RedBlackTree,
                right: RedBlackTree) extends RedBlackTree

object RedBlackTree {

  sealed trait Color
  /** Black */
  final case object B extends Color
  /** Red */
  final case object R extends Color //Red

  /** change color to black */
  def blacken(rbt: RedBlackTree): RedBlackTree = {
    rbt match {
      case Leaf|Node(_, B,_,_,_) => rbt
      case Node(x, R, c, l, r) => Node(x, B, c, l, r)
    }
  }

  /**
    * balance after insertion/deletion
    * Nodes in () are black.
    *
    *      (z)                                             (x)
    *      / \                      y                      / \
    *     y   d     case 1       /    \       case 3      a   y
    *    / \          =>       (x)    (z)       <=           / \
    *   x   c                  / \    / \                   b   z
    *  / \                    a   b  c   d                     / \
    * a   b                                                   c   d
    *
    *      (z)                                             (x)
    *      / \                      y                      / \
    *     x   d     case 2       /    \       case 4      a   z
    *    / \          =>       (x)    (z)       <=           / \
    *   a   y                  / \    / \                   y   d
    *      / \                a   b  c   d                 / \
    *     b   c                                           b   c
    *
    * */
  def balance(rbt: RedBlackTree): RedBlackTree = {
    rbt match {
      case Node(z, B, cnt3, Node(y, R, _, Node(x, R, cnt1, a, b), c), d) =>
        Node(y, R, cnt3, Node(x, B, cnt1, a, b), Node(z, B, 1 + c.count + d.count, c, d ))
      case Node(z, B, cnt3, Node(x, R, _, a, Node(y, R, _, b, c)), d) =>
        Node(y, R, cnt3, Node(x, B, 1 + a.count + b.count, a, b), Node(z, B, 1 + c.count + d.count, c, d))
      case Node(x, B, cnt1, a, Node(y, R, _, b, Node(z, R, cnt3, c, d))) =>
        Node(y, R, cnt1, Node(x, B, 1 + a.count + b.count, a, b), Node(z, B, cnt3, c, d))
      case Node(x, B, cnt1, a, Node(z, R, _, Node(y, R, _, b, c), d)) =>
        Node(y, R, cnt1, Node(x, B, 1 + a.count + b.count, a, b), Node(z, B, 1 + c.count + d.count, c, d))
      case _ => rbt
    }
  }

  /** insert new element */
  def insert(rbt: RedBlackTree, x: Long): RedBlackTree = {
    def helper(_rbt: RedBlackTree, _x: Long): RedBlackTree = {
      _rbt match {
        case Leaf => Node(_x, R, 1, Leaf, Leaf)
        case Node(y, c, cnt, l, r) =>
          if (_x <= y) {
            balance(Node(y, c, cnt + 1, helper(l, _x), r))
          } else {
            balance(Node(y, c, cnt + 1, l, helper(r, _x)))
          }
      }
    }
    blacken(helper(rbt, x))
  }

  /** find Kth smallest */
  @tailrec
  def lookup(rbt: RedBlackTree, k: Int): Long = {
    rbt match {
      case Leaf => -1 //This should never happen
      case Node(x, _, _, l, r) =>
        if (l.count == k) {
          x
        } else if (l.count > k) {
          lookup(l, k)
        } else {
          lookup(r, k - l.count - 1)
        }
    }
  }

  def apply(elems: Long*): RedBlackTree = {
    @tailrec
    def helper(rbt: RedBlackTree, rest: Iterator[Long]): RedBlackTree = {
      if (rest.hasNext) {
        helper(insert(rbt, rest.next()), rest)
      } else {
        rbt
      }
    }
    helper(Leaf, elems.toIterator)
  }
}