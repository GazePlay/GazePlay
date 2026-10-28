Fonts of the menus of GazePlay Learning (loaded by net.gazeplay.ui.menus.MenuFonts). Free and open source, under the
SIL Open Font License 1.1: free to use, modify and redistribute, the license being shipped with the fonts.

- Andika-Regular.ttf, Andika-Bold.ttf: Andika by SIL International, unchanged (Reserved Font Names "Andika" and "SIL").
  License: Andika-OFL.txt. Source: https://github.com/google/fonts/tree/main/ofl/andika

- Fredoka-SemiBold.ttf: fixed instance (weight 600, width 100) of the variable Fredoka by The Fredoka Project Authors
  (no Reserved Font Name). License: Fredoka-OFL.txt.
  Source: "Fredoka[wdth,wght].ttf" of https://github.com/google/fonts/tree/main/ofl/fredoka, made with fontTools:
      fonttools varLib.instancer "Fredoka[wdth,wght].ttf" wght=600 wdth=100 --static --update-name-table -o Fredoka-SemiBold.ttf
  JavaFX only uses the default instance (Regular) of a variable font, hence a fixed one.

The icons of the menus (net.gazeplay.ui.menus.MenuIcon) are Material Symbols Rounded by Google, Apache License 2.0:
the unchanged "24px" SVG files in ../icons ("_fill1": filled variant), license in ../icons/MaterialSymbols-LICENSE.txt.
Source: https://github.com/google/material-design-icons, symbols/web/<name>/materialsymbolsrounded/<name>_24px.svg.
