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

package org.apache.flink.graph.social;

import org.apache.flink.api.common.ProgramDescription;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.operators.SingleInputUdfOperator;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.graph.Edge;
import org.apache.flink.graph.Graph;
import org.apache.flink.graph.Vertex;
import org.apache.flink.graph.library.GSAPageRank;
import org.apache.flink.graph.library.PageRank;
import org.apache.flink.graph.pregel.ComputeFunction;
import org.apache.flink.graph.pregel.MessageCombiner;
import org.apache.flink.graph.pregel.MessageIterator;
import org.apache.flink.types.NullValue;

public class PageRankAlgo implements ProgramDescription {
  
  public static void main(String[] args) throws Exception {
    
    if (!parseParameters(args)) {
      return;
    }
    
    ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
    
    //DataSet<Edge<Long, Double>> edges = getEdgesDataSet(env);
    //Graph<Long, Double, Double> graph = Graph.fromDataSet(edges, new InitVertices(), env);
    DataSet<Tuple2<Long, Long>> edges = getEdgesDataTupleSet(env);
    DataSet<Edge<Long, Double>> edgeDataSet = edges.map((MapFunction<Tuple2<Long, Long>, Edge<Long, Double>>) input
            -> new Edge<Long, Double>(input.f0, input.f1, 1.0))
            .withForwardedFields("f0; f1");
    Graph.fromDataSet(edgeDataSet, new InitVertices(), env);
    Graph<Long, Double, Double> graph = Graph.fromDataSet(edgeDataSet, new InitVertices(), env);


    GSAPageRank<Long> pageRank = new GSAPageRank<Long>(0.85, 35);
    DataSet<Vertex<Long, Double>> result = pageRank.run(graph);


	 /* start = System.nanoTime();
	  PageRank<Long> pageRank2 = new PageRank<Long>(0.85, 35);
	  DataSet<Vertex<Long, Double>> result2 = pageRank2.run(graph);
	  long runtimeSA = System.nanoTime() - start;*/

	long start = System.nanoTime();
    // emit result
    if (fileOutput) {
	    result.writeAsCsv(outputPath, "\n", ",");
	    env.execute("Pregel Single Source Shortest Paths Example");
    } else {
	    result.print();
    }
	  long runtimeGSA = System.nanoTime() - start;
	  System.out.print("Runtime PageRank: ");
	  System.out.print(runtimeGSA / 1000000.0);
	  System.out.println("s");
  }

  // --------------------------------------------------------------------------------------------
  //  Single Source Shortest Path UDFs
  // --------------------------------------------------------------------------------------------

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
  
  private static boolean parseParameters(String[] args) {
    
    if(args.length > 0) {
      if(args.length != 4) {
        System.err.println("Usage: PregelSSSP <source vertex id>" +
                           " <input edges path> <output path> <num iterations>");
        return false;
      }
      
      fileOutput = true;
      edgesInputPath = args[0];
      outputPath = args[1];
    } else {
      System.out.println("Executing Pregel Single Source Shortest Paths example "
                         + "with default parameters and built-in default data.");
      System.out.println("  Provide parameters to read input data from files.");
      System.out.println("  See the documentation for the correct format of input files.");
      System.out.println("Usage: PregelSSSP <source vertex id>" +
                         " <input edges path> <output path> <num iterations>");
    }
    return true;
  }
  
  private static DataSet<Tuple2<Long, Long>> getEdgesDataTupleSet(ExecutionEnvironment env) {
    if (fileOutput) {
      return env.readCsvFile(edgesInputPath)
      .lineDelimiter("\n")
      .fieldDelimiter("\t")
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
