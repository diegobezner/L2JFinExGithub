package net.sf.l2j.gameserver.handler.skillhandlers;

import org.slf4j.LoggerFactory;

import net.sf.l2j.commons.random.Rnd;

import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.model.L2ExtractableProductItem;
import net.sf.l2j.gameserver.model.L2ExtractableSkill;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.holder.IntIntHolder;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.skills.L2Skill;
import net.sf.l2j.gameserver.templates.skills.ESkillType;

public class Extractable implements ISkillHandler {

	private static final ESkillType[] SKILL_IDS
			= {
				ESkillType.EXTRACTABLE,
				ESkillType.EXTRACTABLE_FISH
			};

	@Override
	public void useSkill(Creature activeChar, L2Skill skill, WorldObject[] targets) {
		if (!(activeChar instanceof Player)) {
			return;
		}

		final L2ExtractableSkill exItem = skill.getExtractableSkill();
		if (exItem == null || exItem.getProductItemsArray().isEmpty()) {
			_log.warn("Missing informations for extractable skill id: " + skill.getId() + ".");
			return;
		}

		final Player player = activeChar.getPlayer();
		final int chance = Rnd.get(100000);

		boolean created = false;
		int chanceIndex = 0;

		for (L2ExtractableProductItem expi : exItem.getProductItemsArray()) {
			chanceIndex += (int) (expi.getChance() * 1000);
			if (chance <= chanceIndex) {
				for (IntIntHolder item : expi.getItems()) {
					player.addItem("Extract", item.getId(), item.getValue(), targets[0], true);
				}

				created = true;
				break;
			}
		}

		if (!created) {
			player.sendPacket(SystemMessageId.NOTHING_INSIDE_THAT);
			return;
		}
	}

	@Override
	public ESkillType[] getSkillIds() {
		return SKILL_IDS;
	}
}
