package net.sf.l2j.gameserver.network.serverpackets;

import org.slf4j.LoggerFactory;

import net.sf.l2j.gameserver.model.actor.Player;

public class PrivateStoreMsgSell extends L2GameServerPacket {

	private final Player _activeChar;
	private String _storeMsg;

	public PrivateStoreMsgSell(Player player) {
		_activeChar = player;
		if (_activeChar.getSellList() != null) {
			_storeMsg = _activeChar.getSellList().getTitle();
		}
	}

	@Override
	protected final void writeImpl() {
		writeC(0x9c);
		writeD(_activeChar.getObjectId());
		writeS(_storeMsg);
	}
}
