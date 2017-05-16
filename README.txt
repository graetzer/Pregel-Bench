apt-get update
apt-get install cmake python libssl-dev

git clone https://github.com/graetzer/arangodb.git 
cd arangodb && git submodule update --init --recursive
mkdir build && cd build && cmake .. -DCMAKE_BUILD_TYPE=Release  && make -j32

wget https://storage.googleapis.com/golang/go1.7.1.linux-amd64.tar.gz
tar -zxvf go1.7.1.linux-amd64.tar.gz -C /usr/local/
export PATH=$PATH:/usr/local/go/bin
mkdir $HOME/work && export GOPATH=$HOME/work

mkdir orkut && cd orkut
wget http://snap.stanford.edu/data/bigdata/communities/com-orkut.ungraph.txt.gz
gzip -d com-orkut.ungraph.txt.gz
mv com-orkut.ungraph.txt edges.csv
cat edges.csv | tr '[:space:]' '[\n*]' | grep -v "^\s*$" | awk '!seen[$0]++' > vertices.csv
cat edges.csv | awk -F" " '{print "orkut_v/" $1 "\torkut_v/" $2 "\t" $1}' >> arango-edges.csv
/root/arangodb/build/bin/arangoimp --file vertices.csv --type csv --collection orkut_v --overwrite true --convert false --server.endpoint http+tcp://127.0.0.1:4002  -c none
/root/arangodb/build/bin/arangoimp --file arango-edges.csv --type csv --collection orkut_e --overwrite true --convert false --separator "\t" --server.endpoint http+tcp://127.0.0.1:4002  -c none

## download start helper
git clone https://github.com/arangodb-helper/arangodb.git && cd arangodb && make local

## Start arangod on First server
./arangodb --arangod=/root/arangodb/build/bin/arangod --jsDir=/root/arangodb/js/
./arangodb --arangod=/graetzer/arangodb/build/bin/arangod --jsDir=/graetzer/arangodb/js/ --dataDir=./db/

## Start Second & Third server
./arangodb --arangod=/root/arangodb/build/bin/arangod --jsDir=/root/arangodb/js/ --join 10.132.78.134
./arangodb --arangod=/graetzer/arangodb/build/bin/arangod --jsDir=/graetzer/arangodb/js/ --dataDir=./db/ --join 192.168.10.7


# Java prerequisites
apt-get install default-jre


# Flink
wget http://artfiles.org/apache.org/flink/flink-1.2.0/flink-1.2.0-bin-hadoop27-scala_2.11.tgz
tar -zxvf flink-1.2.0-bin-hadoop27-scala_2.11.tgz

## Add your slaves to "flink/conf/slaves", for example 
echo -e  "10.132.81.151\n10.132.91.87" >> flink/conf/slaves


# Spark
wget http://d3kbcqa49mib13.cloudfront.net/spark-2.1.0-bin-hadoop2.7.tgz
tar -zxvf spark-2.1.0-bin-hadoop2.7.tgz
mv spark-2.1.0-bin-hadoop2.7 spark && cd spark

## on master, adjust local IP accordingly
export SPARK_LOCAL_IP=192.168.10.5
./sbin/start-master.sh --host 192.168.10.5

## on workers & master
export SPARK_LOCAL_IP=
./sbin/start-slave.sh  spark://192.168.10.5:7077

./bin/spark-submit --class org.apache.spark.arango.PageRankAlgo --master spark://192.168.10.5:7077 \
    ~/arangodb-spark-benchmark-2.1.0.jar "/test/orkut/edges.csv" /graetzer/out

./bin/spark-submit --class org.apache.spark.arango.PageRankAlgo --master spark://192.168.10.5:7077 \
    /mnt/c5/graetzer/bench-spark-21.jar "/mnt/c5/graetzer/testdata/orkut/edges.txt" "/graetzer/out"


# hadoop2

# mkdir giraph && cd giraph && wget http://mirror.serversupportforum.de/apache/giraph/giraph-1.2.0/giraph-dist-1.2.0-hadoop2-bin.tar.gz
# tar -zxvf giraph-dist-1.2.0-hadoop2-bin.tar.gz && rm giraph-dist-1.2.0-hadoop2-bin.tar.gz

mkdir giraph && cd giraph && wget http://archive.apache.org/dist/hadoop/core/hadoop-2.5.1/hadoop-2.5.1.tar.gz
tar -zxvf  hadoop-2.5.1.tar.gz && rm hadoop-2.5.1.tar.gz

export JAVA_HOME=/usr/lib/jvm/default-java
export HADOOP_HOME=/graetzer/giraph/hadoop-2.5.1
export HADOOP_PREFIX=/graetzer/giraph/hadoop-2.5.1
export HADOOP_OPTS=-Djava.net.preferIPv4Stack=true

# For DigitalOcean
export JAVA_HOME=/usr/lib/jvm/default-java
export HADOOP_HOME=/root/giraph/hadoop-2.5.1
export HADOOP_PREFIX=/root/giraph/hadoop-2.5.1
export HADOOP_CONF_DIR=/root/giraph/hadoop-2.5.1/etc/hadoop
export HADOOP_OPTS=-Djava.net.preferIPv4Stack=true

# Editiere /etc/hosts mit allen Hostnamen
# z.B.
159.203.115.196 master-1
138.197.70.0 test-01
159.203.115.196 test-02

Benutze das für core-site.xml und yarn-site.xml
fs.defaultFS  / yarn.resourcemanager.hostname

## auf allen Rechnern formartiere HDFS dateisystem
$HADOOP_PREFIX/bin/hdfs namenode -format pregel

# Starte HDFS und yarn
$HADOOP_PREFIX/sbin/start-dfs.sh
$HADOOP_PREFIX/sbin/start-yarn.sh

## Giraph mit hadoop ausführen, beispiele:

nohup $HADOOP_HOME/bin/hadoop jar myjar.jar org.apache.giraph.GiraphRunner PageRank --yarnjars myjar.jar --workers 1 \
--vertexInputFormat org.apache.giraph.examples.LongDoubleNullTextInputFormat --vertexInputPath /user/graetzer/input \
--vertexOutputFormat org.apache.giraph.io.formats.IdWithValueTextOutputFormat --outputPath /user/graetzer/output \
--combiner DoubleSumCombiner -ca giraph.SplitMasterWorker=false &


nohup $HADOOP_HOME/bin/hadoop jar myjar.jar org.apache.giraph.GiraphRunner ShortestPath --yarnjars myjar.jar --workers 1 \
--vertexInputFormat org.apache.giraph.examples.LongDoubleNullTextInputFormat --vertexInputPath /user/graetzer/input \
--vertexOutputFormat org.apache.giraph.io.formats.IdWithValueTextOutputFormat --outputPath /user/graetzer/output \
--combiner DoubleMinCombiner -ca giraph.SplitMasterWorker=false -ca SimpleShortestPathsVertex.sourceId=58829577 &

$HADOOP_HOME/bin/hdfs dfs -put edges-adj.txt /user/root/input



$HADOOP_HOME/bin/hadoop jar /root/giraph/pagerank/myjar.jar org.apache.giraph.GiraphRunner PageRank \
--yarnjars /root/giraph/pagerank/myjar.jar --workers 2 --vertexInputFormat org.apache.giraph.examples.LongDoubleNullTextInputFormat \
--vertexInputPath /user/root/input --vertexOutputFormat org.apache.giraph.io.formats.IdWithValueTextOutputFormat \
--outputPath /user/root/output --combiner DoubleSumCombiner -ca mapred.job.tracker=45.55.50.229 \
-ca mapred.job.map.memory.mb=14848 -ca mapred.job.reduce.memory.mb=14848



$HADOOP_HOME/bin/hadoop jar /root/giraph/shortestpath/myjar.jar org.apache.giraph.GiraphRunner ShortestPath \
--yarnjars /root/giraph/shortestpath/myjar.jar --workers 2 --vertexInputFormat org.apache.giraph.examples.LongDoubleNullTextInputFormat \
--vertexInputPath /user/root/input --vertexOutputFormat org.apache.giraph.io.formats.IdWithValueTextOutputFormat \
--outputPath /user/root/output --combiner DoubleMinCombiner -ca mapred.job.tracker=45.55.50.229 \
-ca mapred.job.map.memory.mb=14848 -ca mapred.job.reduce.memory.mb=14848 -ca SimpleShortestPathsVertex.sourceId=1182118



#$HADOOP_HOME/bin/hadoop jar myjar.jar org.apache.giraph.GiraphRunner PageRank --yarnjars myjar.jar --workers 2 \
#--vertexInputFormat org.apache.giraph.examples.LongDoubleNullTextInputFormat --vertexInputPath /user/root/input \
#--vertexOutputFormat org.apache.giraph.io.formats.IdWithValueTextOutputFormat --outputPath /user/root/output \
#--combiner DoubleSumCombiner -ca mapreduce.jobtracker.address=159.203.115.196:8021 \
#-ca mapreduce.framework.name=yarn -ca mapreduce.map.cpu.vcores=7 -ca mapreduce.reduce.cpu.vcores=7 \
#-ca yarn.app.mapreduce.am.resource.cpu-vcores=7
