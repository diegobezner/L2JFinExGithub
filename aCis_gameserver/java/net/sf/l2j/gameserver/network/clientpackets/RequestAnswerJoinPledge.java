package net.sf.l2j.gameserver.network.clientpackets;

import org.slf4j.LoggerFactory;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.pledge.Clan;
import net.sf.l2j.gameserver.model.pledge.Clan.SubPledge;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.JoinPledge;
import net.sf.l2j.gameserver.network.serverpackets.PledgeShowInfoUpdate;
import net.sf.l2j.gameserver.network.serverpackets.PledgeShowMemberListAdd;
import net.sf.l2j.gameserver.network.serverpackets.PledgeShowMemberListAll;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public final class RequestAnswerJoinPledge extends L2GameClientPacket {

	private int _answer;

	@Override
	protected void readImpl() {
		_answer = readD();
	}

	@Override
	protected void runImpl() {
		Player activeChar = getClient().getActiveChar();
		if (activeChar == null) {
			return;
		}

		Player requestor = activeChar.getRequest().getPartner();
		if (requestor == null) {
			return;
		}

		if (_answer == 0) {
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_DID_NOT_RESPOND_TO_S1_CLAN_INVITATION).addCharName(requestor));
			requestor.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DID_NOT_RESPOND_TO_CLAN_INVITATION).addCharName(activeChar));
		} else {
			if (!(requestor.getRequest().getRequestPacket() instanceof RequestJoinPledge)) {
				return; // hax
			}
			RequestJoinPledge requestPacket = (RequestJoinPledge) requestor.getRequest().getRequestPacket();
			Clan clan = requestor.getClan();

			// we must double check this cause during response time conditions can be changed, i.e. another player could join clan
			if (clan.checkClanJoinCondition(requestor, activeChar, requestPacket.getPledgeType())) {
				activeChar.sendPacket(new JoinPledge(requestor.getClanId()));

				activeChar.setPledgeType(requestPacket.getPledgeType());

				switch (requestPacket.getPledgeType()) {
					case Clan.SUBUNIT_ACADEMY:
						activeChar.setPowerGrade(9);
						activeChar.setLvlJoinedAcademy(activeChar.getLevel());
						break;

					case Clan.SUBUNIT_ROYAL1:
					case Clan.SUBUNIT_ROYAL2:
						activeChar.setPowerGrade(7);
						break;

					case Clan.SUBUNIT_KNIGHT1:
					case Clan.SUBUNIT_KNIGHT2:
					case Clan.SUBUNIT_KNIGHT3:
					case Clan.SUBUNIT_KNIGHT4:
						activeChar.setPowerGrade(8);
						break;

					default:
						activeChar.setPowerGrade(6);
				}

				clan.addClanMember(activeChar);
				activeChar.setClanPrivileges(activeChar.getClan().getRankPrivs(activeChar.getPowerGrade()));

				activeChar.sendPacket(SystemMessageId.ENTERED_THE_CLAN);

				clan.broadcastToOtherOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_JOINED_CLAN).addCharName(activeChar), activeChar);
				clan.broadcastToOtherOnlineMembers(new PledgeShowMemberListAdd(activeChar), activeChar);
				clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));

				// this activates the clan tab on the new member
				activeChar.sendPacket(new PledgeShowMemberListAll(activeChar.getClan(), 0));
				for (SubPledge sp : activeChar.getClan().getAllSubPledges()) {
					activeChar.sendPacket(new PledgeShowMemberListAll(activeChar.getClan(), sp.getId()));
				}

				activeChar.setClanJoinExpiryTime(0);
				activeChar.broadcastUserInfo();
			}
		}
		activeChar.getRequest().onRequestResponse();
	}
}
