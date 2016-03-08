package scouter.lang.pack;

import java.io.IOException;

import scouter.io.DataInputX;
import scouter.io.DataOutputX;

public class SlowQueryPack implements Pack {
	public long time;
	public int objHash;
	public String startTime;
	public String user;
	public String host;
	public String threadId;
	public String db;
	public boolean qcHit;
	public int queryTime;
	public int lockTime;
	public String rowsSent;
	public String rowsExamined;
	public int sqlHash;
	public int normalizedSqlHash;
	
	
	@Override
	public byte getPackType() {
		return PackEnum.SLOW_QUERY;
	}

	@Override
	public void write(DataOutputX out) throws IOException {
		out.writeLong(time);
		out.writeInt(objHash);
		out.writeText(startTime);
		out.writeText(user);
		out.writeText(host);
		out.writeText(threadId);
		out.writeText(db);
		out.writeBoolean(qcHit);
		out.writeInt(queryTime);
		out.writeInt(lockTime);
		out.writeText(rowsSent);
		out.writeText(rowsExamined);
		out.writeInt(sqlHash);
		out.writeInt(normalizedSqlHash);
	}

	@Override
	public Pack read(DataInputX in) throws IOException {
		this.time = in.readLong();
		this.objHash = in.readInt();
		this.startTime = in.readText();
		this.user = in.readText();
		this.host = in.readText();
		this.threadId = in.readText();
		this.db = in.readText(); 
		this.qcHit = in.readBoolean();
		this.queryTime = in.readInt();
		this.lockTime = in.readInt();
		this.rowsSent = in.readText();
		this.rowsExamined = in.readText();
		this.sqlHash = in.readInt();
		this.normalizedSqlHash = in.readInt();
		return this;
	}

}
