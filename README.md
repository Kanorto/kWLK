# kWLK - Whitelist Kick Plugin

A Minecraft 1.21.4 plugin that manages player whitelist and implements ghost mode for dead players.

## Features

### 1. Whitelist Kick System
- **Command**: `/kwlk` - Kicks all players without the `kwlk.bypass` permission
- **Confirmation**: Requires confirmation in chat (`/kwlk confirm` or `/kwlk cancel`)
- **Auto-whitelist**: Automatically adds players with `kwlk.bypass` permission to the server whitelist
- **Customizable Messages**: Kick message uses MiniMessage format for rich text formatting

### 2. Ghost Mode
- **Automatic Activation**: Players become ghosts when they die
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
  ghost-message: "<gray><italic>You have died and become a ghost. You cannot interact with the world.</italic></gray>"
  respawn-message: "<green>You have respawned and are no longer a ghost.</green>"
```

## Permissions

- `kwlk.use` - Allows using the `/kwlk` command (default: op)
- `kwlk.bypass` - Exempts from being kicked and auto-adds to whitelist (default: op)

## Commands

- `/kwlk` - Shows confirmation prompt to kick players
- `/kwlk confirm` - Confirms the kick action
- `/kwlk cancel` - Cancels the kick action

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

