package net.sf.l2j.gameserver.network.clientpackets;

import org.slf4j.LoggerFactory;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.pledge.Clan;
import net.sf.l2j.gameserver.network.serverpackets.ManagePledgePower;

public final class RequestPledgePower extends L2GameClientPacket {

	private int _rank;
	private int _action;
	private int _privs;

	@Override
	protected void readImpl() {
		_rank = readD();
		_action = readD();

		if (_action == 2) {
			_privs = readD();
		} else {
			_privs = 0;
		}
	}

	@Override
	protected void runImpl() {
		final Player player = getClient().getActiveChar();
		if (player == null) {
			return;
		}

		final Clan clan = player.getClan();
		if (clan == null) {
			return;
		}

		if (_action == 2) {
			if (player.isClanLeader()) {
				if (_rank == 9) {
					_privs = (_privs & Clan.CP_CL_VIEW_WAREHOUSE) + (_privs & Clan.CP_CH_OPEN_DOOR) + (_privs & Clan.CP_CS_OPEN_DOOR);
				}

				player.getClan().setRankPrivs(_rank, _privs);
			}
		} else {
			player.sendPacket(new ManagePledgePower(clan, _action, _rank));
		}
	}
}
