package net.sf.l2j.gameserver.network.clientpackets;

import org.slf4j.LoggerFactory;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.partymatching.PartyMatchRoom;
import net.sf.l2j.gameserver.model.partymatching.PartyMatchRoomList;
import net.sf.l2j.gameserver.model.partymatching.PartyMatchWaitingList;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.ExPartyRoomMember;
import net.sf.l2j.gameserver.network.serverpackets.PartyMatchDetail;
import net.sf.l2j.gameserver.network.serverpackets.PartyMatchList;

public final class RequestPartyMatchConfig extends L2GameClientPacket {

	private int _auto, _loc, _lvl;

	@Override
	protected void readImpl() {
		_auto = readD();
		_loc = readD();
		_lvl = readD();
	}

	@Override
	protected void runImpl() {
		final Player activeChar = getClient().getActiveChar();
		if (activeChar == null) {
			return;
		}

		if (!activeChar.isInPartyMatchRoom() && activeChar.getParty() != null && activeChar.getParty().getLeader() != activeChar) {
			activeChar.sendPacket(SystemMessageId.CANT_VIEW_PARTY_ROOMS);
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (activeChar.isInPartyMatchRoom()) {
			// If Player is in Room show him room, not list
			PartyMatchRoomList list = PartyMatchRoomList.getInstance();
			if (list == null) {
				return;
			}

			PartyMatchRoom room = list.getPlayerRoom(activeChar);
			if (room == null) {
				return;
			}

			activeChar.sendPacket(new PartyMatchDetail(room));
			activeChar.sendPacket(new ExPartyRoomMember(room, 2));

			activeChar.setPartyRoom(room.getId());
			activeChar.broadcastUserInfo();
		} else {
			// Add to waiting list
			PartyMatchWaitingList.getInstance().addPlayer(activeChar);

			// Send Room list
			activeChar.sendPacket(new PartyMatchList(activeChar, _auto, _loc, _lvl));
		}
	}
}
