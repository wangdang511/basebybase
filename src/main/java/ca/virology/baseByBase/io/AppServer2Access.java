package ca.virology.baseByBase.io;

import ca.virology.baseByBase.DiffEditor;
import ca.virology.baseByBase.gui.DiffEditorFrame;
import ca.virology.lib.messages.appserver.MafftMessage;
import ca.virology.lib.util.gui.ExtPrefsChooser;
import ca.virology.lib2.common.service.MessageService;
import ca.virology.lib2.common.service.SimpleTextMessage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


//Methods to access the new appserver (used for mafft alignments)
public class AppServer2Access {

    // The point at which to split the data for mafft add
    public static int numAligned = 0;

    /**
     * Overloaded function specifically for mafft add.
     * Additional variable, num, included as an argument so the appserver knows where to split the message into two files.
     *
     */
    public static Vector doAlignmentQuery(String prg, Vector db, int num) throws IOException{

        numAligned = num;

        //System.out.println("In appserver2access, about to get the message service");
        MessageService ms = DiffEditor.context.getMessageService();
        Vector ret = new Vector();

        MafftMessage mafft = new MafftMessage(ret);
        Vector appName = new Vector();
        appName.addElement(ExtPrefsChooser.MAFFT);
        new ExtPrefsChooser(appName, ExtPrefsChooser.MAFFT, false, mafft);

        String message = "";
        if (prg.equals("mafft") || prg.equals("mafftadd")) {
            // Adding the % symbol tells the appserver that it's dealing with a mafft add request and needs to split the message.
            if (DiffEditorFrame.mafftadd)
                message+= numAligned + "%";
            else if (DiffEditorFrame.mafftaddfrag)
                message+= numAligned + "$";

            System.out.println(message);

            message+= mafft.getCommandLineOption().replace(" --clustalout","");
            //message += mafft.getCommandLineOption();
            System.out.println(message);
            List<String> names = new ArrayList<String>();
            for (Object line : db) {
                if (line.toString().charAt(0) == '>') {
                    names.add(line.toString().substring(1));
                }
                message += line.toString() + "\n";
            }

            message += "\n";

            SimpleTextMessage sm = new SimpleTextMessage(message);
            //System.out.println("Command build, about to send");
            String result = ms.sendMessage(sm);
            //System.out.println("Message sent ");
            BufferedReader br = new BufferedReader(new StringReader(result));
          //  System.out.println("About to read result");
            String line = br.readLine();
            while (line != null) {
                //Fix mafft truncating long sequence names
               // System.out.println("return line = " + line);
                //Todo: Change construction and parsing of sequence names so that this does not happen (limit size)?
                int nameEnd = line.indexOf(' ');
                String truncatedName = "";
                if (nameEnd != -1) {
                    truncatedName = line.substring(0, nameEnd).trim();
                }
                if (!truncatedName.equals("")) {
                    for (String name : names) {
                        if (name.contains(truncatedName)) {
                            line = name + line.substring(nameEnd);
                            break;
                        }
                    }
                }

                ret.add(line);
                line = br.readLine();
            }
        }

        // Clean up the sequence names
        if (prg.equals("muscle") || prg.equals("clustalo") || prg.equals("mafft")) {
            Pattern sequencePattern = Pattern.compile("^\\d+;.*");
            for (int i = 0; i < ret.size(); i++) {
                String line = (String) ret.get(i);
                Matcher m = sequencePattern.matcher(line);
                if (m.matches()) {
                    ret.set(i, line.replaceFirst(";", "_"));
                }
            }
        }
        return ret;

    }
    /*

    All other alignment programs are handled here. 

     */
    public static Vector doAlignmentQuery(String prg, Vector db) throws IOException {
        MessageService ms = DiffEditor.context.getMessageService();
        Vector ret = new Vector();

        MafftMessage mafft = new MafftMessage(ret);
        Vector appName = new Vector();
        appName.addElement(ExtPrefsChooser.MAFFT);
        new ExtPrefsChooser(appName, ExtPrefsChooser.MAFFT, false, mafft);

        String message = "";
        if (prg.equals("mafft") || prg.equals("mafftadd")) {

            if (prg.equals("mafftadd")){
                message+= numAligned + "%";
            }
            message += mafft.getCommandLineOption();
            List<String> names = new ArrayList<String>();
            for (Object line : db) {
                if (line.toString().charAt(0) == '>') {
                    names.add(line.toString().substring(1));
                }
                message += line.toString() + "\n";
            }

            message += "\n";

            SimpleTextMessage sm = new SimpleTextMessage(message);
            System.out.println(sm);
            String result = ms.sendMessage(sm);
            BufferedReader br = new BufferedReader(new StringReader(result));
            String line = br.readLine();
            while (line != null) {
                //Fix mafft truncating long sequence names
                //Todo: Change construction and parsing of sequence names so that this does not happen (limit size)?
                int nameEnd = line.indexOf(' ');
                String truncatedName = "";
                if (nameEnd != -1) {
                    truncatedName = line.substring(0, nameEnd).trim();
                }
                if (!truncatedName.equals("")) {
                    for (String name : names) {
                        if (name.contains(truncatedName)) {
                            line = name + line.substring(nameEnd);
                            break;
                        }
                    }
                }

                ret.add(line);
                line = br.readLine();
            }
        }

        // Clean up the sequence names
        if (prg.equals("muscle") || prg.equals("clustalo") || prg.equals("mafft")) {
            Pattern sequencePattern = Pattern.compile("^\\d+;.*");
            for (int i = 0; i < ret.size(); i++) {
                String line = (String) ret.get(i);
                Matcher m = sequencePattern.matcher(line);
                if (m.matches()) {
                    ret.set(i, line.replaceFirst(";", "_"));
                }
            }
        }

        return ret;
    }

}
