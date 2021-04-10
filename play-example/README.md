Refer to Play docs: https://www.playframework.com/documentation/2.8.x/ConfiguringHttps


Example run with SSL:


```
$ sbt play-example/stage 
```

```
$ LETSENCRYPT_CERT_DIR=/path/to/ssl/ \
    ./play-example/target/universal/stage/bin/play-example \
    -java-home /usr/lib/jvm/zulu15/ \
    -Dhttps.port=9443 \
    -Dplay.http.secret.key=111111111111111111111111111111111111111
```
