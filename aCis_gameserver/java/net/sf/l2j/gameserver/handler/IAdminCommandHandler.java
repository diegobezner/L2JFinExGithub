package net.sf.l2j.gameserver.handler;

import org.slf4j.LoggerFactory;

import net.sf.l2j.gameserver.model.actor.Player;

public interface IAdminCommandHandler {

	/**
	 * this is the worker method that is called when someone uses an admin
	 * command.
	 *
	 * @param activeChar
	 * @param command
	 * @return command success
	 */
	public boolean useAdminCommand(String command, Player activeChar);

	/**
	 * this method is called at initialization to register all the item ids
	 * automatically
	 *
	 * @return all known itemIds
	 */
	public String[] getAdminCommandList();
}
