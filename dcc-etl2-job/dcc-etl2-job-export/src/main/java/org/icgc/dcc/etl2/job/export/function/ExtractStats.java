/*
 * Copyright (c) 2015 The Ontario Institute for Cancer Research. All rights reserved.                             
 *                                                                                                               
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * You should have received a copy of the GNU General Public License along with                                  
 * this program. If not, see <http://www.gnu.org/licenses/>.                                                     
 *                                                                                                               
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY                           
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES                          
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT                           
 * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,                                
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED                          
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;                               
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER                              
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN                         
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.icgc.dcc.etl2.job.export.function;

import java.nio.ByteBuffer;
import java.util.Map;

import lombok.val;

import org.apache.hadoop.hbase.KeyValue;
import org.apache.spark.api.java.function.Function;

import scala.Tuple2;
import scala.Tuple3; 

public class ExtractStats implements Function<Tuple2<String, Tuple3<Map<ByteBuffer, KeyValue[]>, Long, Integer>>, Tuple3<String, Long, Integer>> {

  @Override 
  public Tuple3<String, Long, Integer> call(Tuple2<String, Tuple3<Map<ByteBuffer, KeyValue[]>, Long, Integer>> tuple) throws Exception {
    val donorId = tuple._1();
    val tuple3 = tuple._2();
    
    return new Tuple3<String, Long, Integer>(donorId, tuple3._2(), tuple3._3());
  }
}
