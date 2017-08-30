FROM ubuntu:17.04
RUN apt-get update && apt-get install -y \
    openjdk-8-jre \
    openjdk-8-jdk \
	unzip \
    build-essential \
	libssl-dev
	
ADD run.sh run.sh
RUN chmod +x run.sh

ADD /bin/wrk wrk
RUN chmod +x wrk

ADD /target/travels-1.0-SNAPSHOT-jar-with-dependencies.jar app.jar

EXPOSE 80
CMD ["sh", "run.sh"]
