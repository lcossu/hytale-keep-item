# Hytale KeepItem mod

This mods allows players to select which items they want to prevent to be moved during a Quick Stack operation.

Inspired by the ALT + Click inventory functionality in Terraria.

## Base Features

- Select items to keep from being moved during Quick Stack.
- User-friendly interface for item selection.
- Management of stack splitting and merging.
- Compatibility with other inventory management mods (if a mod uses the ItemContaier.quickStackTo method to move items it should
  work with this mod).

## Commands
Use the ```/keepitem``` command to open the Keep Item selection interface.
- `/keepitem --clear` - Clears all current settings.

## Possible Future Features
- Visible indication of kept items in the inventory UI.
- Keyboard shortcuts for quickly enable or disable a specific slot as kept. (Alt + Click?)

Feel free to reach out if you have any questions or feature/improvement ideas.

Based on [Hytale plugin template](https://github.com/realBritakee/hytale-template-plugin)