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

import static org.icgc.dcc.etl2.core.util.ObjectNodes.textValue;
import static org.icgc.dcc.etl2.job.export.model.ExportTables.DATA_CONTENT_FAMILY;
import static org.icgc.dcc.etl2.job.export.model.type.Constants.DONOR_ID;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.TreeMap;

import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.spark.api.java.function.PairFunction;
import org.icgc.dcc.etl2.job.export.util.HTableManager;

import scala.Tuple2;
import scala.Tuple3;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;

@Slf4j
public class ProcessDataType implements
    PairFunction<Tuple2<ObjectNode, Long>, String, Tuple3<Map<ByteBuffer, KeyValue[]>, Long, Integer>> {

  @Override
  public Tuple2<String, Tuple3<Map<ByteBuffer, KeyValue[]>, Long, Integer>> call(Tuple2<ObjectNode, Long> tuple)
      throws Exception {
    long index = tuple._2();
    ObjectNode row = tuple._1();
    String donorId = getKey(row);
    byte[] rowKey = HTableManager.encodedRowKey(Integer.valueOf(donorId), index);
    byte i = -1;
    long totalBytes = 0;
    val kvs = Lists.<KeyValue> newArrayList();
    long now = System.currentTimeMillis();
    val fields = row.fieldNames();
    log.info("row: '{}'", row);
    while (fields.hasNext()) {
      i++;
      val field = fields.next();
      val cellValue = row.get(field);
      if (cellValue == null || cellValue.isNull()) {
        continue;
      }
      String value = cellValue.textValue();
      if (value == null || value.trim().isEmpty()) continue;
      byte[] bytes = Bytes.toBytes(value);
      KeyValue kv = new KeyValue(rowKey, DATA_CONTENT_FAMILY, new byte[] { i }, now, bytes);
      totalBytes = totalBytes + bytes.length;
      kvs.add(kv);
    }

    KeyValue[] kv = kvs.toArray(new KeyValue[kvs.size()]);
    Map<ByteBuffer, KeyValue[]> data = new TreeMap<ByteBuffer, KeyValue[]>();
    data.put(ByteBuffer.wrap(rowKey), kv);
    val tuple3 = new Tuple3<Map<ByteBuffer, KeyValue[]>, Long, Integer>(data, totalBytes, 1);

    return new Tuple2<String, Tuple3<Map<ByteBuffer, KeyValue[]>, Long, Integer>>(donorId, tuple3);
  }

  private String getKey(ObjectNode row) {

    return textValue(row, DONOR_ID);
  }
}
