package scouter.server.core
import scouter.util.RequestQueue
import scouter.lang.pack.SlowQueryPack
import scouter.server.Configure
import scouter.server.util.ThreadScala
import scouter.server.Logger
object SlowQueryCore {
  val conf = Configure.getInstance();
  val queue = new RequestQueue[SlowQueryPack](conf.xlog_queue_size); 
  
  ThreadScala.startDaemon("slowquery daemon") {
    while(CoreRun.running) {
      
    }
  }
  
  def add(p: SlowQueryPack) {
    val ok = queue.put(p);
    if(ok == false) {
      Logger.println("SlowQueryCore", 10, "queue exceeded!!");
    }
  }
  
}