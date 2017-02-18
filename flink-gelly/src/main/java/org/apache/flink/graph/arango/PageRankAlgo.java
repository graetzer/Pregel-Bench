/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink.graph.arango;

import org.apache.flink.api.common.ProgramDescription;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.operators.SingleInputUdfOperator;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.graph.Edge;
import org.apache.flink.graph.Graph;
import org.apache.flink.graph.Vertex;
import org.apache.flink.graph.library.GSAPageRank;
import org.apache.flink.graph.library.PageRank;

public class PageRankAlgo implements ProgramDescription {

  static class TranformClass implements MapFunction<Tuple2<Long, Long>, Edge<Long, Double>> {

    @Override
    public Edge<Long, Double> map(Tuple2<Long, Long> input) throws Exception {
      return new Edge<Long, Double>(input.f0, input.f1, 1.0);
    }
  }
  
  public static void main(String[] args) throws Exception {
    
    if (!parseParameters(args)) {
      return;
    }
    
    ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
    
    //DataSet<Edge<Long, Double>> edges = getEdgesDataSet(env);
    //Graph<Long, Double, Double> graph = Graph.fromDataSet(edges, new InitVertices(), env);
    DataSet<Tuple2<Long, Long>> edges = getEdgesDataTupleSet(env);
    if (edges == null) {
      return;
    }

    DataSet<Edge<Long, Double>> edgeDataSet = edges.map(new TranformClass())
            .withForwardedFields("f0; f1");
    Graph<Long, Double, Double> graph = Graph.fromDataSet(edgeDataSet, new InitVertices(), env);
    //Graph<Long, Double, NullValue> graph = Graph.fromTuple2DataSet(edges, new InitVertices(), env);

    PageRank<Long> pageRank = new PageRank<Long>(0.85, maxIterations);
    //GSAPageRank<Long> pageRank = new GSAPageRank<Long>(0.85, maxIterations);
    DataSet<Vertex<Long, Double>> result = pageRank.run(graph);

	long start = System.nanoTime();
    // emit result
    if (fileOutput) {
	    result.writeAsCsv(outputPath, "\n", ",");
	    env.execute("Pregel PageRank");
    } else {
	    result.print();
    }
	  long runtimeGSA = System.nanoTime() - start;
	  System.out.print("Runtime PageRank: ");
	  System.out.print(runtimeGSA / 1000000.0);
	  System.out.println("s");
  }

  @SuppressWarnings("serial")
  private static final class InitVertices implements MapFunction<Long, Double> {

    public Double map(Long id) { return 1.0; }
  }

  // ******************************************************************************************************************
  // UTIL METHODS
  // ******************************************************************************************************************
  
  private static boolean fileOutput = false;
  
  private static String edgesInputPath = null;
  
  private static String outputPath = null;
    private static String fieldDelimiter = " ";

  
  private static int maxIterations = 49;
  
  private static boolean parseParameters(String[] args) {
    
    if(args.length >= 3) {
      
      fileOutput = true;
      edgesInputPath = args[0];
      outputPath = args[1];
      if ("t".equalsIgnoreCase(args[2])) {
        fieldDelimiter = "\t";
      }
      if (args.length >= 4) {
        maxIterations = Integer.parseInt(args[3]);
      }
      
    } else {
      System.out.println("Usage: Usage: PageRank <input edges path> <output path> <delimiter> [<iterations>]");
    }
    return true;
  }
  
  private static DataSet<Tuple2<Long, Long>> getEdgesDataTupleSet(ExecutionEnvironment env) {
    if (fileOutput) {
      return env.readCsvFile(edgesInputPath)
      .lineDelimiter("\n")
      .fieldDelimiter(fieldDelimiter)
      .ignoreComments("%")
      .types(Long.class, Long.class);
    } else {
      return null;//SingleSourceShortestPathsData.getDefaultEdgeDataSet(env);
    }
  }
  
  @Override
  public String getDescription() {
    return "Vertex-centric Single Source Shortest Paths";
  }
}
