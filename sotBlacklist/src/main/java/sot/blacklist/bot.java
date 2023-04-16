package sot.blacklist;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import javax.security.auth.login.LoginException;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;


public class bot extends ListenerAdapter {
    private static final String PREFIX = "!"; // Change this to your desired bot command prefix
    private List<String> blacklist = new ArrayList<>(); // List to store blacklist
    private List<String> list = new ArrayList<>(); // List to store list
    private String roleName = "Blacklistmod"; // Role name for role specification
    private String removeRoleName = "Blacklistadmin"; // Role name for removing from blacklist

    public bot() {
        // Load blacklist from file
        blacklist = loadBlacklistFromFile();
        list = loadlist();
    }

    public static void main(String[] args) throws LoginException, InterruptedException {
        JDA jda = JDABuilder.createDefault("token")
                .enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT)
                .addEventListeners(new bot())
                .build();
        jda.awaitReady();
        System.out.println("Bot is ready!");
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        // Check if the message is from a user and not a bot
        if (!event.getAuthor().isBot()) {
            Message message = event.getMessage();
            MessageChannel channel = event.getChannel();
            String content = message.getContentRaw();
            Role role = event.getGuild().getRolesByName("blacklistmod", true).stream().findFirst().orElse(null); // Get role by name
            Role removeRole = event.getGuild().getRolesByName(removeRoleName, true).stream().findFirst().orElse(null); // Get role by name

            // Check if the message is sent in the "BlackList" channel
            if (!channel.getName().equalsIgnoreCase("blacklist")) {
                return; // Ignore messages in other channels
            }

            // Allow all users to chat in the blacklist channel
            if (content.startsWith(PREFIX)) {
                // Check if the user has the blacklistmod role
                boolean isBlacklistMod = event.getMember().getRoles().contains(role);

                // Check if the user has the blacklistadmin role
                boolean isBlacklistAdmin = event.getMember().getRoles().contains(removeRole);

                // Add the function to restrict commands based on role
                if (isBlacklistMod) {
                    // Allow only add, showlist, and compare commands
                    if (content.startsWith(PREFIX + "add ") || content.startsWith(PREFIX + "adds ") || content.startsWith(PREFIX + "blacklist")
                            || content.startsWith(PREFIX + "showlist") || content.startsWith(PREFIX + "compare") || content.startsWith(PREFIX + "update")
                            || content.startsWith(PREFIX + "templist") || content.startsWith(PREFIX + "help")) {
                        // Process the command
                    } else {
                        // Show error message for blacklistmod
                        event.getChannel().sendMessage("Command access restricted: Please review command usage or obtain the required permissions for proper execution.").queue();
                    }
                } else if (isBlacklistAdmin) {
                    // Allow all commands
                    // Process the command
                } else {
                    // Show error message for other roles
                    event.getChannel().sendMessage("Command access restricted: Please review command usage or obtain the required permissions for proper execution.").queue();
                }


                // Check if the message is a command to add an ID to the blacklist
                if (content.startsWith(PREFIX + "add ")) {
                    String idToAdd = content.substring((PREFIX + "add ").length()).replaceAll("[\\s,\\/]+", ""); // Extract ID from input and remove spaces, commas, and slashes
                    if (!idToAdd.isEmpty()) { // Check if ID is not empty
                        addId(idToAdd);
                        channel.sendMessage("ID " + idToAdd + " has been added to the blacklist.").queue();
                    } else {
                        channel.sendMessage("Invalid command! Usage: " + PREFIX + "add [ID]").queue(); // Updated error message
                    }
                    saveBlacklistToFile();
                }

                // Check if the message is a command to add IDs to the blacklist
                if (content.startsWith(PREFIX + "adds ")) {
                    String[] split = content.split("\\s+");
                    if (split.length >= 2) {
                        StringBuilder sb = new StringBuilder();
                        for (int i = 1; i < split.length; i++) {
                            sb.append(split[i]).append(" ");
                        }
                        String idsToAdd = sb.toString().trim();
                        String[] ids = idsToAdd.split("\\s+");
                        for (String id : ids) {
                            addId(id);
                        }
                        channel.sendMessage("IDs " + idsToAdd + " have been added to the blacklist.").queue();
                    } else {
                        channel.sendMessage("Invalid command! Usage: " + PREFIX + "addids [ID1] [ID2] ...").queue();
                    }
                    saveBlacklistToFile();
                }

                // Check if the message is a command to remove an ID from the blacklist
                if (content.startsWith(PREFIX + "remove ") && event.getMember().getRoles().contains(removeRole)) {
                    String[] split = content.split("\\s+");
                    if (split.length == 2) {
                        String idToRemove = split[1];
                        removeId(idToRemove);
                        channel.sendMessage("ID " + idToRemove + " has been removed from the blacklist.").queue();
                    } else {
                        channel.sendMessage("Invalid command! Usage: " + PREFIX + "removeid [ID]").queue();
                    }
                    saveBlacklistToFile();
                }

                // Check if the message is a command to search for IDs in the blacklist
                if (content.startsWith(PREFIX + "blacklist ")) {
                    String[] split = content.split("\\s+");
                    if (split.length >= 2) {
                        StringBuilder sb = new StringBuilder();
                        for (int i = 1; i < split.length; i++) {
                            sb.append(split[i]).append(" ");
                        }
                        String idsToSearch = sb.toString().trim();
                        String[] ids = idsToSearch.split("\\s+");
                        List<String> foundIds = new ArrayList<>();
                        for (String id : ids) {
                            if (searchId(id)) {
                                foundIds.add(id);
                            }
                        }
                        if (!foundIds.isEmpty()) {
                            StringBuilder response = new StringBuilder();
                            response.append("IDs ");
                            for (String id : foundIds) {
                                response.append(id).append(", ");
                            }
                            response.delete(response.length() - 2, response.length());
                            response.append(" are in the blacklist.");
                            channel.sendMessage(response.toString()).queue();
                        } else {
                            channel.sendMessage("None of the IDs are in the blacklist.").queue();
                        }
                    } else {
                        channel.sendMessage("Invalid command! Usage: " + PREFIX + "search [ID1] [ID2] ...").queue();
                    }
                }

                // Command to see the list of blacklisted IDs
                if (content.startsWith(PREFIX + "showlist")) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Blacklisted IDs:\n");
                    for (String id : blacklist) {
                        sb.append(id).append("\n");
                    }
                    channel.sendMessage(sb.toString()).queue();
                }

                // Command to clear the list of blacklisted IDs
                if (content.startsWith(PREFIX + "clear") && event.getMember().getRoles().contains(removeRole)) {
                    blacklist.clear();
                    channel.sendMessage("The blacklist has been cleared.").queue();
                    saveBlacklistToFile();
                }

                // Check if the command is "compare"
                if (content.startsWith(PREFIX + "compare")) {
                    // Call the compareBlacklistToWhitelist function
                    compareBlacklistTolist(event);
                }

                // Check if the message is a command to add IDs to the list
                if (content.startsWith(PREFIX + "updatetemp ")) {
                    String[] split = content.split("\\s+");
                    if (split.length >= 2) {
                        StringBuilder sb = new StringBuilder();
                        for (int i = 1; i < split.length; i++) {
                            sb.append(split[i]).append(" ");
                        }
                        String uidsToAdd = sb.toString().trim();
                        String[] uids = uidsToAdd.split("\\s+");
                        for (String uid : uids) {
                            addUid(uid);
                        }
                        channel.sendMessage("IDs " + uidsToAdd + " have been added to the blacklist.").queue();
                        savelist(); // Call savelist() after adding all IDs
                    } else {
                        channel.sendMessage("Invalid command! Usage: " + PREFIX + "update [ID1] [ID2] ...").queue();
                    }
                }


                // Command to clear the list of list IDs
                if (content.startsWith(PREFIX + "Tempclr") && event.getMember().getRoles().contains(removeRole)) {
                    list.clear();
                    channel.sendMessage("The list has been cleared.").queue();
                    savelist();
                }


                // Command to see the list of blacklisted IDs
                if (content.startsWith(PREFIX + "templist")) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("List IDs yet to be checked:\n");
                    for (String id : list) {
                        sb.append(id).append("\n");
                    }
                    channel.sendMessage(sb.toString()).queue();
                }

                // Check if the message is a command to show the list of commands
                if (content.startsWith(PREFIX + "help")) {
                    // Send a message with the list of available commands
                    channel.sendMessage("BLACKlistmod commands:\n" +
                            PREFIX + "add [ID] - Add an ID to the blacklist\n" +
                            PREFIX + "adds [ID1] [ID2] ... - Add multiple IDs to the blacklist\n" +
                            PREFIX + "blacklist [ID] - Search for an ID in the blacklist\n" +
                            PREFIX + "showlist - Show the current list\n" +
                            PREFIX + "compare - Compares the both list\n" +
                            PREFIX + "updatetemp - update the Templist\n" +
                            PREFIX + "templist - show the Templist\n" +
                            PREFIX + "help - Show this list of commands\n"+
                            "BLACKlistadmin commands:\n" +
                            PREFIX + "remove [ID] - Remove an ID from the blacklist\n" +
                            PREFIX + "clear - Clear the blacklist\n" +
                            PREFIX + "Tempclr - clear the list").queue();
                }
            }
        }
    }

    public void compareBlacklistTolist(MessageReceivedEvent event) {
        try {
            // Read the content of blacklist.txt and list.txt
            List<String> blacklist = Files.readAllLines(Paths.get("D:\\Programs\\sotBlacklist\\src\\main\\resources\\blacklist.txt"));
            List<String> whitelist = Files.readAllLines(Paths.get("D:\\Programs\\sotBlacklist\\src\\main\\resources\\list.txt"));

            // Create a Set to store similar IDs
            Set<String> similarIds = new HashSet<>();

            // Iterate through the blacklist and whitelist to find similar IDs
            for (String id : blacklist) {
                if (whitelist.contains(id)) {
                    similarIds.add(id);
                }
            }

            // Check if there are any similar IDs
            if (similarIds.isEmpty()) {
                event.getChannel().sendMessage("No similar IDs found.").queue();
            } else {
                // Send the similar IDs to the Discord channel
                event.getChannel().sendMessage("Similar IDs found: " + similarIds.toString()).queue();
            }
        } catch (IOException e) {
            event.getChannel().sendMessage("Failed to compare IDs. Please try again later.").queue();
            e.printStackTrace();
        }
    }

    // Method to save the updated list to the file
    private void savelist() {
        try {
            File file = new File("D:\\Programs\\sotBlacklist\\src\\main\\resources\\list.txt");
            FileWriter writer = new FileWriter(file);
            for (String uid : list) {
                writer.write(uid + "\n");
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Method to load the list from the file
    private List<String> loadlist() {
        List<String> list = new ArrayList<>();
        try {
            File file = new File("D:\\Programs\\sotBlacklist\\src\\main\\resources\\list.txt");
            FileReader reader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(reader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                list.add(line);
            }
            bufferedReader.close();
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }


    private void saveBlacklistToFile() {
        try {
            File file = new File("D:\\Programs\\sotBlacklist\\src\\main\\resources\\blacklist.txt");
            FileWriter writer = new FileWriter(file);
            for (String id : blacklist) {
                writer.write(id + "\n");
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<String> loadBlacklistFromFile() {
        List<String> blacklist = new ArrayList<>();
        try {
            File file = new File("D:\\Programs\\sotBlacklist\\src\\main\\resources\\blacklist.txt");
            FileReader reader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(reader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                blacklist.add(line);
            }
            bufferedReader.close();
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return blacklist;
    }


    private void addId(String id) {
        if (!blacklist.contains(id)) {
            blacklist.add(id);
        }
    }

    private void removeId(String id) {
        blacklist.remove(id);
    }

    private boolean searchId(String id) {
        return blacklist.contains(id);
    }

    private void addUid(String uid) {
        if (!list.contains(uid)) {
            list.add(uid);
        }
    }
}