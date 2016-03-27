# coffee simulation REST service

by default service uses in-memory relational database

## runnig service

```Shell
mvn clean install

java -jar target/coffee-simulation-0.0.1-SNAPSHOT.jar
```
service will start on 8080 port

## requests example

check whether service started 
```Shell
curl -v "http://localhost:8080/coffee/accounting/status"
```

all types of coffee
```Shell
curl -v "http://localhost:8080/coffee/accounting/type/all"
```

pick favorites method
```Shell
curl -v -X POST -d "coffeeType=ESPRESSO" "http://localhost:8080/coffee/accounting/pick_favorites"
```

pay method
```Shell
curl -v -X PUT -d "paymentType=CASH&orderId=1" "http://localhost:8080/coffee/accounting/pay"
```

get ticket to machine
```Shell
curl -v -X PUT -d "coffeeType=ESPRESSO&orderId=1" "http://localhost:8080/coffee/accounting/machine/assign"
```

coffee is done notification
```Shell
curl -v -X PUT -d "orderId=1" "http://localhost:8080/coffee/accounting/machine/done"
```

coffee sales statistics
```Shell
curl -v -X GET "http://localhost:8080/coffee/stats/sold/total"
```

coffee machine statistics
```Shell
curl -v -X GET "http://localhost:8080/coffee/stats/machine/total"
```

average spend time on get coffee
```Shell
curl -v -X GET -d "http://localhost:8080/coffee/stats/spendtime/avg"
```

max spend time on get coffee
```Shell
curl -v -X GET -d "http://localhost:8080/coffee/stats/spendtime/max"
```

min spend time on get coffee
```Shell
curl -v -X GET -d "http://localhost:8080/coffee/stats/spendtime/min"
```

