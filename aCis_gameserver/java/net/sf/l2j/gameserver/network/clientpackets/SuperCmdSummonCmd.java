package net.sf.l2j.gameserver.network.clientpackets;

import org.slf4j.LoggerFactory;

/**
 * Format chS c: (id) 0x39 h: (subid) 0x01 S: the summon name (or maybe cmd
 * string ?)
 *
 * @author -Wooden-
 */
public class SuperCmdSummonCmd extends L2GameClientPacket {

	@SuppressWarnings("unused")
	private String _summonName;

	@Override
	protected void readImpl() {
		_summonName = readS();
	}

	@Override
	protected void runImpl() {
	}
}
