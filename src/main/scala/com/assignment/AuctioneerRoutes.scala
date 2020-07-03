package com.assignment

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout

class AuctioneerRoutes()(implicit val system: ActorSystem[_]) {

  import JsonFormats._
  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

  private implicit val timeout = Timeout.create(system.settings.config.getDuration("auctioneer-app.routes.ask-timeout"))

  val housingService = new HousingService()
  val auctionService = new AuctionService()

  val routes: Route =
    post {
      pathPrefix("add" / "new" / "house") {
          entity(as[House]) { houseRequest =>
            housingService.addHouse(houseRequest)
            complete(s"House with name ${houseRequest.name} is added for auction")
          }
      }
    } ~ get {
      pathPrefix("list" / "all" / "houses") {
        complete(housingService.getAllHouses())
      }
    } ~ delete {
      pathPrefix("remove" / "house") {
        entity(as[House]) { houseRequest =>
          val msg = housingService.deleteHouse(houseRequest)
          complete(msg)
        }
      }
    } ~ post {
      pathPrefix("add" / "new" / "auction" / "for" / Segment) { houseName =>
        entity(as[HouseAuction]) { auction =>
          auctionService.addAuction(houseName, auction)
          complete(s"Auction added for house: ${houseName}")
        }
      }
    } ~ get {
      pathPrefix("list" / "house" / "auction" / "for" / Segment) { houseName =>
        complete(auctionService.getAuctionsForAHouse(houseName))
      }
    } ~ get {
      pathPrefix("list" / "all" / "house" / "auctions") {
        complete(auctionService.allAuctions)
      }
    } ~ delete {
      pathPrefix("remove" / "auction") {
        entity(as[RemoveAuction]) { removeRequest =>
          auctionService.removeAuction(removeRequest) match {
            case Right((house, auction)) => complete(s"Auction $auction has been removed from house $house")
            case Left(msg) => complete(msg)
          }

        }
      }
    } ~ get {
      pathPrefix("list" / Segment / "auctions" / "for" / "house" / Segment) { (status, house) =>
        complete(auctionService.getAuctionsByStatus(status, house))
      }
    } ~ post {
      pathPrefix("bid" / "for" / "auction") {
        entity(as[Bid]) { bid =>
          val msg = auctionService.bid(bid)
          complete(msg)
        }
      }
    } ~ get {
      pathPrefix("list" / "bids" / "for" / "house" / Segment / "auction" / Segment) { (house, auction) =>
        complete(auctionService.getBids(house, auction))
      }
    }
}
