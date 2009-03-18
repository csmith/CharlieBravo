/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.md87.charliebravo.commands;

import com.dmdirc.parser.irc.ClientInfo;
import com.md87.charliebravo.Command;
import com.md87.charliebravo.CommandOptions;
import com.md87.charliebravo.InputHandler;
import com.md87.charliebravo.Response;

/**
 *
 * @author chris
 */
@CommandOptions(requireAuthorisation=true)
public class SetCommand implements Command {

    public void execute(InputHandler handler, Response response, String line) throws Exception {
        final String openID = (String) handler.getParser().getClientInfoOrFake(response.getSource())
                .getMap().get("OpenID");
        String target = openID;
        String value = line;

        int offset;
        if ((offset = line.toLowerCase().lastIndexOf("on behalf of")) > -1) {
            if (!handler.getConfig().hasOption(openID, "admin.level")
                || Integer.parseInt(handler.getConfig().getOption(openID, "admin.level")) < 100) {
                response.sendMessage("You do not have sufficient access", true);
                return;
            } else {
                final String user = line.substring(offset + 13);
                value = line.substring(0, offset - 1);
                
                final ClientInfo client = handler.getParser().getClientInfo(user);

                if (client != null) {
                    if (client.getMap().get("OpenID") == null) {
                        response.sendMessage(client.getNickname() + " isn't authed", true);
                        return;
                    }

                    target = (String) client.getMap().get("OpenID");
                } else if (handler.getConfig().hasOption(user, "internal.lastseen")) {
                    target = user;
                } else {
                    response.sendMessage("I couldn't find anyone by that name", true);
                    return;
                }
            }
        }

        final String[] parts = value.split("\\s+", 2);

        if (openID == null) {
            response.sendMessage("You must be authorised to use this command", true);
        } else if (parts.length < 2) {
            response.sendMessage("You must specify a setting name and value", true);
        } else if (!handler.getConfig().isLegalSetting(parts[0])) {
            response.sendMessage("That isn't a legal setting", true);
        } else if (parts[0].startsWith("admin.")
                && (!handler.getConfig().hasOption(openID, "admin.level")
                || Integer.parseInt(handler.getConfig().getOption(openID, "admin.level")) < 100)) {
            response.sendMessage("You do not have sufficient access", true);
        } else {
            handler.getConfig().setOption(target, parts[0], parts[1]);
            response.sendMessage("OK", true);
        }
    }

}