package net.sf.l2j.gameserver.handler.itemhandlers;

import org.slf4j.LoggerFactory;

import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.ShotType;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.item.kind.Weapon;
import net.sf.l2j.gameserver.model.item.type.WeaponType;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUse;
import net.sf.l2j.gameserver.util.Broadcast;

/**
 * @author -Nemesiss-
 */
public class FishShots implements IItemHandler {

	@Override
	public void useItem(Playable playable, ItemInstance item, boolean forceUse) {
		if (!(playable instanceof Player)) {
			return;
		}

		final Player activeChar = (Player) playable;
		final ItemInstance weaponInst = activeChar.getActiveWeaponInstance();
		final Weapon weaponItem = activeChar.getActiveWeaponItem();

		if (weaponInst == null || weaponItem.getItemType() != WeaponType.FISHINGROD) {
			return;
		}

		// Fishshot is already active
		if (activeChar.isChargedShot(ShotType.FISH_SOULSHOT)) {
			return;
		}

		// Wrong grade of soulshot for that fishing pole.
		if (weaponItem.getCrystalType() != item.getItem().getCrystalType()) {
			activeChar.sendPacket(SystemMessageId.WRONG_FISHINGSHOT_GRADE);
			return;
		}

		if (!activeChar.destroyItemWithoutTrace("Consume", item.getObjectId(), 1, null, false)) {
			activeChar.sendPacket(SystemMessageId.NOT_ENOUGH_SOULSHOTS);
			return;
		}

		activeChar.setChargedShot(ShotType.FISH_SOULSHOT, true);
		Broadcast.toSelfAndKnownPlayers(activeChar, new MagicSkillUse(activeChar, item.getItem().getStaticSkills().get(0).getId(), 1, 0, 0));
	}
}
