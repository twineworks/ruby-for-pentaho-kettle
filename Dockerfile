FROM brice/pentaho-kettle:5.0.1
MAINTAINER Brandon Rice <brice84@gmail.com>

ADD ./ Ruby-Scripting-for-Kettle/

RUN echo "kettle-dir=/data-integration" > /Ruby-Scripting-for-Kettle/build.properties
RUN cd /Ruby-Scripting-for-Kettle && ant install

WORKDIR /data-integration

EXPOSE 8181

CMD ["./carte.sh", "0.0.0.0", "8181"]

