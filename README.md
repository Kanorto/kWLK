# kWLK - Whitelist Kick Plugin

A Minecraft 1.21.4 plugin that manages player whitelist and implements ghost mode for dead players.

## Features

### 1. Whitelist Kick System
- **Command**: `/kwlk` - Kicks all players without the `kwlk.bypass` permission
- **Confirmation**: Requires confirmation in chat (`/kwlk confirm` or `/kwlk cancel`)
- **Auto-whitelist**: Automatically adds players with `kwlk.bypass` permission to the server whitelist
- **Customizable Messages**: Kick message uses MiniMessage format for rich text formatting
- **Async Processing**: Kick operations are processed asynchronously to prevent server lag

### 2. Ghost Mode
- **Automatic Activation**: Players become ghosts when they die
- **Second Death Penalty**: If a ghost dies again, they are kicked from the server
- **Restrictions**:
  - Cannot fly
  - Invisible to other players
  - Cannot interact with blocks (break/place)
  - Cannot interact with entities
  - Cannot drop or pick up items
  - Cannot damage entities
  - Cannot consume items
  - Cannot sleep in beds
  - Set to Adventure mode
- **Respawn**: Ghost mode is automatically removed upon respawn

### 3. Respawn Item System
- **Special Item**: Configurable item (default: Totem of Undying) that can revive ghosts
- **Usage**: Right-click the respawn item to revive a random ghost within 5 blocks
- **Item Breaking**: The respawn item breaks after use
- **Command**: `/giverespawn <player> [amount]` - Give respawn items to players
- **Async Processing**: Ghost search is done asynchronously to prevent lag

## Configuration

The plugin uses `config.yml` to customize messages and behavior. All messages support [MiniMessage format](https://docs.advntr.dev/minimessage/format.html).

### Default Configuration
```yaml
# Kick message in MiniMessage format
kick-message: "<red><bold>You have been kicked from the server!</bold></red>\n<gray>Reason: Whitelist purge</gray>"

# Confirmation timeout in seconds
confirmation-timeout: 30

# Ghost mode settings
ghost-mode:
  enabled: true
  kick-on-second-death: true
  ghost-message: "<gray><italic>You have died and become a ghost. You cannot interact with the world.</italic></gray>"
  respawn-message: "<green>You have respawned and are no longer a ghost.</green>"
  ghost-kick-message: "<red><bold>You died as a ghost!</bold></red>\n<gray>Ghosts cannot die twice.</gray>"

# Respawn item settings
respawn-item:
  enabled: true
  material: "TOTEM_OF_UNDYING"
  display-name: "<gold><bold>Totem of Revival</bold></gold>"
  search-radius: 5
  success-message: "<green>You have revived <player>!</green>"
  revived-message: "<green>You have been revived by <reviver>!</green>"
  no-ghosts-message: "<red>No ghosts found within <radius> blocks!</red>"
  enabled: true
  ghost-message: "<gray><italic>You have died and become a ghost. You cannot interact with the world.</italic></gray>"
  respawn-message: "<green>You have respawned and are no longer a ghost.</green>"
```

## Permissions

- `kwlk.use` - Allows using the `/kwlk` command (default: op)
- `kwlk.bypass` - Exempts from being kicked and auto-adds to whitelist (default: op)
- `kwlk.giverespawn` - Allows giving respawn items to players (default: op)

## Commands

- `/kwlk` - Shows confirmation prompt to kick players
- `/kwlk confirm` - Confirms the kick action
- `/kwlk cancel` - Cancels the kick action
- `/giverespawn <player> [amount]` - Give respawn items to a player

## Performance

All potentially expensive operations are performed asynchronously to prevent server lag:
- Player kick processing
- Ghost search for respawn item
- Whitelist operations

## Automatic Builds

The plugin is automatically built and released when changes are pushed to the main branch. Releases include:
- Compiled JAR file
- Version tag with commit hash
- Release notes with commit message

## Building

This plugin requires Java 21 and Gradle to build.

```bash
./gradlew build
```

The compiled JAR will be in `build/libs/kWLK-1.0.0.jar`

## Installation

1. Download or build the plugin JAR
2. Place it in your server's `plugins` folder
3. Start or restart the server
4. Configure `plugins/kWLK/config.yml` as needed
5. Reload with `/reload` or restart the server

## Requirements

- Minecraft Server 1.21.3+ (Paper/Spigot)
- Java 21

## License

See LICENSE file

