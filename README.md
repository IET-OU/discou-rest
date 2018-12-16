# discou-rest


## Run a release

Latest runnable Jar is [version v0.0.2](https://github.com/the-open-university/discou/blob/mvn-repo/discou/discou-rest/0.0.2/discou-rest-0.0.2.jar?raw=true)

Lookup the info running the jar with -h:
```
$ java -jar discou-rest-0.0.2.jar -h
#1: welcome to discou
usage: java [java-opts] -jar [jarfile]
 -a,--annotator <arg>   Set the url of the spotlight annotator. Defaults
                        to http://spotlight.dbpedia.org/rest/annotate.
 -h,--help              Show this help.
 -i,--index <arg>       Set the index location.
 -n,--interface <arg>   The folder where the html/js alfa interface is
                        located.
 -p,--port <arg>        Set the port the server will listen to (defaults
                        to 8080).

```

Logging is provided using [Log4J](http://logging.apache.org/log4j/1.2/).
