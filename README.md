# House Auctioneer application

This application is a akka-http application for handling different web request regarding house auctions.


## Table of Contents
- [Getting Started](#getting-started)
- [PostmanRequest](#postman)
- [Test](#test)

## Getting Started

* Clone the repo.
* Run: `sbt cleanFiles`
* Run: `sbt clean compile`.
* Run `sbt run`
* Profit. :tada:

## Postman

### A postman collection file is attached in the repo. This postman collection has all the requests for the urls that the web application serves.

1. Install postman through Google Chrome extension or install it directly
2. Import the json file
3. Try the different routes

## Test

### The best way to understand the application is through its test cases. Which you can run by the following

1. Run `sbt test`