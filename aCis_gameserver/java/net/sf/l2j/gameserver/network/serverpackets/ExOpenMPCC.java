package net.sf.l2j.gameserver.network.serverpackets;

import org.slf4j.LoggerFactory;

/**
 * @author chris_00 opens the CommandChannel Information window
 */
public class ExOpenMPCC extends L2GameServerPacket {

	public static final ExOpenMPCC STATIC_PACKET = new ExOpenMPCC();

	private ExOpenMPCC() {
	}

	@Override
	protected void writeImpl() {
		writeC(0xfe);
		writeH(0x25);
	}
}
