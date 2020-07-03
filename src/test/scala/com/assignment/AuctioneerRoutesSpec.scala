package com.assignment

import java.sql.Timestamp

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.actor.typed.scaladsl.adapter._
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}

class AuctioneerRoutesSpec extends WordSpec with Matchers with ScalaFutures with ScalatestRouteTest {
  lazy val testKit = ActorTestKit()
  implicit def typedSystem = testKit.system
  override def createActorSystem(): akka.actor.ActorSystem =
    testKit.system.toClassic

  lazy val auctioneer = new AuctioneerRoutes()
  lazy val routes = auctioneer.routes

  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import JsonFormats._

  "Routes" should {


    "be able to add house" in {
      val house = House("Nirala")
      val houseEntity = Marshal(house).to[MessageEntity].futureValue
      val request = Post("/add/new/house").withEntity(houseEntity)
      request ~> routes ~> check {
        entityAs[String] should ===("House with name Nirala is added for auction")
      }
    }

    "be able to list all house" in {
      val request = HttpRequest(uri = "/list/all/houses")

      val req = request ~> (routes)
          req ~> check {
        entityAs[AllHouses] should ===(AllHouses(List(House("Nirala"))))
      }
    }

    "be able to remove house" in {
      val house = House("Nirala")
      val houseEntity = Marshal(house).to[MessageEntity].futureValue
      val request = Delete("/remove/house").withEntity(houseEntity)
      request ~> routes ~> check {
        entityAs[String] should ===("House Nirala deleted")
      }
    }

    "be able to add auction for house" in {
      val house = HouseAuction("fresh", "Fresh auction", new Timestamp(System.now))
      val houseEntity = Marshal(house).to[MessageEntity].futureValue
      val request = Delete("/add/new/auction/for/Nirala").withEntity(houseEntity)
      request ~> routes ~> check {
        entityAs[String] should ===("House Nirala deleted")
      }
    }

  }

}
