FROM ubuntu:latest
LABEL authors="italo"

ENTRYPOINT ["top", "-b"]