![ParagonBanner1](https://user-images.githubusercontent.com/85251388/154859895-65627d8d-0753-43ea-91c4-d8c092e478c3.png)

[![Downloads](https://img.shields.io/github/downloads/Wolfsurge/Paragon/total.svg)](https://github.com/Wolfsurge/Paragon/releases/)
[![Discord](https://img.shields.io/discord/936976249300086854?color=purple&label=Discord&logo=Discord&style=for-the-badge)](https://discord.gg/28JNQsXUzb)
![Lines of code](https://img.shields.io/tokei/lines/github/Wolfsurge/Paragon?color=blueviolet&label=lines%20of%20code&style=for-the-badge)

# Paragon
A 1.12.2 Anarchy Client. Still in development.

# FAQ
<details>
  <summary> How do I open the ClickGUI? </summary>
  
  > The default ClickGUI bind is `RSHIFT`
</details>

<details>
  <summary> How do I use commands? </summary>
  
  > The command prefix is `$`, and you can run `$help` to get a list of all commands
</details>

<details>
  <summary> How do I request help, or suggest a feature? </summary>
  
  > You can join the discord server (linked above) and use the appropriate channels
</details>

# Founders
Wolfsurge <br>
TeleTofu

## Developers
Bush <br>
Master7720 (kinda)

## Other Contributors
Doogie13

## Other Credits
Most files will have an @author javadoc tag at the top to tell you who wrote the file, if it doesn't, then Wolfsurge wrote it

# Build instructions
Windows:

`.\gradlew build`

Linux/Mac (or any other based UNIX/UNIX-LIKE OS):

`chmod +x gradlew`

`./gradlew build`

# Contributing
Fork the repository and push your changes to the fork. Then, create a pull request to this repository and it will be reviewed.

Try and follow the code style found in the rest of the client, such as using `lowerCamelCase` and not letting `{`s have their own lines.

Both Kotlin and Java are allowed (Kotlin preferred)

# Using the Custom Font
A directory is created in the Paragon config folder (/.minecraft/paragon), called "font". In here, upon first opening the client, two files will be present.
"font.ttf" is the font that will be used when the Font module is enabled. The second file is "font_config.json". Here you can find a couple of font rendering
settings. "size" is an integer (whole number) value that determines the font's size. "y_offset" is a float (decimal) value to determin how much to add to the Y value when text is rendered. (this is so you can center it so it looks good (*cough* wp3 *cough*).

You can change the custom font by simply deleting "font.ttf" in the font folder, and replacing it with a different TTF font file, which is also called "font.ttf". It is
imperative that it is named this as otherwise the client will not detect the font, and will therefore default to Java's default font. Support for fonts with different file names might be added in the future.
