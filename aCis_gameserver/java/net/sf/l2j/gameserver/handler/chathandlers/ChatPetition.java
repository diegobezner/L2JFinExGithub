package net.sf.l2j.gameserver.handler.chathandlers;

import org.slf4j.LoggerFactory;

import net.sf.l2j.gameserver.handler.IChatHandler;
import net.sf.l2j.gameserver.instancemanager.PetitionManager;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.SystemMessageId;

public class ChatPetition implements IChatHandler {

	private static final int[] COMMAND_IDS
			= {
				6,
				7
			};

	@Override
	public void handleChat(int type, Player activeChar, String target, String text) {
		if (!PetitionManager.getInstance().isPlayerInConsultation(activeChar)) {
			activeChar.sendPacket(SystemMessageId.YOU_ARE_NOT_IN_PETITION_CHAT);
			return;
		}

		PetitionManager.getInstance().sendActivePetitionMessage(activeChar, text);
	}

	@Override
	public int[] getChatTypeList() {
		return COMMAND_IDS;
	}
}
