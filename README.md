#Black-List

Black-List is a professionally designed Discord bot implemented in Java, offering robust functionality for efficient management of string blacklists, facilitating seamless server ban enforcement. Developed using Maven and the Discord API, this feature-rich bot provides a diverse range of commands, including but not limited to adding IDs, searching and displaying the blacklist, comparing with other lists, updating a temporary list, removing IDs, clearing the blacklist, and providing a help command for user-friendly navigation.

With Black-List, server administrators can effortlessly add IDs to the blacklist using simple commands, conduct targeted searches for specific IDs within the blacklist, and conveniently display the current list of blacklisted values. The bot also enables efficient cross-referencing of multiple blacklists through its capability to compare with other lists. Additionally, administrators can seamlessly update the temporary list and access it as needed.

Black-List also offers a comprehensive set of powerful administration commands, including the ability to selectively remove specific IDs from the blacklist, or clear the entire blacklist when necessary. The bot's sleek and intuitive user interface ensures a seamless experience for both moderators and administrators, making it a valuable tool for effective server ban enforcement.

Built using Maven and the Discord API, Black-List is a reliable and efficient solution for managing string blacklists in Discord servers. Its professional design, comprehensive functionality, and user-friendly experience make it an ideal choice for server administrators seeking to effectively enforce ban rules. Below are the commands available for BLACKlistmod and BLACKlistadmin:

**BLACKlistmod commands:**

| Command             |               Description                          |
|---------------------|----------------------------------------------------|
| !add [ID]                         | Add an ID to the blacklist           |
| !adds [ID1] [ID2] ...             | Add multiple IDs to the blacklist    |
| !blacklist [ID]                   | Search for an ID in the blacklist    |
| !showlist                         | Show the current list                |
| !compare                          | Compares the both list               |
| !updatetemp                       | Update the Templist                  |
| !templist                         | Show the Templist                    |
| !help                             | Show this list of commands           |

**BLACKlistadmin commands:**

| Command           |                Description                           |
|-----------------|--------------------------------------------------------|
| !remove [ID]                         | Remove an ID from the blacklist   |
| !clear                               | Clear the blacklist               |
| !Tempclr                             | Clear the list                    |
