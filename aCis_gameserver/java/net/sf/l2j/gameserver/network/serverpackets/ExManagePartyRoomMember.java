package net.sf.l2j.gameserver.network.serverpackets;

import org.slf4j.LoggerFactory;

import net.sf.l2j.gameserver.data.MapRegionTable;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.partymatching.PartyMatchRoom;

public class ExManagePartyRoomMember extends L2GameServerPacket {

	private final Player _activeChar;
	private final PartyMatchRoom _room;
	private final int _mode;

	public ExManagePartyRoomMember(Player player, PartyMatchRoom room, int mode) {
		_activeChar = player;
		_room = room;
		_mode = mode;
	}

	@Override
	protected void writeImpl() {
		writeC(0xfe);
		writeH(0x10);
		writeD(_mode);
		writeD(_activeChar.getObjectId());
		writeS(_activeChar.getName());
		writeD(_activeChar.getActiveClass());
		writeD(_activeChar.getLevel());
		writeD(MapRegionTable.getInstance().getClosestLocation(_activeChar.getX(), _activeChar.getY()));
		if (_room.getOwner().equals(_activeChar)) {
			writeD(1);
		} else {
			if ((_room.getOwner().isInParty() && _activeChar.isInParty()) && (_room.getOwner().getParty().getLeaderObjectId() == _activeChar.getParty().getLeaderObjectId())) {
				writeD(2);
			} else {
				writeD(0);
			}
		}
	}
}
