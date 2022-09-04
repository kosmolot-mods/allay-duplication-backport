# Allay Duplication, backported to 1.19

[![ko-fi](https://ko-fi.com/img/githubbutton_sm.svg)](https://ko-fi.com/Y8Y726QMH)

This is a janky mod that backports the allay duplication mechanics
to Minecraft 1.19. It only exists so that we don't have to wait for
Minecraft 1.19.1 to start using that mechanic.

The mechanics are not backported exactly. The following is implemented:

- Jukebox has to be playing nearby (within 8 blocks)
- You need to give the allay an amethyst shard
- There's a cooldown of 2500 ticks after duplication

The following is janky:

- Particles (they don't work for me for some reason, too lazy to investigate)
- The original allay is removed from the game and two new ones are spawned
  (because of some weird bugs that I was too lazy to fix)
- No dancing :(

## Usage

Fabric Loader is required.
