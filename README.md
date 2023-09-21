# 프로젝트 실행 및 API 호출 방법

## gradle 서버 실행

```c
jar 생성 및 실행
./gradlew clean bootJar --stacktrace --info --refresh-dependencies -x test -x asciidoctor
java -jar build/libs/Premission-0.0.1-SNAPSHOT.jar
```

## jar 서버 실행
- java -jar Premission-0.0.1-SNAPSHOT.jar

## API 호출
- <a href="http://13.125.227.54:8080" target="_blank"> Spring Rest Docs </a>


# 프로젝트 설명
- JAVA 17, SpringBoot 3.1.3
- RabbitMQ를 활용하여 회원별 포인트 적립, 사용, 취소등 트래픽 분산.
- 에러발생 시 손실되는 메세지를 줄이기 위해 @Retryable 사용.
- 메시지 큐를 활용하여 분산 및 비동기 시스템의 성능, 신뢰성 및 확장성을 향상.
- 비동기 처리를 통해 처리속도 증가.
- API 명세화를 위해 Spring Rest Docs를 활용.
![image](https://github.com/dbscks97/marketboro-mission/assets/75676309/e7fa3a93-4e8a-45b6-b226-a4e4ea52fdb3)


# API 스펙
## 1.회원별 적립금 합계 조회

### curl request
```c
curl 'http://localhost:8500/api/v1/1/points' -i -X GET \
    -H 'Content-Type: application/json' \
    -H 'X-MEMBER-ID: 12345'
```

### HTTP response
```c
HTTP/1.1 200 OK
Content-Type: application/json
Content-Length: 85

{
  "code" : "0000",
  "message" : "Success",
  "data" : {
    "points" : 10000
  }
}
```

## 2.회원별 적립금 적립/사용 내역 조회

### curl request
```c
curl 'http://localhost:8500/api/v1/1/point-history' -i -X GET \
    -H 'Content-Type: application/json' \
    -H 'X-MEMBER-ID: 12345'
```

### HTTP response
```c
HTTP/1.1 200 OK
Content-Type: application/json
Content-Length: 264

{
  "code" : "0000",
  "message" : "Success",
  "data" : {
    "history" : [ {
      "historyId" : 359,
      "points" : 1000,
      "type" : "적립",
      "createdAt" : "2023-09-21T18:43:23.979411",
      "updatedAt" : "2023-09-21T18:43:23.979427"
    } ]
  }
}
```

## 3.회원별 적립금 적립

### curl request
```c
curl 'http://localhost:8500/api/v1/1/accrue?point=1000' -i -X POST \
    -H 'Content-Type: application/json' \
    -H 'X-MEMBER-ID: 12345' \
    -d '{
  "points" : 1000
}'
```

### HTTP response
```c
HTTP/1.1 200 OK
Content-Type: application/json
Content-Length: 46

{
  "code" : "0000",
  "message" : "Success"
}
```

## 4.회원별 적립금 사용

### curl request
```c
curl 'http://localhost:8500/api/v1/1/use?pointsToUse=100' -i -X POST \
    -H 'Content-Type: application/json' \
    -H 'X-MEMBER-ID: 12345' \
    -d '{
  "points" : 100
}'
```

### HTTP response
```c
HTTP/1.1 200 OK
Content-Type: application/json
Content-Length: 112

{
  "code" : "0000",
  "message" : "Success",
  "data" : {
    "pointsUsed" : 100,
    "deductPointNo" : 0
  }
}
```

## 5.회원별 적립금 사용취소 

### curl request
```c
curl 'http://localhost:8500/api/v1/1/cancel?pointsToCancel=100&deductPointNo=1' -i -X POST \
    -H 'Content-Type: application/json' \
    -H 'X-MEMBER-ID: 12345' \
    -d '{
  "points" : 100
}'
```

### HTTP response
```c
HTTP/1.1 200 OK
Content-Type: application/json
Content-Length: 115

{
  "code" : "0000",
  "message" : "Success",
  "data" : {
    "pointCanceled" : 100,
    "deductPointNo" : 1
  }
}
```


# 테스트 코드 수행 결과
![스크린샷 2023-09-21 오후 6 49 56](https://github.com/dbscks97/marketboro-mission/assets/75676309/deffa42f-ba2f-4411-9488-387db7685a4d)
![image](https://github.com/dbscks97/marketboro-mission/assets/75676309/570b1c3b-1b37-44ea-915d-c839d3f24119)

