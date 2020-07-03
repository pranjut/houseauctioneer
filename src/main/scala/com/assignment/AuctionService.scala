package com.assignment

import java.sql.Timestamp

class AuctionService {


  import Database._

  def addAuction(houseName: String, auctionHouse: HouseAuction): Map[String, List[HouseAuction]] = {

    houseAuctions = houseAuctions.get(houseName) match {
      case Some(list) => houseAuctions.+(( houseName,  (list :+ auctionHouse)))
      case None => houseAuctions.+(( houseName,  List(auctionHouse)))
    }
    houseAuctions
  }

  def getAuctionsForAHouse(houseName: String): Auctions = {
    Auctions(houseAuctions.get(houseName).getOrElse(Nil).filter(_.status != Some("Removed")))
  }

  def allAuctions: Auctions = {
    Auctions(houseAuctions.values.flatten.toList.filter(_.status != Some("Removed")))
  }

  def removeAuction(removeAuction: RemoveAuction): Either[String, (String, String)] = {
    houseAuctions.get(removeAuction.houseName) match {
      case Some(auctions)=>
        houseAuctions = houseAuctions + ((removeAuction.houseName, auctions.map{
          case auct if auct.name == removeAuction.auctionName =>

            auct.copy(status = Some("Removed"))
          case auct =>
            auct
        }))
        Right((removeAuction.houseName, removeAuction.auctionName))
      case None => Left("No house found to remove the auction")
    }
  }

  def getAuctionsByStatus(status: String, house: String): Auctions = {
    val auctions = houseAuctions.values.toList.flatten
    val filteredAuctions = findAuctions(status, auctions)
    Auctions(filteredAuctions)
  }

  private def findAuctions(status: String, auctions: List[HouseAuction]): List[HouseAuction] = {
    status match {
      case "NotStarted" =>
        auctions.filter(auct =>auct.status != Some("Removed") && auct.startingTime.after(new Timestamp(System.currentTimeMillis())))
      case "Running" =>
        auctions.filter(auct => auct.status != Some("Removed") && auct.startingTime.before(new Timestamp(System.currentTimeMillis()))
          && auct.endTime.after(new Timestamp(System.currentTimeMillis())))
      case "Terminated" => auctions.filter(auct => auct.status != Some("Removed") && auct.endTime.before(new Timestamp(System.currentTimeMillis())))
      case "Deleted" => auctions.filter(auct => auct.status == Some("Removed"))
      case _ => auctions
    }
  }

  def bid(bd: Bid) = {
    houseAuctions.get(bd.houseName) match {
      case Some(auctions) =>
        val updatedAuctions = auctions.map{
          case auct if auct.status != Some("Removed") && auct.startingTime.before(new Timestamp(System.currentTimeMillis()))
            && auct.endTime.after(new Timestamp(System.currentTimeMillis())) && bd.biddingPrice > auct.currentPrice
            && auct.name == bd.auctionName =>
            val updatedBids = auct.bids.map(b => b ::: List(bd)).getOrElse(List(bd))
            auct.copy(currentPrice = bd.biddingPrice, bids = Some(updatedBids))
          case a => a
        }
        houseAuctions = houseAuctions + ((bd.houseName, updatedAuctions))
        if(auctions == updatedAuctions){
          "There is some problem in bidding check the bidding price or status of the auctions"
        } else {
          "Successfully place the bid"
        }
      case None => "House not found for bidding"
    }
  }

  def getBids(house: String, auctionName: String): Option[BidStatus] = {
    houseAuctions.get(house).flatMap{
      auctions =>
        auctions.find(_.name == auctionName) match {
        case None => None
        case Some(auct) => if(auct.status != Some("Removed") && auct.endTime.before(new Timestamp(System.currentTimeMillis()))){
          Some(BidStatus(auct.bids.getOrElse(Nil), auct.bids.flatMap(_.lastOption)))
        } else{
          Some(BidStatus(auct.bids.getOrElse(Nil), None))
        }
      }
    }
  }

}
