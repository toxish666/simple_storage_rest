# Simple rest in-memory storage

  - You can find and example of my code here.
  - [Swagger specification][sw]
  - [Unit tests][ut]

    [sw]: <https://github.com/toxish666/simple_storage_rest/blob/master/openapi3.0.1>
    [ut]: <https://github.com/toxish666/simple_storage_rest/tree/master/src/test>

### Deployment instructions

You can run application as-is or run it in docker container with the following build commands:
```sh
$ cd simple_storage_rest
$ sbt docker:publishLocal
$ docker run --rm -p 8080:8080 toxrepo/storage-service:0.1
```

### Rest call examples

  - POST
 ```sh
 $ curl -sL -w "%{http_code}\n" -X POST "http://127.0.0.1:8080/catalogue" -H  "accept: */*" -H  "Content-Type: application/json" -d "{\"elid\":0,\"elname\":\"string\",\"elsize\":1}"
 $ curl -sL -w "%{http_code}\n" -X POST "http://127.0.0.1:8080/catalogue" -H  "accept: */*" -H  "Content-Type: application/json" -d "{\"elid\":1,\"elname\":\"string\",\"elsize\":500}"
 ```
  - DELETE
 ```sh
 $ curl -sL -w "%{http_code}\n" -X DELETE "http://127.0.0.1:8080/catalogue/0" -H  "accept: */*"
  ```
  - GET element
 ```sh
 $ curl -X GET "http://127.0.0.1:8080/catalogue/1" -H  "accept: application/json"
  ```
  - GET with pagination & ordering
 ```sh
 $ curl -X GET "http://127.0.0.1:8080/catalogue?offset=0&limit=10&sort=size,desc&sort=name,asc" -H  "accept: application/json"
  ```