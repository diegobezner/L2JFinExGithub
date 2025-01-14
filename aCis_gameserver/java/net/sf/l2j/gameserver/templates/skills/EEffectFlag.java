package net.sf.l2j.gameserver.templates.skills;

/**
 * @author UnAfraid
 */
public enum EEffectFlag {
	NONE,
	CHARM_OF_COURAGE,
	CHARM_OF_LUCK,
	PHOENIX_BLESSING,
	NOBLESS_BLESSING,
	SILENT_MOVE,
	PROTECTION_BLESSING,
	RELAXING,
	FEAR,
	CONFUSED,
	MAGIC_MUTED,
	PHYSICAL_MUTED,
	ABILITY_MUTED,
	ULTIAMTE_MUTED,
	RACE_MUTED,
	POTION_MUTED,
	PROFESSION_MUTED,
	ROOTED,
	SLEEP,
	STUNNED,
	BETRAYED,
	MEDITATING,
	PARALYZED;

	public int getMask() {
		return 1 << ordinal();
	}
}
