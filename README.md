# SamiPlugin
Official HideySMP whitelisting and balance plugin

### Configuration

```yaml
webhook: url
port: 8010
color:
  system: 65535
  join: 65280
  sami: 15369278
  leave: 16711680
  death: 8388736
  advancement: 65535
```

- `webhook` must be a valid Discord webhook URL, you can find more about Discord Webhooks on [this link](https://support.discord.com/hc/en-us/articles/228383668-Intro-to-Webhooks)
- `port` represents the port on witch the Balance API will respond to
- The color section must contain only valid color decimal or hexadecimal numbers

### Libraries used

- [Spark](https://github.com/perwendel/spark)
- [Jackson Core](https://github.com/FasterXML/jackson-core)
- [Discord Webhooks](https://github.com/MinnDevelopment/discord-webhooks)
- [Logback Classic](https://github.com/qos-ch/logback)
- [Spigot API](https://hub.spigotmc.org/javadocs/bukkit/)
- [Vault API](https://github.com/MilkBowl/VaultAPI)
- [LuckPerms API](https://github.com/lucko/LuckPerms)