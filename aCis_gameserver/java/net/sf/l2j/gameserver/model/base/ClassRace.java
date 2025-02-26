package net.sf.l2j.gameserver.model.base;

import org.slf4j.LoggerFactory;

/**
 * This class defines all races that a player can choose.
 */
public enum ClassRace {
	HUMAN(1),
	ELF(1.5),
	DARK_ELF(1.5),
	ORC(0.9),
	DWARF(0.8);

	private final double _breathMultiplier;

	private ClassRace(double breathMultiplier) {
		_breathMultiplier = breathMultiplier;
	}

	/**
	 * @return the breath multiplier.
	 */
	public double getBreathMultiplier() {
		return _breathMultiplier;
	}
}
