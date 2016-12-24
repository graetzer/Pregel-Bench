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

package org.apache.flink.graph;

import org.apache.commons.lang3.text.StrBuilder;
import org.apache.flink.client.program.ProgramInvocationException;

/**
 * This default main class prints usage listing available classes.
 */
public class Usage {

	private static final Class[] EXAMPLES = new Class[]{
		org.apache.flink.graph.social.SSSP.class,
		org.apache.flink.graph.social.PageRankAlgo.class
	};

	private static String getUsage() {
		StrBuilder strBuilder = new StrBuilder();

		strBuilder.appendNewLine();
		strBuilder.appendln("Example classes illustrate Gelly APIs or alternative algorithms:");
		for (Class cls : EXAMPLES) {
			strBuilder.append("  ").appendln(cls.getName());
		}

		return strBuilder.toString();
	}

	public static void main(String[] args) throws Exception {
		// this exception is throw to prevent Flink from printing an error message
		throw new ProgramInvocationException(getUsage());
	}
}
