package net.sf.l2j.gameserver.network.clientpackets;

import org.slf4j.LoggerFactory;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.pledge.Clan;
import net.sf.l2j.gameserver.network.SystemMessageId;

public final class RequestAnswerJoinAlly extends L2GameClientPacket {

	private int _response;

	@Override
	protected void readImpl() {
		_response = readD();
	}

	@Override
	protected void runImpl() {
		final Player activeChar = getClient().getActiveChar();
		if (activeChar == null) {
			return;
		}

		final Player requestor = activeChar.getRequest().getPartner();
		if (requestor == null) {
			return;
		}

		activeChar.getRequest().onRequestResponse();

		if (_response == 0) {
			activeChar.sendPacket(SystemMessageId.YOU_DID_NOT_RESPOND_TO_ALLY_INVITATION);
			requestor.sendPacket(SystemMessageId.NO_RESPONSE_TO_ALLY_INVITATION);
		} else {
			if (!(requestor.getRequest().getRequestPacket() instanceof RequestJoinAlly)) {
				return;
			}

			if (!Clan.checkAllyJoinCondition(requestor, activeChar)) {
				return;
			}

			activeChar.getClan().setAllyId(requestor.getClan().getAllyId());
			activeChar.getClan().setAllyName(requestor.getClan().getAllyName());
			activeChar.getClan().setAllyPenaltyExpiryTime(0, 0);
			activeChar.getClan().changeAllyCrest(requestor.getClan().getAllyCrestId(), true);
			activeChar.getClan().updateClanInDB();

			activeChar.sendPacket(SystemMessageId.YOU_ACCEPTED_ALLIANCE);
		}
	}
}
