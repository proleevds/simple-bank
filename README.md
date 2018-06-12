# Simplebank
This application based on Jetty + Jersey contains im-memory h2 db as datasource and exposes RESTful API that takes and returns data in JSON.

## Build and run
Application uses Maven to build so-called uber jar with jetty server inside that is ready to run. Application is available on localhost:8181 after start. <Link to built jar would be here>

## Tests
Project contains unit tests, that basically checks most of service methods.

## Application API
Following operations over accounts implemented:
* Create new account:
```
curl -i -H "Content-Type: application/json" -d '{"name":"test","balance":50.00}' http://localhost:8181/account/create
```
```
201 Created, Location: http://localhost:8181/account/1
```
* Request account by its id:
```
 curl -i http://localhost:8181/account/1
```
```
{"balance":50.00,"id":1,"name":"test"}
```
or error message with 404 http status in a case of account is not exist:
```
{"error":"Account with id=123 not found"}
```
* Make money income:
```
curl -i -H "Content-Type: application/json" -d '{"value":49.99}' http://localhost:8181/account/1/income
```
and receive account state after income operation:
```
{"balance":99.99,"id":1,"name":"test"}
```
* Make money outcome:
```
curl -i -H "Content-Type: application/json" -d '{"value":59.99}' http://localhost:8181/account/1/outcome
```
and receive account state after outcome operation:
```
{"balance":40.00,"id":1,"name":"test"}
```
or error message with 400 bad request state in a case of money is not enough:
```
{"error":"Insufficient account balance"}
```
* Make money transfer operation from 1 account to another, i.e. transactionally decreases sender's balance and increases recipient's:
```
curl -i -H "Content-Type: application/json" -d '{"senderId":1,"recipientId":2,"value":0.01}' http://localhost:8181/moneyTransfer
```
and receive sender's account state after transfer operation or error message with 400 bad request state in a case of sender's money is not enough:
```
{"error":"Transfer failed, insufficient sender account balance"}
```

Note: I would like to discuss these topics during interview:
- handling/mapping internal exceptions;
- possible transfer operation implementations.