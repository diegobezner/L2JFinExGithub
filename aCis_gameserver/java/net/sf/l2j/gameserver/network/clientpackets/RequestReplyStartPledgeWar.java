package net.sf.l2j.gameserver.network.clientpackets;

import org.slf4j.LoggerFactory;

import net.sf.l2j.gameserver.data.sql.ClanTable;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.SystemMessageId;

public final class RequestReplyStartPledgeWar extends L2GameClientPacket {

	private int _answer;

	@Override
	protected void readImpl() {
		_answer = readD();
	}

	@Override
	protected void runImpl() {
		final Player activeChar = getClient().getActiveChar();
		if (activeChar == null) {
			return;
		}

		final Player requestor = activeChar.getActiveRequester();
		if (requestor == null) {
			return;
		}

		if (_answer == 1) {
			ClanTable.getInstance().storeClansWars(requestor.getClanId(), activeChar.getClanId());
		} else {
			requestor.sendPacket(SystemMessageId.WAR_PROCLAMATION_HAS_BEEN_REFUSED);
		}

		activeChar.setActiveRequester(null);
		requestor.onTransactionResponse();
	}
}
