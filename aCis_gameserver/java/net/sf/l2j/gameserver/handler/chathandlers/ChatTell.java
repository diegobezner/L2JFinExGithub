package net.sf.l2j.gameserver.handler.chathandlers;

import org.slf4j.LoggerFactory;

import net.sf.l2j.gameserver.handler.IChatHandler;
import net.sf.l2j.gameserver.model.BlockList;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;

public class ChatTell implements IChatHandler {

	private static final int[] COMMAND_IDS
			= {
				2
			};

	@Override
	public void handleChat(int type, Player activeChar, String target, String text) {
		if (target == null) {
			return;
		}

		final Player receiver = World.getInstance().getPlayer(target);
		if (receiver == null || receiver.getClient().isDetached()) {
			activeChar.sendPacket(SystemMessageId.TARGET_IS_NOT_FOUND_IN_THE_GAME);
			return;
		}

		if (activeChar.equals(receiver)) {
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			return;
		}

		if (receiver.isInJail() || receiver.isChatBanned()) {
			activeChar.sendPacket(SystemMessageId.TARGET_IS_CHAT_BANNED);
			return;
		}

		if (!activeChar.isGM() && (receiver.isInRefusalMode() || BlockList.isBlocked(receiver, activeChar))) {
			activeChar.sendPacket(SystemMessageId.THE_PERSON_IS_IN_MESSAGE_REFUSAL_MODE);
			return;
		}

		receiver.sendPacket(new CreatureSay(activeChar.getObjectId(), type, activeChar.getName(), text));
		activeChar.sendPacket(new CreatureSay(activeChar.getObjectId(), type, "->" + receiver.getName(), text));
	}

	@Override
	public int[] getChatTypeList() {
		return COMMAND_IDS;
	}
}
