package com.assignment

import java.sql.Timestamp
import java.time.ZoneOffset

import spray.json.{DefaultJsonProtocol, DeserializationException, JsNumber, JsString, JsValue, JsonFormat}

object JsonFormats  {
  import DefaultJsonProtocol._

  implicit object TimestampFormat extends JsonFormat[Timestamp] {
    def write(obj: Timestamp) = JsString(obj.toLocalDateTime.atZone(ZoneOffset.UTC).toString)

    def read(json: JsValue) = json match {
      case JsNumber(time) => new Timestamp(time.toLong)
      case JsString(date) =>
        import java.sql.Timestamp
        import java.text.SimpleDateFormat
        val dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        val parsedDate = dateFormat.parse(date)
        new Timestamp(parsedDate.getTime)
      case _ => throw new DeserializationException("Date expected")
    }
  }

  implicit val houseJsonFormat = jsonFormat1(House)
  implicit val allHousesJsonFormat = jsonFormat1(AllHouses)
  implicit val bidJsonFormat = jsonFormat4(Bid)
  implicit val auctionHouseJsonFormat = jsonFormat8(HouseAuction)
  implicit val auctionsJsonFormat = jsonFormat1(Auctions)
  implicit val removeAuctionJsonFormat = jsonFormat2(RemoveAuction)
  implicit val bidStatusJsonFormat = jsonFormat2(BidStatus)


}
