package net.sf.l2j.gameserver.cache;

import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;

/**
 * @author Layane, reworked by Java-man, Hasha
 */
public class CrestCache {

	private static final Logger _log = LoggerFactory.getLogger(CrestCache.class.getName());

	private static final String CRESTS_DIR = "./data/crests/";

	private final Map<Integer, byte[]> _crests;
	private final FileFilter _ddsFilter;

	public static enum CrestType {
		PLEDGE("Crest_", 256),
		PLEDGE_LARGE("LargeCrest_", 2176),
		ALLY("AllyCrest_", 192);

		final String _prefix;
		final int _size;

		private CrestType(String prefix, int size) {
			_prefix = prefix;
			_size = size;
		}
	}

	public static CrestCache getInstance() {
		return SingletonHolder._instance;
	}

	public CrestCache() {
		_crests = new HashMap<>();
		_ddsFilter = new DdsFilter();

		load();
	}

	public final void reload() {
		_crests.clear();

		load();
	}

	private final void load() {
		final File directory = new File(CRESTS_DIR);
		directory.mkdirs();

		for (File file : directory.listFiles(_ddsFilter)) {
			byte[] data;
			try (RandomAccessFile f = new RandomAccessFile(file, "r")) {
				data = new byte[(int) f.length()];
				f.readFully(data);
			} catch (Exception e) {
				_log.warn("CrestCache: Error loading crest file: " + file.getName());
				continue;
			}

			final String fileName = file.getName();

			for (CrestType type : CrestType.values()) {
				if (!fileName.startsWith(type._prefix)) {
					continue;
				}

				_crests.put(Integer.valueOf(fileName.substring(type._prefix.length(), fileName.length() - 4)), data);
			}
		}

		_log.info("CrestCache: Loaded " + _crests.size() + " crest files.");
	}

	public final byte[] getCrest(CrestType type, int id) {
		// get crest data
		byte[] data = _crests.get(id);

		// crest data is not required type, return
		if (data == null || data.length != type._size) {
			return null;
		}

		return data;
	}

	public final void removeCrest(CrestType type, int id) {
		// get crest data
		byte[] data = _crests.get(id);

		// crest data is not required type, return
		if (data == null || data.length != type._size) {
			return;
		}

		// remove from cache
		_crests.remove(id);

		// delete file
		final File file = new File(CRESTS_DIR + type._prefix + id + ".dds");
		if (!file.delete()) {
			_log.warn("CrestCache: Error deleting crest file: " + file.getName());
		}
	}

	public final boolean saveCrest(CrestType type, int id, byte[] data) {
		// create file
		File file = new File(CRESTS_DIR + type._prefix + id + ".dds");

		try (FileOutputStream out = new FileOutputStream(file)) {
			// save crest
			out.write(data);

			// put crest to cache
			_crests.put(id, data);

			return true;
		} catch (IOException e) {
			_log.warn("CrestCache: Error saving crest file: " + file.getName());
			return false;
		}
	}

	private static class SingletonHolder {

		protected static final CrestCache _instance = new CrestCache();
	}

	protected class DdsFilter implements FileFilter {

		@Override
		public boolean accept(File file) {
			// client<>server crest transfer is using images in DDS file format (DXT1)
			return file.getName().endsWith(".dds");
		}
	}
}
