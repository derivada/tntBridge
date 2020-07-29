package me.tntpablo.thebridge.files;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import me.tntpablo.thebridge.Main;

public class DataManager {

	private Main plugin;
	private FileConfiguration dataConfig = null;
	private File configFile = null;
	private String configFileName;

	public DataManager(Main plugin, String configFileName) {
		this.plugin = plugin;
		this.configFileName = configFileName;
		// guarda / inicializa la configuracion
		saveDefaultConfig();
	}

	// crear la configuracion y ver que el YML es correcto
	public void reloadConfig() {
		try {
			if (this.configFile == null) {
				// Crear el archivo de configuracion
				configFile = new File(this.plugin.getDataFolder(), configFileName);
			}

			// Cargar el archivo
			this.dataConfig = YamlConfiguration.loadConfiguration(this.configFile);

			// I/O
			InputStream defaultStream = this.plugin.getResource(configFileName);

			if (defaultStream != null) {
				@SuppressWarnings("unused")
				YamlConfiguration defaultConfig = YamlConfiguration
						.loadConfiguration(new InputStreamReader(defaultStream));

			}

		} catch (Exception e) {
			// Quizas el nombre del archivo esta mal.
			// TODO: Attach a logger
			e.printStackTrace();
		}
	}

	// coger la configuracion
	public FileConfiguration getConfig() {
		if (this.dataConfig == null)
			reloadConfig();
		return this.dataConfig;
	}

	// guardar la configuracion, esto destruira los comentarios
	public void saveConfig() {
		if (this.dataConfig == null || this.configFile == null)
			return;

		try {
			this.getConfig().save(this.configFile);
		} catch (IOException e) {
			this.plugin.getLogger().log(Level.SEVERE, "Could not save config to " + this.configFile, e);
		}

	}

	// inicializa el archivo
	public void saveDefaultConfig() {
		if (this.configFile == null)
			this.configFile = new File(this.plugin.getDataFolder(), configFileName);

		if (!this.configFile.exists()) {
			this.plugin.saveResource(configFileName, false);
		}

	}

}
