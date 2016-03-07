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

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Properties;

import scouter.server.Configure;
import scouter.server.db.io.RealDataFile;
import scouter.server.db.io.zip.GZipStore;
import scouter.util.FileUtil;
import scouter.util.IClose;

object SlowQueryDataWriter {
    val table = new Hashtable[String, SlowQueryDataWriter]();

    def open(date: String, file: String): SlowQueryDataWriter = {
        table.synchronized {
            var writer = table.get(file);
            if (writer != null) {
                writer.refrence += 1;
            } else {
                writer = new SlowQueryDataWriter(date, file);
                table.put(file, writer);
            }
            return writer;
        }
    }

}
class SlowQueryDataWriter(date: String, file: String) extends IClose {
    var refrence = 0;
    var out:RealDataFile = null
    out=new RealDataFile(file + ".slowquery");
      
    def write(bytes: Array[Byte]): Long = {
        this.synchronized {
            val point = out.getOffset();
            out.writeShort(bytes.length.toShort);
            out.write(bytes);
            out.flush();
            return point;
        }
    }

    override def close() {
        SlowQueryDataWriter.table.synchronized {
            if (this.refrence == 0) {
                SlowQueryDataWriter.table.remove(this.file);
                FileUtil.close(out);
            } else {
                this.refrence -= 1
            }
        }
    }
}