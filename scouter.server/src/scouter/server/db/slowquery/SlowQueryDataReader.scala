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

package scouter.server.db.slowquery;

import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import java.util.Hashtable
import scouter.server.db.io.zip.GZipStore
import scouter.util.FileUtil
import scouter.util.IClose
import scouter.server.Configure

object SlowQueryDataReader {
    val table = new Hashtable[String, SlowQueryDataReader]();

    def open(date: String, file: String): SlowQueryDataReader = {
        table.synchronized {
            var reader = table.get(file);
            if (reader != null) {
                reader.refrence += 1;
            } else {
                reader = new SlowQueryDataReader(date, file);
                table.put(file, reader);
            }
            return reader;
        }
    }

}
class SlowQueryDataReader(date: String, file: String) extends IClose {

    var refrence = 0;
    var pointFile: RandomAccessFile = null
    
    val slowQueryFile = new File(file + ".slowquery");
    if (slowQueryFile.canRead()) {
        this.pointFile = new RandomAccessFile(slowQueryFile, "r");
    }

    def read(point: Long): Array[Byte] = {
        if (pointFile == null)
            return null;
        try {
            this.synchronized {
                pointFile.seek(point);
                val len = pointFile.readShort();
                val buffer = new Array[Byte](len);
                pointFile.read(buffer);
                return buffer;
            }
        } catch {
            case e: IOException =>
                throw new RuntimeException(e);
        }
    }

    override def close() {
        SlowQueryDataReader.table.synchronized {
            if (this.refrence == 0) {
                SlowQueryDataReader.table.remove(this.file);
                pointFile = FileUtil.close(pointFile);
            } else {
                this.refrence -= 1;
            }
        }
    }

}