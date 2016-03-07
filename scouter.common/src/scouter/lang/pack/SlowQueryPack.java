package scouter.lang.pack;

import java.io.IOException;

import scouter.io.DataInputX;
import scouter.io.DataOutputX;

public class SlowQueryPack implements Pack {
	public String startTime;
	public String user;
	public String host;
	public String threadId;
	public String db;
	public boolean qcHit;
	public String queryTime;
	public String lockTime;
	public String rowsSent;
	public String rowsExamined;
	public int sqlHash;
	
	@Override
	public byte getPackType() {
		return PackEnum.SLOW_QUERY;
	}

	@Override
	public void write(DataOutputX out) throws IOException {
		out.writeText(startTime);
		out.writeText(user);
		out.writeText(host);
		out.writeText(threadId);
		out.writeText(db);
		out.writeBoolean(qcHit);
		out.writeText(queryTime);
		out.writeText(lockTime);
		out.writeText(rowsSent);
		out.writeText(rowsExamined);
		out.writeInt(sqlHash);
	}

	@Override
	public Pack read(DataInputX in) throws IOException {
		this.startTime = in.readText();
		this.user = in.readText();
		this.host = in.readText();
		this.threadId = in.readText();
		this.db = in.readText(); 
		this.qcHit = in.readBoolean();
		this.queryTime = in.readText();
		this.lockTime = in.readText();
		this.rowsSent = in.readText();
		this.rowsExamined = in.readText();
		this.sqlHash = in.readInt();
		return this;
	}

}
