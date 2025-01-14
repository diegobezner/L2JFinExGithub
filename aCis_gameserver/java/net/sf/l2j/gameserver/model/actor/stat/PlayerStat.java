package net.sf.l2j.gameserver.model.actor.stat;

import java.util.Map;
import net.sf.finex.model.creature.player.ComboComponent;
import net.sf.finex.model.talents.LineagePointsManager;
import net.sf.l2j.Config;
import net.sf.l2j.commons.math.MathUtil;
import net.sf.l2j.gameserver.instancemanager.ZoneManager;
import net.sf.l2j.gameserver.model.RewardInfo;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Pet;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.base.Experience;
import net.sf.l2j.gameserver.model.group.Party;
import net.sf.l2j.gameserver.model.pledge.Clan;
import net.sf.l2j.gameserver.model.zone.ZoneId;
import net.sf.l2j.gameserver.model.zone.type.L2SwampZone;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.PledgeShowMemberListUpdate;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.network.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.network.serverpackets.UserInfo;
import net.sf.l2j.gameserver.scripting.QuestState;
import net.sf.l2j.gameserver.skills.L2Skill;
import net.sf.l2j.gameserver.skills.Stats;

public class PlayerStat extends PlayableStat {

	private int _oldMaxHp; // stats watch
	private int _oldMaxMp; // stats watch
	private int _oldMaxCp; // stats watch

	public PlayerStat(Player activeChar) {
		super(activeChar);
	}

	@Override
	public boolean addExp(long value) {
		// Allowed to gain exp?
		if (!getActiveChar().getAccessLevel().canGainExp()) {
			return false;
		}

		if (!super.addExp(value)) {
			return false;
		}

		getActiveChar().sendPacket(new UserInfo(getActiveChar()));
		return true;
	}

	/**
	 * Add Experience and SP rewards to the Player, remove its Karma (if
	 * necessary) and Launch increase level task.
	 * <ul>
	 * <li>Remove Karma when the player kills L2MonsterInstance</li>
	 * <li>Send StatusUpdate to the Player</li>
	 * <li>Send a Server->Client System Message to the Player</li>
	 * <li>If the Player increases its level, send SocialAction (broadcast)</li>
	 * <li>If the Player increases its level, manage the increase level task
	 * (Max MP, Max MP, Recommandation, Expertise and beginner skills...)</li>
	 * <li>If the Player increases its level, send UserInfo to the Player</li>
	 * </ul>
	 *
	 * @param addToExp The Experience value to add
	 * @param addToSp The SP value to add
	 */
	@Override
	public boolean addExpAndSp(long addToExp, int addToSp) {
		final ComboComponent combo = getActiveChar().getComponent(ComboComponent.class);
		if (!getActiveChar().isInParty() && combo.isActivated()) {
			addToExp *= combo.calcExpAndSp();
			addToSp *= combo.calcExpAndSp();
		}

		if (!super.addExpAndSp(addToExp, addToSp)) {
			return false;
		}

		SystemMessage sm;

		if (addToExp == 0 && addToSp > 0) {
			sm = SystemMessage.getSystemMessage(SystemMessageId.ACQUIRED_S1_SP).addNumber(addToSp);
		} else if (addToExp > 0 && addToSp == 0) {
			sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S1_EXPERIENCE).addNumber((int) addToExp);
		} else {
			sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_EARNED_S1_EXP_AND_S2_SP).addNumber((int) addToExp).addNumber(addToSp);
		}

		getActiveChar().sendPacket(sm);

		return true;
	}

	/**
	 * Add Experience and SP rewards to the Player, remove its Karma (if
	 * necessary) and Launch increase level task.
	 * <ul>
	 * <li>Remove Karma when the player kills L2MonsterInstance</li>
	 * <li>Send StatusUpdate to the Player</li>
	 * <li>Send a Server->Client System Message to the Player</li>
	 * <li>If the Player increases its level, send SocialAction (broadcast)</li>
	 * <li>If the Player increases its level, manage the increase level task
	 * (Max MP, Max MP, Recommandation, Expertise and beginner skills...)</li>
	 * <li>If the Player increases its level, send UserInfo to the Player</li>
	 * </ul>
	 *
	 * @param addToExp The Experience value to add
	 * @param addToSp The SP value to add
	 * @param rewards The list of players and summons, who done damage
	 * @return
	 */
	public boolean addExpAndSp(long addToExp, int addToSp, Map<Creature, RewardInfo> rewards) {
		// GM check concerning canGainExp().
		if (!getActiveChar().getAccessLevel().canGainExp()) {
			return false;
		}

		// If this player has a pet, give the xp to the pet now (if any).
		if (getActiveChar().hasPet()) {
			final Pet pet = (Pet) getActiveChar().getActiveSummon();
			if (pet.getStat().getExp() <= (pet.getTemplate().getPetDataEntry(81).getMaxExp() + 10000) && !pet.isDead()) {
				if (MathUtil.checkIfInShortRadius(Config.PARTY_RANGE, pet, getActiveChar(), true)) {
					int ratio = pet.getPetData().getExpType();
					long petExp = 0;
					int petSp = 0;
					if (ratio == -1) {
						RewardInfo r = rewards.get(pet);
						RewardInfo reward = rewards.get(getActiveChar());
						if (r != null && reward != null) {
							double damageDoneByPet = ((double) (r.getDamage())) / reward.getDamage();
							petExp = (long) (addToExp * damageDoneByPet);
							petSp = (int) (addToSp * damageDoneByPet);
						}
					} else {
						// now adjust the max ratio to avoid the owner earning negative exp/sp
						if (ratio > 100) {
							ratio = 100;
						}

						petExp = Math.round(addToExp * (1 - (ratio / 100.0)));
						petSp = (int) Math.round(addToSp * (1 - (ratio / 100.0)));
					}

					addToExp -= petExp;
					addToSp -= petSp;
					pet.addExpAndSp(petExp, petSp);
				}
			}
		}
		return addExpAndSp(addToExp, addToSp);
	}

	@Override
	public boolean removeExpAndSp(long removeExp, int removeSp) {
		return removeExpAndSp(removeExp, removeSp, true);
	}

	public boolean removeExpAndSp(long removeExp, int removeSp, boolean sendMessage) {
		final int oldLevel = getLevel();

		if (!super.removeExpAndSp(removeExp, removeSp)) {
			return false;
		}

		// Send messages.
		if (sendMessage) {
			if (removeExp > 0) {
				getActiveChar().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EXP_DECREASED_BY_S1).addNumber((int) removeExp));
			}

			if (removeSp > 0) {
				getActiveChar().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.SP_DECREASED_S1).addNumber(removeSp));
			}

			if (getLevel() < oldLevel) {
				getActiveChar().broadcastStatusUpdate();
			}
		}
		return true;
	}

	@Override
	public final boolean addLevel(byte value) {
		if (getLevel() + value > Experience.MAX_LEVEL - 1) {
			return false;
		}

		boolean levelIncreased = super.addLevel(value);

		if (levelIncreased) {
			if (!Config.DISABLE_TUTORIAL) {
				QuestState qs = getActiveChar().getQuestState("Tutorial");
				if (qs != null) {
					qs.getQuest().notifyEvent("CE40", null, getActiveChar());
				}
			}

			getActiveChar().setCurrentCp(getMaxCp());
			getActiveChar().broadcastPacket(new SocialAction(getActiveChar(), 15));
			getActiveChar().sendPacket(SystemMessageId.YOU_INCREASED_YOUR_LEVEL);
		}

		// Give Expertise skill of this level
		getActiveChar().rewardSkills();

		final Clan clan = getActiveChar().getClan();
		if (clan != null) {
			clan.updateClanMember(getActiveChar());
			clan.broadcastToOnlineMembers(new PledgeShowMemberListUpdate(getActiveChar()));
		}

		// Recalculate the party level
		final Party party = getActiveChar().getParty();
		if (party != null) {
			party.recalculateLevel();
		}

		// Set new reach level for all game
		LineagePointsManager.getInstance().giveLineagePoints(getActiveChar());
		// Update the overloaded status of the player
		getActiveChar().refreshOverloaded();
		// Update the expertise status of the player
		getActiveChar().refreshExpertisePenalty();
		// Send UserInfo to the player
		getActiveChar().sendPacket(new UserInfo(getActiveChar()));

		return levelIncreased;
	}

	@Override
	public final long getExpForLevel(int level) {
		return Experience.LEVEL[level];
	}

	@Override
	public final Player getActiveChar() {
		return (Player) super.getActiveChar();
	}

	@Override
	public final long getExp() {
		if (getActiveChar().isSubClassActive()) {
			return getActiveChar().getSubClasses().get(getActiveChar().getClassIndex()).getExp();
		}

		return super.getExp();
	}

	@Override
	public final void setExp(long value) {
		if (getActiveChar().isSubClassActive()) {
			getActiveChar().getSubClasses().get(getActiveChar().getClassIndex()).setExp(value);
		} else {
			super.setExp(value);
		}
	}

	@Override
	public final byte getLevel() {
		if (getActiveChar().isSubClassActive()) {
			return getActiveChar().getSubClasses().get(getActiveChar().getClassIndex()).getLevel();
		}

		return super.getLevel();
	}

	@Override
	public final void setLevel(byte value) {
		if (value > Experience.MAX_LEVEL - 1) {
			value = Experience.MAX_LEVEL - 1;
		}

		if (getActiveChar().isSubClassActive()) {
			getActiveChar().getSubClasses().get(getActiveChar().getClassIndex()).setLevel(value);
		} else {
			super.setLevel(value);
		}
	}

	@Override
	public final int getMaxCp() {
		// Get the Max CP (base+modifier) of the player
		int val = (int) calcStat(Stats.MaxCP, getActiveChar().getTemplate().getBaseCpMax(getActiveChar().getLevel()), null, null);
		if (val != _oldMaxCp) {
			_oldMaxCp = val;

			// Launch a regen task if the new Max CP is higher than the old one
			if (getActiveChar().getStatus().getCurrentCp() != val) {
				getActiveChar().getStatus().setCurrentCp(getActiveChar().getStatus().getCurrentCp()); // trigger start of regeneration
			}
		}
		return val;
	}

	@Override
	public final int getMaxHp() {
		// Get the Max HP (base+modifier) of the player
		int val = super.getMaxHp();
		if (val != _oldMaxHp) {
			_oldMaxHp = val;

			// Launch a regen task if the new Max HP is higher than the old one
			if (getActiveChar().getStatus().getCurrentHp() != val) {
				getActiveChar().getStatus().setCurrentHp(getActiveChar().getStatus().getCurrentHp()); // trigger start of regeneration
			}
		}

		return val;
	}

	@Override
	public final int getMaxMp() {
		// Get the Max MP (base+modifier) of the player
		int val = super.getMaxMp();

		if (val != _oldMaxMp) {
			_oldMaxMp = val;

			// Launch a regen task if the new Max MP is higher than the old one
			if (getActiveChar().getStatus().getCurrentMp() != val) {
				getActiveChar().getStatus().setCurrentMp(getActiveChar().getStatus().getCurrentMp()); // trigger start of regeneration
			}
		}

		return val;
	}

	@Override
	public final int getSp() {
		if (getActiveChar().isSubClassActive()) {
			return getActiveChar().getSubClasses().get(getActiveChar().getClassIndex()).getSp();
		}

		return super.getSp();
	}

	@Override
	public final void setSp(int value) {
		if (getActiveChar().isSubClassActive()) {
			getActiveChar().getSubClasses().get(getActiveChar().getClassIndex()).setSp(value);
		} else {
			super.setSp(value);
		}

		StatusUpdate su = new StatusUpdate(getActiveChar());
		su.addAttribute(StatusUpdate.SP, getSp());
		getActiveChar().sendPacket(su);
	}

	@Override
	public int getBaseRunSpeed() {
		if (getActiveChar().isMounted()) {
			int base = (getActiveChar().isFlying()) ? getActiveChar().getPetDataEntry().getMountFlySpeed() : getActiveChar().getPetDataEntry().getMountBaseSpeed();

			if (getActiveChar().getLevel() < getActiveChar().getMountLevel()) {
				base /= 2;
			}

			if (getActiveChar().checkFoodState(getActiveChar().getPetTemplate().getHungryLimit())) {
				base /= 2;
			}

			return base;
		}

		return super.getBaseRunSpeed();
	}

	public int getBaseSwimSpeed() {
		if (getActiveChar().isMounted()) {
			int base = getActiveChar().getPetDataEntry().getMountSwimSpeed();

			if (getActiveChar().getLevel() < getActiveChar().getMountLevel()) {
				base /= 2;
			}

			if (getActiveChar().checkFoodState(getActiveChar().getPetTemplate().getHungryLimit())) {
				base /= 2;
			}

			return base;
		}

		return getActiveChar().getTemplate().getBaseSwimSpeed();
	}

	@Override
	public float getMoveSpeed() {
		// get base value, use swimming speed in water
		float baseValue = getActiveChar().isInsideZone(ZoneId.WATER) ? getBaseSwimSpeed() : getBaseMoveSpeed();

		// apply zone modifier before final calculation
		if (getActiveChar().isInsideZone(ZoneId.SWAMP)) {
			final L2SwampZone zone = ZoneManager.getInstance().getZone(getActiveChar(), L2SwampZone.class);
			if (zone != null) {
				baseValue *= (100 + zone.getMoveBonus()) / 100.0;
			}
		}

		// apply armor grade penalty before final calculation
		final int penalty = getActiveChar().getExpertiseArmorPenalty();
		if (penalty > 0) {
			baseValue *= Math.pow(0.84, penalty);
		}

		// calculate speed
		return (float) calcStat(Stats.Speed, baseValue, null, null);
	}

	@Override
	public int getMAtk(Creature target, L2Skill skill) {
		if (getActiveChar().isMounted()) {
			double base = getActiveChar().getPetDataEntry().getMountMAtk();

			if (getActiveChar().getLevel() < getActiveChar().getMountLevel()) {
				base /= 2;
			}

			return (int) calcStat(Stats.MAtk, base, null, null);
		}

		return super.getMAtk(target, skill);
	}

	@Override
	public int getMAtkSpd() {
		double base = 333;

		if (getActiveChar().isMounted()) {
			if (getActiveChar().checkFoodState(getActiveChar().getPetTemplate().getHungryLimit())) {
				base /= 2;
			}
		}

		final int penalty = getActiveChar().getExpertiseArmorPenalty();
		if (penalty > 0) {
			base *= Math.pow(0.84, penalty);
		}

		return (int) calcStat(Stats.MAtkSpd, base, null, null);
	}

	@Override
	public int getPAtk(Creature target) {
		if (getActiveChar().isMounted()) {
			double base = getActiveChar().getPetDataEntry().getMountPAtk();

			if (getActiveChar().getLevel() < getActiveChar().getMountLevel()) {
				base /= 2;
			}

			return (int) calcStat(Stats.PAtk, base, null, null);
		}

		return super.getPAtk(target);
	}

	@Override
	public int getPAtkSpd() {
		if (getActiveChar().isMounted()) {
			int base = getActiveChar().getPetDataEntry().getMountAtkSpd();

			if (getActiveChar().checkFoodState(getActiveChar().getPetTemplate().getHungryLimit())) {
				base /= 2;
			}

			return (int) calcStat(Stats.PAtkSpd, base, null, null);
		}
		return super.getPAtkSpd();
	}

	@Override
	public int getEvasionRate(Creature target) {
		int val = super.getEvasionRate(target);

		final int penalty = getActiveChar().getExpertiseArmorPenalty();
		if (penalty > 0) {
			val -= (2 * penalty);
		}

		return val;
	}

	@Override
	public int getAccuracy() {
		int val = super.getAccuracy();

		if (getActiveChar().getExpertiseWeaponPenalty()) {
			val -= 20;
		}

		return val;
	}

	@Override
	public int getPhysicalAttackRange() {
		return (int) calcStat(Stats.PAtkRange, getActiveChar().getAttackType().getRange(), null, null);
	}

	public int getCpRegenInterval() {
		return (int) calcStat(Stats.CpRegenInterval, 3000, null, null);
	}

	@Override
	public int getMpConsume(L2Skill skill) {
		int mpConsume = super.getMpConsume(skill);
		
		if (getActiveChar().hasTalent(14)) {
			switch (skill.getId()) {
				// reduce mana consume for all sonic skills which consumes charges
				case 6:
				case 5:
				case 261:
				case 7:
				case 9:
					final double hpBonus = (getActiveChar().getMaxHp() - getActiveChar().getCurrentHp()) / 117;
					mpConsume = (int) (1 - hpBonus * 1.5 / 100.0);
					break;
			}
		}
		
		return mpConsume;
	}
}
