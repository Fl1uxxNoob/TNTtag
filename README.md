
# TNTTag Plugin

A Minecraft Bukkit/Spigot plugin that implements the classic TNTTag minigame where players pass around a TNT item and try to avoid being the holder when the timer runs out.

## Features

- **Dynamic Round Timer**: Configurable round duration with GUI controls
- **Multi-Arena Support**: Define multiple arenas with different spawn points
- **WorldGuard Integration**: Automatic arena configuration with region protection
- **Interactive GUI**: Easy-to-use interface for game management
- **Real-time Scoreboard**: Live display of game status and player count
- **TNT Passing Mechanics**: Players can pass TNT by hitting other players
- **Explosion Effects**: Visual and sound effects when rounds end
- **Permission System**: Role-based access control for game management
- **Configurable Messages**: Customizable messages and GUI text
- **Arena Selection**: Choose specific arenas or random selection

## Requirements

- **Minecraft Version**: 1.8.8+
- **Server Software**: Bukkit/Spigot/Paper
- **Dependencies**: WorldGuard plugin (required)

## Installation

1. Download the TNTTag plugin JAR file
2. Ensure WorldGuard is installed on your server
3. Place the TNTTag JAR in your server's `plugins` folder
4. Restart your server
5. Configure the plugin using the generated config files

## Configuration

### Main Configuration (`config.yml`)

```yaml
spawn:
  world: world
  x: 0.5
  y: 65
  z: 0.5
arenas:
  Arena1:
    world: arena_world1
    x: 100.5
    y: 65
    z: 100.5
  Arena2:
    world: arena_world2
    x: 200.5
    y: 70
    z: 200.5
```

- **spawn**: Default spawn location for players
- **arenas**: Define multiple arenas with their respective spawn coordinates

### Messages Configuration (`messages.yml`)

The plugin supports full message customization including:
- GUI titles and button text
- Game status messages
- Error messages
- WorldGuard integration messages
- Scoreboard labels

All messages support Minecraft color codes using the `&` symbol.

## Commands

### `/tnttag`
Base command for all TNTTag operations.

**Usage**: `/tnttag <start|stop|reload|gui>`

#### Subcommands:

- **`/tnttag start`**: Start a new TNTTag game
  - Requires `tnttag.host` permission
  - Needs at least 2 players online
  - Cannot start if a game is already active

- **`/tnttag stop`**: Stop the current game
  - Requires `tnttag.host` permission
  - Only works if a game is currently running

- **`/tnttag reload`**: Reload all configuration files
  - Requires `tnttag.host` permission
  - Reloads config.yml, messages.yml, and arena settings

- **`/tnttag gui`**: Open the TNTTag management GUI
  - Requires `tnttag.host` permission
  - Cannot be used while a game is active

## Permissions

- **`tnttag.host`**: Allows access to all TNTTag commands and GUI
  - Required for starting/stopping games
  - Required for accessing the management GUI
  - Required for reloading configurations

## Game Mechanics

### TNT Passing
- Players receive a TNT item when the game starts
- The TNT can be passed between players by hitting them
- Only the current TNT holder can pass the TNT to others
- TNT items cannot be dropped, moved in inventory, or placed as blocks

### Round System
- Each round has a configurable duration (default: 60 seconds)
- A countdown timer shows remaining time
- When time expires, the current TNT holder is eliminated
- Game continues until only one player remains

### Arena Management
- Multiple arenas can be defined in the configuration
- Players can select specific arenas or choose random selection
- WorldGuard integration automatically applies protection settings
- Each arena supports custom spawn coordinates

## GUI System

### Main GUI
- **Timer Control**: Adjust round duration (+/- 10 seconds per click)
- **Start Game**: Launch a new TNTTag session
- **Arena Selection**: Choose which arena to play in

### Arena Selection GUI
- **Random Option**: Let the system choose an arena
- **Specific Arenas**: Select from configured arenas
- **Pagination**: Navigate through multiple arena pages

## WorldGuard Integration

The plugin automatically configures WorldGuard settings for arena worlds:

- **PvP Protection**: Prevents player damage outside TNT mechanics
- **Block Protection**: Prevents block breaking/placing in arenas
- **Game Rules**: Applies appropriate game rules for arena worlds
- **Region Flags**: Configures necessary WorldGuard flags

## Scoreboard Features

Real-time scoreboard display showing:
- Game title
- Current round timer
- Active player count
- Customizable labels and formatting

## Technical Details

### Architecture
- **Manager System**: Centralized game state management
- **Event-Driven**: Listener-based event handling
- **Singleton Pattern**: Single instance management for GUIs
- **Configuration Management**: Separate managers for messages and settings

### Performance
- Efficient player tracking with ArrayList storage
- Minimal resource usage during idle periods
- Optimized event handling for game mechanics
- Clean resource cleanup on plugin disable

## Development

### Building from Source
1. Clone the repository
2. Ensure you have Gradle installed
3. Place WorldGuard JAR in the `libs` folder
4. Run `gradle build`
5. Find the compiled JAR in `build/libs/`

### API Usage
The plugin provides access to the TNTTagManager for other plugins:
```java
TNTTagManager manager = TntTag.getInstance().getManager();
```

## Troubleshooting

### Common Issues

**"WorldGuard not found" error**
- Ensure WorldGuard is properly installed
- Check that WorldGuard loads before TNTTag
- Verify WorldGuard version compatibility

**"Not enough players" message**
- Minimum 2 players required to start a game
- Check that players are actually online and connected

**GUI not opening**
- Verify player has `tnttag.host` permission
- Ensure no active game is running
- Check for any conflicting plugins

**Arena world not found**
- Verify world names in config.yml match actual world folders
- Ensure worlds are properly loaded
- Check for typos in world names

## Support

For issues, feature requests, or general support, please refer to the plugin documentation or contact the developer.

## License

AFKGuard is licensed under the **GNU General Public License v3.0** (GPL-3.0).  
You are free to use, modify, and distribute this software under the terms of the license.  
A copy of the license is available in the [LICENSE](./LICENSE) file.
