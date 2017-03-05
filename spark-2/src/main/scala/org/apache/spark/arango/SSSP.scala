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
import org.apache.spark.graphx.{Graph, VertexId, GraphLoader}
import org.apache.spark.graphx.util.GraphGenerators
import org.apache.spark.sql.SparkSession
import org.apache.spark.internal.Logging

object SSSP {
  def main(args: Array[String]): Unit = {
    // Creates a SparkSession.
    val spark = SparkSession
      .builder
      .appName(s"${this.getClass.getSimpleName}")
      .getOrCreate()
    val sc = spark.sparkContext

    if (args.length < 3) {
      println("Submit at least <edge file  path> <out path> <source vertex id>")
      return
    }
    val inFile = args(0)
    val outFile = args(1)
    val sourceId : VertexId = args(2).toLong
    printf("Using source ID " + sourceId)
    // The ultimate source 58829577
    // Orkut: 1182118

    // A graph with edge attributes containing distances
    val graph: Graph[Int, Int] = GraphLoader.edgeListFile(sc, inFile)

    // Initialize the graph such that all vertices except the root have distance infinity.
    val initialGraph = graph.mapVertices((id, _) =>
        if (id == sourceId) 0 else 2323123)

    println("Loaded Graph from " + inFile)
    val sssp = initialGraph.pregel(Int.MaxValue)(
      (id, dist, newDist) => math.min(dist, newDist), // Vertex Program
      triplet => {  // Send Message
        if (triplet.srcAttr + triplet.attr < triplet.dstAttr) {
          Iterator((triplet.dstId, triplet.srcAttr + triplet.attr))
        } else {
          Iterator.empty
        }
      },
      (a, b) => math.min(a, b) // Merge Message
    )

    val pr = sssp.vertices.cache()
    println("Saving pageranks of pages to " + outFile)
    pr.map { case (id, r) => id + "\t" + r }.saveAsTextFile(outFile)

    spark.stop()
  }
}
