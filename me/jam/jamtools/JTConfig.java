package me.jam.jamtools;

public class JTConfig {
    private File configf, specialf;
    private FileConfiguration config, special;

    @Override
    public void onEnable(){
        createFiles();
    }

    public FileConfiguration getSpecialConfig() {
        return this.special;
    }

    private void createFiles() {

        configf = new File(getDataFolder(), "config.yml");

        if (!configf.exists()) {
            configf.getParentFile().mkdirs();
            saveResource("config.yml", false);
        }

        config = new YamlConfiguration();
        
        try {
            config.load(configf);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public String GetValue(String sName)
    {
    	// Reading from the config
    	String retValue = plugin.getConfig().getString(sName);
    	return (retValue);
    	
    }
    public SetValue()
    {
    	// Writing to the config
    	plugin.getConfig().set("player-name", name);
    }
}
