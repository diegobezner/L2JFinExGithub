package net.sf.l2j.gameserver.network.clientpackets;

import org.slf4j.LoggerFactory;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.group.Party;
import net.sf.l2j.gameserver.network.SystemMessageId;

public final class RequestChangePartyLeader extends L2GameClientPacket {

	private String _name;

	@Override
	protected void readImpl() {
		_name = readS();
	}

	@Override
	protected void runImpl() {
		final Player player = getClient().getActiveChar();
		if (player == null) {
			return;
		}

		final Party party = player.getParty();
		if (party == null || !party.isLeader(player)) {
			player.sendPacket(SystemMessageId.ONLY_A_PARTY_LEADER_CAN_TRANSFER_ONES_RIGHTS_TO_ANOTHER_PLAYER);
			return;
		}

		party.changePartyLeader(_name);
	}
}
