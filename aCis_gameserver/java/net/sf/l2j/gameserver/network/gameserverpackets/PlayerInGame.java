package net.sf.l2j.gameserver.network.gameserverpackets;

import org.slf4j.LoggerFactory;

import java.util.List;

public class PlayerInGame extends GameServerBasePacket
{
	public PlayerInGame(String player)
	{
		writeC(0x02);
		writeH(1);
		writeS(player);
	}
	
	public PlayerInGame(List<String> players)
	{
		writeC(0x02);
		writeH(players.size());
		for (String pc : players)
			writeS(pc);
	}
	
	@Override
	public byte[] getContent()
	{
		return getBytes();
	}
}