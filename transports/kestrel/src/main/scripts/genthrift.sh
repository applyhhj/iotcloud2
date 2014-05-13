rm -rf gen-javabean
rm -rf ../java/net/lag/kestrel/thrift
thrift --gen java:beans,hashcode,nocamel kestrel.thrift
mv gen-javabean/net/lag/kestrel/thrift ../java/net/lag/kestrel/thrift
#rm -rf gen-javabean
