# VelocityCommandBlocker

A Velocity proxy plugin that blocks commands for members across **all** backend servers
(works with 1.8.8 - 1.21.x clients/servers behind the proxy).

No need to install anything on the backend servers - the proxy handles everything.

## Features

- ✅ Runs on the Velocity proxy only
- ✅ Applies to all servers behind the proxy
- ✅ Customizable `config.yml` with colors (& codes) and messages
- ✅ Blacklist **or** whitelist mode
- ✅ Bypass permission for admins/staff
- ✅ Reload command without restart
- ✅ Auto-build with GitHub Actions

## Permissions (add with LuckPerms on Velocity)

| Permission | Description | Default |
|---|---|---|
| `commandblocker.bypass` | Skip command blocking | false |
| `commandblocker.admin`  | Use `/commandblocker reload` | op |

Example:

```
/lp user Notch permission set commandblocker.bypass true
/lp group admin permission set commandblocker.bypass true
/lp group default permission set commandblocker.bypass false
```

## Commands

- `/commandblocker reload` (aliases: `/cb`, `/vcb`, `/cmdblock`) - reload config

## Installation

1. Download the JAR from GitHub Actions (Artifacts tab after a build) or build it yourself.
2. Drop the JAR into your **Velocity** `plugins/` folder.
3. Restart the proxy.
4. Edit `plugins/velocitycommandblocker/config.yml`.
5. Run `/cb reload` from the console or in-game.

## Building

### Locally

```bash
mvn clean package
```

Output: `target/VelocityCommandBlocker.jar`

### With GitHub Actions (no setup)

1. Create a new GitHub repository.
2. Push this project.
3. Go to the **Actions** tab -> wait for the workflow.
4. Download the JAR from the **Artifacts** section of the finished run.

## config.yml example

```yaml
settings:
  use-whitelist: false
  case-insensitive: true
  log-blocked: true

messages:
  prefix: "&8[&cCommandBlocker&8] &r"
  blocked: "&cYou don't have permission to use this command!"

blocked-commands:
  - op
  - gamemode
  - give
  - tp

allowed-commands:
  - spawn
  - hub
```

## License

MIT
