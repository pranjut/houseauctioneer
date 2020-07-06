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

    val time = new Timestamp(System.currentTimeMillis())
    val auction = HouseAuction("fresh", "Fresh auction", time, time, 20000, 20000)
    "be able to add auction for house" in {
      val houseEntity = Marshal(auction).to[MessageEntity].futureValue
      val request = Post("/add/new/auction/for/Nirala").withEntity(houseEntity)
      request ~> routes ~> check {
        entityAs[String] should ===("Auction added for house: Nirala")
      }
    }

    "be able to list auctions for house" in {
      val request = Get("/list/house/auction/for/Nirala")
      request ~> routes ~> check {
        entityAs[Auctions].auctions.size should === (1)
      }
    }

    "be able to list all auctions for house" in {
      val request = Get("/list/all/house/auctions")
      request ~> routes ~> check {
        entityAs[Auctions].auctions.size should === (1)
      }
    }

    "be able to removeAuction" in {
      val rem = RemoveAuction("Nirala", "fresh")
      val auctionEntity = Marshal(rem).to[MessageEntity].futureValue
      val request = Delete("/remove/auction").withEntity(auctionEntity)
      request ~> routes ~> check {
        entityAs[String] should === ("Auction fresh has been removed from house Nirala")
      }
    }

    "be able to bid for auction" in {
      val startTime = new Timestamp(System.currentTimeMillis() - (1000 * 60 * 60 * 24 * 2))
      val endTime = new Timestamp(System.currentTimeMillis() + (1000 * 60 * 60 * 24 * 2))
      val houseAuction = HouseAuction("FreshBid", "Fresh bid", startTime, endTime, 20000, 20000)
      val newAuctionService = new AuctionService
      newAuctionService.addAuction("Nirala", houseAuction)
      val bid = Bid("Pranjut", "Nirala", "FreshBid", 20100)

      val bidEntity = Marshal(bid).to[MessageEntity].futureValue
      val request = Post("/bid/for/auction").withEntity(bidEntity)
      request ~> routes ~> check {
        entityAs[String] should === ("Successfully place the bid")
      }
    }

    "be able to list bids for a particular auction" in {
      val request = Get("/list/bids/for/house/Nirala/auction/FreshBid")
      val bid = Bid("Pranjut", "Nirala", "FreshBid", 20100)
      request ~> routes ~> check {
        val bidStatus = entityAs[BidStatus]
         bidStatus.bids should === (List(bid))
         bidStatus.winner should === (None)
      }
    }

    "be able to declare the winner" in {
      val startTime = new Timestamp(System.currentTimeMillis() - (1000 * 60 * 60 * 24 * 2))
      val endTime = new Timestamp(System.currentTimeMillis() + (1000 * 10 ))
      val houseAuction = HouseAuction("ReFreshBid", "Fresh bid", startTime, endTime, 20000, 20000)
      val newAuctionService = new AuctionService
      newAuctionService.addAuction("Nirala", houseAuction)
      val bid = Bid("Pranjut", "Nirala", "ReFreshBid", 20100)

      val bidEntity = Marshal(bid).to[MessageEntity].futureValue
      val request = Post("/bid/for/auction").withEntity(bidEntity)
      request ~> routes ~> check {
        entityAs[String] should === ("Successfully place the bid")
      }
      Thread.sleep(1000 * 12)
      val getRequest = Get("/list/bids/for/house/Nirala/auction/ReFreshBid")
      getRequest ~> routes ~> check {
        val bidStatus = entityAs[BidStatus]
        bidStatus.winner should === (Some(bid))
      }
    }

  }

}
