package scouter.server.core
import scouter.util.RequestQueue
import scouter.lang.pack.SlowQueryPack
import scouter.server.Configure
import scouter.server.util.ThreadScala
import scouter.server.Logger
import scouter.io.DataOutputX
import scouter.server.core.cache.SlowQueryCache
import scouter.server.db.SlowQueryWR
object SlowQueryCore {
  val conf = Configure.getInstance();
  val queue = new RequestQueue[SlowQueryPack](conf.xlog_queue_size); 
  
  ThreadScala.startDaemon("scouter.server.core.XLogCore", { CoreRun.running })  {
     val m = queue.get();
     ServerStat.put("slowquery.core.queue", queue.size());
     val b = new DataOutputX().writePack(m).toByteArray();
     SlowQueryCache.put(m.objHash, m.queryTime.toInt, b);
     SlowQueryWR.add(m.startTime.toInt, m.queryTime.toInt, b);
    
  }
  
  def add(p: SlowQueryPack) {
    val ok = queue.put(p);
    if(ok == false) {
      Logger.println("SlowQueryCore", 10, "queue exceeded!!");
    }
  }
  
}