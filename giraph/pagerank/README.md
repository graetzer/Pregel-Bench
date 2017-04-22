# hadoop2

```
mkdir giraph && cd giraph && wget http://mirror.serversupportforum.de/apache/giraph/giraph-1.2.0/giraph-dist-1.2.0-hadoop2-bin.tar.gz
tar -zxvf giraph-dist-1.2.0-hadoop2-bin.tar.gz && rm giraph-dist-1.2.0-hadoop2-bin.tar.gz
```

```
wget http://archive.apache.org/dist/hadoop/core/hadoop-2.5.1/hadoop-2.5.1.tar.gz
tar -zxvf  hadoop-2.5.1.tar.gz && rm hadoop-2.5.1.tar.gz
```

```
export JAVA_HOME=/usr/lib/jvm/default-java
export HADOOP_HOME=/graetzer/giraph/hadoop-2.5.1
export HADOOP_PREFIX=/graetzer/giraph/hadoop-2.5.1
export HADOOP_OPTS=-Djava.net.preferIPv4Stack=true
```

```
wget https://github.com/apache/giraph/archive/rel/1.2.0-RC1.tar.gz
tar -zxvf   1.2.0-RC1.tar.gz
mvn package -DskipTests -Phadoop_2
```

```
$HADOOP_HOME/bin/hdfs dfs -rmr /user/graetzer/input
$HADOOP_HOME/bin/hdfs dfs -mkdir /user
$HADOOP_HOME/bin/hdfs dfs -mkdir /user/graetzer
$HADOOP_HOME/bin/hdfs dfs -put ~/testdata/orkut/edges-adj.txt /user/graetzer/input

$HADOOP_HOME/bin/hadoop jar myjar.jar org.apache.giraph.GiraphRunner PageRank --yarnjars myjar.jar --workers 1 \
--vertexInputFormat org.apache.giraph.examples.LongDoubleNullTextInputFormat --vertexInputPath /user/graetzer/input \
--vertexOutputFormat org.apache.giraph.io.formats.IdWithValueTextOutputFormat --outputPath /user/graetzer/output \
--combiner org.apache.giraph.combiner.DoubleSumCombiner -ca giraph.SplitMasterWorker=false
```
