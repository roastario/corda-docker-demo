FROM openjdk:8-jdk-alpine

RUN apk --no-cache --update add bash
RUN apk add curl
RUN mkdir -p /opt/corda
COPY node.conf /opt/notaries/node.conf
COPY corda.jar /opt/corda/corda.jar



COPY start.sh start.sh
RUN chmod +x start.sh
EXPOSE 8080
EXPOSE 10200
CMD ["/start.sh"]
COPY app.jar app.jar
RUN mkdir -p /jars
VOLUME ["/jars"]
HEALTHCHECK --interval=5s --timeout=3s CMD curl --fail http://localhost:8080/network-map/map-stats || exit 1
