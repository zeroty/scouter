/*
 *  Copyright 2015 the original author or authors. 
 *  @https://github.com/scouter-project/scouter
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); 
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License. 
 *
 */

package scouter.server.db;

import java.io.File
import java.util.List
import java.util.Vector
import scouter.io.DataOutputX
import scouter.util.FileUtil
import scouter.server.db.io.IndexTimeFile
import scouter.server.db.slowquery.SlowQueryDataReader
import scouter.server.db.slowquery.SlowQueryIndex


object SlowQueryRD {

    def readByTime(date: String, fromTime: Long, toTime: Long, handler: (Long, Array[Byte]) => Any) {
        val path = SlowQueryWR.getDBPath(date);
        if (new File(path).canRead()) {
            val file = path + "/" + XLogWR.prefix;
            var reader: SlowQueryDataReader = null;
            var table: IndexTimeFile = null;
            try {
                reader = SlowQueryDataReader.open(date, file);
                table = new IndexTimeFile(file + SlowQueryIndex.POSTFIX_TIME);
                table.read(fromTime, toTime, handler, reader.read)
            } catch {
                case e: Exception => e.printStackTrace()
                case _ :Throwable=>
            } finally {
                FileUtil.close(table);
                FileUtil.close(reader);
            }
        }
    }

    def readFromEndTime(date: String, fromTime: Long, toTime: Long, handler: (Long, Array[Byte]) => Any) {

        val path = XLogWR.getDBPath(date);
        if (new File(path).canRead()) {
            val file = path + "/" + XLogWR.prefix;
            var reader: SlowQueryDataReader = null;
            var table: IndexTimeFile = null;
            try {
                reader = SlowQueryDataReader.open(date, file);
                table = new IndexTimeFile(file + SlowQueryIndex.POSTFIX_TIME);
                table.readFromEnd(fromTime, toTime, handler, reader.read)
            } catch {
                case e: Throwable => e.printStackTrace();
            } finally {
                FileUtil.close(table);
                FileUtil.close(reader);
            }
        }
    }

   

}