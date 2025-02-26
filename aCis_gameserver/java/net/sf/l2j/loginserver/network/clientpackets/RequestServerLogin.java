package net.sf.l2j.loginserver.network.clientpackets;

import org.slf4j.LoggerFactory;

import net.sf.l2j.Config;
import net.sf.l2j.loginserver.LoginController;
import net.sf.l2j.loginserver.network.SessionKey;
import net.sf.l2j.loginserver.network.serverpackets.LoginFail.LoginFailReason;
import net.sf.l2j.loginserver.network.serverpackets.PlayFail.PlayFailReason;
import net.sf.l2j.loginserver.network.serverpackets.PlayOk;

/**
 * Fromat is ddc d: first part of session id d: second part of session id c:
 * server ID
 */
public class RequestServerLogin extends L2LoginClientPacket {

	private int _skey1;
	private int _skey2;
	private int _serverId;

	public int getSessionKey1() {
		return _skey1;
	}

	public int getSessionKey2() {
		return _skey2;
	}

	public int getServerID() {
		return _serverId;
	}

	@Override
	public boolean readImpl() {
		if (super._buf.remaining() >= 9) {
			_skey1 = readD();
			_skey2 = readD();
			_serverId = readC();
			return true;
		}
		return false;
	}

	@Override
	public void run() {
		SessionKey sk = getClient().getSessionKey();

		// if we didnt showed the license we cant check these values
		if (!Config.SHOW_LICENCE || sk.checkLoginPair(_skey1, _skey2)) {
			if (LoginController.getInstance().isLoginPossible(getClient(), _serverId)) {
				getClient().setJoinedGS(true);
				getClient().sendPacket(new PlayOk(sk));
			} else {
				getClient().close(PlayFailReason.REASON_TOO_MANY_PLAYERS);
			}
		} else {
			getClient().close(LoginFailReason.REASON_ACCESS_FAILED);
		}
	}
}
