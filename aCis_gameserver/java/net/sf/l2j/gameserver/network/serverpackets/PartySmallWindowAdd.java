package net.sf.l2j.gameserver.network.serverpackets;

import org.slf4j.LoggerFactory;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.group.Party;

public final class PartySmallWindowAdd extends L2GameServerPacket {

	private final Player _member;
	private final int _leaderId;
	private final int _distribution;

	public PartySmallWindowAdd(Player member, Party party) {
		_member = member;
		_leaderId = party.getLeaderObjectId();
		_distribution = party.getLootRule().ordinal();
	}

	@Override
	protected final void writeImpl() {
		writeC(0x4f);
		writeD(_leaderId);
		writeD(_distribution);
		writeD(_member.getObjectId());
		writeS(_member.getName());
		writeD((int) _member.getCurrentCp());
		writeD(_member.getMaxCp());
		writeD((int) _member.getCurrentHp());
		writeD(_member.getMaxHp());
		writeD((int) _member.getCurrentMp());
		writeD(_member.getMaxMp());
		writeD(_member.getLevel());
		writeD(_member.getClassId().getId());
		writeD(0);// writeD(0x01); ??
		writeD(0);
	}
}
