package me.naptie.bukkit.game.bedwars.data;

import me.naptie.bukkit.game.bedwars.Main;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.io.*;

public class RollbackHandler {

	private static RollbackHandler rollbackHandler = new RollbackHandler();

	public static RollbackHandler get() {
		return rollbackHandler;
	}

	void rollback(World world) {

		Main.getInstance().getServer().unloadWorld(world, false);

		try {
			rollback(world.getName());
		} catch (Exception ex) {
			Bukkit.getLogger().severe("Could not roll-back the world '" + world.getName() + "'!");
		}
	}

	public void rollback(String worldName) {
		String rootDirectory = Main.getInstance().getServer().getWorldContainer().getAbsolutePath();

		if (worldName.endsWith("_active")) {
			worldName = worldName.replace("_active", "");
		}
		File srcFolder = new File(rootDirectory, worldName);
		File destFolder = new File(rootDirectory, worldName + "_active");

		delete(destFolder);

		try {
			copyWorld(srcFolder, destFolder);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("ResultOfMethodCallIgnored")
	private void delete(File delete) {
		if (delete.isDirectory()) {
			String[] files = delete.list();

			if (files != null) {
				for (String file : files) {
					File toDelete = new File(file);
					toDelete.delete();
				}
			}
			delete.delete();
		} else {
			delete.delete();
		}
	}

	@SuppressWarnings("ResultOfMethodCallIgnored")
	private void copyWorld(File src, File dest) throws IOException {
		if (src.isDirectory()) {

			if (!dest.exists()) {
				dest.mkdir();
			}

			String[] files = src.list();

			if (files != null) {
				for (String file : files) {

					File srcFile = new File(src, file);
					File destFile = new File(dest, file);
					if (srcFile.getName().equals("uid.dat")) {
						continue;
					}
					if (srcFile.isDirectory()) {
						FileUtils.copyDirectory(srcFile, destFile);
					} else if (srcFile.isFile()) {
						FileUtils.copyFile(srcFile, destFile);
					}
				}
			}

		} else {

			InputStream in = new FileInputStream(src);
			OutputStream out = new FileOutputStream(dest);

			byte[] buffer = new byte[1024];

			int length;

			while ((length = in.read(buffer)) > 0) {
				out.write(buffer, 0, length);
			}

			in.close();
			out.close();

		}
	}

}