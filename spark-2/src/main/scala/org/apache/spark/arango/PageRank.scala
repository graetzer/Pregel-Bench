/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.spark.arango
import org.apache.spark.graphx.GraphLoader
import org.apache.spark.graphx.lib.PageRank
import org.apache.spark.sql.SparkSession
import org.apache.spark.internal.Logging


object PageRankAlgo {
  def main(args: Array[String]): Unit = {
    // Creates a SparkSession.
    val spark = SparkSession
      .builder
      .appName(s"${this.getClass.getSimpleName}")
      .getOrCreate()
    val sc = spark.sparkContext

    if (args.length < 2) {
      println("Submit at least <edge file path> <out path>")
      return
    }
    val inFile = args(0) //sc.textFile(args(0)
    val outFile = args(1) //sc.textFile()

    // $example on$
    // Load the edges as a graph
    val graph = GraphLoader.edgeListFile(sc, inFile, true)
    println("Loaded graph from " + inFile)

    // Run PageRank
    val outGraph = PageRank.run(graph, 20)
    val pr = outGraph.vertices.cache()

    println("Saving pageranks of pages to " + outFile)

    pr.map { case (id, r) => id + "\t" + r }.saveAsTextFile(outFile)

    sc.stop()
    spark.stop()
  }
}
// scalastyle:on println
