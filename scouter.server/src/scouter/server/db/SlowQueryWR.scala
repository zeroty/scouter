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

import scouter.server.Configure
import scouter.server.Logger
import scouter.server.core.ServerStat
import scouter.server.db.xlog.XLogDataWriter
import scouter.server.db.xlog.XLogIndex
import scouter.server.util.OftenAction
import scouter.server.util.ThreadScala
import scouter.util.DateUtil
import scouter.util.FileUtil
import scouter.util.RequestQueue

object SlowQueryWR {

    val dir = "/slowquery"
    val prefix = "slowquery"

   // val queue = new RequestQueue[Data](Configure.getInstance().xlog_queue_size);
    val queue = new RequestQueue[Data](DBCtr.LARGE_MAX_QUE_SIZE);

    var currentDateUnit: Long = 0
    var index: XLogIndex = null
    var writer: XLogDataWriter = null

    ThreadScala.start("scouter.server.db.SlowQueryWR") {
        while (DBCtr.running) {
            val m = queue.get();
            
            ServerStat.put("slowquery.db.queue",queue.size());
            try {
                if (currentDateUnit != DateUtil.getDateUnit(m.time)) {
                    currentDateUnit = DateUtil.getDateUnit(m.time);
                    close();
                    open(DateUtil.yyyymmdd(m.time));
                }
                if (index == null) {
                    OftenAction.act("SlowQueryWR", 10) {
                        queue.clear();
                        currentDateUnit = 0;
                    }
                    Logger.println("SlowQueryWR", 10, "can't open ");
                } else {
                    val location = writer.write(m.data);
                    index.setByTime(m.time, location);
                    /*index.setByTxid(m.txid, location);
                    index.setByGxid(m.gxid, location);*/
                }
            } catch {
                case t: Throwable => t.printStackTrace()
            }
        }
        close()
    }

    def add(time: Long, queryTime: Int, data: Array[Byte]) {
        val ok = queue.put(new Data(time, queryTime, data));
        if (ok == false) {
            Logger.println("SlowQueryWR", 10, "queue exceeded!!");
        }
    }

    class Data(_time: Long, _queryTime: Int, _data: Array[Byte]) {
        val time = _time;
        val queryTime = _queryTime;
        val data = _data;
    }

    def close() {
        FileUtil.close(index);
        FileUtil.close(writer);
        index = null;
        writer = null;
    }

    def open(date: String) {
        try {
            val path = getDBPath(date);
            val f = new File(path);
            if (f.exists() == false)
                f.mkdirs();
            val file = path + "/" + prefix;
            index = XLogIndex.open(file);
            writer = XLogDataWriter.open(date, file);
        } catch {
            case e: Throwable => {
                e.printStackTrace();
                close()
            }
        }
    }

    def getDBPath(date: String): String = {
        val sb = new StringBuffer();
        sb.append(DBCtr.getRootPath());
        sb.append("/").append(date).append(dir);
        return sb.toString();
    }
}
