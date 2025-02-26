package net.sf.l2j.gameserver.network.serverpackets;

import org.slf4j.LoggerFactory;

import net.sf.finex.model.creature.attack.DamageInfo;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Creature;

/**
 * format dddc dddh (ddc)
 */
public class Attack extends L2GameServerPacket {

	public static final int HITFLAG_USESS = 0x10;
	public static final int HITFLAG_CRIT = 0x20;
	public static final int HITFLAG_SHLD = 0x40;
	public static final int HITFLAG_MISS = 0x80;

	public class Hit {

		protected final int _targetId;
		protected final int _damage;
		protected int _flags;

		Hit(WorldObject target, DamageInfo info) {
			_targetId = target.getObjectId();
			_damage = info.damage;

			if (info.isMiss) {
				_flags = HITFLAG_MISS;
				return;
			}

			if (soulshot) {
				_flags = HITFLAG_USESS | _ssGrade;
			}

			if (info.isCrit) {
				_flags |= HITFLAG_CRIT;
			}

			if ((info.shieldResult > 0 || info.isParry) && !(target.isPlayer() && target.getPlayer().isInOlympiadMode())) {
				_flags |= HITFLAG_SHLD;
			}
		}
	}

	private final int _attackerObjId;
	public final boolean soulshot;
	public final int _ssGrade;
	private final int _x, _y, _z;
	private Hit[] _hits;

	/**
	 * @param attacker The attacking Creature.
	 * @param useShots True if soulshots are used.
	 * @param ssGrade The grade of the soulshots.
	 */
	public Attack(Creature attacker, boolean useShots, int ssGrade) {
		_attackerObjId = attacker.getObjectId();
		soulshot = useShots;
		_ssGrade = ssGrade;
		_x = attacker.getX();
		_y = attacker.getY();
		_z = attacker.getZ();
	}

	public Hit createHit(WorldObject target, DamageInfo info) {
		return new Hit(target, info);
	}

	public void hit(Hit... hits) {
		if (_hits == null) {
			_hits = hits;
			return;
		}

		// this will only happen with pole attacks
		Hit[] tmp = new Hit[hits.length + _hits.length];
		System.arraycopy(_hits, 0, tmp, 0, _hits.length);
		System.arraycopy(hits, 0, tmp, _hits.length, hits.length);
		_hits = tmp;
	}

	/**
	 * @return True if the Server-Client packet Attack contains at least 1 hit.
	 */
	public boolean hasHits() {
		return _hits != null;
	}

	@Override
	protected final void writeImpl() {
		writeC(0x05);

		writeD(_attackerObjId);
		writeD(_hits[0]._targetId);
		writeD(_hits[0]._damage);
		writeC(_hits[0]._flags);
		writeD(_x);
		writeD(_y);
		writeD(_z);
		writeH(_hits.length - 1);
		// prevent sending useless packet while there is only one target.
		if (_hits.length > 1) {
			for (int i = 1; i < _hits.length; i++) {
				writeD(_hits[i]._targetId);
				writeD(_hits[i]._damage);
				writeC(_hits[i]._flags);
			}
		}
	}
}
