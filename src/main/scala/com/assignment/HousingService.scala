package com.assignment

class HousingService {

  import Database._

  def addHouse(house: House) = {
    houseAuctions.get(house.name) match {
      case None =>
        houseAuctions = houseAuctions + ((house.name, Nil))
        houseAuctions.keys.toList
      case Some(_) => houseAuctions.keys.toList
    }

  }

  def getAllHouses(): AllHouses = {
    AllHouses(houseAuctions.keys.toList.map(House(_)))
  }

  def deleteHouse(house: House): String = {
    houseAuctions.get(house.name) match {
      case None => s"There is no house ${house.name} to delete"
      case Some(_) =>
        houseAuctions = houseAuctions.removed(house.name)
        s"House ${house.name} deleted"
    }
  }



}
