package com.assignment

import java.sql.Timestamp

class ActioneerModels {

}

case class House(name: String)
case class AllHouses(houseList: List[House])
case class HouseAuction(name: String,
                        description: String,
                        startingTime: Timestamp,
                        endTime: Timestamp,
                        startPrice: Double,
                        currentPrice: Double,
                        bids: Option[List[Bid]] = None,
                        status: Option[String] = Some("NotStarted")
                       )
case class Auctions(auctions: List[HouseAuction])

case class RemoveAuction(houseName: String, auctionName: String)

case class Bid(userName: String, houseName: String, auctionName: String, biddingPrice: Double)

case class BidStatus(bids: List[Bid], winner: Option[Bid])