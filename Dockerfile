FROM java:8-alpine
MAINTAINER Your Name <you@example.com>

ADD target/uberjar/zadania.jar /zadania/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/zadania/app.jar"]
