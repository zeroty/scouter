package scouter.server.core
import scouter.util.RequestQueue
import scouter.lang.pack.SlowQueryPack
import scouter.server.Configure
import scouter.server.util.ThreadScala
import scouter.server.Logger
import scouter.io.DataOutputX
import scouter.server.core.cache.SlowQueryCache
import scouter.server.db.SlowQueryWR
import scouter.server.db.DBCtr
object SlowQueryCore {
  val conf = Configure.getInstance();
  val queue = new RequestQueue[SlowQueryPack](DBCtr.LARGE_MAX_QUE_SIZE); 
  
  ThreadScala.startDaemon("scouter.server.core.SlowQuerCore", { CoreRun.running })  {
     val m = queue.get();
     ServerStat.put("slowquery.core.queue", queue.size());
     val b = new DataOutputX().writePack(m).toByteArray();
     SlowQueryCache.put(m.objHash, m.queryTime, b);
     SlowQueryWR.add(m.time, m.queryTime, b);
     
  }
  
  def add(p: SlowQueryPack) {
    val ok = queue.put(p);
    if(ok == false) {
      Logger.println("SlowQueryCore", 10, "queue exceeded!!");
    }
  }
  
}