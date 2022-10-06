# SamiPlugin

Official HideySMP whitelisting and balance plugin

### Features

- Discord based whitelisting
- Discord webhooks
- On join LuckPerms group management (`group.supporter` and `group-mod`)
- Dragon drops elytra
- Double shoulker shells
- Turtle drops 2 scutes when grown
- Bats dont spawn
- Ender dragon doesnt grief
- /lookup command
- /lore command to set the item role for a price
- /discord command for plugin reload
- Rest API for balance

To be added (soon):

- Rest API for server status
- Plugin API for accessing player discord data and sending webhooks

### Configuration

```yaml
webhook: url
port: 8010
auth:
  guild: 264801645370671114
  role: 426156903555399680
  staff: 863124017064706069
  supporter: 743861104819830854
enable:
  advancements: false
  balance-api: false
  supporter-bypass: false
  maintenance: false
color:
  system: 65535
  join: 65280
  sami: 15369278
  leave: 16711680
  death: 8388736
  advancement: 65535
```

- `webhook` must be a valid Discord Webhook URL, you can find more about Discord Webhooks
  on [this link](https://support.discord.com/hc/en-us/articles/228383668-Intro-to-Webhooks)
- `port` represents a TCP port where the Balance API will listen to
- `auth.guild` is a discord server id you want to use for verification
- `auth.role` is a role you want to use for verification
- `auth.staff` is the staff role id
- `auth.supporter` represents a supporter only channel id
- The color section must contain only valid color decimal or hexadecimal numbers

> Guild is set by default to [Sami's Hidey Hole](http://discord.gg/sami) and role to the Media role. Make sure that the roles and channels in your configuration are from the correct guild

### Vault Balance API

Real time player balance can be obtained using this HTTP GET endpoint

- `https://api.samifying.com/v1/balance?id=<discord-id>`

> If you are running the plugin on your own server the path will be by default `http://localhost:8010/v1/balance`. Make sure to change the host and port according to your plugin and server configuration

### Libraries used

- [Spark](https://github.com/perwendel/spark)
- [Jackson Core](https://github.com/FasterXML/jackson-core)
- [Discord Webhooks](https://github.com/MinnDevelopment/discord-webhooks)
- [Logback Classic](https://github.com/qos-ch/logback)
- [Spigot API](https://hub.spigotmc.org/javadocs/bukkit/)
- [Vault API](https://github.com/MilkBowl/VaultAPI)
- [LuckPerms API](https://github.com/lucko/LuckPerms)
- [Lombok](https://projectlombok.org/)