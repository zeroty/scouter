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

import java.io.IOException
import java.util.ArrayList
import java.util.HashMap
import java.util.Hashtable
import java.util.Iterator
import java.util.List
import java.util.Map
import java.util.Set
import scouter.server.db.io.IndexKeyFile
import scouter.server.db.io.IndexTimeFile
import scouter.io.DataInputX
import scouter.io.DataOutputX
import scouter.util.FileUtil
import scouter.util.IClose;
import scouter.server.util.EnumerScala

object SlowQueryIndex {
    val POSTFIX_TIME = "_tim";
    val POSTFIX_GID = "_gid";
    val POSTFIX_TID = "_tid";

    val table = new Hashtable[String, SlowQueryIndex]();

    def open(file: String): SlowQueryIndex = {
        table.synchronized {
            var index = table.get(file);
            if (index != null) {
                index.refrence += 1;
                return index;
            } else {
                index = new SlowQueryIndex(file);
                table.put(file, index);
                return index;
            }
        }
    }
}

class SlowQueryIndex(_file: String) extends IClose {

    val file = _file
    var refrence = 0;
    var timeIndex: IndexTimeFile = null
    
    def setByTime(time: Long, pos: Long) {
        if (this.timeIndex == null) {
            this.timeIndex = new IndexTimeFile(file + SlowQueryIndex.POSTFIX_TIME);
        }
        this.timeIndex.put(time, DataOutputX.toBytes5(pos));
    }
    
    override def close() {
        SlowQueryIndex.table.synchronized {
            if (this.refrence == 0) {
                SlowQueryIndex.table.remove(this.file);
                FileUtil.close(this.timeIndex);
            } else {
                this.refrence -= 1;
            }
        }
    }

}